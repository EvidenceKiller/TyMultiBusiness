package com.ty.tybusbase.sport.sport.fbjump

import android.util.Log
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * 侧面前后跳
 *
 * @author pc
 */
class FBackJumpDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {

    private val TAG = "FBackJump"

    /**
     * 记录运动中脚踝的最左边与最右边,右边最大
     */
    private var leftAnkleX = 0f
    private var rightAnkleX = 0f

    /**
     * 上一帧脚踝的值
     */
    private var upAnkleX = 0f

    /**
     * 记录运动过程中小腿最大距离
     */
    private var maxDistance = 0f

    /**
     * 手部
     */
    private var handLevel: Int = FINE

    /**
     * 腿部
     */
    private var footLevel: Int = FINE
    private var jumpToLeft = false
    private var jumpToRight = false
    private var currentX = 0f
    private val rightRunable = Runnable {
        jumpToRight = false
        reset()
    }
    private val leftRunnable = Runnable {
        jumpToLeft = false
        reset()
    }

    init {
        this.config = detectorConfig
    }

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return

        val rShoulder: AdlPoseKeyPoint = body.getRShoulder()
        val lShoulder: AdlPoseKeyPoint = body.getLShoulder()
        val lKnee: AdlPoseKeyPoint = body.getLKnee()
        val rKnee: AdlPoseKeyPoint = body.getRKnee()
        val rAnkle: AdlPoseKeyPoint = body.getRAnkle()
        val lAnkle: AdlPoseKeyPoint = body.getLAnkle()
        val lHip: AdlPoseKeyPoint = body.getLHip()
        val rHip: AdlPoseKeyPoint = body.getRHip()
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


        //判断身体方向
        val isLeft: Boolean
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
            maxDistance = Math.abs(lHip.y - lShoulder.y)
            isLeft = true
        } else if (!b && rReliability) {
            isLeft = false
            maxDistance = Math.abs(rHip.y - rShoulder.y)
        } else {
            return
        }

        //请侧面站立
        if (Math.abs(rShoulder.getX() - lShoulder.getX()) > maxDistance / 1.4) {
            Log.d(
                TAG,
                "肩距：" + (rShoulder.getX() - lShoulder.getX()) + "应该不是侧面，参考距离是：" + maxDistance
            )
            reset()
            return
        }
        if (isLeft) {
            if (rAnkle.score >= config!!.reliabilityThreshold && Math.abs(lAnkle.x - rAnkle.x) > maxDistance / 6) {
                Log.d(TAG, "脚踝距离：" + (lAnkle.x - rAnkle.x) + "太大，怀疑是走动---左")
                leftAnkleX = 0f
                rightAnkleX = 0f
                reset()
                return
            }
            if (lHip.y >= lWrist.y) {
                handLevel = GOOD
            }
            currentX = lAnkle.x
        } else {
            if (lAnkle.score >= config!!.reliabilityThreshold && Math.abs(lAnkle.x - rAnkle.x) > maxDistance / 6) {
                Log.d(TAG, "脚踝距离：" + (lAnkle.x - rAnkle.x) + "太大，怀疑是走动---右")
                leftAnkleX = 0f
                rightAnkleX = 0f
                reset()
                return
            }
            if (rHip.y >= rWrist.y) {
                handLevel = GOOD
            }
            currentX = rAnkle.x
        }


        //获取当前最右边与最左边
        if (leftAnkleX == 0f || leftAnkleX > currentX) {
            leftAnkleX = currentX
        }
        if (rightAnkleX == 0f || rightAnkleX < currentX) {
            rightAnkleX = currentX
        }

//        //向左
//        if (upAnkleX > currentX) {
//            //如果是首次向右边运动记录下时间
//            if (state == 0) {
//                state = 1;
//                rightState = 0;
//                oneStateTime = System.currentTimeMillis();
//                handler.removeCallbacksAndMessages(null);
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        LogUtils.e("左边单个动作运动超时了《重置");
//                        jumpToLeft = jumpToRight = false;
//                        reset();
//                    }
//                }, config.maxFinishTime);
//            }
//        } else if (upAnkleX < currentX) {
//            if (rightState == 0) {
//                state = 0;
//                rightState = 1;
//                oneStateTime = System.currentTimeMillis();
//                handler.removeCallbacksAndMessages(null);
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        LogUtils.e("右边单个动作运动超时了《重置");
//                        jumpToLeft = jumpToRight = false;
//                        reset();
//                    }
//                }, config.maxFinishTime);
//            }
//        } else {
//            result.setMessage("没有检测到运动呢");
//            return;
//        }
        upAnkleX = currentX

        //判断向右边的位移
        if (!jumpToRight) {
            val displacement = currentX - leftAnkleX
            if (displacement >= maxDistance / 6) {
                handler.removeCallbacks(rightRunable)
                jumpToLeft = false
                jumpToRight = true
                if (displacement >= maxDistance / 4) {
                    footLevel = GOOD
                }
                handler.postDelayed(rightRunable, config!!.maxFinishTime.toLong())
                leftAnkleX = 0f
                result.singleSportLevel = judgeSportLevel()
                result.sportAchievement = 1
                //result.setPointForAnimation(new Point((int) ((rAnkle.getX() + ((rAnkle.getX() - lAnkle.getX()) / 2)) * scaleW), (int) ((rAnkle.getY() + ((rAnkle.getY() - lAnkle.getY()) / 2)) * scaleH)));
                result.setMessage("右侧侧运动完成")
                //                sportDetectorCallBack.onComplete(result);
                singleEuBaseCallBack?.onComplete(result)
                reset()
            }
        }
        if (!jumpToLeft) {
            //向左边的位移判断
            val displacement = rightAnkleX - currentX
            if (displacement >= maxDistance / 6) {
                handler.removeCallbacks(leftRunnable)
                jumpToLeft = true
                jumpToRight = false
                if (displacement >= maxDistance / 3) {
                    footLevel = GOOD
                }
                rightAnkleX = 0f
                result.singleSportLevel = judgeSportLevel()
                result.sportAchievement = 1
                //result.setPointForAnimation(new Point((int) ((rAnkle.getX() + ((rAnkle.getX() - lAnkle.getX()) / 2)) * scaleW), (int) ((rAnkle.getY() + ((rAnkle.getY() - lAnkle.getY()) / 2)) * scaleH)));
                result.setMessage("左侧侧运动完成")
                //                sportDetectorCallBack.onComplete(result);
                singleEuBaseCallBack?.onComplete(result)
                reset()
            }
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
        if (handLevel == SportConstant.GOOD) {
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
        //
//        jumpToLeft = jumpToRight = false;
        rightAnkleX = 0f
        leftAnkleX = 0f
        footLevel = FINE
        handLevel = footLevel
    }
}