package com.ty.tybusbase.sport.sport.witharm

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.helper.SportUtil
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @description: 振臂运动处理器
 * @author: chenwei
 * @date :   2022/8/19 10:50 上午
 */
class WithArmDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var hangUpTop = false
    private var hangUpDown = false
    private var hangUpTopR = false
    private var hangUpDownR = false

    //level 锻炼等级
    var level = 0

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lAnkle = body.lAnkle
        val rAnkle = body.rAnkle
        val lKnee = body.lKnee
        val rKnee = body.rKnee

        //肩膀
        val lShoulder = body.lShoulder
        val rShoulder = body.rShoulder

        //臀部
        val rHip = body.rHip
        val lHip = body.lHip

        //手腕
        val lWrist = body.lWrist
        val rWrist = body.rWrist

        //手肘
        val lElbow = body.lElbow
        val rElbow = body.rElbow
        if (!EuHelper.notNull(
                *arrayOf<AdlPoseKeyPoint>(
                    lWrist,
                    rWrist,
                    rShoulder,
                    lShoulder,
                    lKnee,
                    rKnee,
                    rHip,
                    lHip,
                    lAnkle,
                    rAnkle
                )
            )
        ) {
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }
        //手肘夹角
        val lElbowAngle = SportUtil.getRotationBetweenLines2(lElbow, lWrist, lShoulder)
        val rElbowAngle = SportUtil.getRotationBetweenLines2(rElbow, rWrist, rShoulder)

        //肩角
        val lShoulderAngle = SportUtil.getRotationBetweenLines2(lShoulder, lElbow, lHip)
        val rShoulderAngle = SportUtil.getRotationBetweenLines2(rShoulder, rElbow, rHip)
        if (lElbowAngle > 130 && lShoulderAngle > 160) {
            hangUpTop = true
            hangUpDown = false
        }
        if (lElbowAngle > 130 && lShoulderAngle < 30) {
            hangUpDown = true
            level = SPORT_PERFECT
        }
        if (lElbowAngle > 130 && lShoulderAngle <= 160 && lShoulderAngle >= 120) {
            hangUpTop = true
            hangUpDown = false
        }
        if (lElbowAngle > 130 && lShoulderAngle >= 30 && lShoulderAngle <= 80) {
            hangUpDown = true
            level = SPORT_TERRIFIC
        }
        if (rElbowAngle > 130 && rShoulderAngle > 160) {
            hangUpTopR = true
            hangUpDownR = false
        }
        if (rElbowAngle > 130 && rShoulderAngle < 30) {
            hangUpDownR = true
            level = SPORT_PERFECT
        }
        if (rElbowAngle > 130 && rShoulderAngle <= 160 && rShoulderAngle >= 120) {
            hangUpTopR = true
            hangUpDownR = false
        }
        if (rElbowAngle > 130 && rShoulderAngle >= 30 && rShoulderAngle <= 80) {
            hangUpDownR = true
            level = SPORT_TERRIFIC
        }
        if (hangUpDown && hangUpTop) {
            state = SPORT_STATE_COMPLETE
            result.singleSportLevel = level
            result.setMessage("完成")
            result.sportAchievement = 1
            singleEuBaseCallBack?.onComplete(result)
            hangUpTop = false
            hangUpDown = false
            reset()
        }
        if (hangUpDownR && hangUpTopR) {
            state = SPORT_STATE_COMPLETE
            result.singleSportLevel = level
            result.setMessage("完成")
            result.sportAchievement = 1
            singleEuBaseCallBack?.onComplete(result)
            hangUpTopR = false
            hangUpDownR = false
            reset()
        }
    }

    override fun reset() {
        super.reset()
    }

    companion object {
        private const val TAG = "------振臂运动   "
    }
}