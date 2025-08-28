package com.ty.tybusbase.sport.sport.armaround

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.helper.SportUtil
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @description:
 * @author: chenwei
 * @date :   2022/10/7 4:59 下午
 */
class ArmAroundDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var isReady = false
    private var level = 0
    private var count = 0
    private var lUP = false
    private var lDown = false
    private var rUP = false
    private var rDown = false


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
        //计算上臂与身体夹角
        val lShoulderAngle = SportUtil.getRotationBetweenLines2(lShoulder, lElbow, lHip)
        val rShoulderAngle = SportUtil.getRotationBetweenLines2(rShoulder, rElbow, rHip)
        if (lShoulderAngle >= 80 && lShoulderAngle < 150 && rShoulderAngle >= 80 && rShoulderAngle < 150) {
            isReady = true
            level = SPORT_PERFECT
        } else if (lShoulderAngle > 30 && lShoulderAngle < 80 && rShoulderAngle > 30 && rShoulderAngle < 80) {
            isReady = true
            level = SPORT_TERRIFIC
        } else {
            isReady = false
            level = SPORT_COME_ON
        }
        if (lShoulderAngle > 100 && lShoulderAngle < 160) {
            lUP = true
            lDown = false
        }
        if (rShoulderAngle > 100 && rShoulderAngle < 160) {
            rUP = true
            rDown = false
        }
        if (lShoulderAngle < 90) {
            lDown = true
        }
        if (rShoulderAngle < 90) {
            rDown = true
        }
        if (isReady && lUP && lDown
            && rDown && rUP
        ) {
            count++
            if (count == 1) {
                state = SPORT_STATE_COMPLETE
                result.singleSportLevel = level
                result.setMessage("完成")
                result.sportAchievement = 1
                singleEuBaseCallBack?.onComplete(result)
                isReady = false
                lUP = false
                lDown = false
                rUP = false
                rDown = false
                count = 0
                reset()
            }
        }
    }

    companion object {
        private const val TAG = "------平举环绕 "
    }
}