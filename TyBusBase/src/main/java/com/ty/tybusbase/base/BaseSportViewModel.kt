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
import com.adl.base.common.AdlVideoAsyncRecorder
import com.adl.service.entity.SportStartRecordEntity
import com.adl.service.entity.SportStartRecordEntity.SportStartAccountDetail
import com.adl.service.outer.UserCaller
import com.adl.service.web.SportPlanService
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.LearnClassInfo
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.toObject
import com.ty.tybusbase.helper.TeachHelper
import com.ty.tybusbase.utils.BusiCoreEngine
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.VideoUtil
import kotlinx.coroutines.withContext

open class BaseSportViewModel(application: Application) : BaseViewModel(application) {
    var sportListLiveData: BaseLiveData<MutableList<SceneSportEntity>> = BaseLiveData()
    val statusLD = MutableLiveData<SportStatusEnum>()
    val accountInfoLD: BaseLiveData<AccountInfo> = BaseLiveData()
    val dictMapLD = BaseLiveData<MutableList<DictExcelEntity>>()
    val remindLD = MutableLiveData<Int>()
    var sportInfo: SceneSportInfoEntity? = null //运动详情数据结构
    var recordMeet: RecordMeeting? = null //运动会数据结构
    var sceneId: String? = null
    var matchId: String? = null
    var ptPlanId: String? = null
    var ptStandardId: String? = null
    var energyPoint: Int = 0
    var streamType = 1 //1:摄像头播放，2:视频播放
    var canPlayBgm = true //是否能播背景音乐，用于上层业务控制音乐播放
    var sportStartId = ""

    val sportResult: SportResult by lazy { SportResult() }

    protected var uiHandler: Handler = AdlHandlerThread.getUIHandler()

    private val playerHandler: AdlHandlerThread = AdlHandlerThread("#Player")

    val sportEngine: SportEngineSingle by lazy { SportEngineSingle.createEngine(0) }

    val videoRecorder by lazy { AdlVideoAsyncRecorder() }

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

    var videoFile2: File? = null
    var videoCover2: String? = null

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

    //  切换运动结算状态
    open fun toStatusSportCountDown() {
        statusLD.postValue(SportStatusEnum.SPORT_COUNTDOWN)
    }

