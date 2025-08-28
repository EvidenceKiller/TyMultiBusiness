package com.ty.tybusbase.sport.sport.singlehandrope

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.helper.SportUtil
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack


/**
 * @description: 左右手单手摇绳
 * @author: chenwei
 * @date :   2022/9/16 2:06 下午
 */
class SingleHandRopeDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var rDown = false
    private var lUp = false
    private var lDown = false
    private var rUp = false
    var lLevel = 0
    var rLevel = 0


    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return

        val lAnkle: AdlPoseKeyPoint = body.getLAnkle()
        val rAnkle: AdlPoseKeyPoint = body.getRAnkle()
        val lKnee: AdlPoseKeyPoint = body.getLKnee()
        val rKnee: AdlPoseKeyPoint = body.getRKnee()

        //肩膀
        val lShoulder: AdlPoseKeyPoint = body.getLShoulder()
        val rShoulder: AdlPoseKeyPoint = body.getRShoulder()

        //臀部
        val rHip: AdlPoseKeyPoint = body.getRHip()
        val lHip: AdlPoseKeyPoint = body.getLHip()

        //手腕
        val lWrist: AdlPoseKeyPoint = body.getLWrist()
        val rWrist: AdlPoseKeyPoint = body.getRWrist()

        //手肘
        val lElbow: AdlPoseKeyPoint = body.getLElbow()
        val rElbow: AdlPoseKeyPoint = body.getRElbow()
        if (!EuHelper.notNull(
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
        ) {
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }
        //手肘夹角
        val lElbowAngle = SportUtil.getRotationBetweenLines2(lElbow, lWrist, lShoulder)
        val rElbowAngle = SportUtil.getRotationBetweenLines2(rElbow, rWrist, rShoulder)

        //当左右手肘角度同时小于120度 判断为双手绕绳
        if (lElbowAngle < 120 && rElbowAngle < 120) {
            return
        }
        //当手肘的角度小于100度时记录向上运动为true
        if (rElbowAngle < 110) {
            rUp = true
            rDown = false
        }
        //当手肘的角度大于130度时记录向下运动为true 如果大于140则位移过大 level 为真棒  以下同理
        if (rElbowAngle > 120) {
            rDown = true
            if (rElbowAngle > 140) {
                rLevel = SPORT_TERRIFIC
            } else {
                rLevel = SPORT_PERFECT
            }
        }
        if (lElbowAngle < 110) {
            lUp = true
            lDown = false
        }
        if (lElbowAngle > 120) {
            lDown = true
            if (lElbowAngle > 140) {
                lLevel = SPORT_TERRIFIC
            } else {
                lLevel = SPORT_PERFECT
            }
        }
        //向上并完成向下的动作记录为一次运动 并回调结果 以下同理
        if (lUp && lDown) {
            state = SPORT_STATE_COMPLETE
            result.setMessage("完成")
            result.sportAchievement = 1
            result.singleSportLevel = lLevel
            singleEuBaseCallBack?.onComplete(result)
            reset()
        }
        if (rUp && rDown) {
            state = SPORT_STATE_COMPLETE
            result.setMessage("完成")
            result.sportAchievement = 1
            result.singleSportLevel = rLevel
            singleEuBaseCallBack?.onComplete(result)
            reset()
        }
    }

    override fun reset() {
        super.reset()
        lUp = false
        lDown = false
        rUp = false
        rDown = false
        lLevel = 0
        rLevel = 0
    }

    companion object {
        private const val TAG = "------左右手单手摇绳   "
    }
}