package com.ty.tybusbase.identify

import com.adl.lib.pose.AdlPoseSkeleton
import kotlin.math.abs

class ShakeHandDetection : IStartGestureDetection {

    //是否已检测过了
    private var isDetected = false
    private var firstHandX = 0f
    private var shakeDistance = 10f

    // 挥动次数
    private var shakeCount = 0

    // 配置的挥动次数阈值
    private var requiredShakeCount = 4

    // 记录举的是哪只手
    private var handRaised: String? = null

    // 标记是否已经记录过此次挥动
    private var hasRecordedShake = false

    //判断是正还是负
    private var isPositive = false

    //检测手是否挥动
    override fun startGestureDetection(skeleton: AdlPoseSkeleton?, handType: Int): Boolean {
        if (skeleton == null) return false
        //已经检测过了
        if (isDetected) return false

        // 判断举手
        val isHandUp = judgeHandUp(skeleton, handType)
        if (!isHandUp) {
            return false
        }

        return checkShake(skeleton, handType)
    }


    private fun checkShake(skeleton: AdlPoseSkeleton, handType: Int): Boolean {
        var wristX = 0f
        val leftShoulder = skeleton.lShoulder
        val rightShoulder = skeleton.rShoulder
        var shoulderX = 0f
        //肩宽，挥动以肩点，左右正负交替到达肩宽的值就累计计数
        val shoulderWidth = abs(leftShoulder.x - rightShoulder.x) / 2f
        when (handType) {
            0 -> {
                wristX = skeleton.lWrist.x
                shoulderX = skeleton.lShoulder.x
            }

            1 -> {
                wristX = skeleton.rWrist.x
                shoulderX = skeleton.rShoulder.x
            }
        }

        //以肩膀中间点为中点，检测是否挥动
        if (isPositive && wristX - shoulderX <= -shoulderWidth) {
            isPositive = false
            shakeCount++
        }

        if (!isPositive && wristX - shoulderX >= shoulderWidth) {
            isPositive = true
            shakeCount++
        }

        if (shakeCount >= requiredShakeCount) {
            isDetected = true
            return true
        }

        return false
    }

    private fun judgeHandUp(skeleton: AdlPoseSkeleton, handUpType: Int): Boolean {
        return when (handUpType) {
            0 -> {
                // 判断左手
                if (skeleton.lWrist == null || skeleton.lShoulder == null) return false
                val isLeftHandUp = skeleton.lWrist.y < skeleton.lShoulder.y
                handRaised = if (isLeftHandUp) {
                    "左手"
                } else {
                    null
                }
                isLeftHandUp
            }

            1 -> {
                // 判断右手
                if (skeleton.rWrist == null || skeleton.rShoulder == null) return false
                val isRightHandUp = skeleton.rWrist.y < skeleton.rShoulder.y
                handRaised = if (isRightHandUp) {
                    "右手"
                } else {
                    null
                }
                isRightHandUp
            }

            else -> false
        }
    }

    override fun reset() {
        isDetected = false
        shakeCount = 0
        firstHandX = 0f
        hasRecordedShake = false
    }
}