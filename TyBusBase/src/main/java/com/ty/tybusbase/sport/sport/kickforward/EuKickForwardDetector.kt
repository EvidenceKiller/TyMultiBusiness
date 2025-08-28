package com.ty.tybusbase.sport.sport.kickforward

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @author :WEI_CP
 * Time:2023/5/5 10:20
 * Description: 原地前踢
 */
class EuKickForwardDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    //    private EuSportParam mParam;
    private var sportLevel: Int = SportConstant.SPORT_TERRIFIC
    private var statePoint: AdlPoseKeyPoint? = null
    private var preValue = 0f

    init {
        this.config = detectorConfig
        state = SPORT_STATE_IDLE
    }

    override fun onSkeleton(skeleton: AdlPoseSkeleton?) {
        if (skeleton == null) return
        val lAnkle: AdlPoseKeyPoint = skeleton.getLAnkle()
        val rAnkle: AdlPoseKeyPoint = skeleton.getRAnkle()
        val lKnee: AdlPoseKeyPoint = skeleton.getLKnee()
        val rKnee: AdlPoseKeyPoint = skeleton.getRKnee()
        val lHip: AdlPoseKeyPoint = skeleton.getLHip()
        val rHip: AdlPoseKeyPoint = skeleton.getRHip()
        if (!EuHelper.notNull(lAnkle, rAnkle, lKnee, rKnee, lHip, rHip)) {
            return
        }
        val v: Float = config!!.reliabilityThreshold
        val scoreL = getScore(lAnkle, lKnee, lHip)
        val scoreR = getScore(rAnkle, rKnee, rHip)
        var isLeft = false
        isLeft = if (scoreL > scoreR) true else if (scoreL < scoreR) false else {
            return
        }
        if (!isReliability(v, lAnkle, rAnkle, lKnee, rKnee, lHip, rHip)) {
            e("关键点阈值未达标，阈值：$v")
            return
        }
        //判断双腿是否伸直
        val legL: Double = EuHelper.calculateAngle(lKnee, lAnkle, lHip)
        if (100 > legL || 210 < legL) {
            e("左腿未伸直:$legL")
            return
        }
        val legR: Double = EuHelper.calculateAngle(lKnee, lAnkle, lHip)
        if (100 > legR || 210 < legR) {
            e("右腿未伸直:$legR")
            return
        }
        val disAnkle: Double = EuHelper.distance(lAnkle, rAnkle)
        val disLegL: Double = EuHelper.distance(lAnkle, lKnee)
        val disLegR: Double = EuHelper.distance(rAnkle, rKnee)
        val disLeg = Math.max(disLegL, disLegR)
        //判断是否单腿前伸
        d("脚踝距离：$disAnkle-----------小腿阈值：$disLeg")
        if (state == SPORT_STATE_IDLE && disAnkle >= disLeg && isLegChange(lAnkle, rAnkle)) {
            if (disAnkle >= disLeg * 1.2) {
                sportLevel = SportConstant.SPORT_PERFECT
            }
            state = 1
        }
        if (state != 1) return
        val disX: Float = Math.abs(lAnkle.x - rAnkle.x)
        val disY: Float = Math.abs(lAnkle.y - rAnkle.y)
        d("脚踝水平距离：$disX>>>>>>>>>>>>>>>>>脚踝垂直距离：$disY")
        if (disX < disLeg / 3 && disY < disLeg / 3) {
            //  if(!isGround(disLeg,lAnkle,rAnkle)) e("怀疑是走动");
            state = SPORT_STATE_COMPLETE
            result.singleSportLevel = sportLevel
            result.sportAchievement = 1
            singleEuBaseCallBack?.onComplete(result)
            d(OkLine)
            reset()
        }
    }

    /**
     * 判断是否是交替腿
     * 腿交替，Lankle.x - rAnkle 的值会正负交替
     *
     * @param lAnkle
     * @param rAnkle
     * @return
     */
    private fun isLegChange(lAnkle: AdlPoseKeyPoint, rAnkle: AdlPoseKeyPoint): Boolean {
        //第一次，返回true
        val v: Float = lAnkle.x - rAnkle.x
        if (preValue == 0f) {
            preValue = v
            return true
        }
        //上一次为正，这次为负，或者上一次为负，这次为正，既满足交叉腿
        if (preValue > 0 && v < 0 || preValue < 0 && v > 0) {
            preValue = v
            return true
        }
        return false
    }


    override fun reset() {
        super.reset()
        orderBody.clear()
        state = SPORT_STATE_IDLE
        statePoint = null
        sportLevel = SPORT_TERRIFIC
    }

    override fun stop() {
        super.stop()
        reset()
    }

    companion object {
        fun isReliability(v: Float, vararg points: AdlPoseKeyPoint?): Boolean {
            if (points != null && points.size > 0) {
                for (p in points) {
                    if (p == null) {
                        continue
                    }
                    if (p.score < v) return false
                }
                return true
            }
            return false
        }

        fun getScore(vararg jfPoseKeyPoints: AdlPoseKeyPoint?): Float {
            var score = 0f
            if (jfPoseKeyPoints != null && jfPoseKeyPoints.size > 0) {
                for (p in jfPoseKeyPoints) {
                    if (p == null) {
                        continue
                    }
                    score += p.getScore()
                }
            }
            return score
        }
    }
}