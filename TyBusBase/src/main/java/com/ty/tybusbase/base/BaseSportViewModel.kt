package com.ty.tybusbase.base

import android.app.Application
import android.graphics.Bitmap
import android.media.AsyncPlayer
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.base.common.AdlBitmapUtil
import com.adl.base.common.AdlHandlerThread
import com.adl.base.file.AdlFileHelper
import com.adl.base.log.AdlLogger
import com.adl.service.entity.DictExcelEntity
import com.adl.service.entity.SceneSportEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.SportIndicatorsEntity
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.SportRecordEntity.RecordCompetition
import com.adl.service.entity.SportRecordEntity.RecordMeeting
import com.adl.service.entity.SportRecordEntity.RecordPlan
import com.adl.service.entity.SportRecordEntity.SportTrainPlan
import com.adl.service.entity.SportVideoEntity
import com.adl.service.outer.AdlService
import com.adl.service.upload.FileUploadSyncService
import com.adl.service.web.SportRecordService
import com.adl.service.web.SportService
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.helper.SportHelper
import com.zy.sport.core.SportEngineSingle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import androidx.core.net.toUri
import com.adl.auth.core.AdlAuthFactory
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.utils.BusiCoreEngine
import com.ty.tybusbase.utils.VideoUtil

open class BaseSportViewModel(application: Application) : BaseViewModel(application) {
    var sportListLiveData: BaseLiveData<MutableList<SceneSportEntity>> = BaseLiveData()
    val statusLD = MutableLiveData<SportStatusEnum>()
    val accountInfoLD: BaseLiveData<AccountInfo> = BaseLiveData()
    val dictMapLD = BaseLiveData<MutableList<DictExcelEntity>>()
    val remindLD = MutableLiveData<Int>()
    var sportInfo: SceneSportInfoEntity? = null
    var sceneId: String? = null

    protected var uiHandler: Handler = AdlHandlerThread.getUIHandler()

    private val playerHandler: AdlHandlerThread = AdlHandlerThread("#Player")

    val sportEngine: SportEngineSingle by lazy { SportEngineSingle.createEngine(0) }

    //存储上传的资源信息，处理重复资源上传问题
    val uploadResMap = ConcurrentHashMap<String, String?>()

    /**
     * 运动开始时间
     */
    var startSportTime: Long = 0

    /**
     * 运动结束时间
     */
    var endSportTime: Long = 0

    private var asyncPlayer: AsyncPlayer? = null

    var videoFile: File? = null
    var videoCover: String? = null

    open fun toStatusPrepare() {
        statusLD.postValue(SportStatusEnum.PREPARE)
    }

    //  切换识别人脸状态
    open fun toStatusIdentifyFace() {
        statusLD.postValue(SportStatusEnum.IDENTIFY_FACE)
    }

    // 切换到垫子识别
    open fun toStatusIdentifyMat() {
        statusLD.postValue(SportStatusEnum.IDENTIFY_MAT)
    }

    // 运动区域检测
    open fun toStatusCheckArea() {
        statusLD.postValue(SportStatusEnum.CHECK_AREA)
    }

    //  切换识别人脸成功状态
    open fun toStatusIdentifySuccess() {
        statusLD.postValue(SportStatusEnum.IDENTIFY_FACE_SUCCESS)
    }

    //  切换运动开始状态
    open fun toStatusSportStart() {
        startSportTime = System.currentTimeMillis()
        statusLD.postValue(SportStatusEnum.SPORT_START)
        if (sceneId == null || (!sceneId.equals(SceneConstants.INDEPENDENT_TEST) && !sceneId.equals(
                SceneConstants.CLASS_TEST
            ))
        ) {
            playBgm()
        }
    }

    //  切换运动结束状态
    open fun toStatusSportEnd() {
        endSportTime = System.currentTimeMillis()
        statusLD.postValue(SportStatusEnum.SPORT_END)
        stopPlayBgm()
    }


    fun getUserName(): String? {
        val value = accountInfoLD.value ?: return ""
        return value.studentEntity?.accountName
    }

    fun getUserHeadImg(): String? {
        val value = accountInfoLD.value ?: return ""
        return value.studentEntity?.userFaceImgUrl
    }

