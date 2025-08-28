package com.ty.tybusbase.sport.helper

import android.graphics.PointF
import com.adl.lib.pose.AdlPoseKeyPoint
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * @author :WEI_CP
 * Time:2022/3/6 9:24
 * Description: this is SportUtil
 */
object SportUtil {
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
        val dx = (p2.x - p1.x).toDouble()
        val dy = (p2.y - p1.y).toDouble()
        val angleRadians = atan2(dy, dx) // 根据该方法，可以直接获取坐标点和x轴的夹角，返回的是一个-π到π之间的弧度值
        val degrees = Math.toDegrees(angleRadians) //调用Math的API 将弧度转为角度，角度值范围为±180°。
        return degrees
    }
}
