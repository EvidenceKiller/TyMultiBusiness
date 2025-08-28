package com.ty.tybusbase.sport.sport.lrjump

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * 开合跳判定器
 *
 * @author pc
 */
class LRightJumpDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    /**
     * 记录运动中脚踝的最左边与最右边,右边最大
     */
    private var leftMinAnkleX = 0f
    private var rightMinAnkleX = 0f

    /**
     * 记录运动过程中小腿最大距离
     */
    private var maxDistance = 0f

    /**
     * 手部
     */
    private var handLevel = 0

    /**
     * 腿部
     */
    private var footLevel = 0
    private var jumpToleft = false
    private var jumpToRight = false
    private var ankleDisY = 0f
    private var ankleDisX = 0f
    private var leftMaxAnkleX = 0f
    private var rightMaxAnkleX = 0f


    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        val lElbow: AdlPoseKeyPoint = body.getLElbow()
        val rElbow: AdlPoseKeyPoint = body.getRElbow()
        val rShoulder: AdlPoseKeyPoint = body.getRShoulder()
        val lShoulder: AdlPoseKeyPoint = body.getLShoulder()
        val lKnee: AdlPoseKeyPoint = body.getLKnee()
        val rKnee: AdlPoseKeyPoint = body.getRKnee()
        val rWrist: AdlPoseKeyPoint = body.getRWrist()
        val lWrist: AdlPoseKeyPoint = body.getLWrist()
        val rAnkle: AdlPoseKeyPoint = body.getRAnkle()
        val lAnkle: AdlPoseKeyPoint = body.getLAnkle()
        val rHip: AdlPoseKeyPoint = body.getRHip()
        val lHip: AdlPoseKeyPoint = body.getLHip()
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
        if (lElbow.getScore() < config!!.reliabilityThreshold
            || rElbow.getScore() < config!!.reliabilityThreshold
            || rShoulder.getScore() < config!!.reliabilityThreshold
            || lShoulder.getScore() < config!!.reliabilityThreshold
            || rAnkle.getScore() < config!!.reliabilityThreshold
            || lAnkle.getScore() < config!!.reliabilityThreshold
            || lKnee.getScore() < config!!.reliabilityThreshold
            || rKnee.getScore() < config!!.reliabilityThreshold
            || rWrist.getScore() < config!!.reliabilityThreshold
            || lWrist.getScore() < config!!.reliabilityThreshold
        ) {
            return
        }

        //参考距离
        maxDistance = Math.abs((lHip.y + rHip.y) / 2 - (lShoulder.y + rShoulder.y) / 2)
        if (Math.abs(rShoulder.getX() - lShoulder.getX()) < maxDistance / 2) {
            // result.setMessage("肩距：" + (rShoulder.getX() - lShoulder.getX()) + "应该不是正面面向屏幕，参考距离是：" + maxDistance);
        }
        maxDistance = Math.abs(rShoulder.x - lShoulder.x)

        //获取当前最右边与最左边
        val currentLeftX: Float = lAnkle.x
        val currentRightX: Float = rAnkle.x
        if (leftMaxAnkleX == 0f || leftMinAnkleX == 0f || rightMinAnkleX == 0f || rightMaxAnkleX == 0f) {
            leftMaxAnkleX = currentLeftX
            leftMinAnkleX = leftMaxAnkleX
            rightMinAnkleX = currentRightX
            rightMaxAnkleX = rightMinAnkleX
            return
        }
        if (leftMinAnkleX > currentLeftX) {
            leftMinAnkleX = currentLeftX
        }
        if (leftMaxAnkleX == 0f || leftMaxAnkleX < currentLeftX) {
            leftMaxAnkleX = currentLeftX
        }
        if (rightMinAnkleX == 0f || rightMinAnkleX > currentRightX) {
            rightMinAnkleX = currentRightX
        }
        if (rightMaxAnkleX == 0f || rightMaxAnkleX < currentRightX) {
            rightMaxAnkleX = currentRightX
        }
        if (judeFootDis(lAnkle, rAnkle)) {
            return
        }

        //判定手部动作是否符合要求
        if (rWrist.y < rHip.y && lWrist.y < lHip.y) {
            handLevel = GOOD
        }


        //判断有没有完成向右边跳动
        if (!jumpToRight) {
            val displacement = currentLeftX - leftMinAnkleX
            if (displacement >= maxDistance / 4 && currentRightX - rightMinAnkleX >= maxDistance / 4) {
                jumpToleft = false
                jumpToRight = true
                if (displacement <= maxDistance / 2) {
                    footLevel = GOOD
                }
                result.singleSportLevel = judgeSportLevel()
                result.sportAchievement = 1
                // result.setPointForAnimation(new Point((int)((rAnkle.getX()+((rAnkle.getX()-lAnkle.getX())/2))*scaleW),(int)((rAnkle.getY()+((rAnkle.getY()-lAnkle.getY())/2))*scaleH)));
                result.setMessage("右侧运动完成")
                //                sportDetectorCallBack.onComplete(result);
                singleEuBaseCallBack?.onComplete(result)
                resetLeft()
                return
            }
        }
        if (!jumpToleft) {
            //向左边的位移判断
            val displacement = rightMaxAnkleX - currentRightX
            if (displacement >= maxDistance / 4 && leftMaxAnkleX - currentLeftX > maxDistance / 4) {
                if (displacement <= maxDistance / 2) {
                    footLevel = GOOD
                }
                jumpToleft = true
                jumpToRight = false
                result.singleSportLevel = judgeSportLevel()
                result.sportAchievement = 1
                // result.setPointForAnimation(new Point((int)((rAnkle.getX()+((rAnkle.getX()-lAnkle.getX())/2))*scaleW),(int)((rAnkle.getY()+((rAnkle.getY()-lAnkle.getY())/2))*scaleH)));
                result.setMessage("左侧运动完成")
                //                sportDetectorCallBack.onComplete(result);
                singleEuBaseCallBack?.onComplete(result)
                resetRight()
            }
        }
    }

    override fun reset() {
        super.reset()
        resetLeft()
        resetRight()
    }

    fun resetLeft() {
        rightMaxAnkleX = 0f
        leftMaxAnkleX = rightMaxAnkleX
        rightMinAnkleX = leftMaxAnkleX
        leftMinAnkleX = rightMinAnkleX
        jumpToleft = false
    }

    fun resetRight() {
        rightMaxAnkleX = 0f
        leftMaxAnkleX = rightMaxAnkleX
        rightMinAnkleX = leftMaxAnkleX
        leftMinAnkleX = rightMinAnkleX
        jumpToRight = false
    }

    private fun judeFootDis(lAnkle: AdlPoseKeyPoint, rAnkle: AdlPoseKeyPoint): Boolean {
        if (ankleDisY == 0f || ankleDisX == 0f) {
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            return true
        }
        if (Math.abs(ankleDisX - Math.abs(lAnkle.x - rAnkle.x)) > maxDistance / 4) {
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            return true
        }
        if (Math.abs(ankleDisY - Math.abs(lAnkle.y - rAnkle.y)) > maxDistance / 4) {
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            return true
        }
        return false
    }

    /**
     * 判断本次运动的得分
     *
     * @return 返回本次运动的得分
     */
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
            SPORT_PERFECT
        } else if (goodCount == 1) {
            SPORT_TERRIFIC
        } else {
            SPORT_COME_ON
        }
    }
}