package com.ty.tybusbase.sport.sport.highknee

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * 高抬腿
 * 备忘：如果需要区别双脚起跳，可以通过在完成状态下去判断下两个膝盖或者两个脚踝的垂直距离
 *
 * @author pc
 */
class HighKneeDetector2(
    detectorConfig: DetectorConfig,
    singleEuBaseCallBack: SingleEuBaseCallBack?
) : CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    /**
     * 记录运动中膝盖的最小最高点
     */
    private var lMaxDisHipWithAnkle = 0f
    private var lMinDisHipWithAnkle = 0f
    private var rMaxDisHipWithAnkle = 0f
    private var rMinDisHipWithAnkle = 0f

    /**
     * 记录运动中手肘的最小最高点
     */
    private var maxLElbowY = 0f
    private var maxRElbowY = 0f
    private var miniLElbowY = 0f
    private var miniRElbowY = 0f

    /**
     * 另一条腿的角度要求阈值
     */
    private var otherKneeAngle = 150

    /**
     * 记录参考距离
     */
    private var refDistance = 0f

    /**
     * 举手标记
     */
    private var handLevel = 0

    /**
     * 腿标记
     */
    private var kneeLevel = 0

    /**
     * 左边检测完毕
     */
    private var leftOk = false

    /**
     * 右边运动完毕
     */
    private var rightOk = false
    private var rightState = 0
    private val rightRunnable = Runnable {
        rightOk = false
        resetRight()
    }
    private val leftRunnable = Runnable {
        leftOk = false
        resetLeft()
    }

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lElbow = body.lElbow
        val lWrist = body.lWrist
        val rElbow = body.rElbow
        val rWrist = body.rWrist
        val rShoulder = body.rShoulder
        val lShoulder = body.lShoulder
        val lKnee = body.lKnee
        val rKnee = body.rKnee
        val rHip = body.rHip
        val lHip = body.lHip
        val lAnkle = body.lAnkle
        val rAnkle = body.rAnkle
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
            if (null != singleEuBaseCallBack) {
                singleEuBaseCallBack!!.onSpeaker("请保持正面面向屏屏幕")
            }
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
                    || lHip.getScore() < config!!.reliabilityThreshold)
        ) {
//            sportDetectorCallBack!!.onFrame(state, "骨骼点可信度低" + config.reliabilityThreshold)
            return
        }
        //获取当前左右膝盖与手臂的高度
        val currentLKneeY = body.lKnee.getY()
        val currentLElbowY = body.lElbow.getY()
        val currentRElbowY = body.rElbow.getY()
        val currentRKneeY = body.rKnee.getY()
        val currentLDisHipWithKnee = Math.abs(lKnee.y - lHip.y)
        val currentRDisHipWithKnee = Math.abs(rKnee.y - rHip.y)

        //做一些必要点的判断与存储
        updateValue(
            rShoulder,
            lShoulder,
            currentRDisHipWithKnee,
            currentLDisHipWithKnee,
            currentLKneeY,
            currentLElbowY,
            currentRElbowY,
            currentRKneeY
        )
        val LAKH = EuHelper.calculateAngle(
            lKnee,
            lAnkle,
            lHip
        )
        val RAKH = EuHelper.calculateAngle(
            rKnee,
            rAnkle,
            rHip
        )
        //是否已经完成了左边的记录
        judeHand(lWrist, rWrist, lHip, rHip)
        if (Math.abs(rShoulder.x - lShoulder.x) < Math.abs(rShoulder.y - rHip.y) / 3) {
            logD("请保持正面面向屏屏幕")
//            sportDetectorCallBack!!.onFrame(state, "请保持正面面向屏屏幕")
            return
        }
        judeLeft(currentLDisHipWithKnee, currentLKneeY, currentRKneeY, RAKH)
        //        if (result.getFrameState() == SPORT_STATE_COMPLETE) {
