package com.ty.tybusbase.sport.sport.squat

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectError
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import kotlin.math.abs
import kotlin.math.max


/**
 * 下蹲
 *
 * @author pc
 */
class SquatDetector(singleEuBaseCallBack: SingleEuBaseCallBack?, detectorConfig: DetectorConfig) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    /**
     * 记录运动中髋关节Y
     */
    private var maxHipY = 0f
    private var miniHipY = 0f

    /**
     * 参考距离
     */
    private var maxDistance = 0f

    /**
     * 下蹲
     */
    private var squatLevel: Int = FINE

    /**
     * 向上标记
     */
    private var upLevel: Int = GOOD
    private var upLankleY = 0f
    private var upRankleY = 0f
    private var moveLimit = 0f
    private var upAnkleDis = 0f

    protected var error: DetectError? = null


    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lElbow: AdlPoseKeyPoint = body.getLElbow()
        val rElbow: AdlPoseKeyPoint = body.getRElbow()
        val rWrist: AdlPoseKeyPoint = body.getRWrist()
        val lWrist: AdlPoseKeyPoint = body.getLWrist()
        val rShoulder: AdlPoseKeyPoint = body.getRShoulder()
        val lShoulder: AdlPoseKeyPoint = body.getLShoulder()
        val lKnee: AdlPoseKeyPoint = body.getLKnee()
        val rKnee: AdlPoseKeyPoint = body.getRKnee()
        val rHip: AdlPoseKeyPoint = body.getRHip()
        val lHip: AdlPoseKeyPoint = body.getLHip()
        val lAnkle: AdlPoseKeyPoint = body.getLAnkle()
        val rAnkle: AdlPoseKeyPoint = body.getRAnkle()
        if (!EuHelper.notNull(
                *arrayOf(
                    lElbow,
                    lWrist,
                    rElbow,
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

        //判断骨骼点的可行度
        if (config != null && (lElbow.getScore() < config!!.reliabilityThreshold
                    || rElbow.getScore() < config!!.reliabilityThreshold
                    || rShoulder.getScore() < config!!.reliabilityThreshold
                    || lShoulder.getScore() < config!!.reliabilityThreshold
                    || lKnee.getScore() < config!!.reliabilityThreshold
                    || rKnee.getScore() < config!!.reliabilityThreshold
                    || rHip.getScore() < config!!.reliabilityThreshold
                    || lHip.getScore() < config!!.reliabilityThreshold
                    || lAnkle.getScore() < config!!.reliabilityThreshold
                    || rAnkle.getScore() < config!!.reliabilityThreshold
                    || rWrist.getScore() < config!!.reliabilityThreshold
                    || lWrist.getScore() < config!!.reliabilityThreshold)
        ) {
            return
        }
        maxDistance = max(maxDistance,abs(rShoulder.x - lShoulder.x))

        moveLimit = maxDistance / 2
        val currentHipY: Float = (lHip.y + rHip.y) / 2

        //记录髋关节最大点 身体越向下值越大
        if (maxHipY == 0f || maxHipY < currentHipY) {
            maxHipY = currentHipY
        }
        if (miniHipY == 0f || miniHipY > currentHipY) {
            miniHipY = currentHipY
        }

        //判断是否单脚
        val ankleYDis: Float = abs(lAnkle.y - rAnkle.y)
        upAnkleDis = if (upAnkleDis == 0f) {
            ankleYDis
        } else if (abs(ankleYDis - upAnkleDis) > moveLimit / 2) {
            singleEuBaseCallBack?.onError(Exception("双脚间距变化太大了----->重置"))
            upAnkleDis = 0f
            reset()
            return
        } else {
            ankleYDis
        }

        //  要求是手部距离需要伸直向前，通过手臂的距离来判断的
        if (Math.abs(lWrist.y - lElbow.y) <= moveLimit / 2 && Math.abs(lElbow.y - lShoulder.y) <= moveLimit / 2 && Math.abs(
                rWrist.y - rElbow.y
            ) <= moveLimit / 2 && Math.abs(rElbow.y - rShoulder.y) <= moveLimit / 2
        ) {
            upLevel = GOOD
        }


        //向下的位移判断
        if (state <= 1) {

            if (currentHipY - miniHipY >= moveLimit * 1.5) {
                //识别到向下的位移
                if (currentHipY - miniHipY >= moveLimit * 2) {
                    squatLevel = GOOD
                }
                if (state == SPORT_STATE_IDLE) {
                    state = 1
                    oneStateTime = System.currentTimeMillis()
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed(Runnable {
                        singleEuBaseCallBack?.onError(Exception("单个完成超时了》重置"))
                        reset()
                    }, config?.maxFinishTime?.toLong() ?: 0)
                }
            }
        }
        if (state == SPORT_STATE_IDLE) {
            return
        }


        //向上的位移判断
        if (maxHipY - currentHipY >= moveLimit) {
            if (System.currentTimeMillis() - oneStateTime < (config?.minFinishTime ?: 0)) {
                singleEuBaseCallBack?.onError(Exception("完成时间太快了"))
                return
            }
            if (System.currentTimeMillis() - upCountTime < (config?.minIntervalTime ?: 0)) {
                singleEuBaseCallBack?.onError(Exception("连续完成两个的间隔时间小于最小的间隔时间了"))
                return
            }

            //result.setPointForAnimation(new Point((int) ((rAnkle.getX() + ((rAnkle.getX() - lAnkle.getX()) / 2)) * scaleW), (int) ((rAnkle.getY() + ((rAnkle.getY() - lAnkle.getY()) / 2)) * scaleH)));
            result.singleSportLevel = judgeSportLevel()
            result.sportAchievement = 1
            result.setMessage("----------------------->完成一次")
            upCountTime = System.currentTimeMillis()
            //            sportDetectorCallBack.onComplete(result);
            singleEuBaseCallBack?.onComplete(result)
            reset()
        }
    }


    /**
     * 判断本次运动的得分
     *
     * @return 返回本次运动的得分
     */
    @SportConstant.SingleSportLevel
    fun judgeSportLevel(): Int {
        var goodCount = 0
        if (upLevel == GOOD) {
            goodCount += 1
        }
        if (squatLevel == GOOD) {
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
        maxHipY = 0f
        miniHipY = maxHipY
        upLankleY = 0f
        upRankleY = 0f
        squatLevel = FINE
    }
}