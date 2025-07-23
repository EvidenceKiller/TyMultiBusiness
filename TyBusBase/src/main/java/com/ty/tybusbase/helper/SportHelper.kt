package com.ty.tybusbase.helper

import android.graphics.PointF
import com.adl.lib.pose.AdlPoseKeyPoint
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.constants.SportConstant.SportAiCode
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt


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

    /**
     * 获取两线之间的夹角
     *
     * @param center 中点
     * @param p1     起始点
     * @param p2     终点
     * @return
     */
    fun getRotationBetweenLines2(
        center: AdlPoseKeyPoint,
        p1: AdlPoseKeyPoint, p2: AdlPoseKeyPoint
    ): Float {
        try {
            val fp_x = (p1.getX() - center.getX()).toDouble()
            val fp_y = (p1.getY() - center.getY()).toDouble()
            val sp_x = (p2.getX() - center.getX()).toDouble()
            val sp_y = (p2.getY() - center.getY()).toDouble()
            val hyp = (fp_x * sp_x) + (fp_y * sp_y)
            val hyp0 = sqrt(fp_x * fp_x + fp_y * fp_y)
            val hyp1 = sqrt(sp_x * sp_x + sp_y * sp_y)
            val cos = hyp / (hyp0 * hyp1)
            return (acos(cos) * 180 / Math.PI).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0f
    }

    fun getDistance2(p1: AdlPoseKeyPoint, p2: AdlPoseKeyPoint): Double {
        try {
            return sqrt(((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)).toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0
    }


    //    public static float getRotationBetweenLines(BodyPoint center,
    //                                                BodyPoint p1, BodyPoint p2) {
    //        return getRotationBetweenLines(toPoint(center), toPoint(p1), toPoint(p2));
    //    }
    /**
     * 获取两线之间的夹角
     *
     * @param center 中点
     * @param p1     起始点
     * @param p2     终点
     * @return
     */
    fun getRotationBetweenLines(
        center: AdlPoseKeyPoint,
        p1: AdlPoseKeyPoint, p2: AdlPoseKeyPoint
    ): Float {
        try {
            val fp_x = (p1.getX() - center.getX()).toDouble()
            val fp_y = (p1.getY() - center.getY()).toDouble()
            val sp_x = (p2.getX() - center.getX()).toDouble()
            val sp_y = (p2.getY() - center.getY()).toDouble()
            val hyp = (fp_x * sp_x) + (fp_y * sp_y)
            val hyp0 = sqrt(fp_x * fp_x + fp_y * fp_y)
            val hyp1 = sqrt(sp_x * sp_x + sp_y * sp_y)
            val cos = hyp / (hyp0 * hyp1)
            return (acos(cos) * 180 / Math.PI).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0f
    }


    fun getDistance(p1: AdlPoseKeyPoint, p2: AdlPoseKeyPoint): Double {
        try {
            return sqrt(((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)).toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0
    }


    //    public static AdlPoseKeyPoint toPoint(BodyPoint p) {
    //        return new AdlPoseKeyPoint(p.x, p.y, p.score);
    //    }
    fun getDegrees(p1: PointF, p2: PointF): Double {
        //得到两个坐标点的差值， 其实得到的dx 和dy 就是我们线条的向量了
        val dx: Double = (p2.x - p1.x).toDouble()
        val dy: Double = (p2.y - p1.y).toDouble()
        val angleRadians = atan2(dy, dx) // 根据该方法，可以直接获取坐标点和x轴的夹角，返回的是一个-π到π之间的弧度值
        val degrees = Math.toDegrees(angleRadians) //调用Math的API 将弧度转为角度，角度值范围为±180°。
        return degrees
    }
}