    //  切换运动开始状态
    open fun toStatusSportStart() {
        startSportTime = System.currentTimeMillis()
        statusLD.postValue(SportStatusEnum.SPORT_START)
        if (canPlayBgm && (!sceneId.equals(SceneConstants.INDEPENDENT_TEST) && !sceneId.equals(
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

    //  切换运动结算状态
    open fun toStatusSportSettlement() {
        statusLD.postValue(SportStatusEnum.SPORT_SETTLEMENT)
    }

    //  切换运动结果状态
    open fun toStatusSportResult() {
        statusLD.postValue(SportStatusEnum.SPORT_RESULT)
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
     * 检查学生是否在计划内
     */
    fun checkStudent(accountId: String, planId: String?, callback: Action1<Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (planId == null) {
                withContext(Dispatchers.Main) {
                    callback.invoke(true)
                }
            } else {
                if (accountId == BusiBaseConstantsValue.VISITOR_USER_ID) {
                    withContext(Dispatchers.Main) {
                        callback.invoke(false)
                    }
                } else {
                    //掉接口判断学生是否在计划内
                    val result = SportPlanService.checkStudent(planId, accountId)
                    withContext(Dispatchers.Main) {
                        callback.invoke(result ?: false)
                    }
                }
            }
        }
    }

    /**
     * 检查学生是否在计划内
     */
    fun checkStudent(accountId: String, planId: String?): Boolean {
        return if (planId == null) {
            true
        } else {
            if (accountId == BusiBaseConstantsValue.VISITOR_USER_ID) {
                false
            } else {
                //掉接口判断学生是否在计划内
                val result = SportPlanService.checkStudent(planId, accountId)
                result ?: false
            }
        }
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
                        "3", "4", "5", "10", "30" -> {
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
                    indicatorList,
                    recordPlan
                )
            BusiCoreEngine.getResultCallback()?.onSportRecordEntity(sportRecordEntity)
        }
    }

    @Synchronized
    open fun uploadRecord(sportRecordEntity: SportRecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            BusiCoreEngine.getResultCallback()?.onSportRecordEntity(sportRecordEntity)
        }
    }

    fun uploadSportStartRecord(
        sportDetail: MutableList<SportStartAccountDetail>? = null
    ) {
        if (sceneId != SceneConstants.CLASS_TEST && sceneId != SceneConstants.CLASS_EXERCISE && sceneId != SceneConstants.CLASS_COMPETITION) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val sportStartInfo = SportStartRecordEntity().apply {
                val org = UserCaller.instance().currentOrg
                if (org != null) {
                    this.orgId = org.orgId
                }
                val learnInfo =
                    MMKVUtil.decodeString(MMKVBusiKey.TEACH_LEARN_INFO)?.toObject<LearnClassInfo>()
                this.lessonId = learnInfo?.lessonId
                this.classId = TeachHelper.classId
                this.deviceId = MMKVUtil.decodeString(MMKVBusiKey.REGISTER_CODE)
                this.sportItemCode = sportInfo?.sportItemCode
                this.sportSkuId = sportInfo?.sportSkuId
                this.sportDetails = sportDetail ?: mutableListOf<SportStartAccountDetail>(
                    SportStartAccountDetail().apply {
                        accountId = accountInfoLD.value?.studentEntity?.accountId
                        this.sportStartTime = startSportTime
                    }
                )

            }
            val rere = SportRecordService.uploadSportStartRecord(sportStartInfo)
            sportStartId = rere.sportStartId
        }
    }

    fun saveSportResultToDB(sportRecordEntity: SportRecordEntity) {
        AdlService.getService().recordUploadDao.insert(sportRecordEntity)
    }

    fun buildSportRecordEntity(
        sceneId: String,
        result: SportResult,
        aId: String?,
        recordCompetition: RecordCompetition? = null,
        recordMeeting: RecordMeeting? = null,
        trPlan: SportTrainPlan? = null,
        indicatorList: List<SportIndicatorsEntity<*, *>>? = null,
        recordPlan: RecordPlan? = null
    ): SportRecordEntity {
        return SportRecordEntity(UUID.randomUUID().toString()).apply {
            val authFactory = AdlAuthFactory.create(getApplication())
            accountId = aId
            sportSceneCode = sceneId
            appCode = authFactory.appCode
            accountType = "1"
            sportTime = ((result.sportDuration / 1000) * 1000).toString()
            sportStartTime = result.startTime.toString()
            sportEndTime = result.endTime.toString()
            deviceId = authFactory.registerCode
            sportSkuId = sportInfo?.sportSkuId
            sportUnit = sportInfo?.measureUnitCode
            sportNatureCodes = sportInfo?.sportNatureCodes
            sportTypeCode = sportInfo?.sportTypeCode
            sportResult = result.sportResult
            sportCount = result.sportCount
            sportTotalCount = result.sportTotalCount
            sportNo = result.sportNo
            if (result.getTotalScore() > 0) {
                sportScore = result.getTotalScore().toString()
            }
            multiPersonId = result.multiPersonId
            multiTeamId = result.multiTeamId
            multiPersonRank = result.rank
            multiPersonIndex = result.multiPersonIndex
            sportSkuSubType = result.sportSkuSubType
            trainPlan = trPlan
            competitionAthletics = recordCompetition
            sportMeet = recordMeeting
            sportIndicators = indicatorList
            sportViolationStatus = result.sportViolationStatus
            sportViolationNumber = result.sportViolationNumber
            reportVersion = result.reportVersion
            reportCode = result.reportCode
            if (recordPlan != null) {
                recordPlan.sportViolationStatus = result.sportViolationStatus
                recordPlan.sportViolationNumber = result.sportViolationNumber
            }
            sportPlan = recordPlan
            //处理视频

            if (sportStartId.isNullOrEmpty().not()){
                this.sportStartId = this@BaseSportViewModel.sportStartId
                this.lessonId =
                    MMKVUtil.decodeString(MMKVBusiKey.TEACH_LEARN_INFO)
                        ?.toObject<LearnClassInfo>()?.lessonId
            }

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

            if (result.videoFile2 != null && result.videoFile2!!.exists()) {
                if (result.videoCover2 == null) {
                    result.videoCover2 =
                        "${result.videoFile2!!.parent}/${System.currentTimeMillis()}_cover.png"
                }
                val file = File(result.videoCover2)
                if (!file.exists()) {
                    val bitmap = VideoUtil.getVideoThumbnail(result.videoFile2!!.absolutePath)
                    AdlBitmapUtil.save(bitmap, File(result.videoCover2), Bitmap.CompressFormat.PNG)
                }
                if (!file.exists()) {
                    result.videoCover2 = ""
                }
                if (sportVideos == null) {
                    sportVideos = ArrayList()
                }
                sportVideos.add(
                    SportVideoEntity(
                        result.videoFile2!!.absolutePath,
                        result.videoCover2,
                        "视频地址2"
                    )
                )
            }
        }
    }

    fun getVideoPath(sAi: String? = sportInfo?.sportSkuId): File {
        if (videoFile == null) {
            val userDir = AdlFileHelper.createUserDir(
                accountInfoLD.value?.studentEntity?.accountId
                    ?: BusiBaseConstantsValue.VISITOR_USER_ID,
                sAi
            )
            val timestamp = System.currentTimeMillis()
            val fileName = "${sAi}_${timestamp}.mp4"
            videoFile = File(userDir, fileName)
            videoCover = "${videoFile!!.parent}/${timestamp}_cover.png"
        }
        return videoFile!!
    }

    fun getVideoPath2(sAi: String? = sportInfo?.sportSkuId): File {
        if (videoFile2 == null) {
            val userDir = AdlFileHelper.createUserDir(
                accountInfoLD.value?.studentEntity?.accountId
                    ?: BusiBaseConstantsValue.VISITOR_USER_ID,
                sAi
            )
            val timestamp = System.currentTimeMillis()
            val fileName = "${sAi}_${timestamp}_${UUID.randomUUID()}.mp4"
            videoFile2 = File(userDir, fileName)
            videoCover2 = "${videoFile2!!.parent}/${UUID.randomUUID()}_cover.png"
        }
        return videoFile2!!
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