    fun getUserId(): String? {
        val value = accountInfoLD.value ?: return ""
        return value.studentEntity?.accountId
    }

    fun toTwoStr(value: Int): String? {
        return if (value > 9) value.toString() else "0$value"
    }

    fun getAccountInfo(): AccountInfo? {
        return accountInfoLD.value
    }

    fun getSportSkuId(): String? {
        return sportInfo?.sportSkuId
    }

    fun getEndType(): String? {
        return sportInfo?.endType
    }

    fun getEndValue(): Long {
        return sportInfo?.endValue?.toLong() ?: 0
    }

    /**
     * 获取题库
     */
    fun getDictMap() {
        viewModelScope.launch(Dispatchers.IO) {
            val dictMap = SportService.getDictMap(sportInfo?.sportSkuId) ?: mutableListOf()
            if (dictMap.isNotEmpty()) {
                dictMapLD.postValue(dictMap)
            }
        }
    }

    private fun playBgm() {
        playerHandler.post {
            if (asyncPlayer == null) {
                asyncPlayer = AsyncPlayer("AsyncPlayer")

                //先用运动SkuID去匹配，再用itemCode去匹配
                val uri: Uri? =
                    when (sportInfo?.testCount) {
                        "3", "5" -> {
                            ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.skip_rope_multi_bgm).toUri()
                        }

                        else -> null
                    } ?: run {
                        when (SportHelper.getSpeedType(sportInfo?.sportItemCode ?: "")) {
                            SportHelper.HIGH_SPEED2 -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.quickly_sport_bgm).toUri()
                            }

                            SportHelper.MEDIUM_SPEED -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.normal_sprot_bgm).toUri()
                            }

