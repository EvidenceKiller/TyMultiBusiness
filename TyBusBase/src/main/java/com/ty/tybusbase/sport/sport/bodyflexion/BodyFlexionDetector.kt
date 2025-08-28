package com.ty.tybusbase.sport.sport.bodyflexion

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.helper.SportUtil
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * 站立体前屈
 *
 * @author pc
 */
class BodyFlexionDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) : CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    /**
     * 记录运动过程中小腿最大距离
     */
    private var maxDistance = 0f

    /**
     * 手腕成绩
     */
    private var handLever = 0

    /**
     * 膝角
     */
    private var kneeAngleLevel = 0

    /**
     * 限时运动成功后回调
     */
    private var isTimed = false
    private var isLeftBody = false

    init {
        this.config = detectorConfig
    }

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lKnee = body.getLKnee()
        val rKnee = body.getRKnee()
        val rHip = body.getRHip()
        val lHip = body.getLHip()
        val lShoulder = body.getLShoulder()
        val rShoulder = body.getRShoulder()
        val rAnkle = body.getRAnkle()
        val lAnkle = body.getLAnkle()
        val lWrist = body.getLWrist()
        val rWrist = body.getRWrist()
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

        //右边关键点是否可行
        val rReliability =
            rShoulder.getScore() >= config!!.reliabilityThreshold && rKnee.getScore() >= config!!.reliabilityThreshold && rAnkle.getScore() >= config!!.reliabilityThreshold
        //左边关键点是否可行
        val lReliability =
            rShoulder.getScore() >= config!!.reliabilityThreshold && rKnee.getScore() >= config!!.reliabilityThreshold && rAnkle.getScore() >= config!!.reliabilityThreshold
        //左边关键点可行度是否大于右边
        val b: Boolean =
            lShoulder.getScore() + lKnee.getScore() + lAnkle.getScore() >= rShoulder.getScore() + rKnee.getScore() + rAnkle.getScore()
        if (b && lReliability) {
            maxDistance = Math.abs(lHip.y) - lShoulder.y
            isLeftBody = true
        } else if (!b && rReliability) {
            isLeftBody = false
            maxDistance = Math.abs(rHip.y) - rShoulder.y
        } else {
            return
        }
        //请侧面站立
//        if(Math.abs(rShoulder.getX()-lShoulder.getX())>maxDistance/3){
//            result.setMessage("肩距："+(rShoulder.getX()-lShoulder.getX())+"应该不是侧面，参考距离是："+maxDistance );
//            return ;
//        }
        //记录小腿距离
        maxDistance =
            if (isLeftBody) Math.abs(lAnkle.getY() - lKnee.getY()) else Math.abs(rAnkle.getY() - rKnee.getY())

        //获取手部与膝盖的高度
        val currentWristY: Float = if (isLeftBody) lWrist.getY() else rWrist.getY()
        val currentKneeY: Float = if (isLeftBody) lKnee.getY() else rKnee.getY()
        if (currentWristY >= currentKneeY) {
            if (currentWristY >= currentKneeY + maxDistance * 3 / 4) {
                handLever = GOOD
            } else if (handLever == 0) {
                handLever = FINE
            }
        } else {
            reset()
            return
        }


        //获取膝角
        val currentKneeAngle = if (isLeftBody) SportUtil.getRotationBetweenLines2(
            lKnee,
            lAnkle,
            lHip
        ) else SportUtil.getRotationBetweenLines2(rKnee, rAnkle, rHip)
        if (currentKneeAngle > 140) {
            if (currentKneeAngle >= 170) {
                kneeAngleLevel = GOOD
            } else if (kneeAngleLevel == 0) {
                kneeAngleLevel = FINE
            }
        } else {
            singleEuBaseCallBack?.onError(Exception("膝盖不对currentShoulderAngle：$kneeAngleLevel"))
            reset()
            return
        }
        if (!isTimed) {
            isTimed = true
            handler.postDelayed(Runnable {
                result.sportAchievement = 1
                result.singleSportLevel = judgeSportLevel()
                singleEuBaseCallBack?.onComplete(result)
                reset()
            }, 1000)
        }
        return
    }

    /**
     * 判断本次运动的得分
     *
     * @return 返回本次运动的得分
     */
    @SportConstant.SingleSportLevel
    fun judgeSportLevel(): Int {
        var goodCount = 0
        if (handLever == GOOD) {
            goodCount += 1
        }
        if (kneeAngleLevel == GOOD) {
            goodCount += 1
        }
        //设置运动质量
        return if (goodCount >= 2) {
            SPORT_PERFECT
        } else if (goodCount == 1) {
            SPORT_TERRIFIC
        } else {
            SPORT_COME_ON
        }
    }

    override fun reset() {
        super.reset()
        isTimed = false
        handler.removeCallbacksAndMessages(null)
    }
}