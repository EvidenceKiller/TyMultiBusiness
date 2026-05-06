package com.ty.bus.multi.core.ui.sport.doublesport

import android.app.Application
import com.ty.bus.multi.core.config.DoubleSportConfig
import com.ty.tybusbase.base.BaseDoubleSportViewModel
import com.ty.tybusbase.constants.SportConstant.SPORT_COME_ON
import com.ty.tybusbase.constants.SportConstant.SPORT_PERFECT
import com.ty.tybusbase.constants.SportConstant.SPORT_TERRIFIC
import com.ty.tybusbase.helper.SportHelper

class DoubleSportViewModel(application: Application) : BaseDoubleSportViewModel(application) {

    var leftValue = 0
    var rightValue = 0

    private var upCountLeftTime: Long = 0

    private var leftComboCount = 0

    private var rightComboCount = 0

    private val comboLimitTime = (2 * 1000).toLong()

    private var upCountRightTime: Long = 0

    var doubleSportConfig: DoubleSportConfig? = null

    fun setSportDuration(timeMillis: Int) {
        leftSportResult.sportDuration = if (timeMillis == 0) 0 else timeMillis
        rightSportResult.sportDuration = if (timeMillis == 0) 0 else timeMillis
    }

    fun refreshLeftValue(sportAchievement: Int, singleSportLevel: Int) {
        leftValue += sportAchievement
        val now = System.currentTimeMillis()
        if (now - upCountLeftTime < comboLimitTime) leftComboCount += 1 else {
            leftSportResult.maxComboCount =
                leftSportResult.maxComboCount.coerceAtLeast(leftComboCount)
            leftComboCount = 0
        }
        upCountLeftTime = now
        leftSportResult.sportAchievement = leftValue
        leftSportResult.sportResult = leftValue.toString()
        var score = 0
        when (singleSportLevel) {
            SPORT_PERFECT -> {
                leftSportResult.perfectCount = leftSportResult.perfectCount + 1
                score = SportHelper.getScore(
                    leftComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_PERFECT
                )
                leftSportResult.perfectScore += score
            }

            SPORT_TERRIFIC -> {
                leftSportResult.goodCount = leftSportResult.goodCount + 1
                score = SportHelper.getScore(
                    leftComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_TERRIFIC
                )
                leftSportResult.goodScore += score
            }

            SPORT_COME_ON -> {
                leftSportResult.comeOnCount = leftSportResult.comeOnCount + 1
                score = SportHelper.getScore(
                    leftComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_COME_ON
                )
                leftSportResult.comeOnScore += score
            }
        }
        leftSportResult.totalScore += score
    }


    fun refreshRightValue(sportAchievement: Int, singleSportLevel: Int) {
        val now = System.currentTimeMillis()
        if (now - upCountRightTime < comboLimitTime) rightComboCount += 1 else {
            rightSportResult.maxComboCount =
                rightSportResult.maxComboCount.coerceAtLeast(rightComboCount)
            rightComboCount = 0
        }
        upCountRightTime = now
        rightValue += sportAchievement
        rightSportResult.sportAchievement = rightValue
        rightSportResult.sportResult = rightValue.toString()
        var score = 0
        when (singleSportLevel) {
            SPORT_PERFECT -> {
                rightSportResult.perfectCount = rightSportResult.perfectCount + 1
                score = SportHelper.getScore(
                    rightComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_PERFECT
                )
                rightSportResult.perfectScore += score
            }

            SPORT_TERRIFIC -> {
                rightSportResult.goodCount = rightSportResult.goodCount + 1
                score = SportHelper.getScore(
                    rightComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_TERRIFIC
                )
                rightSportResult.goodScore += score
            }

            SPORT_COME_ON -> {
                rightSportResult.comeOnCount = rightSportResult.comeOnCount + 1
                score = SportHelper.getScore(
                    rightComboCount,
                    SportHelper.getSpeedType(getSportCode()),
                    SPORT_COME_ON
                )
                rightSportResult.comeOnScore += score
            }
        }
        rightSportResult.totalScore += score
    }

    fun setLeftComboCount(comboCount: Int) {
        leftSportResult.maxComboCount =
            leftSportResult.maxComboCount.coerceAtLeast(comboCount)
    }

    fun setRightComboCount(comboCount: Int) {
        leftSportResult.maxComboCount =
            leftSportResult.maxComboCount.coerceAtLeast(comboCount)
    }

    fun getSportCode() = sportInfo?.sportItemCode ?: ""
}