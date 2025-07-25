package com.ty.tybusbase.helper

import com.adl.base.log.AdlLogger
import com.adl.service.upload.FileUploadSyncService
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ResUploadHelper {
    val uploadResMap = ConcurrentHashMap<String, String?>()
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
}