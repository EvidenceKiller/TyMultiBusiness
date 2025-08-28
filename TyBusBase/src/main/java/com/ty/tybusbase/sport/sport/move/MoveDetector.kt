package com.ty.tybusbase.sport.sport.move

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @author :WEI_CP 判断是否静止/运动
 * Time:2022/4/28 15:27
 * Description: this is move dector
 */
class MoveDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        val lAnkle = body.lAnkle
        val rAnkle = body.rAnkle
        val lWrist = body.lWrist
        val rWrist = body.rWrist
        if (!EuHelper.notNull(lAnkle, rAnkle, lWrist, rWrist)) {
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }
        judgeTrot(lAnkle, rAnkle, lWrist, rWrist)
    }

    private var lastJudgeTime: Long = 0
    private val delayTime: Long = 500 //这么多秒内置检测一次
    private val ERROR_RANGE = 30 //误差范围，越大越抗干扰，越小误判越大
    private var lastLAnkle: AdlPoseKeyPoint? = null
    private var lastRAnkle: AdlPoseKeyPoint? = null
    private var lastLWrist: AdlPoseKeyPoint? = null
    private var lastRWrist: AdlPoseKeyPoint? = null

    private fun judgeTrot(
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint,
        lWrist: AdlPoseKeyPoint,
        rWrist: AdlPoseKeyPoint
    ) {
        val now = System.currentTimeMillis()
        if (lastJudgeTime == 0L || now - lastJudgeTime > delayTime) {
            if (null == lastLWrist || null == lastRWrist || null == lastLAnkle || null == lastRAnkle) {
                moved()
            } else {

                //脚的位置
                val ldeltaVy = Math.abs(lAnkle.y - lastLAnkle!!.y) //
                val ldeltaVx = Math.abs(lAnkle.x - lastLAnkle!!.x) //
                val rdeltaVy = Math.abs(rAnkle.y - lastRAnkle!!.y) //
                val rdeltaVx = Math.abs(rAnkle.x - lastRAnkle!!.x) //

                //手的位置
                val lHandX = Math.abs(lWrist.x - lastLWrist!!.x)
                val lHandY = Math.abs(lWrist.y - lastLWrist!!.y)
                val rHandX = Math.abs(rWrist.x - lastRWrist!!.x)
                val rHandY = Math.abs(rWrist.y - lastRWrist!!.y)
                if (ldeltaVy > ERROR_RANGE || ldeltaVx > ERROR_RANGE || rdeltaVy > ERROR_RANGE || rdeltaVx > ERROR_RANGE || lHandX > ERROR_RANGE || lHandY > ERROR_RANGE || rHandX > ERROR_RANGE || rHandY > ERROR_RANGE) {
                    moved()
                }
            }
            lastJudgeTime = now
            lastLAnkle = lAnkle
            lastRAnkle = rAnkle
            lastLWrist = lWrist
            lastRWrist = rWrist
        }
    }

    /**
     * 动了
     */
    private fun moved() {
        singleEuBaseCallBack?.onComplete(SportDetectResult())
    }

}