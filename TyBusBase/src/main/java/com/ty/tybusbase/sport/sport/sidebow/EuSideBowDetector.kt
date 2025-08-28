package com.ty.tybusbase.sport.sport.sidebow

import android.util.Log
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * 侧弓步
 * @author CP
 */
class EuSideBowDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {
    private var isTime = false
    protected var disposable: Disposable? = null

    private val TAG = "SIDEBOW"
    override fun reset() {
        super.reset()
        orderBody.clear()
        isTime = false
        cancelTimer()
    }

    override fun stop() {
        super.stop()
        reset()
    }

    override fun onSkeleton(skeleton: AdlPoseSkeleton?) {
        if (skeleton == null) return
        val rShoulder: AdlPoseKeyPoint? = skeleton.getRShoulder()
        val lShoulder: AdlPoseKeyPoint? = skeleton.getLShoulder()
        val rHip: AdlPoseKeyPoint? = skeleton.getRHip()
        val lHip: AdlPoseKeyPoint? = skeleton.getLHip()
        val lAnkle: AdlPoseKeyPoint? = skeleton.getLAnkle()
        val rAnkle: AdlPoseKeyPoint? = skeleton.getRAnkle()
        val lKnee: AdlPoseKeyPoint? = skeleton.getLKnee()
        val rKnee: AdlPoseKeyPoint? = skeleton.getRKnee()
        if (!EuHelper.notNull(
                rShoulder,
                lShoulder,
                lAnkle,
                rAnkle,
                rHip,
                lHip,
                lKnee,
                rKnee
            )
        ) {
            return
        }
        val dL: Float = Math.abs(lHip!!.x - lAnkle!!.x)
        val dR: Float = Math.abs(rHip!!.x - rAnkle!!.x)
        val dA: Float = Math.abs(lAnkle.x - rAnkle.x)
        if (dL > dR) {
            toLeft(rHip, lHip, lAnkle, rAnkle, lKnee!!, rKnee!!, dA)
        } else if (dR > dL) {
            toRight(rHip, lHip, lAnkle, rAnkle, lKnee!!, rKnee!!, dA)
        } else {
            reset()
        }
    }

    /**
     * 右腿直，左腿弓步
     * @param rHip
     * @param lHip
     * @param lAnkle
     * @param rAnkle
     * @param lKnee
     * @param rKnee
     * @param dA
     */
    private fun toRight(
        rHip: AdlPoseKeyPoint,
        lHip: AdlPoseKeyPoint,
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint,
        lKnee: AdlPoseKeyPoint,
        rKnee: AdlPoseKeyPoint,
        dA: Float
    ) {
        val aK: Double = EuHelper.calculateAngle(rKnee, rHip, rAnkle)
        if (aK < 160) {
            Log.e(TAG,"右腿未打直$aK")
            reset()
            return
        }
        val aLK: Double = EuHelper.calculateAngle(lKnee, lHip, lAnkle)
        if (aLK > 135) {
            Log.e(TAG,"左腿未玩去$aLK")
            reset()
            return
        }
        val legL: Double = EuHelper.distance(rAnkle, rKnee)
        if (dA < legL * 2) {
            Log.e(TAG,"双腿距离不够" + dA + "-------" + legL * 2)
            reset()
            return
        }
        val disAnkleY: Float = Math.abs(lAnkle.y - rAnkle.y)
        if (disAnkleY > legL / 3) {
            Log.e(TAG,"双腿高度差距太大" + disAnkleY + "-------" + legL / 3)
            reset()
            return
        }
        if (!isTime) {
            isTime = true
            startTimer(1000, object : TimerCallback {
                override fun timeOver() {
                    eventOk()
                }
            })
        }
    }

    /**
     * 左腿直，右腿弓步
     * @param rHip
     * @param lHip
     * @param lAnkle
     * @param rAnkle
     * @param lKnee
     * @param rKnee
     * @param dA
     */
    private fun toLeft(
        rHip: AdlPoseKeyPoint,
        lHip: AdlPoseKeyPoint,
        lAnkle: AdlPoseKeyPoint,
        rAnkle: AdlPoseKeyPoint,
        lKnee: AdlPoseKeyPoint,
        rKnee: AdlPoseKeyPoint,
        dA: Float
    ) {
        val aK: Double = EuHelper.calculateAngle(lKnee, lHip, lAnkle)
        if (aK < 160) {
            Log.e(TAG,"左腿未打直$aK")
            reset()
            return
        }
        val aRK: Double = EuHelper.calculateAngle(rKnee, rHip, rAnkle)
        if (aRK > 135) {
            Log.e(TAG,"右腿弯曲不够$aRK")
            reset()
            return
        }
        val legL: Double = EuHelper.distance(lAnkle, lKnee)
        if (dA < legL * 2) {
            Log.e(TAG,"双腿距离不够" + dA + "-------" + legL * 2)
            reset()
            return
        }
        val disAnkleY: Float = Math.abs(lAnkle.y - rAnkle.y)
        if (disAnkleY > legL / 3) {
            Log.e(TAG,"双腿高度差距太大" + disAnkleY + "-------" + legL / 3)
            reset()
            return
        }
        if (!isTime) {
            isTime = true
            startTimer(1000, object : TimerCallback {
                override fun timeOver() {
                    eventOk()
                }
            })
        }
    }

    private fun eventOk() {
        result.sportAchievement = 1
        if (singleEuBaseCallBack != null) {
            result.singleSportLevel = SportConstant.SPORT_TERRIFIC
            singleEuBaseCallBack?.onComplete(result)
        }
        reset()
    }

    protected fun cancelTimer() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
        }
    }

    protected fun startTimer(millisecond: Long, callback: TimerCallback) {
        cancelTimer()
        disposable =
            Observable.timer(millisecond, TimeUnit.MILLISECONDS).subscribe {
                callback.timeOver()
                cancelTimer()
            }
    }

    protected interface TimerCallback {
        fun timeOver()
    }
}