//            return;
//        }
        judeRight(currentRDisHipWithKnee, currentLKneeY, currentRKneeY, LAKH)
    }

    private fun judeHand(
        lWrist: AdlPoseKeyPoint,
        rWrist: AdlPoseKeyPoint,
        lHip: AdlPoseKeyPoint,
        rHip: AdlPoseKeyPoint
    ) {
        if (lWrist.y < lHip.y && rWrist.y < rHip.y) {
            handLevel = GOOD
        }
    }

    /**
     * 判断左边身体的运动
     *
     * @param
     * @return
     */
    private fun judeLeft(
        currentLDisHipWithKnee: Float,
        currentLKneeY: Float,
        currentRKneeY: Float,
        angle: Double,
    ) {
        //if (!leftOk && maxLKneeY != 0) {
        //向上的位移判断
        val upDis = lMaxDisHipWithAnkle - currentLDisHipWithKnee
        logD(upDis.toString() + ">>>>参考值::" + refDistance / 4)
        if (upDis >= refDistance / 5 && Math.abs(currentLKneeY - currentRKneeY) > upDis / 2 && angle > otherKneeAngle) {
            state = 1
            rightState = SPORT_STATE_IDLE
            rMinDisHipWithAnkle = 0f
            rMaxDisHipWithAnkle = rMinDisHipWithAnkle
            lMinDisHipWithAnkle = currentLDisHipWithKnee
            if (upDis >= refDistance / 2) {
                kneeLevel = GOOD
            }
        }
        if (state == SPORT_STATE_IDLE) {
            logD("左边向上的运动不足")
            return
        }

        //向下的位移判断
        if (currentLDisHipWithKnee - lMinDisHipWithAnkle >= refDistance / 5) {
            handler.removeCallbacks(leftRunnable)
            leftOk = true
            rightOk = false

            //result.setPointForAnimation(new Point((int) (lAnkle.getX() * scaleW), (int) (lAnkle.getY() * scaleH)));
            result.singleSportLevel = judgeSportLevel()
            result.setMessage("完成————————————————左边")
            result.sportAchievement = 1
            handler.postDelayed(leftRunnable, config?.maxFinishTime?.toLong() ?: 0)
            logD("完成————————————————————————————左边")
//            sportDetectorCallBack!!.onComplete(result)
            singleEuBaseCallBack?.onComplete(result)
            reset()
        }
    }

    /**
     * 判断右边身体的运动
     *
     * @param
     * @return
     */
    private fun judeRight(
        currentRDisHipWithKnee: Float,
        currentLKneeY: Float,
        currentRKneeY: Float,
        angle: Double,
    ) {
        //  if (!rightOk && maxRKneeY != 0) {
        //向上的位移判断
        val upDis = rMaxDisHipWithAnkle - currentRDisHipWithKnee
        if (upDis >= refDistance / 5 && Math.abs(currentLKneeY - currentRKneeY) > upDis / 3 && angle > otherKneeAngle) {
            state = SPORT_STATE_IDLE
            rightState = 1
            lMaxDisHipWithAnkle = 0f
            lMinDisHipWithAnkle = lMaxDisHipWithAnkle
            rMinDisHipWithAnkle = currentRDisHipWithKnee
            if (upDis >= refDistance / 2) {
                kneeLevel = GOOD
            }
        }
        if (rightState == SPORT_STATE_IDLE) {
            return
        }

        //向下的位移判断
        if (currentRDisHipWithKnee - rMinDisHipWithAnkle >= refDistance / 5) {
            handler.removeCallbacks(rightRunnable)
            handler.postDelayed(rightRunnable, config?.maxFinishTime?.toLong() ?: 0)
            leftOk = false
            rightOk = true
            //result.setPointForAnimation(new Point((int) (rAnkle.getX() * scaleW), (int) (rAnkle.getY() * scaleH)));
            result.singleSportLevel = judgeSportLevel()
            result.sportAchievement = 1
            result.setMessage("完成————————————————右边")
//            sportDetectorCallBack!!.onComplete(result)
            if (null != singleEuBaseCallBack) {
                singleEuBaseCallBack!!.onComplete(result)
            }
            onReset()
        }
    }

    /**
     * 更新某些值
     *
     * @param rShoulder
     * @param lShoulder
     * @param currentLKneeY
     * @param currentLElbow
     * @param currentRElbow
     * @param currentRKneeY
     */
    private fun updateValue(
        rShoulder: AdlPoseKeyPoint,
        lShoulder: AdlPoseKeyPoint,
        currentRDisHipWithKnee: Float,
        currentLDisHipWithKnee: Float,
        currentLKneeY: Float,
        currentLElbow: Float,
        currentRElbow: Float,
        currentRKneeY: Float
    ) {
        //记录肩膀与屁股的距离
        refDistance = Math.abs(rShoulder.x - lShoulder.x)
        //记录膝关节点
        if (lMaxDisHipWithAnkle == 0f || lMaxDisHipWithAnkle < currentLDisHipWithKnee) {
            lMaxDisHipWithAnkle = currentLDisHipWithKnee
        }
        if (lMinDisHipWithAnkle == 0f || lMinDisHipWithAnkle > currentLDisHipWithKnee) {
            lMinDisHipWithAnkle = currentLDisHipWithKnee
        }
        if (rMaxDisHipWithAnkle == 0f || rMaxDisHipWithAnkle < currentRDisHipWithKnee) {
            rMaxDisHipWithAnkle = currentRDisHipWithKnee
        }
        if (rMinDisHipWithAnkle == 0f || rMinDisHipWithAnkle > currentRDisHipWithKnee) {
            rMinDisHipWithAnkle = currentRDisHipWithKnee
        }

        //记肘点
        if (maxLElbowY == 0f || maxLElbowY < currentLElbow) {
            maxLElbowY = currentLElbow
        }
        if (miniLElbowY == 0f || miniLElbowY > currentLElbow) {
            miniLElbowY = currentLElbow
        }
        if (maxRElbowY == 0f || maxRElbowY < currentRElbow) {
            maxRElbowY = currentRElbow
        }
        if (miniRElbowY == 0f || miniRElbowY > currentRElbow) {
            miniRElbowY = currentRElbow
        }
    }

    override fun onReset() {
        super.onReset()
        resetLeft()
        resetRight()
    }

    private fun resetLeft() {
        state = SPORT_STATE_IDLE
        lMaxDisHipWithAnkle = 0f
        lMinDisHipWithAnkle = lMaxDisHipWithAnkle
    }

    private fun resetRight() {
        rightState = SPORT_STATE_IDLE
        rMinDisHipWithAnkle = 0f
        rMaxDisHipWithAnkle = rMinDisHipWithAnkle
    }

    /**
     * 判断本次运动的得分
     *
     * @return 返回本次运动的得分
     */
    @SportConstant.SingleSportLevel
    fun judgeSportLevel(): Int {
        var goodCount = 0
        if (leftOk) {
            if (handLevel == GOOD) {
                goodCount += 1
            }
            if (kneeLevel == GOOD) {
                goodCount += 1
            }
        } else {
            if (handLevel == GOOD) {
                goodCount += 1
            }
            if (kneeLevel == GOOD) {
                goodCount += 1
            }
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

}