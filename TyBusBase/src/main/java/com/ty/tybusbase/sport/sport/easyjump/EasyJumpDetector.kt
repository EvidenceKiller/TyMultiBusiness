package com.ty.tybusbase.sport.sport.easyjump

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @author :WEI_CP 跳摆，跳绳
 * Time:2022/4/28 15:27
 * Description: this is skip01
 */
class EasyJumpDetector(
    singleEuBaseCallBack: SingleEuBaseCallBack?,
    detectorConfig: DetectorConfig
) :
    CommonEuSingleBase(singleEuBaseCallBack, detectorConfig) {
    private var maxAnkleY = 0f
    private var minAnkleY = 0f
    private var refDistance = 0f
    private var handLevel = 0
    private var footLevel = 0
    private var maxKneeAndAnkleY = 0f
    private var minKneeAndAnkleY = 0f
    private var minShoulderAndHipY = 0f
    private var maxShoulderAndHipY = 0f
    private var ankleDisY = 0f
    private var ankleDisX = 0f
    private var intervalTime = 0.0
    private var thEasyDownMove = 0f
    private var thEasyUpMove = 0f
    private var thHardUpMove = 0f
    private var thHardDownMove = 0f


    init {
        this.config = detectorConfig
    }

    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val key: String = body.getKey()
        val lAnkle = body.getLAnkle()
        val rAnkle = body.getRAnkle()
        val lKnee = body.getLKnee()
        val rKnee = body.getRKnee()
        val lShoulder = body.getLShoulder()
        val rShoulder = body.getRShoulder()
        val rHip = body.getRHip()
        val lHip = body.getLHip()
        val lElbow = body.getLElbow()
        val rElbow = body.getRElbow()
        val lWrist = body.getLWrist()
        val rWrist = body.getRWrist()
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
                rAnkle,
                lElbow,
                rElbow
            )
        ) {
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }

        ////////////////// 校验身体站立 add by wei.zhou /////////////////
        // 身高
        val bh: Double = EuHelper.distance(lShoulder, lAnkle)
        if (bh < 1080 * 0.15) {
            d("身高不满足条件")
            return
        }

        // 上半身
        val v1: Double = EuHelper.calculateAngle(lHip, lShoulder, lKnee)
        val v2: Double = EuHelper.calculateAngle(rHip, rShoulder, rKnee)
        if (!(EuHelper.inRange(v1, 140.0, 180.0) && EuHelper.inRange(v2, 140.0, 180.0))) {
            d("上半身不满足条件: v1=$v1, v2=$v2")
            return
        }

        // 下半身
        val v3: Double = EuHelper.calculateAngle(lKnee, lHip, lAnkle)
        val v4: Double = EuHelper.calculateAngle(rKnee, rHip, rAnkle)
        if (!(EuHelper.inRange(v3, 100.0, 180.0) && EuHelper.inRange(v4, 100.0, 180.0))) {
            d("下半身不满足条件: v3=$v3, v4=$v4")
            return
        }

        // 手臂角度
        /*double v5 = EuHelper.calculateAngle(lElbow, lShoulder, lWrist);
        double v6 = EuHelper.calculateAngle(rElbow, rShoulder, rWrist);
        if (!(EuHelper.inRange(v5, 40, 160) && EuHelper.inRange(v6, 40, 160))) {
            d("手臂角度不满: v5=" + v5 + ", v6=" + v6);
            return;
        }*/
        val dy = (bh * 0.3f).toFloat()
        if (lWrist.y - dy > lHip.y || rWrist.y - dy > rHip.y) {
            d("手臂应该高于胯部")
            return
        }
        ////////////////////// end //////////////////////////

        //获取当前的位置
        val lAnkleY: Float = lAnkle.y
        val rAnkleY: Float = rAnkle.y
        val lShoulderY: Float = lShoulder.y
        val rShoulderY: Float = rShoulder.y
        val ankleY = (lAnkleY + rAnkleY) / 2
        val shoulderAndHipY: Float = (lShoulderY + rShoulderY + lHip.y + rHip.y) / 4
        val kennAndAnkle: Float = (lAnkleY + rAnkleY + lKnee.y + rKnee.y) / 4

        //检查关键点的可信程度
        if (!reliability(lAnkle, rAnkle, lShoulder, rShoulder)) {
//            logD("异常骨骼点可行度不够");
//            return;
        }

        //更新相关值
        if (updateFrame(
                lAnkle,
                rAnkle,
                lShoulder,
                rShoulder,
                ankleY,
                shoulderAndHipY,
                kennAndAnkle
            )
        ) return

        //判断是不是走动
        if (judeFootDis(lAnkle, rAnkle)) {
            // logD("异常走動");
            return
        }

        //判断是否是正面面向屏幕
        if (Math.abs(lShoulder.x - rShoulder.x) < Math.abs(lHip.y - lKnee.y) / 4) {
            reset()
            //  logD("异常请正面站立");
//            sportDetectorCallBack.onError(error.setMsgError("异常请正面站立"))
            return
        }

        //手部结果判定
        if (lWrist.y > lHip.y && rWrist.y > rHip.y) {
            handLevel = GOOD
        }

