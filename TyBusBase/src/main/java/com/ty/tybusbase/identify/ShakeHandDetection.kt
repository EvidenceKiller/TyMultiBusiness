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
    private var requiredShakeCount = 3

    // 记录举的是哪只手
    private var handRaised: String? = null

    // 标记是否已经记录过此次挥动
    private var hasRecordedShake = false

    //检测手是否挥动
    override fun startGestureDetection(skeleton: AdlPoseSkeleton?, handType: Int): Boolean {
        //已经检测过了
        if (skeleton == null) return false
        if (isDetected) return false

        // 判断举手
        val isHandUp = judgeHandUp(skeleton, handType)
        if (!isHandUp) {
            return false
        }

        val currentHandX = when (handType) {
            0 -> skeleton.lWrist?.x
            1 -> skeleton.rWrist?.x
            else -> null
        }

        if (currentHandX != null) {
            if (firstHandX == 0f) {
                firstHandX = currentHandX
                hasRecordedShake = false
            } else if (abs(currentHandX - firstHandX) > shakeDistance && !hasRecordedShake) {
                shakeCount++
                if (shakeCount >= requiredShakeCount) {
                    isDetected = true
                    return true
                }
                firstHandX = currentHandX
                hasRecordedShake = true
            } else if (abs(currentHandX - firstHandX) <= shakeDistance / 2) {
                hasRecordedShake = false
            }
        }
        return false
    }

    private fun judgeHandUp(skeleton: AdlPoseSkeleton, handUpType: Int): Boolean {
        if (skeleton == null) return false
        return when (handUpType) {
            0 -> {
                // 判断左手
                if (skeleton.lWrist == null || skeleton.lShoulder == null) return false
                val isLeftHandUp = skeleton.lWrist.y < skeleton.getLShoulder().y
                if (isLeftHandUp) {
                    handRaised = "左手"
                } else {
                    handRaised = null
                }
                isLeftHandUp
            }

            1 -> {
                // 判断右手
                if (skeleton.rWrist == null || skeleton.rShoulder == null) return false
                val isRightHandUp = skeleton.rWrist.y < skeleton.getRShoulder().y
                if (isRightHandUp) {
                    handRaised = "右手"
                } else {
                    handRaised = null
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