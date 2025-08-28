package com.ty.tybusbase.sport.sport.onelegjump

import android.util.Log
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import kotlin.math.abs

/**
 * @author :WEI_CP
 * Time:2022/4/28 15:27
 * Description: this is skip01
 */
class OneLegJumpDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {

    private val TAG = "ONELEG"

    /**
     * refDistance
     * 记录参考距离，这里使用肩膀之间的距离
     */
    private var refDistance = 0f
    private var handLevel = 0
    private var footLevel = 0
    private var minShoulderAndHipY = 0f
    private var maxShoulderAndHipY = 0f
    private var intervalTime = 0
    private var thHardUpMove = 0f
    private var thHardDownMove = 0f

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        val lAnkle: AdlPoseKeyPoint = body.getLAnkle()
        val rAnkle: AdlPoseKeyPoint = body.getRAnkle()
        val lKnee: AdlPoseKeyPoint = body.getLKnee()
        val rKnee: AdlPoseKeyPoint = body.getRKnee()
        val lShoulder: AdlPoseKeyPoint = body.getLShoulder()
        val rShoulder: AdlPoseKeyPoint = body.getRShoulder()
        val rHip: AdlPoseKeyPoint = body.getRHip()
        val lHip: AdlPoseKeyPoint = body.getLHip()
        val lWrist: AdlPoseKeyPoint = body.getLWrist()
        val rWrist: AdlPoseKeyPoint = body.getRWrist()
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
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }


        //获取当前的位置
        val lAnkleY: Float = lAnkle.y
        val rAnkleY: Float = rAnkle.y
        val lShoulderY: Float = lShoulder.y
        val rShoulderY: Float = rShoulder.y
        val shoulderAndHipY: Float = (lShoulderY + rShoulderY + lHip.y + rHip.y) / 4
        val ankleAndKneeYL = abs(lAnkleY - lKnee.y)
        val ankleAndKneeYR = abs(rAnkleY - rKnee.y)


        //检查关键点的可信程度
        if (!reliability(lAnkle, rAnkle, lShoulder, rShoulder)) {
            logD("异常骨骼点可行度不够")
            return
        }

        //更新相关值
        if (updateFrame(Math.max(ankleAndKneeYL, ankleAndKneeYR), shoulderAndHipY)) return
        val betweenAnkleDis = Math.abs(lAnkleY - rAnkleY)
        val LAKH = EuHelper.calculateAngle(
            body.getLKnee(),
            body.getLAnkle(),
            body.getLHip()
        )
        val RAKH = EuHelper.calculateAngle(
            body.getRKnee(),
            body.getRAnkle(),
            body.getRHip()
        )
        if (betweenAnkleDis < refDistance * 0.3) {
            // reset();
            logD("异常请保持单脚状态")
            //            ToastUtil.getInstance(AppApplication.getInstance().getApplicationContext(),"请保持单脚跳").show();
            return
        }

        //判断是否是正面面向屏幕
        if (Math.abs(lShoulder.x - rShoulder.x) < Math.abs(lHip.y - lKnee.y) / 1.5) {
            reset()
            Log.e(TAG, "异常请正面站立")
            return
        }


        //手部结果判定
        if (lWrist.y > lHip.y && rWrist.y > rHip.y) {
            handLevel = GOOD
        }
        hardJudge(shoulderAndHipY, false)
    }

    private fun updateFrame(
        ankleAndKneeY: Float,
        shoulderAndHipY: Float
    ): Boolean {
        //记录当前的参考距离
        refDistance = Math.max(refDistance, ankleAndKneeY)
        logD("阈值：$refDistance")

        //向上的位移
        thHardUpMove = refDistance * 0.12f
        thHardDownMove = refDistance * 0.13f
        if (minShoulderAndHipY == 0f || maxShoulderAndHipY == 0f) {
            maxShoulderAndHipY = shoulderAndHipY
            minShoulderAndHipY = maxShoulderAndHipY
            logD("  初始帧")
            return true
        }
        if (maxShoulderAndHipY < shoulderAndHipY) {
            maxShoulderAndHipY = shoulderAndHipY
        }
        if (minShoulderAndHipY > shoulderAndHipY) {
            minShoulderAndHipY = shoulderAndHipY
        }
        return false
    }

    private fun hardJudge(shoulderAndHipY: Float, isEasy: Boolean) {

        //高帧率情况下通过多次 低帧率情况下通过少次多量的方式
        val tempUpDis = thHardUpMove
        val tempDownDis = thHardDownMove


        //向上这里只用初始帧来判定
        if (maxShoulderAndHipY - shoulderAndHipY >= tempUpDis) {
            if (state === SPORT_STATE_IDLE) {
                minShoulderAndHipY = shoulderAndHipY
                state = 1
                logD("  位移模式向上满足了")
                oneStateTime = System.currentTimeMillis()
            }
            if (maxShoulderAndHipY - shoulderAndHipY > tempUpDis * 2) {
                footLevel = GOOD
            }
        }
        if (state === SPORT_STATE_IDLE) {
            return
        }
        val now = System.currentTimeMillis()
        if (now - oneStateTime > config!!.maxFinishTime) {
            Log.e(TAG, "异常超过了单个最大时间")
            reset()
            return
        }
        if (shoulderAndHipY - minShoulderAndHipY >= tempDownDis) {
            if (now - upCountTime < config!!.minIntervalTime) {
                Log.e(
                    TAG,
                    "异常两次计数时间" + (now - upCountTime) + "小于 最小 间隔 时间 " + config!!.minIntervalTime
                )
                reset()
                return
            }
            if (now - oneStateTime < config!!.minFinishTime) {
                if ((now - oneStateTime).toInt() == 0) {
                    return
                }
                Log.e(
                    TAG,
                    "  单个运动完成时间" + (now - oneStateTime) + "小于最小完成时间" + config!!.minFinishTime
                )
                reset()
                return
            }
            eventOk(now)
        }
    }

    private fun eventOk(now: Long) {
        result.sportAchievement = 1
        result.singleSportLevel = judgeSportLevel()
        result.setMessage(TAG + "完成耗时：" + (now - oneStateTime))
        logD("  >>>>>完成>>>>>" + (now - oneStateTime))
        reset()
        if (intervalTime < 300 && intervalTime != 0 && Math.abs(intervalTime * 2 - (now - upCountTime)) < 60) {
            result.sportAchievement = 1
            logD("补齐一个，上次间隔时间" + intervalTime + "本次间隔时间：" + (now - upCountTime))
        }
        intervalTime = (now - upCountTime).toInt()
        logD("  间隔时间：$intervalTime")
        upCountTime = now
        //        sportDetectorCallBack.onComplete(result);
        singleEuBaseCallBack?.onComplete(result)
    }

    /**
     * 检查所需关键点的可信度
     *
     * @param lAnkle
     * @param rAnkle
     * @param lShoulder
     * @param rShoulder
     * @return
     */
    private fun reliability(
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint,
        lShoulder: AdlPoseKeyPoint,
        rShoulder: AdlPoseKeyPoint
    ): Boolean {
        if (lAnkle.getScore() < config!!.reliabilityThreshold) {
            result.setMessage("跳绳：lAnkle 骨骼点可信度低：" + lAnkle.getScore())
            return false
        }
        if (rAnkle.getScore() < config!!.reliabilityThreshold) {
            result.setMessage("跳绳：rAnkle 骨骼点可信度低：" + rAnkle.getScore())
            return false
        }
        if (lShoulder.getScore() < config!!.reliabilityThreshold) {
            result.setMessage("跳绳：lShoulder 骨骼点可信度低：" + lShoulder.getScore())
            return false
        }
        if (rShoulder.getScore() < config!!.reliabilityThreshold) {
            result.setMessage("跳绳：rShoulder 骨骼点可信度低：" + rShoulder.getScore())
            return false
        }
        return true
    }

    @SportConstant.SingleSportLevel
    fun judgeSportLevel(): Int {
        var goodCount = 0
        if (handLevel == GOOD) {
            goodCount += 1
        }
        if (footLevel == GOOD) {
            goodCount += 1
        }
        //设置运动质量
        return if (goodCount >= 2) {
            SportConstant.SPORT_PERFECT
        } else if (goodCount == 1) {
            SportConstant.SPORT_TERRIFIC
        } else {
            SportConstant.SPORT_COME_ON
        }
    }

    override fun reset() {
        super.reset()
        logD("重置处理器")
        footLevel = 0
        handLevel = footLevel
        maxShoulderAndHipY = 0f
        minShoulderAndHipY = maxShoulderAndHipY
    }

    companion object {
        private const val TAG = "------单脚跳》   "
    }
}