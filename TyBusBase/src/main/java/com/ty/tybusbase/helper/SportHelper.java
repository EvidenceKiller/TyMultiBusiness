package com.ty.tybusbase.helper;

import androidx.annotation.NonNull;

import com.ty.tybusbase.constants.SportConstant;

/**
 * @description: 运动分数获取帮助类
 * @author: chenwei
 * @date :   2022/4/21 2:41 下午
 */
public class SportHelper {
    //高速
    public static final String HIGH_SPEED = "0";
    public static final String HIGH_SPEED2 = "3";
    //中速
    public static final String MEDIUM_SPEED = "1";
    //低速
    public static final String LOW_SPEED = "2";

    /**
     * 获取运动速度类型
     *
     * @param sportAiCode
     * @return
     */
    public static String getSpeedType(@SportConstant.SportAiCode @NonNull String sportAiCode) {
        String speedType = sportAiCode;
        switch (sportAiCode) {
            case SportConstant.SPORT_SIT_UP:
            case SportConstant.SPORT_OPEN_CLOSE_JUMP:
            case SportConstant.SPORT_SQUATS:
            case SportConstant.SPORT_PUSH_UP:
            case SportConstant.SPORT_PLANK:
            case SportConstant.SPORT_UTTHANASANA:
            case SportConstant.SPORT_HIGH_KNEE:
            case SportConstant.SPORT_RED_FLAG_GAME:
                //振臂运动
            case SportConstant.SPORT_WITH_ARM:
                //扩胸运动
            case SportConstant.SPORT_ENLARGE_BOSOM:
                //左右手单手摇绳
            case SportConstant.SPORT_SWING_ONE_HAND:
            case SportConstant.SPORT_ARM_AROUND:
            case SportConstant.SPORT_SLOW_SKIP_ROPE:
            case SportConstant.SPORT_SQUAT_JUMP_HIGH:
            case SportConstant.SPORT_KICH_FORWARD:
            case SportConstant.SPORT_SIDE_BOW:
                speedType = MEDIUM_SPEED;
                break;

            case SportConstant.SPORT_LEFT_RIGHT_ROPE:
            case SportConstant.SPORT_FRONT_BACK_SKIP_ROPE:
                speedType = HIGH_SPEED2;
                break;
            case SportConstant.SPORT_ONE_LEG_SKIP_ROPE:
            case SportConstant.SPORT_SKIP_ROPE:
            case SportConstant.SPORT_CROSS_SKIP_ROPE:
            case SportConstant.SPORT_JUMPING_JACK_SKIP_ROPE:
            case SportConstant.SPORT_DOUBLE_SHAKE_JUMP:
            case SportConstant.SPORT_ALTERNATE_SKIP_ROPE:
            case SportConstant.SPORT_SKIP_ROPE_SKU_ID:
            case SportConstant.SPORT_TROT:
                speedType = HIGH_SPEED;
                break;
            case SportConstant.SPORT_GAME_BEE:
                speedType = LOW_SPEED;
                break;
        }
        return speedType;
    }

    /**
     * 获取分数
     *
     * @param comboCount
     * @param speedType
     * @param scoreType
     * @return
     */
    public static int getScore(int comboCount, String speedType, int scoreType) {
        int score = 0;

        switch (speedType) {

            /**
             * 高速连击计分 分为三个等级
             * LV0=1-10个 不加分
             * LV1=11-20个 加1分
             * LV2=21-30个 加2分
             * MAX=31+个 加3分
             */
            case HIGH_SPEED:
            case HIGH_SPEED2:
                if (comboCount >= 11 && comboCount <= 20) {
                    score += scoreType + 1;
                } else if (comboCount >= 21 && comboCount <= 30) {
                    score += scoreType + 2;
                } else if (comboCount >= 31) {
                    score += scoreType + 3;
                } else {
                    score += scoreType;
                }
                break;

            /**
             * 中速连击计分 分为三个等级
             * LV0=1-5个 不加分
             * LV1=6-10个 加1分
             * LV2=11-20个 加2分
             * MAX=21+个 加3分
             */
            case MEDIUM_SPEED:
                if (comboCount >= 6 && comboCount <= 10) {
                    score += scoreType + 1;
                } else if (comboCount >= 11 && comboCount <= 20) {
                    score += scoreType + 2;
                } else if (comboCount >= 21) {
                    score += scoreType + 3;
                } else {
                    score += scoreType;
                }
                break;
        }
        return score;
    }


}
