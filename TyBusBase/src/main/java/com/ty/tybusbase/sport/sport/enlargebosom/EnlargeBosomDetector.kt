package com.ty.tybusbase.sport.sport.enlargebosom

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.helper.SportUtil
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @description:
 * @author: chenwei
 * @date :   2022/8/23 11:22 上午
 */
class EnlargeBosomDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var rDown = false
    private var rOn = true
    private var lDown = false
    private var lOn = true

    //level 锻炼等级
    var rLevel = 0
    var lLevel = 0


    override fun onSkeleton(body: AdlPoseSkeleton) {
        singleEuBaseCallBack?.onSkeleton(body)
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
                *arrayOf(
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
            if (null != singleEuBaseCallBack) {
                singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            }
            return
        }
        //肩角
        val lShoulderAngle: Float = SportUtil.getRotationBetweenLines2(lShoulder, lElbow, lHip)
        val rShoulderAngle: Float = SportUtil.getRotationBetweenLines2(rShoulder, rElbow, rHip)
        //臀部到肩膀的距离
        val hipToShoulderYDis: Float = Math.abs(lHip.y - lShoulder.y)
        //获取手腕位移距离 以肩膀为参照
        val lElbowXDis: Float = Math.abs(lWrist.x - lShoulder.x)
        val rElbowXDis: Float = Math.abs(rWrist.x - rShoulder.x)
        if (lElbowXDis < 50) {
            rOn = true
        }
        if (rElbowXDis < 50) {
            lOn = true
        }
        if (!lOn || !rOn) {
            return
        }
        if (lShoulderAngle in 80.0..150.0 && lElbowXDis > hipToShoulderYDis * 2 / 3) {
            rLevel = SPORT_PERFECT
            rDown = true
        }
        if (lShoulderAngle in 80.0..150.0 && lElbowXDis >= hipToShoulderYDis * 1 / 2 && lElbowXDis <= hipToShoulderYDis * 2 / 3) {
            rLevel = SPORT_TERRIFIC
            rDown = true
        }
        if (lShoulderAngle in 60.0..80.0 && lElbowXDis > hipToShoulderYDis * 2 / 3) {
            rLevel = SPORT_TERRIFIC
            rDown = true
        }
        if (lShoulderAngle in 60.0..80.0 && lElbowXDis >= hipToShoulderYDis * 1 / 2 && lElbowXDis <= hipToShoulderYDis * 2 / 3) {
            rLevel = SPORT_COME_ON
            rDown = true
        }
        if (rShoulderAngle in 80.0..150.0 && rElbowXDis > hipToShoulderYDis * 2 / 3) {
            lLevel = SPORT_PERFECT
            lDown = true
        }
        if (rShoulderAngle in 80.0..150.0 && rElbowXDis >= hipToShoulderYDis * 1 / 2 && rElbowXDis <= hipToShoulderYDis * 2 / 3) {
            lLevel = SPORT_TERRIFIC
            lDown = true
        }
        if (rShoulderAngle in 60.0..80.0 && rElbowXDis > hipToShoulderYDis * 2 / 3) {
            lLevel = SPORT_TERRIFIC
            lDown = true
        }
        if (rShoulderAngle in 60.0..80.0 && rElbowXDis >= hipToShoulderYDis * 1 / 2 && rElbowXDis <= hipToShoulderYDis * 2 / 3) {
            lLevel = SPORT_COME_ON
            lDown = true
        }
        if (lLevel == SPORT_PERFECT && rLevel == SportConstant.SPORT_PERFECT) {
            result.singleSportLevel = SportConstant.SPORT_PERFECT
        }
        if (lLevel == SPORT_TERRIFIC && rLevel == SportConstant.SPORT_PERFECT) {
            result.singleSportLevel = SportConstant.SPORT_PERFECT
        }
        if (lLevel == SPORT_PERFECT && rLevel == SportConstant.SPORT_TERRIFIC) {
            result.singleSportLevel = SportConstant.SPORT_TERRIFIC
        }
        if (lLevel == SPORT_COME_ON && rLevel == SportConstant.SPORT_COME_ON) {
            result.singleSportLevel = SportConstant.SPORT_COME_ON
        }
        if (rDown && lDown) {
            state = SPORT_STATE_COMPLETE
            result.setMessage("完成")
            result.sportAchievement = 1
            if (result.singleSportLevel == 0) { //默认给一个level为0的问题
                result.singleSportLevel = SportConstant.SPORT_COME_ON
            }
            if (null != singleEuBaseCallBack) {
                singleEuBaseCallBack?.onComplete(result)
            }
            rDown = false
            rOn = false
            lDown = false
            lOn = false
            reset()
        }
    }

    companion object {
        private const val TAG = "------扩胸运动   "
    }
}