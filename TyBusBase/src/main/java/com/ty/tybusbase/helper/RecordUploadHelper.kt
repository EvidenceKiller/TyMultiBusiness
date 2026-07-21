package com.ty.tybusbase.helper

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.adl.auth.core.AdlAuthFactory
import com.adl.service.entity.RecordResEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.SportRecordEntity
import com.adl.service.outer.AdlService
import com.adl.service.upload.OssFileUploadService
import com.adl.service.web.SportRecordService
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.expand.getFileExtension
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.LogUtil
import java.io.File
import java.lang.ref.SoftReference
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object RecordUploadHelper {
    val uploadResMap = ConcurrentHashMap<String, SoftReference<String>?>()
    val uploadResFileInfoMap = ConcurrentHashMap<String, SoftReference<Pair<String, String>>?>()
    val handler = Handler(Looper.getMainLooper())
    val executor = Executors.newSingleThreadExecutor()

    //成绩上传后，延时传资源，防止多人成绩上传，资源重复上传问题，很多异步问题，加锁效率太低，故延时处理
    val UPLOAD_RES_DELAY_TIME = 2000L
    fun getCacheUploadResFileInfo(filePath: String): Pair<String, String>? =
        uploadResFileInfoMap.getOrDefault(filePath, null)?.get()

    fun cacheUploadResFileInfo(filePath: String, filePrefix: String, remoteFileName: String) {
        LogUtil.d("缓存文件远端地址，localPath:${filePath} remotePath: ${filePrefix}/${remoteFileName}")
        uploadResFileInfoMap[filePath] = SoftReference(filePrefix to remoteFileName)
    }

    fun getCacheUploadResUrl(
        localFilePath: String,
        filePrefix: String,
        remoteFileName: String
    ): String? {
        val hhh =
            uploadResMap.getOrDefault("${localFilePath}_${filePrefix}_${remoteFileName}", null)
                ?.get()
        LogUtil.d("当前记录：${localFilePath}_${filePrefix}_${remoteFileName} -- ${if (hhh != null) "已存在" else "不存在"}")
        return hhh
    }

    fun cacheUploadResUrl(
        localFilePath: String,
        filePrefix: String,
        remoteFileName: String,
        url: String?
    ) {
        if (url.isNullOrEmpty()) return
        LogUtil.d("缓存上传成功的地址记录，localPath:${localFilePath} remotePath: ${url}")
        uploadResMap["${localFilePath}_${filePrefix}_${remoteFileName}"] = SoftReference(url)
    }

    /**
     * 存储资源信息到数据库
     */
    fun handleUploadResUrl(
        sportRecordEntity: SportRecordEntity,
        filePath: String,
        type: Int
    ): String {
        val uploadFileInfo = createRemoteFileName(
            filePath,
            sportRecordEntity.sportNatureCodes,
            sportRecordEntity.sportTypeCode,
            type
        )
        return handleUploadResUrl(
            sportRecordEntity,
            filePath,
            uploadFileInfo.first,
            uploadFileInfo.second
        )
    }

    fun handleUploadResUrl(
        sportRecordEntity: SportRecordEntity,
        filePath: String,
        uploadPrefix: String,
        uploadFileName: String
    ): String {
        AdlService.getService().recordResDao.insert(
            createRecordResEntity(
                sportRecordEntity.accountId,
                sportRecordEntity.sportSkuId,
                filePath,
                uploadPrefix,
                uploadFileName
            )
        )
        return "${AdlService.getService().facePrefix}${uploadPrefix}/${uploadFileName}"
    }

    fun createRecordResEntity(
        accountId: String?,
        sportSkuId: String?,
        localFilePath: String,
        filePrefix: String,
        fileName: String
    ): RecordResEntity = RecordResEntity().apply {
        this.accountId = accountId
        this.sportSkuId = sportSkuId
        this.localPath = localFilePath
        this.uploadFilePrefix = filePrefix
        this.uploadFileName = fileName
    }


    /**
     *
     * 图片路劲：
     * /{业务类型代码}/sport/indicator/images/{运动性质}/{运动类型}/{记录标识码}_{序列码}.png
     * 视频封面路径：
     * /{业务类型代码}/sport/indicator/images/cover/{运动性质}/{运动类型}/{记录标识码}_{序列码}.png
     * 视频路径格式如下：
     * /{业务类型代码}/sport/indicator/videos/{运动性质}/{运动类型}/{记录标识码}_{序列码}.mp4
     * type: 1 = 图片，2 = 视频封面，3 = 视视频
     *
     */
    fun createRemoteFileName(
        localFilePath: String,
        sportNatureCodes: String,
        sportTypeCode: String,
        type: Int,
    ): Pair<String, String> {
        getCacheUploadResFileInfo(localFilePath)?.let {
            LogUtil.d("已存在上传记录，直接返回 localPath:${localFilePath} remotePath: ${it.second}")
            return it
        }
        val remoteFileName =
            "${UUID.randomUUID()}.${localFilePath.getFileExtension()}"
        val filePrefix = when (type) {
            1 -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}"
            2 -> "PTE/sport/indicator/images/cover/${sportNatureCodes}/${sportTypeCode}"
            3 -> "PTE/sport/indicator/videos/${sportNatureCodes}/${sportTypeCode}"
            else -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}"
        }
        cacheUploadResFileInfo(localFilePath, filePrefix, remoteFileName)
        return filePrefix to remoteFileName
    }

    fun syncUploadRes(filePath: String, filePrefix: String, remoteFileName: String): String? {
        val cacheUrl = getCacheUploadResUrl(filePath, filePrefix, remoteFileName)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }

        val uploadResult =
            OssFileUploadService.instance().syncUploadFile(filePath, filePrefix, remoteFileName)
        return if (uploadResult.isSuccess) {
            val logMsg = String.format(
                "[成功] %s\n  URL: %s\n  耗时: %s",
                //上传到OSS的文件名
                uploadResult.fileName,
                //上传到OSS的url路径
                uploadResult.fileUrl,
                //上传耗时
                uploadResult.formattedUploadTime
            );
            LogUtil.d(logMsg)
            if (uploadResult.fileUrl.isNullOrEmpty().not()) {
                cacheUploadResUrl(filePath, filePrefix, remoteFileName, uploadResult.fileUrl)
            }
            uploadResult.fileUrl
        } else {
            val logMsg = String.format(
                "[失败] 错误: %s\n  耗时: %s",
                uploadResult.errorMessage,
                uploadResult.formattedUploadTime
            );
            LogUtil.d(logMsg)
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
        type: Int,
        sportInfo: SceneSportInfoEntity
    ): String? = syncUploadRes(filePath, sportInfo.sportNatureCodes, sportInfo.sportTypeCode, type)

    fun syncUploadRes(
        filePath: String,
        sportNatureCodes: String,
        sportTypeCode: String,
        type: Int,
    ): String? {
        //有缓存直接从缓存里面取
        val fileInfo = createRemoteFileName(filePath, sportNatureCodes, sportTypeCode, type)
        val cacheUrl = getCacheUploadResUrl(filePath, fileInfo.first, fileInfo.second)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }
        return syncUploadRes(filePath, fileInfo.first, fileInfo.second)
    }

    /**
     * 上传成绩，需要在子线程调用
     */
    fun uploadRecord(sportRecordEntity: SportRecordEntity) {
        //先处理视频本地和远程地址存储，后续再传资源
        /**
         * 七牛云视频文件命名规则：
         * /{端标识（1001、1002）}/video/{业务标识（体测、体锻等）}/{项目编码}/{年}/{月}/{日}/{视频文件}
         * 七牛云图片文件命名规则：
         * /{端标识（1001、1002）}/image/{业务标识（体测、体锻等）}/{项目编码}/{年}/{月}/{日}/{视频文件}
         * //第一个条件为运动会集体赛，集体赛以班级为单位，只传一个成绩，表示为classId,accountId可为空
         */
        if (visitorIfUpload(sportRecordEntity).not()) {
            LogUtil.d("游客模式，不进行上传")
            AdlService.getService()?.recordUploadDao?.clearById(sportRecordEntity.id)
            return
        }
        //先存原始数据到数据库
        saveSportResultToDB(sportRecordEntity)
        //接口服务或者前缀不OK，不处理上传逻辑
        if (AdlService.getService() == null || AdlService.getService().facePrefix == null) {
            LogUtil.e("接口服务或者前缀不OK，不处理上传逻辑,service:${AdlService.getService()}--facePrefix:${AdlService.getService()?.facePrefix}")
            return
        }
        val calendar = Calendar.getInstance()
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

                    it.imageUrl =
                        handleUploadResUrl(sportRecordEntity, it.imageUrl, 1)
                }
            }

            //上传视频
            indicators.indicatorsVideos?.forEach {
                if (it.videoCover.isNullOrEmpty().not()
                    && it.videoCover.startsWith("http").not()
                    && File(it.videoCover).exists()
                ) {
                    it.videoCover =
                        handleUploadResUrl(sportRecordEntity, it.videoCover, 2)
                }
                if (it.videoUrl.isNullOrEmpty().not()
                    && it.videoUrl.startsWith("http").not()
                    && File(it.videoUrl).exists()
                ) {
                    it.videoUrl =
                        handleUploadResUrl(sportRecordEntity, it.videoUrl, 3)
                }
            }

        }

        //上传运动视频
        val authFactory = AdlAuthFactory.create(ApplicationUtil.getContext())
        sportRecordEntity.sportVideos?.forEach {
            //视频需要上传
            if (it.videoUrl != null && it.videoUrl.isNotEmpty() && File(it.videoUrl).exists()) {
                val cacheUploadFileInfo = getCacheUploadResFileInfo(it.videoUrl)
                val videoPrefix =
                    cacheUploadFileInfo?.first
                        ?: "${authFactory.appCode}/video/${sportRecordEntity.sportSceneCode}/${sportRecordEntity.sportTypeCode}/${
                            calendar.get(
                                Calendar.YEAR
                            )
                        }/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
                val videoName = cacheUploadFileInfo?.second ?: "${UUID.randomUUID()}.mp4"
                cacheUploadResFileInfo(it.videoUrl, videoPrefix, videoName)
                it.videoUrl = handleUploadResUrl(
                    sportRecordEntity,
                    it.videoUrl,
                    videoPrefix,
                    videoName
                )
                    .replace(AdlService.getService().facePrefix, "")
            }
            //封面需要上传
            if (it.videoCover != null && it.videoCover.isNotEmpty() && File(it.videoCover).exists()) {
                val cacheUploadFileInfo = getCacheUploadResFileInfo(it.videoCover)
                val coverPrefix = cacheUploadFileInfo?.first
                    ?: "${authFactory.appCode}/image/${sportRecordEntity.sportSceneCode}/${sportRecordEntity.sportTypeCode}/${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
                val coverName = cacheUploadFileInfo?.second ?: "${UUID.randomUUID()}.png"
                cacheUploadResFileInfo(it.videoCover, coverPrefix, coverName)
                it.videoCover = handleUploadResUrl(
                    sportRecordEntity,
                    it.videoCover,
                    coverPrefix,
                    coverName
                ).replace(AdlService.getService().facePrefix, "")
            }
        }
        //更新哈数据库,存储已上传的资源
        saveSportResultToDB(sportRecordEntity)
        //游客使用游客接口上传成绩
        val uploadRecord =
            if (TextUtils.isEmpty(sportRecordEntity.accountId) || sportRecordEntity.accountId == BusiBaseConstantsValue.VISITOR_USER_ID) {
                sportRecordEntity.sportCount = sportRecordEntity.sportResult
                SportRecordService.uploadCustomRecord(sportRecordEntity)
            } else {
                SportRecordService.uploadRecord(sportRecordEntity)
            }
        LogUtil.d("成绩上传完成：accountId = ${sportRecordEntity.accountId} 上传结果：${if (uploadRecord?.isOk == true) "成功" else "失败"}")
        if (uploadRecord?.isOk == true || uploadRecord?.msg?.contains("记录id重复") == true) {
            AdlService.getService()?.recordUploadDao?.clearById(sportRecordEntity.id)
        }
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            executor.execute {
                //补传资源
                AdlService.getService().recordResDao.queryNotUploadRecordRes()
                    .filter { it.uploadFlag == 0 }
                    .map {
                        AdlService.getService().recordResDao.insert(it.apply {
                            uploadFlag = 1
                            startUploadTime = System.currentTimeMillis().toString()
                        })
                        it
                    }.forEach {
                        val url =
                            syncUploadRes(it.localPath, it.uploadFilePrefix, it.uploadFileName)
                        if (!TextUtils.isEmpty(url)) {
                            //上传成功，删除资源数据
                            AdlService.getService().recordResDao.clear(it)
                        }
                    }
            }
        }, UPLOAD_RES_DELAY_TIME)

    }

    fun saveSportResultToDB(sportRecordEntity: SportRecordEntity) {
        AdlService.getService()?.recordUploadDao?.insert(sportRecordEntity)
    }

    /**
     * 校验访客
     * 访客是否上传
     *  如果为游客，锻炼,趣味，微球场，竞技场和自主测试可以上传成绩
     */
    private fun visitorIfUpload(entity: SportRecordEntity): Boolean {
        //集体赛
        val isTeam = entity.sportMeet?.meetGroupType == "1"
        val visitorCanUpload =
            entity.sportSceneCode in setOf(
                SceneConstants.FREE,
                SceneConstants.INDEPENDENT_TEST, SceneConstants.PK,
                SceneConstants.FUN, SceneConstants.MICRO_COURT
            )
        val isVisitor =
            TextUtils.isEmpty(entity.accountId) || entity.accountId == BusiBaseConstantsValue.VISITOR_USER_ID

        return (isTeam || visitorCanUpload) || !isVisitor
    }

}
