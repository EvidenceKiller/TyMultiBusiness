package com.ty.tybusbase.sport.bean

import android.util.Size

/**
 * @author :WEI_CP
 * Time:2022/3/2 14:36
 * Description: this is SportConfig
 */
class DetectorConfig private constructor(builder: Builder) {
    /**
     * 屏幕尺寸
     */
    private val screenSize: Size?

    /**
     * 骨骼图片大小，既骨骼是基于多大分辨率的图像生成的
     */
    private val bodyImageSize: Size?

    /**
     * 设计图尺寸
     */
    private val designSize: Size?

    /**
     * 关键骨骼点可信度
     */
    var reliabilityThreshold: Float = 0.4f

    /**
     * 首个状态到完成运动的最大允许时间,单位毫秒
     */
    var maxFinishTime: Int = Int.Companion.MAX_VALUE

    /**
     * 首个状态到完成运动的最小时间，单位毫秒
     */
    var minFinishTime: Int

    /**
     * 连续两个动作的最小间隔时间，单位毫秒
     */
    var minIntervalTime: Int

    var needPrescription: Boolean

    var otherRule: Any?

    init {
        screenSize = builder.getScreenSize()
        bodyImageSize = builder.getBodyImageSize()
        designSize = builder.getDesignSize()
        reliabilityThreshold = builder.getReliabilityThreshold()
        maxFinishTime = builder.getLimitMaxTime()
        minFinishTime = builder.getLimitMinTime()
        minIntervalTime = builder.getIntervalTime()
        needPrescription = builder.getNeedPrescription()
        otherRule = builder.getOtherRule()
    }


    class Builder {
        private var screenSize: Size? = null
        private var bodyImageSize: Size? = null
        private var designSize: Size? = null
        private var reliabilityThreshold = 0f
        private var limitMaxTime = 0
        private var limitMinTime = 0
        private var intervalTime = 0
        private var needPrescription = false
        private var otherRule: Any? = null

        fun setScreenSize(screenSize: Size): Builder {
            this.screenSize = screenSize
            return this
        }

        fun getScreenSize(): Size? {
            return screenSize
        }

        fun setBodyImageSize(bodyImageSize: Size): Builder {
            this.bodyImageSize = bodyImageSize
            return this
        }

        fun getBodyImageSize(): Size? {
            return bodyImageSize
        }

        fun setDesignSize(designSize: Size): Builder {
            this.designSize = designSize
            return this
        }

        fun getDesignSize(): Size? {
            return designSize
        }

        fun setReliabilityThreshold(reliabilityThreshold: Float): Builder {
            this.reliabilityThreshold = reliabilityThreshold
            return this
        }

        fun getReliabilityThreshold(): Float {
            return reliabilityThreshold
        }

        fun setLimitMaxTime(limitMaxTime: Int): Builder {
            this.limitMaxTime = limitMaxTime
            return this
        }

        fun getLimitMaxTime(): Int {
            return limitMaxTime
        }

        fun setLimitMinTime(limitMinTime: Int): Builder {
            this.limitMinTime = limitMinTime
            return this
        }

        fun getLimitMinTime(): Int {
            return limitMinTime
        }

        fun setIntervalTime(intervalTime: Int): Builder {
            this.intervalTime = intervalTime
            return this
        }

        fun getIntervalTime(): Int {
            return intervalTime
        }

        fun setNeedPrescription(needPrescription: Boolean): Builder {
            this.needPrescription = needPrescription
            return this
        }

        fun getNeedPrescription(): Boolean {
            return needPrescription
        }

        fun setOtherRule(otherRule: Any?): Builder {
            this.otherRule = otherRule
            return this
        }

        fun getOtherRule(): Any? {
            return otherRule
        }

        fun build(): DetectorConfig {
            return DetectorConfig(this)
        }
    }
}
