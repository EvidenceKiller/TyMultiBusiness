package com.ty.tybusbase.sport.sport.squathighjump

import android.annotation.SuppressLint
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @author zhouwenhua
 * @date 2023/8/3
 * @describe 纵跳摸高
 */
class SquatHighJumpDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var maxHipY = 0f
    private var miniHipY = 0f
    private var maxAnkleY = 0f
    private var miniAnkleY = 0f

    //单次参考距离
    private var refDistance = 0f

    //下蹲质量等级-
    private var squatLevel: Int = FINE

    //起立质量等级
    private val upLevel: Int = GOOD

    //位移限制
    private var moveLimit = 0f
    private var ankleDisX = 0.0
    private var ankleDisY = 0.0


    override fun onSkeleton(body: AdlPoseSkeleton?) {
        if (body == null) return
        singleEuBaseCallBack?.onSkeleton(body)
        val lElbow = body.getLElbow()
        val rElbow = body.getRElbow()
        val rWrist = body.getRWrist()
        val lWrist = body.getLWrist()
        val rShoulder = body.getRShoulder()
        val lShoulder = body.getLShoulder()
        val lKnee = body.getLKnee()
        val rKnee = body.getRKnee()
        val lAnkle = body.getLAnkle()
        val rAnkle = body.getRAnkle()
        val rHip = body.getRHip()
        val lHip = body.getLHip()
        if (!EuHelper.notNull(
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
        ) {
            singleEuBaseCallBack?.onSpeaker("请保持正面面向屏屏幕")
            return
        }

        ////////////////// 校验身体站立 add by wei.zhou /////////////////
        // 身高
        val bh: Double = EuHelper.distance(lShoulder, lAnkle)
        if (bh < 1080 * 0.15) {
            d("身高不满足条件")
            reset()
            return
        }

        ////////////////////// end //////////////////////////
        //增加一个靠近屏幕的条件限制？身高小于2倍肩宽
        if (bh <= Math.abs(lShoulder.getX()) - rShoulder.getX()) {
            d("骨骼异常: 身高小于2倍肩宽")
            return
        }
        refDistance = Math.abs(rShoulder.x - lShoulder.x)
        moveLimit = refDistance / 3
        val currentHipY: Float = (lHip.y + rHip.y) / 2
        val currentAnkleY: Float = (lAnkle.y + rAnkle.y) / 2
        if (ankleDisX == 0.0 || ankleDisY == 0.0) {
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x).toDouble()
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y).toDouble()
        }
        updatePoint(currentHipY, currentAnkleY)
        if (currentHipY - miniHipY >= moveLimit) {
            //识别到向下的位移
            if (currentHipY - miniHipY >= moveLimit * 2) {
                squatLevel = GOOD
            }
            if (state === SPORT_STATE_IDLE) {
                state = 1
                maxHipY = currentHipY
                return
            }
        }
        if (state === 1 && maxHipY - currentHipY >= moveLimit) {
            miniAnkleY = currentAnkleY
            maxAnkleY = miniAnkleY
            state = 2
        }
        val sAL: Double = EuHelper.calculateAngle(lShoulder, lElbow, lHip)
        val sAr: Double = EuHelper.calculateAngle(rShoulder, rElbow, rHip)
        val eAl: Double = EuHelper.calculateAngle(lElbow, lWrist, lShoulder)
        val eAr: Double = EuHelper.calculateAngle(rElbow, rWrist, rShoulder)
        val b = sAL > 150 && eAl > 150 || sAr > 150 && eAr > 150
        if (state === 2 && maxAnkleY - currentAnkleY > moveLimit / 2 && b) {
            state = 3
        }
        if (state === 3 && currentAnkleY - miniAnkleY > moveLimit / 2) {
            result.singleSportLevel = judgeSportLevel()
            result.sportAchievement = 1
            singleEuBaseCallBack?.onComplete(result)
            d(OkLine)
            reset()
        }
    }

    private fun updatePoint(currentHipY: Float, currentAnkleY: Float) {
        if (maxHipY < currentHipY) maxHipY = currentHipY
        if (miniHipY == 0f || miniHipY > currentHipY) miniHipY = currentHipY
        if (maxAnkleY < currentAnkleY) maxAnkleY = currentAnkleY
        if (miniAnkleY == 0f || miniAnkleY > currentAnkleY) miniAnkleY = currentAnkleY
    }

    private fun judeFootDis(lAnkle: AdlPoseKeyPoint, rAnkle: AdlPoseKeyPoint): Boolean {
        if (Math.abs(ankleDisX - Math.abs(lAnkle.x - rAnkle.x)) > refDistance / 3) {
            callInfo("脚踝水平距离发生改变太大——怀疑是走动")
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x).toDouble()
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y).toDouble()
            return true
        }
        if (Math.abs(ankleDisY - Math.abs(lAnkle.y - rAnkle.y)) > refDistance / 3) {
            callInfo("脚踝垂直距离发生改变太大____怀疑是走动")
            reset()
            ankleDisX = Math.abs(lAnkle.x - rAnkle.x).toDouble()
            ankleDisY = Math.abs(lAnkle.y - rAnkle.y).toDouble()
            return true
        }
        return false
    }

    @SuppressLint("WrongConstant")
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
        state = SPORT_STATE_IDLE
        miniAnkleY = 0f
        maxAnkleY = miniAnkleY
        maxHipY = maxAnkleY
        miniHipY = maxHipY
        squatLevel = FINE
        ankleDisY = 0.0
        ankleDisX = ankleDisY
    }
}