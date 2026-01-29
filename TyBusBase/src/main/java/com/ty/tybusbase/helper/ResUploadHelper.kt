package com.ty.tybusbase.helper

import com.adl.base.log.AdlLogger
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.upload.FileUploadSyncService
import com.adl.service.upload.OssFileUploadService
import com.ty.tybusbase.utils.LogUtil
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ResUploadHelper {
    val uploadResMap = ConcurrentHashMap<String, String?>()
//    fun syncUploadRes(
//        filePath: String,
//        sportNatureCodes: String,
//        sportTypeCode: String,
//        id: String,
//        type: Int,
//    ): String? {
//        //有缓存直接从缓存里面取
//        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
//        if (cacheUrl.isNullOrEmpty().not()) {
//            return cacheUrl
//        }
//        var qnUrl: String? = null
//        val remoteFileName = when (type) {
//            1 -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
//            2 -> "PTE/sport/indicator/images/cover/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
//            3 -> "PTE/sport/indicator/videos/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.mp4"
//            else -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}/${id}_${System.currentTimeMillis()}.png"
//        }
//        val requestResult = FileUploadSyncService.instance()
//            .syncUploadFile(File(filePath), remoteFileName)
//        if (requestResult.isOk && requestResult.content is String) {
//            val url = requestResult.content as? String
//            AdlLogger.d("upload Res 成功： ${url}")
//            qnUrl = url
//        } else {
//            AdlLogger.d("upload Res 失败： ${requestResult.msg}===${filePath}")
//        }
//        //存缓存，防止资源重复上传
//        if (filePath.isNotEmpty() && qnUrl != null) {
//            uploadResMap[filePath] = qnUrl
//        }
//        return qnUrl
//    }

    fun syncUploadRes(filePath: String, filePrefix: String, remoteFileName: String): String? {
        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
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
                uploadResMap[filePath] = uploadResult.fileUrl
            }
            uploadResult.fileUrl
        } else {
            val logMsg = String.format(
                "[失败] 错误: %s\n  耗时: %s",
                uploadResult.getErrorMessage(),
                uploadResult.getFormattedUploadTime()
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
        id: String,
        type: Int,
        sportInfo: SceneSportInfoEntity
    ): String? {
        //有缓存直接从缓存里面取
        val cacheUrl = uploadResMap.getOrDefault(filePath, null)
        if (cacheUrl.isNullOrEmpty().not()) {
            return cacheUrl
        }
        var qnUrl: String? = null
        val remoteFileName = when (type) {
            1 -> "${id}_${System.currentTimeMillis()}.png"
            2 -> "${id}_${System.currentTimeMillis()}.png"
            3 -> "${id}_${System.currentTimeMillis()}.mp4"
            else -> "${id}_${System.currentTimeMillis()}.png"
        }

        val filePrefix = when (type) {
            1 -> "PTE/sport/indicator/images/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}"
            2 -> "PTE/sport/indicator/images/cover/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}"
            3 -> "PTE/sport/indicator/videos/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}"
            else -> "PTE/sport/indicator/images/${sportInfo!!.sportNatureCodes}/${sportInfo!!.sportTypeCode}"
        }
        val uploadResult =
            OssFileUploadService.instance().syncUploadFile(filePath, filePrefix, remoteFileName)
        if (uploadResult.isSuccess) {
            qnUrl = uploadResult.fileUrl
            val logMsg = String.format(
                "[成功] %s\n  URL: %s\n  耗时: %s",
                //上传到OSS的文件名
                uploadResult.fileName,
                //上传到OSS的url路径
                uploadResult.fileUrl,
                //上传耗时
                uploadResult.formattedUploadTime
            );
            //存缓存，防止资源重复上传
            if (qnUrl.isNullOrEmpty().not()) {
                uploadResMap[filePath] = qnUrl
            }
            LogUtil.d(logMsg)
        } else {
            val logMsg = String.format(
                "[失败] 错误: %s\n  耗时: %s",
                uploadResult.getErrorMessage(),
                uploadResult.getFormattedUploadTime()
            );
            LogUtil.d(logMsg)
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
            1 -> "${id}_${System.currentTimeMillis()}.png"
            2 -> "${id}_${System.currentTimeMillis()}.png"
            3 -> "${id}_${System.currentTimeMillis()}.mp4"
            else -> "${id}_${System.currentTimeMillis()}.png"
        }

        val filePrefix = when (type) {
            1 -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}"
            2 -> "PTE/sport/indicator/images/cover/${sportNatureCodes}/${sportTypeCode}"
            3 -> "PTE/sport/indicator/videos/${sportNatureCodes}/${sportTypeCode}"
            else -> "PTE/sport/indicator/images/${sportNatureCodes}/${sportTypeCode}"
        }

        val uploadResult =
            OssFileUploadService.instance().syncUploadFile(filePath, filePrefix, remoteFileName)
        if (uploadResult.isSuccess) {
            qnUrl = uploadResult.fileUrl
            val logMsg = String.format(
                "[成功] %s\n  URL: %s\n  耗时: %s",
                //上传到OSS的文件名
                uploadResult.fileName,
                //上传到OSS的url路径
                uploadResult.fileUrl,
                //上传耗时
                uploadResult.formattedUploadTime
            );
            //存缓存，防止资源重复上传
            if (qnUrl.isNullOrEmpty().not()) {
                uploadResMap[filePath] = qnUrl
            }
            LogUtil.d(logMsg)
        } else {
            val logMsg = String.format(
                "[失败] 错误: %s\n  耗时: %s",
                uploadResult.getErrorMessage(),
                uploadResult.getFormattedUploadTime()
            );
            LogUtil.d(logMsg)
        }
        return qnUrl
    }
}