package com.ty.tybusbase.helper

import adl.ssp.common.report.model.IndicatorsResultDto
import adl.ssp.common.report.model.SportIndicators
import adl.ssp.common.report.model.SportVideo
import android.graphics.Bitmap
import android.graphics.Rect
import com.adl.base.common.AdlBitmapUtil
import com.adl.service.entity.SportImageEntity
import com.adl.service.entity.SportIndicatorsEntity
import com.adl.sport.general.EuExerciseFileGroup
import com.adl.sport.general.pullup.PullUpPrescription
import com.adl.sport.general.ropeskip.EuRopeSkipExercisePrescription
import com.adl.sport.general.situp.SitUpPrescription
import com.ty.tybusbase.entity.IndicatorSportStartTimeInvalid
import com.ty.tybusbase.entity.IndicatorSportTimeInvalid
import com.alibaba.fastjson.JSON
import com.ty.tybusbase.constants.IndicatorsConstants
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.IndicatorInterrupt
import com.ty.tybusbase.entity.IndicatorXY
import com.ty.tybusbase.entity.WristRate
import com.ty.tybusbase.utils.NumberUtil
import com.ty.tybusbase.utils.VideoUtil
import com.zy.logic.Point
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

object IndicatorsHelper {
    private val hgl = 100  //合格率
    fun getSkipIndicatorsList(
        pres: EuRopeSkipExercisePrescription?,
        startSportTime: Long,
        endSportTime: Long,
        sportRect: Rect?
    ): MutableList<SportIndicatorsEntity<*, *>>? {
        if (pres == null || pres.totalCount == 0) return null
        val indicatorsList = mutableListOf<SportIndicatorsEntity<*, *>>()
        indicatorsList.add(SportIndicatorsEntity<List<Float>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_SINGLE_TIME_DISTRIBUTION
            this.indicatorsName = "运动单个时间分布"
            val list = mutableListOf<Float>()
            pres.scoreTimeList?.forEach {
                list.add(NumberUtil.formatHalfUp((it - startSportTime) / 1000f).toFloat())
            }
            this.indicatorsValue = list
        })
        indicatorsList.add(SportIndicatorsEntity<Int, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_VALID_COUNT
            this.indicatorsName = "有效个数"
            this.indicatorsValue = pres.totalCount
        })
        indicatorsList.add(SportIndicatorsEntity<Int, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_VIOLATION_COUNT
            this.indicatorsName = "违规个数"
            val list = mutableListOf<Float>()
            pres.scoreTimeList?.forEach {
                list.add(NumberUtil.formatHalfUp((it - startSportTime) / 1000f).toFloat())
            }
            this.indicatorsValue = (list.size - pres.totalCount).coerceAtLeast(0)
        })
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorXY<Float>>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_SPEED_TIME_DISTRIBUTION
            this.indicatorsName = "速度时间分布"
            val list = mutableListOf<IndicatorXY<Float>>()
            var time = 0
            pres.speedArr?.forEach {
                time += 3
                list.add(IndicatorXY(time.toFloat(), NumberUtil.formatHalfUp(it / 60f).toFloat()))
            }

            val sportTime = NumberUtil.formatHalfUp((endSportTime - startSportTime) / 1000f).toFloat().roundToInt()
            //补齐时间线
            while (time < sportTime) {
                time += 3
                if (time > sportTime) {
                    time = sportTime.toInt()
                }
                list.add(IndicatorXY(time.toFloat(), 0f))
            }
            //有多一个的情况，一般只多一个，如果多了，清除最后一个
            val count = NumberUtil.formatHalfUp(sportTime / 3f).toFloat().roundToInt()

            var size = list.size
            while(size > count){
                list.removeAt(list.lastIndex)
                size--
            }
            this.indicatorsValue = list
        })
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorInterrupt>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_INTERRUPT_TIME_DISTRIBUTION
            this.indicatorsName = "中断时间区域分布"
            val list = mutableListOf<IndicatorInterrupt>()
            pres.interruptList?.forEach {
                list.add(
                    IndicatorInterrupt(
                        NumberUtil.formatHalfUp((it.startTime - startSportTime) / 1000f).toFloat(),
                        NumberUtil.formatHalfUp((it.endTime - startSportTime) / 1000f).toFloat()
                    )
                )
            }
            this.indicatorsValue = list
        })
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_TOTAL_TIME
            this.indicatorsName = "总时长"
            this.indicatorsUnit = "s"
            this.indicatorsValue =
                NumberUtil.formatHalfUp((endSportTime - startSportTime) / 1000f).toFloat()
        })
        indicatorsList.add(SportIndicatorsEntity<Int, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_MAST_COMBO_COUNT
            this.indicatorsName = "最大连击数"
            this.indicatorsUnit = "g"
            this.indicatorsValue = pres.comboCountBest
        })
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorXY<Float>>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_POSITION_OFFSET
            this.indicatorsName = "位置偏移"
            val list = mutableListOf<IndicatorXY<Float>>()
            pres.trackPointArr?.forEach {
                list.add(getOffset(it, sportRect))
            }
            this.indicatorsValue = list
        })
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_UPPER_ARM_ANGLE_RATE
            this.indicatorsName = "摇绳大臂夹角合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.formatHalfUp(pres.jumpingSwingPosture).toFloat()
            this.indicatorsImages = mutableListOf()
            if (pres.jumpingSwingPosture < hgl) {
                pres.jumpingSwingPostureIconWorst?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "典型错误",
                            "0"
                        )
                    )
                }
            } else {
                pres.jumpingSwingPostureIconBest?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "运动风采",
                            "0"
                        )
                    )
                }
            }
        })
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_FOREARM_ANGLE_RATE
            this.indicatorsName = "摇绳小臂夹角合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.formatHalfUp(pres.jumpingHLShoulder).toFloat()
            this.indicatorsImages = mutableListOf()
            if (pres.jumpingHLShoulder < hgl) {
                pres.jumpingHLShoulderIconWorst?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "典型错误",
                            "0"
                        )
                    )
                }
            } else {
                pres.jumpingHLShoulderIconBest?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "运动风采",
                            "0"
                        )
                    )
                }
            }

        })
        indicatorsList.add(SportIndicatorsEntity<WristRate, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_WRIST_RATE
            this.indicatorsName = "摇绳手腕对称性合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue =
                WristRate(
                    NumberUtil.formatHalfUp(pres.jumpingHLShoulder).toFloat(),
                    if (pres.jumpingHLShoulder < 85) pres.jumpingHLShoulderLevel else "0"
                )
            this.indicatorsImages = mutableListOf()
            if (pres.jumpingHLShoulder < hgl) {
                pres.jumpingHLShoulderIconWorst?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "典型错误",
                            "0"
                        )
                    )
                }
            } else {
                pres.jumpingHLShoulderIconBest?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "运动风采",
                            "0"
                        )
                    )
                }
            }

        })
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_JUMP_HIGH_RATE
            this.indicatorsName = "起跳高度合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.formatHalfUp(pres.jumpingHeight).toFloat()
            this.indicatorsImages = mutableListOf()
            pres.jumpingHeightIcon?.let {
                this.indicatorsImages.add(
                    SportImageEntity(
                        it.absolutePath,
                        "",
                        if (pres.jumpingHeight < hgl) "典型错误" else "运动风采",
                        "0"
                    )
                )
            }
        })
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SKIP_FOOT_SPACE_RATE
            this.indicatorsName = "双脚间距合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.formatHalfUp(pres.jumpingAnkleWidth).toFloat()
            this.indicatorsImages = mutableListOf()
            if (pres.jumpingAnkleWidth < hgl) {
                pres.jumpingAnkleWidthIconWorst?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "典型错误",
                            "0"
                        )
                    )
                }
            } else {
                pres.jumpingAnkleWidthIconBest?.let {
                    this.indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            "",
                            "运动风采",
                            "0"
                        )
                    )
                }
            }
        })
        return indicatorsList
    }


    /**
     * 获取引体向上运动指标
     */
    fun getPullUpIndicatorsList(
        pres: PullUpPrescription?,
        startSportTime: Long,
        endSportTime: Long,
    ): MutableList<SportIndicatorsEntity<*, *>>? {
        if (pres == null) return null
        val indicatorsList = mutableListOf<SportIndicatorsEntity<*, *>>()

        // 运动单个时间分布
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorSportTimeInvalid>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_SINGLE_TIME_DISTRIBUTION
            this.indicatorsName = "运动单个时间分布"
            val list = mutableListOf<IndicatorSportTimeInvalid>()
            pres.timeDistribute?.forEach {
                list.add(
                    IndicatorSportTimeInvalid(
                        NumberUtil.format1(it.elapsedTime / 1000f).toFloat(), it.invalidFlag
                    )
                )
            }
            this.indicatorsValue = list
        })

        // 速度时间分布
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorXY<Int>>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_SPEED_TIME_DISTRIBUTION
            this.indicatorsName = "速度时间分布"
            val list = mutableListOf<IndicatorXY<Int>>()
            this.indicatorsValue = list
            list.add(IndicatorXY(0, 0))
            pres.points.forEach {
                list.add(IndicatorXY(it.index * 3, it.countOk))
            }
        })

        // 总时长
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_TOTAL_TIME
            this.indicatorsName = "总时长"
            this.indicatorsUnit = "s"
            this.indicatorsValue =
                NumberUtil.formatHalfUp((endSportTime - startSportTime) / 1000f).toFloat()
        })


        // 双手握距合格率
        indicatorsList.add(SportIndicatorsEntity<WristRate, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_HAND_GRIP_DISTANCE_PASS_RATE
            this.indicatorsName = "双手握距合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue =
                WristRate(
                    NumberUtil.format1(pres.holdWidthOkRate * 100).toFloat(),
                    pres.holdWidthLevel.toString()
                )
            this.indicatorsImages = addIndicatorsImages(pres.holdWidthInvalidImage)
        })

        // 左右均衡合格率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_LEFT_RIGHT_BALANCE_PASS_RATE
            this.indicatorsName = "左右均衡合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.leftRightBalanceOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.leftRightBalanceInvalidImage)
        })

        // 动作行程合格率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_MOTION_PROCESS_PASS_RATE
            this.indicatorsName = "动作行程合格率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.journeyOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.journeyInvalidImage)
        })

        // 失败次数（下颌未过杆）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_FAIL_COUNT_CHIN_NOT_OVER_BAR
            this.indicatorsName = "失败次数（下颌未过杆）"
            this.indicatorsValue = pres.chinFlags?.toList()
        })

        // 失败次数（手臂未伸直）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.PULL_UP_FAIL_COUNT_ARM_NOT_STRAIGHT
            this.indicatorsName = "失败次数（手臂未伸直）"
            this.indicatorsValue = pres.armFlags?.toList()
        })


        return indicatorsList
    }


    /**
     * 获取仰卧起坐指标
     */
    fun getSitUpIndicatorsList(
        pres: SitUpPrescription?,
        startSportTime: Long,
        endSportTime: Long,
    ): MutableList<SportIndicatorsEntity<*, *>>? {
        if (pres == null) return null
        val indicatorsList = mutableListOf<SportIndicatorsEntity<*, *>>()

        val sportTime = (endSportTime - startSportTime) / 1000

        // 运动单个时间分布
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorSportStartTimeInvalid>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_SINGLE_TIME_DISTRIBUTION
            this.indicatorsName = "运动单个时间分布"
            val list = mutableListOf<IndicatorSportStartTimeInvalid>()
            pres.timeDistribute?.forEach {
                list.add(
                    IndicatorSportStartTimeInvalid(
                        NumberUtil.format1(it.startTime / 1000f).toFloat(),
                        NumberUtil.format1(it.endTime / 1000f).toFloat(),
                        it.invalidFlag
                    )
                )
            }
            this.indicatorsValue = list
        })

        // 速度时间分布
        indicatorsList.add(SportIndicatorsEntity<List<IndicatorXY<Int>>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_SPEED_TIME_DISTRIBUTION
            this.indicatorsName = "速度时间分布"
            val list = mutableListOf<IndicatorXY<Int>>()
            this.indicatorsValue = list
            list.add(IndicatorXY(0, 0))
            pres.points.forEach {
                val time = it.index * 3
                if (time <= sportTime) {
                    list.add(IndicatorXY(time, it.countOk))
                }
            }
        })

        // 总时长
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_TOTAL_TIME
            this.indicatorsName = "总时长"
            this.indicatorsUnit = "s"
            this.indicatorsValue = sportTime.toFloat()
        })

        // 失败次数（双手未护耳）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_FAIL_COUNT_NOT_HOLDING_HEAD
            this.indicatorsName = "违规次数（双手未护耳）"
            this.indicatorsValue = pres.handToEarFlags?.toList()
        })

        // 失败次数（未屈膝 90°）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_FAIL_COUNT_KNEE_NOT_90_DEGREE
            this.indicatorsName = "违规次数（未屈膝 90°）"
            this.indicatorsValue = pres.KneeAngleFlags?.toList()
        })

        // 失败次数（双肘未触膝）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_FAIL_COUNT_ELBOW_NOT_TOUCHING_KNEE
            this.indicatorsName = "违规次数（双肘未触膝）"
            this.indicatorsValue = pres.touchKneeFlags?.toList()
        })

        // 失败次数（肩背未触底）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode =
                IndicatorsConstants.SITE_UP_FAIL_COUNT_SHOULDER_NOT_TOUCHING_GROUND
            this.indicatorsName = "违规次数（肩背未触底）"
            this.indicatorsValue = pres.shoulderToGroundFlags?.toList()
        })

        // 失败次数（垫臀）
        indicatorsList.add(SportIndicatorsEntity<List<Int>, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_FAIL_COUNT_HIPS_LIFTING
            this.indicatorsName = "违规次数（垫臀）"
            this.indicatorsValue = pres.upliftHipFlags?.toList()
        })

        // 上肢姿势达标率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_UPPER_LIMB_POSTURE_PASS_RATE
            this.indicatorsName = "上肢姿势达标率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.upperLimbPostureOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.upperLimbPostureImage)
        })

        // 核心发力达标率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_CORE_STRENGTH_PASS_RATE
            this.indicatorsName = "核心发力达标率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.coreForceOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.coreForceImage)
        })

        // 屈膝角度达标率
        indicatorsList.add(SportIndicatorsEntity<WristRate, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_KNEE_ANGLE_PASS_RATE
            this.indicatorsName = "屈膝角度达标率"
            this.indicatorsUnit = "%"
            this.indicatorsValue =
                WristRate(
                    NumberUtil.format1(pres.kneeAngleOkRate * 100).toFloat(),
                    pres.kneeAngleLevel.toString()
                )
            this.indicatorsImages = addIndicatorsImages(pres.kneeAngleImage)
        })

        // 髋部稳定达标率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_HIP_STABILITY_PASS_RATE
            this.indicatorsName = "髋部稳定达标率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.hipStableOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.hipStableImage)
        })

        // 仰卧姿势达标率
        indicatorsList.add(SportIndicatorsEntity<Float, String>().apply {
            this.indicatorsCode = IndicatorsConstants.SITE_UP_SUPINE_POSTURE_PASS_RATE
            this.indicatorsName = "仰卧姿势达标率"
            this.indicatorsUnit = "%"
            this.indicatorsValue = NumberUtil.format1(pres.shoulderToGroundOkRate * 100).toFloat()
            this.indicatorsImages = addIndicatorsImages(pres.shoulderToGroundImage)
        })
        return indicatorsList
    }

    private fun addIndicatorsImages(groupList: List<EuExerciseFileGroup>?): MutableList<SportImageEntity> {
        val indicatorsImages = mutableListOf<SportImageEntity>()
        groupList?.forEach { group ->
            group.items?.forEach { item ->
                item.file?.let {
                    indicatorsImages.add(
                        SportImageEntity(
                            it.absolutePath,
                            item.fileName,
                            item.fileImageDesc,
                            group.index.toString()
                        )
                    )
                }
            }
        }
        return indicatorsImages
    }

    fun createIndicatorsResultDto(
        code: String,
        indicatorsList: MutableList<SportIndicatorsEntity<*, *>>?,
        startSportTime: Long,
        endSportTime: Long,
        count: Int,
        videoFile: File?,
        videoCover: String?,
        accountName: String?,
        accountSex: Int?,
        accountAvatar: String?
    ): IndicatorsResultDto? {
        if (indicatorsList == null) return null
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return IndicatorsResultDto().apply {
            this.sportTypeCode = code
            this.userName = accountName
            this.sex = accountSex ?: 0
            this.userAvatar = accountAvatar
            this.sportTestTime = format.format(Date(startSportTime))
            this.sportTime = (endSportTime - startSportTime).toString()
            this.sportResult = "$count"
            this.sportVideos = mutableListOf()
            val videoList = mutableListOf<SportVideo>()
            videoFile?.absolutePath?.let {
                val file = File(videoCover ?: "")
                if (!file.exists()) {
                    val bitmap = VideoUtil.getVideoThumbnail(it)
                    AdlBitmapUtil.save(bitmap, File(videoCover), Bitmap.CompressFormat.PNG)
                }
                videoList.add(SportVideo("视频地址", it, videoCover))
            }
            this.sportVideos = videoList
            val toJson = JSON.toJSONString(indicatorsList)
            val indicators = JSON.parseArray(toJson, SportIndicators::class.java).toMutableList()
            this.sportIndicatorsList = indicators
        }
    }


    private fun getOffset(p: Point, sportRect: Rect?): IndicatorXY<Float> {
        return sportRect?.let {
            val width = it.width()
            val height = it.height()
            val centerX = it.centerX()
            val centerY = it.centerY()
            IndicatorXY(
                NumberUtil.format2((p.x - centerX) / (width / 2f)).toFloat().coerceIn(-1f, 1f),
                NumberUtil.format2((centerY - p.y) / (height / 2f)).toFloat().coerceIn(-1f, 1f)
            )
        } ?: IndicatorXY(
            NumberUtil.format2(p.x / 1000.0).toFloat(),
            NumberUtil.format2(p.y / 1000.0).toFloat()
        )
    }
}