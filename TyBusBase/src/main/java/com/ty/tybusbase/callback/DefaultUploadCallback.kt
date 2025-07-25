package com.ty.tybusbase.callback

import com.adl.auth.core.AdlAuthFactory
import com.adl.base.log.AdlLogger
import com.adl.service.entity.SportRecordEntity
import com.adl.service.outer.AdlService
import com.adl.service.web.SportRecordService
import com.ty.tybusbase.helper.ResUploadHelper.syncUploadRes
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.BusiCoreEngine
import com.ty.tybusbase.utils.ResultCallback
import java.io.File
import java.util.Calendar

class DefaultUploadCallback : ResultCallback {
    override fun onSportRecordEntity(sportRecordEntity: SportRecordEntity) {

        saveSportResultToDB(sportRecordEntity)
        uploadRecord(sportRecordEntity)
    }

    fun uploadRecord(sportRecordEntity: SportRecordEntity) {
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
        val authFactory = AdlAuthFactory.create(ApplicationUtil.getContext())
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
            AdlLogger.d("成绩上传完成：accountId = ${sportRecordEntity.accountId} isOk = ${uploadRecord?.isOk}")
            if (uploadRecord?.isOk == true || uploadRecord?.msg?.contains("记录id重复") == true) {
                AdlService.getService().recordUploadDao.clearById(sportRecordEntity.id)
            }
        } else {
            //成绩上传失败，原因：资源上传失败
            AdlLogger.d("成绩上传失败：accountId = ${sportRecordEntity.accountId} ,有资源未上传成功！！！")
        }
    }

    fun saveSportResultToDB(sportRecordEntity: SportRecordEntity) {
        AdlService.getService().recordUploadDao.insert(sportRecordEntity)
    }
}