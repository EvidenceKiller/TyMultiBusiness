package com.ty.tybusbase.sport.sport.trot

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import kotlin.math.sqrt

/**
 * 小跑判定器
 */
class TrotDetector2(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    /*----------------运动处方------------------*/
    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (null == body) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lKnee = body.lKnee
        val rKnee = body.rKnee
        val lAnkle = body.lAnkle
        val rAnkle = body.rAnkle
        if (!EuHelper.notNull(lKnee, rKnee, lAnkle, rAnkle)) {
            if (null != singleEuBaseCallBack) {
                singleEuBaseCallBack!!.onSpeaker("请保持正面面向屏屏幕")
            }
            return
        }
        judgeTrot(lKnee, rKnee, lAnkle, rAnkle)
    }

    private val STATUS_LEFT = -1
    private val STATUS_STATIC = 0
    private val STATUS_RIGHT = 1
    private var currentMoveStatus = STATUS_STATIC
    private var currentTempStatus = STATUS_STATIC
    private var level = 0
    private val throld = 0.12f //短的一边腿，小于长的一边对的0.12 就认为已经抬腿了

    /**
     * 主要是通过比较脚踝和膝盖的长度，距离短的腿被认为是抬起来的
     * 如果差值超过throld值，确认抬腿
     * @param lKnee
     * @param rKnee
     * @param lAnkle
     * @param rAnkle
     */
    private fun judgeTrot(
        lKnee: AdlPoseKeyPoint,
        rKnee: AdlPoseKeyPoint,
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint
    ) {
        val lLegLen = computeDistance(lKnee, lAnkle)
        val rLegLen = computeDistance(rKnee, rAnkle)
        val delta = Math.abs(lLegLen - rLegLen)
        val min = Math.min(lLegLen, rLegLen)
        val max = Math.max(lLegLen, rLegLen)
        val deltaPercent = delta / max
        if (min == lLegLen) {
            if (deltaPercent > throld) {
                currentTempStatus = STATUS_LEFT
            }
        } else if (min == rLegLen) {
            if (deltaPercent > throld) {
                currentTempStatus = STATUS_RIGHT
            }
        }
        if (currentTempStatus != currentMoveStatus) {
            currentMoveStatus = currentTempStatus
            if (null != singleEuBaseCallBack) {
                level = SPORT_PERFECT
                result.singleSportLevel = level
                result.setMessage("完成")
                result.sportAchievement = 1
                singleEuBaseCallBack!!.onComplete(result)
            }
        }
    }

    private fun computeDistance(p1: AdlPoseKeyPoint, p2: AdlPoseKeyPoint): Float {
        val r =
            sqrt(((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)).toDouble())
        return r.toFloat()
    }
}