                            SportHelper.HIGH_SPEED -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.qucikly_sport_bgm2).toUri()
                            }

                            SportHelper.LOW_SPEED -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.bgm_shot_bee).toUri()
                            }

                            SportConstant.SPORT_TOUCH_THE_BALL -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.double_tounch_ball_bgm).toUri()
                            }

                            SportConstant.SPORT_STONE_SCISSORS_CLOTH -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.rock_paper_scissors_bgm).toUri()
                            }

                            SportConstant.SPORT_PLANE_WAR -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.bgm_plane_war).toUri()
                            }

                            SportConstant.SPORT_CAT_RAT -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.cat_rat_bgm).toUri()
                            }

                            SportConstant.SPORT_FILL_BLANK_GAME, SportConstant.SPORT_FILL_BLANK_EN_GAME -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.fill_blank_bgm).toUri();
                            }

                            SportConstant.SPORT_GAME_PACMAN -> {
                                ("android.resource://" + getApplication<Application>().packageName + "/" + R.raw.pacman_bg).toUri()
                            }

                            else -> null                      //趣味

                        }
                    }

                uri?.let {
                    asyncPlayer?.play(getApplication(), uri, true, AudioManager.STREAM_MUSIC)
                }
            }
        }
    }

    open fun stopPlayBgm() {
        if (asyncPlayer != null) {
            asyncPlayer!!.stop()
            asyncPlayer = null
        }
    }

    fun uploadSportResult(
        sceneId: String,
        sportResult: SportResult,
        modelId: String?,
        matchId: String?,
        matchSportId: String?,
        matchName: String?,
        recordMeet: RecordMeeting? = null
    ) {
        var competitionAthletics: RecordCompetition? = null
        var trainPlan: SportTrainPlan? = null
        when (modelId) {
            SportConstant.MATCH -> {
                competitionAthletics = RecordCompetition().apply {
                    competitionId = matchId
                    competitionName = matchName
                    competitionAthSportId = matchSportId
                    competitionAthSportName = sportInfo?.sportSkuName
                    isCompetitionCheckStatus = true
                    isCompetitionTaskStatus = true
                }
            }

            SportConstant.TRAIN -> {
                trainPlan = SportTrainPlan().apply {
                    trainPlanId = matchId
                    trainPlanName = matchName
                    trainPlanSportId = matchSportId
                    trainPlanSportName = sportInfo?.sportSkuName
                }
            }
        }
        uploadSportResult(
            sceneId,
            sportResult,
            recordCompetition = competitionAthletics,
            sportTrainPlan = trainPlan,
            recordMeeting = recordMeet
        )

    }

    open fun uploadSportResult(
        sceneId: String,
        result: SportResult,
        accountId: String? = accountInfoLD.value?.studentEntity?.accountId,
        recordCompetition: RecordCompetition? = null,
        recordMeeting: RecordMeeting? = null,
        sportTrainPlan: SportTrainPlan? = null,
        indicatorList: List<SportIndicatorsEntity<*, *>>? = null,
        recordPlan: RecordPlan? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sportRecordEntity =
                buildSportRecordEntity(
                    sceneId,
                    result,
                    accountId,
                    recordCompetition,
                    recordMeeting,
                    sportTrainPlan,
                    recordPlan = recordPlan
                )
            BusiCoreEngine.getResultCallback()?.onSportRecordEntity(sportRecordEntity)
        }
    }

    @Synchronized
    open fun uploadRecord(sportRecordEntity: SportRecordEntity) {
        //先处理视频上传
        /**
         * 七牛云视频文件命名规则：
         * /{端标识（1001、1002）}/video/{业务标识（体测、体锻等）}/{项目编码}/{年}/{月}/{日}/{视频文件}
         * 七牛云图片文件命名规则：
         * /{端标识（1001、1002）}/image/{业务标识（体测、体锻等）}/{项目编码}/{年}/{月}/{日}/{视频文件}
         *
         */
        val calendar = Calendar.getInstance()
        var uploadOk = true

        //指标上传
        sportRecordEntity.sportIndicators?.forEach { indicators ->
            //上传图片
            indicators.indicatorsImages?.let {
                for (it in indicators.indicatorsImages) {
                    //如果地址为空，赋值空串儿
                    if (it.imageUrl.isNullOrEmpty()) {
                        it.imageUrl = ""
                        continue
                    }
                    //如果是HTTP开头的表示已经是上传链接了
                    if (it.imageUrl.startsWith("http")
                        || File(it.imageUrl).exists().not()
                    ) continue

                    val url = syncUploadRes(
                        it.imageUrl,
                        sportRecordEntity.sportNatureCodes,
                        sportRecordEntity.sportTypeCode,
                        sportRecordEntity.id,
                        1
                    )

                    if (url.isNullOrEmpty()) {
                        uploadOk = false
                    } else {
                        it.imageUrl = url
                    }
                }
            }

            //上传视频
            indicators.indicatorsVideos?.forEach {
                if (it.videoCover.isNullOrEmpty().not()
                    && it.videoCover.startsWith("http").not()
                    && File(it.videoCover).exists()
                ) {
                    val url = syncUploadRes(
                        it.videoCover,
                        sportRecordEntity.sportNatureCodes,
                        sportRecordEntity.sportTypeCode,
                        sportRecordEntity.id,
                        2
                    )
                    if (url.isNullOrEmpty()) {
                        uploadOk = false
                    } else {
                        it.videoCover = url
                    }
                }
                if (it.videoUrl.isNullOrEmpty().not()
                    && it.videoUrl.startsWith("http").not()
                    && File(it.videoUrl).exists()
                ) {
                    val url = syncUploadRes(
                        it.videoUrl,
                        sportRecordEntity.sportNatureCodes,
                        sportRecordEntity.sportTypeCode,
                        sportRecordEntity.id,
                        3
                    )
                    if (url.isNullOrEmpty()) {
                        uploadOk = false
                    } else {
                        it.videoUrl = url
                    }
                }
            }

        }

        //上传运动视频
        val authFactory = AdlAuthFactory.create(getApplication())
        sportRecordEntity.sportVideos?.forEach {
            //视频需要上传
            if (it.videoUrl != null && it.videoUrl.isNotEmpty() && File(it.videoUrl).exists()) {
                val videoPath =
                    "${authFactory.appCode}/video/${sportRecordEntity.sportSceneCode}/${sportRecordEntity.sportTypeCode}/${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}/${System.currentTimeMillis()}.mp4"
                var url = syncUploadRes(it.videoUrl, videoPath)
                if (url.isNullOrEmpty()) {
                    uploadOk = false
                } else {
                    it.videoUrl = url.replace(AdlService.getService().facePrefix, "")
                }
            }
            //封面需要上传
            if (it.videoCover != null && it.videoCover.isNotEmpty() && File(it.videoCover).exists()) {
                val videoCover =
                    "${authFactory.appCode}/image/${sportRecordEntity.sportSceneCode}/${sportRecordEntity.sportTypeCode}/${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}/${System.currentTimeMillis()}.png"
                val coverUrl = syncUploadRes(it.videoCover, videoCover)
                if (coverUrl.isNullOrEmpty()) {
                    uploadOk = false
                } else {
                    it.videoCover = coverUrl.replace(AdlService.getService().facePrefix, "")
                }
            }
        }
        //更新哈数据库,存储已上传的资源
        saveSportResultToDB(sportRecordEntity)
        if (uploadOk) {
            val uploadRecord = SportRecordService.uploadRecord(sportRecordEntity)
            AdlLogger.d("成绩上传完成：name = ${accountInfoLD.value?.studentEntity?.accountName} isOk = ${uploadRecord?.isOk}")
            if (uploadRecord?.isOk == true || uploadRecord?.msg?.contains("记录id重复") == true) {
                AdlService.getService().recordUploadDao.clearById(sportRecordEntity.id)
            }
        } else {
            //成绩上传失败，原因：资源上传失败
            AdlLogger.d("成绩上传失败：name = ${accountInfoLD.value?.studentEntity?.accountName} ,有资源未上传成功！！！")
        }
    }

    fun saveSportResultToDB(sportRecordEntity: SportRecordEntity) {
        AdlService.getService().recordUploadDao.insert(sportRecordEntity)
    }

    private fun buildSportRecordEntity(
        sceneId: String,
        result: SportResult,
        id: String?,
        recordCompetition: RecordCompetition? = null,
        recordMeeting: RecordMeeting? = null,
        trPlan: SportTrainPlan? = null,
        indicatorList: List<SportIndicatorsEntity<*, *>>? = null,
        recordPlan: RecordPlan? = null
    ): SportRecordEntity {
        return SportRecordEntity(UUID.randomUUID().toString()).apply {
            val authFactory = AdlAuthFactory.create(getApplication())
            accountId = id
            sportSceneCode = sceneId
            appCode = authFactory.appCode
            accountType = "1"
            sportTime = result.sportDuration.toString()
            sportStartTime = result.startTime.toString()
            sportEndTime = result.endTime.toString()
            deviceId = authFactory.registerCode
            sportSkuId = sportInfo?.sportSkuId
            sportUnit = sportInfo?.measureUnitCode
            sportNatureCodes = sportInfo?.sportNatureCodes
            sportTypeCode = sportInfo?.sportTypeCode
            sportResult = result.sportResult
            if (result.getTotalScore() > 0) {
                sportScore = result.getTotalScore().toString()
            }
            multiPersonId = result.multiPersonId
            multiPersonRank = result.rank
            multiPersonIndex = result.multiPersonIndex
            sportSkuSubType = result.sportSkuSubType
            trainPlan = trPlan
            competitionAthletics = recordCompetition
            sportMeet = recordMeeting
            sportIndicators = indicatorList
            sportViolationStatus = result.sportViolationStatus
            sportViolationNumber = result.sportViolationNumber
            if (recordPlan != null) {
                recordPlan.sportViolationStatus = result.sportViolationStatus
                recordPlan.sportViolationNumber = result.sportViolationNumber
            }
            sportPlan = recordPlan
            //处理视频

            if (result.videoFile != null && result.videoFile!!.exists()) {
                if (result.videoCover == null) {
                    result.videoCover =
                        "${result.videoFile!!.parent}/${System.currentTimeMillis()}_cover.png"
                }
                val file = File(result.videoCover)
                if (!file.exists()) {
                    val bitmap = VideoUtil.getVideoThumbnail(result.videoFile!!.absolutePath)
                    AdlBitmapUtil.save(bitmap, File(result.videoCover), Bitmap.CompressFormat.PNG)
                }
                if (!file.exists()) {
                    result.videoCover = ""
                }
                if (sportVideos == null) {
                    sportVideos = ArrayList()
                }
                sportVideos.add(
                    SportVideoEntity(
                        result.videoFile!!.absolutePath,
                        result.videoCover,
                        "视频地址"
                    )
                )
            }
        }
    }

    fun getVideoPath(sAi: String? = sportInfo?.sportSkuId): File {
        if (videoFile == null) {
            val userDir = AdlFileHelper.createUserDir(
                accountInfoLD.value?.studentEntity?.accountId ?: BusiBaseConstantsValue.VISITOR_USER_ID,
                sAi
            )
            val timestamp = System.currentTimeMillis()
            val fileName = "${sAi}_${timestamp}.mp4"
            videoFile = File(userDir, fileName)
            videoCover = "${videoFile!!.parent}/${timestamp}_cover.png"
        }
        return videoFile!!
    }

    fun resetVideoPath() {
        videoFile = null
        videoCover = null
    }

    fun syncUploadRes(filePath: String, remoteFileName: String): String? {
        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }

        val requestResult = FileUploadSyncService.instance()
            .syncUploadFile(File(filePath), remoteFileName)
        return if (requestResult.isOk && requestResult.content is String) {
            val url = requestResult.content as? String
            AdlLogger.d("upload Res 成功： ${url}")
            if (url.isNullOrEmpty().not()) {
                uploadResMap[filePath] = url
            }
            url
        } else {
            AdlLogger.d("upload Res 失败： ${requestResult.msg}===${filePath}")
            null
        }
    }

    /**
     *
     *              图片路劲：
     *              /{业务类型代码}/sport/indicator/images/{运动性质}/{运动类型}/{记录标识码}_{序列码}.png
     *             视频封面路径：
     *             /{业务类型代码}/sport/indicator/images/cover/{运动性质}/{运动类型}/{记录标识码}_{序列码}.png
     *             视频路径格式如下：
     *             /{业务类型代码}/sport/indicator/videos/{运动性质}/{运动类型}/{记录标识码}_{序列码}.mp4
     *
     *   type: 1 = 图片，2 = 视频封面，3 = 视视频
     *
     */
    fun syncUploadRes(
        filePath: String,
        id: String,
        type: Int,
    ): String? {
        //有缓存直接从缓存里面取
        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }
        var qnUrl: String? = null
        val remoteFileName = when (type) {
            1 -> "PTE/sport/indicator/images/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
            2 -> "PTE/sport/indicator/images/cover/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
            3 -> "PTE/sport/indicator/videos/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}/${id}_${System.currentTimeMillis()}.mp4"
            else -> "PTE/sport/indicator/images/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
        }
        val requestResult = FileUploadSyncService.instance()
            .syncUploadFile(File(filePath), remoteFileName)
        if (requestResult.isOk && requestResult.content is String) {
            val url = requestResult.content as? String
            AdlLogger.d("upload Res 成功： ${url}")
            qnUrl = url
        } else {
            AdlLogger.d("upload Res 失败： ${requestResult.msg}===${filePath}")
        }
        //存缓存，防止资源重复上传
        if (filePath.isNotEmpty() && qnUrl != null) {
            uploadResMap[filePath] = qnUrl
        }
        return qnUrl
    }


    fun syncUploadRes(
        filePath: String,
        sportNatureCodes: String,
        sportTypeCode: String,
        id: String,
        type: Int,
    ): String? {
        //有缓存直接从缓存里面取
        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }
        var qnUrl: String? = null
        val remoteFileName = when (type) {
            1 -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
            2 -> "PTE/sport/indicator/images/cover/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
            3 -> "PTE/sport/indicator/videos/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.mp4"
            else -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
        }
        val requestResult = FileUploadSyncService.instance()
            .syncUploadFile(File(filePath), remoteFileName)
        if (requestResult.isOk && requestResult.content is String) {
            val url = requestResult.content as? String
            AdlLogger.d("upload Res 成功： ${url}")
            qnUrl = url
        } else {
            AdlLogger.d("upload Res 失败： ${requestResult.msg}===${filePath}")
        }
        //存缓存，防止资源重复上传
        if (filePath.isNotEmpty() && qnUrl != null) {
            uploadResMap[filePath] = qnUrl
        }
        return qnUrl
    }


    open fun reset() {
        resetVideoPath()
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacksAndMessages(null)
        stopPlayBgm()
        playerHandler.quit()

        sportEngine.onStop()
    }
}