//        //根据具体情况判断
//        if (upCountTime == 0 || System.currentTimeMillis() - upCountTime > 1000) {
//            //严格判断逻辑
//            //logD("  严格判断》" );
//            //hardJudge(lAnkle, rAnkle, shoulderAndHipY, kennAndAnkle, ankleY,false);
//            easyJudge(lAnkle, rAnkle, shoulderAndHipY, kennAndAnkle, ankleY,true);
//        } else {
//            //简单计数判定逻辑
//            logD("  简单判断");
//            easyJudge(lAnkle, rAnkle, shoulderAndHipY, kennAndAnkle, ankleY,true);
//            //hardJudge(lAnkle, rAnkle, shoulderAndHipY, kennAndAnkle, ankleY,false);
//        }
        easyJudge(shoulderAndHipY, kennAndAnkle, ankleY)
    }

    private fun updateFrame(
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint,
        lShoulder: AdlPoseKeyPoint,
        rShoulder: AdlPoseKeyPoint,
        ankleY: Float,
        shoulderAndHipY: Float,
        kennAndAnkle: Float
    ): Boolean {

        //记录当前的参考距离
        refDistance = Math.abs(lShoulder.x - rShoulder.x) / 2
        //  logD("阈值：" + refDistance);

        //向上的位移
        thHardUpMove = refDistance * 0.12f
        thHardDownMove = refDistance * 0.13f

        //简单计次判断时候的阈值
        thEasyUpMove = refDistance * 0.13f
        thEasyDownMove = refDistance * 0.13f
        if (minShoulderAndHipY == 0f || maxShoulderAndHipY == 0f || minAnkleY == 0f || maxAnkleY == 0f || minKneeAndAnkleY == 0f || maxKneeAndAnkleY == 0f || ankleDisX == 0f || ankleDisY == 0f) {
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            minAnkleY = ankleY
            maxAnkleY = minAnkleY
            minKneeAndAnkleY = kennAndAnkle
            maxKneeAndAnkleY = minKneeAndAnkleY
            maxShoulderAndHipY = shoulderAndHipY
            minShoulderAndHipY = maxShoulderAndHipY
            // logD("  初始帧");
            return true
        }
        if (maxShoulderAndHipY < shoulderAndHipY) {
            maxShoulderAndHipY = shoulderAndHipY
        }
        if (minShoulderAndHipY > shoulderAndHipY) {
            minShoulderAndHipY = shoulderAndHipY
        }
        if (maxAnkleY < ankleY) {
            maxAnkleY = ankleY
        }
        if (minAnkleY > ankleY) {
            minAnkleY = ankleY
        }
        if (maxKneeAndAnkleY < kennAndAnkle) {
            maxKneeAndAnkleY = kennAndAnkle
        }
        if (minKneeAndAnkleY > kennAndAnkle) {
            minKneeAndAnkleY = kennAndAnkle
        }
        return false
    }

    private fun easyJudge(
        shoulderAndHipY: Float,
        kneeAndAnkleY: Float,
        ankleY: Float,
    ) {

        //高帧率情况下通过多次 低帧率情况下通过少次多量的方式
        val tempUpDis = thEasyUpMove
        val tempDownDis = thEasyDownMove

        //向上这里只用初始帧来判定
        if (maxShoulderAndHipY - shoulderAndHipY >= tempUpDis || maxKneeAndAnkleY - kneeAndAnkleY >= tempUpDis || maxAnkleY - ankleY >= tempUpDis) {
            if (state === SPORT_STATE_IDLE) {
//                if(maxAnkleY<=ankleY){
//                    logD("异常简单位移瞒住，脚踝方向向上不满住了");
//                    return;
//                }
                minShoulderAndHipY = shoulderAndHipY
                minAnkleY = ankleY
                minKneeAndAnkleY = kneeAndAnkleY
                state = 1
                //logD("  位移模式向上满足了");
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
            //logD("异常超过了单个最大时间");
            reset()
            return
        }
        if (shoulderAndHipY - minShoulderAndHipY >= tempDownDis || kneeAndAnkleY - minKneeAndAnkleY >= tempDownDis || ankleY - minAnkleY >= tempDownDis) {
            /* if(ankleY<=minAnkleY){
                logD("简单异常位移瞒住，脚踝方向向下不满住了");
                return;
            }*/
            if (now - upCountTime < config!!.minIntervalTime) {
//                sportDetectorCallBack.onError(error.setMsgError("两次计数时间" + (now - upCountTime) + "小于最小间隔时间" + config.minIntervalTime))
                reset()
                return
            }
            if (now - oneStateTime < config!!.minFinishTime) {
                if ((now - oneStateTime).toInt() == 0) {
                    // logD("异常完成时间是0");
                    return
                }
//                sportDetectorCallBack.onError(error.setMsgError("  单个运动完成时间" + (now - oneStateTime) + "小于最小完成时间" + config.minFinishTime))
                reset()
                return
            }
            eventOk(now)
        }
    }

    private fun eventOk(now: Long) {
        result.sportAchievement = 1
        logD("  >>>>>完成>>>>>" + (now - oneStateTime))
        if (intervalTime < 300 && intervalTime != 0.0 && Math.abs(intervalTime * 2 - (now - upCountTime)) < 70) {
            result.sportAchievement = 2
            // logD("补齐一个，上次间隔时间" + intervalTime + "本次间隔时间：" + (now - upCountTime));
        }
        intervalTime = (now - upCountTime).toDouble()
        // logD("  间隔时间：" + intervalTime);
        upCountTime = now
        //        sportDetectorCallBack.onComplete(result);
        singleEuBaseCallBack?.onComplete(result)
        reset()
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

    private fun judeFootDis(lAnkle: AdlPoseKeyPoint, rAnkle: AdlPoseKeyPoint): Boolean {
        if (Math.abs(ankleDisX - Math.abs(lAnkle.x - rAnkle.x)) > refDistance / 3) {
            logD("脚踝水平距离发生改变太大——怀疑是走动")
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            return true
        }
        if (Math.abs(ankleDisY - Math.abs(lAnkle.y - rAnkle.y)) > refDistance / 3) {
            logD("脚踝垂直距离发生改变太大____怀疑是走动")
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x)
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y)
            return true
        }
        return false
    }


    override fun reset() {
        super.reset()
        logD("重置处理器")
        footLevel = 0
        handLevel = footLevel
        maxShoulderAndHipY = 0f
        minShoulderAndHipY = maxShoulderAndHipY
        minKneeAndAnkleY = 0f
        maxKneeAndAnkleY = minKneeAndAnkleY
        minAnkleY = maxKneeAndAnkleY
        maxAnkleY = minAnkleY
        ankleDisY = 0f
        ankleDisX = ankleDisY
    }
}