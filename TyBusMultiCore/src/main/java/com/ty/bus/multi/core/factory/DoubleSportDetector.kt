package com.ty.bus.multi.core.factory

import com.adl.mount.sport.TsSportChannel
import com.adl.sport.general.EuSingleBase
import com.adl.sport.general.ropeskip.EuRopeSkip
import com.adl.sport.general.ropeskip.EuRopeSkipCallback
import com.adl.sport.general.ropeskip.EuRopeSkipResult
import com.ty.bus.multi.core.callback.DoubleBaseCallback
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuDoubleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import com.ty.tybusbase.sport.sport.highknee.HighKneeDetector2
import com.ty.tybusbase.sport.sport.openclose.OpenCloseDetector
import com.ty.tybusbase.sport.sport.squat.SquatDetector

class DoubleSportDetector(
    private val code: String,
    val callback: DoubleBaseCallback,
) : CommonEuDoubleBase(callback) {

    private lateinit var sportLeftDetector: EuSingleBase
    private lateinit var sportRightDetector: EuSingleBase
    private val leftResult by lazy { SportDetectResult() }
    private val rightResult by lazy { SportDetectResult() }
    private fun initDetector() {
        val builder: DetectorConfig.Builder =
            DetectorConfig.Builder()
        when (code) {
            SportConstant.SPORT_SKIP_ROPE -> {
                sportLeftDetector = EuRopeSkip(object : EuRopeSkipCallback() {
                    override fun onRopeSkipComplete(result: EuRopeSkipResult) {
                        leftResult.sportAchievement = result.sportAchievement
                        leftResult.singleSportLevel = result.singleSportLevel
                        leftResult.sportPrescription = result.sportPrescription
                        callback.onLeftCompleteOnce(leftResult)
                    }
                })
                sportRightDetector = EuRopeSkip(object : EuRopeSkipCallback() {
                    override fun onRopeSkipComplete(result: EuRopeSkipResult) {
                        rightResult.sportAchievement = result.sportAchievement
                        rightResult.singleSportLevel = result.singleSportLevel
                        rightResult.sportPrescription = result.sportPrescription
                        callback.onRightCompleteOnce(rightResult)
                    }
                })
            }

            SportConstant.SPORT_OPEN_CLOSE_JUMP -> {
                sportLeftDetector = OpenCloseDetector(object : SingleEuBaseCallBack() {
                    override fun onComplete(result: SportDetectResult) {
                        callback.onLeftCompleteOnce(result)
                    }
                })
                sportRightDetector = OpenCloseDetector(object : SingleEuBaseCallBack() {
                    override fun onComplete(result: SportDetectResult) {
                        callback.onRightCompleteOnce(result)
                    }
                })
            }

            SportConstant.SPORT_HIGH_KNEE -> {
                sportLeftDetector =
                    HighKneeDetector2(builder.setIntervalTime(200).setLimitMaxTime(1000)
                        .setLimitMinTime(50).build(),
                        object : SingleEuBaseCallBack() {
                            override fun onComplete(result: SportDetectResult) {
                                callback.onLeftCompleteOnce(result)
                            }
                        })

                sportRightDetector = HighKneeDetector2(
                    builder.setIntervalTime(200).setLimitMaxTime(1000).setLimitMinTime(50).build(),
                    object : SingleEuBaseCallBack() {
                        override fun onComplete(result: SportDetectResult) {
                            callback.onRightCompleteOnce(result)
                        }
                    })
            }

            SportConstant.SPORT_SQUATS -> {
                sportLeftDetector = SquatDetector(object : SingleEuBaseCallBack() {
                    override fun onComplete(result: SportDetectResult) {
                        callback.onLeftCompleteOnce(result)
                    }
                }, builder.setIntervalTime(500).setLimitMaxTime(2000).setLimitMinTime(50).build())

                sportRightDetector = SquatDetector(object : SingleEuBaseCallBack() {
                    override fun onComplete(result: SportDetectResult) {
                        callback.onRightCompleteOnce(result)
                    }
                }, builder.setIntervalTime(500).setLimitMaxTime(2000).setLimitMinTime(50).build())
            }
        }
    }

    init {
        initDetector()
    }

    override fun onChannel(left: TsSportChannel?, right: TsSportChannel?) {
        callback.onChannel(left, right)
        left?.skeleton?.let { sportLeftDetector.onSkeleton(it) }
        right?.skeleton?.let { sportRightDetector.onSkeleton(it) }
    }

}