package com.ty.tybusbase.helper

import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.constants.SportConstant.SportAiCode

/**
 * @description: 运动分数获取帮助类
 * @author: chenwei
 * @date :   2022/4/21 2:41 下午
 */
object SportHelper {
    //高速
    const val HIGH_SPEED: String = "0"
    const val HIGH_SPEED2: String = "3"

    //中速
    const val MEDIUM_SPEED: String = "1"

    //低速
    const val LOW_SPEED: String = "2"

    /**
     * 获取运动速度类型
     *
     * @param sportAiCode
     * @return
     */
    fun getSpeedType(@SportAiCode sportAiCode: String): String {
        var speedType = sportAiCode
        when (sportAiCode) {
            SportConstant.SPORT_SIT_UP, SportConstant.SPORT_OPEN_CLOSE_JUMP, SportConstant.SPORT_SQUATS, SportConstant.SPORT_PUSH_UP, SportConstant.SPORT_PLANK, SportConstant.SPORT_UTTHANASANA, SportConstant.SPORT_HIGH_KNEE, SportConstant.SPORT_RED_FLAG_GAME, SportConstant.SPORT_WITH_ARM, SportConstant.SPORT_ENLARGE_BOSOM, SportConstant.SPORT_SWING_ONE_HAND, SportConstant.SPORT_ARM_AROUND, SportConstant.SPORT_SLOW_SKIP_ROPE, SportConstant.SPORT_SQUAT_JUMP_HIGH, SportConstant.SPORT_KICH_FORWARD, SportConstant.SPORT_SIDE_BOW -> speedType =
                MEDIUM_SPEED

            SportConstant.SPORT_LEFT_RIGHT_ROPE, SportConstant.SPORT_FRONT_BACK_SKIP_ROPE -> speedType =
                HIGH_SPEED2

            SportConstant.SPORT_ONE_LEG_SKIP_ROPE, SportConstant.SPORT_SKIP_ROPE, SportConstant.SPORT_CROSS_SKIP_ROPE, SportConstant.SPORT_JUMPING_JACK_SKIP_ROPE, SportConstant.SPORT_DOUBLE_SHAKE_JUMP, SportConstant.SPORT_ALTERNATE_SKIP_ROPE, SportConstant.SPORT_SKIP_ROPE_SKU_ID, SportConstant.SPORT_TROT -> speedType =
                HIGH_SPEED

            SportConstant.SPORT_GAME_BEE -> speedType = LOW_SPEED
        }
        return speedType
    }

    /**
     * 获取分数
     *
     * @param comboCount
     * @param speedType
     * @param scoreType
     * @return
     */
    fun getScore(comboCount: Int, speedType: String, scoreType: Int): Int {
        var score = 0

        when (speedType) {
            HIGH_SPEED, HIGH_SPEED2 -> score += if (comboCount >= 11 && comboCount <= 20) {
                scoreType + 1
            } else if (comboCount >= 21 && comboCount <= 30) {
                scoreType + 2
            } else if (comboCount >= 31) {
                scoreType + 3
            } else {
                scoreType
            }

            MEDIUM_SPEED -> score += if (comboCount >= 6 && comboCount <= 10) {
                scoreType + 1
            } else if (comboCount >= 11 && comboCount <= 20) {
                scoreType + 2
            } else if (comboCount >= 21) {
                scoreType + 3
            } else {
                scoreType
            }
        }
        return score
    }
}
