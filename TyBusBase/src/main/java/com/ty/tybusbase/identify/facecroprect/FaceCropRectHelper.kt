package com.ty.tybusbase.identify.facecroprect

import android.graphics.Rect
import com.adl.lib.pose.AdlPoseSkeleton
import com.ty.tybusbase.utils.LogUtil

/**
 * 人脸识别裁剪框入口：默认 Eyes → Ears → Shoulder。
 * 算不出有效框时返回空 [Rect]，调用方应 `if (rect.isEmpty)` 跳过识别（无兜底框）。
 */
class FaceCropRectHelper(
    private val config: FaceCropRectConfig = FaceCropRectConfig(),
    private val strategies: List<FaceCropRectStrategy> = defaultStrategies(config),
) {

    fun getRect(skeleton: AdlPoseSkeleton?, width: Int, height: Int): Rect {
        if (width <= 0 || height <= 0) return Rect()
        if (skeleton == null) {
            LogUtil.d("FaceCropRectHelper: skeleton is null, skip identify")
            return Rect()
        }
        val rect = strategies.firstNotNullOfOrNull { it.build(skeleton) }
        if (rect == null || rect.isEmpty) {
            LogUtil.d("FaceCropRectHelper: key point is missing, skip identify")
            return Rect()
        }
        correctRect(rect, width, height, config.minSize)
        return rect
    }

    companion object {
        fun defaultStrategies(config: FaceCropRectConfig = FaceCropRectConfig()): List<FaceCropRectStrategy> =
            listOf(
                FaceCropRectByEyesStrategy(config),
                FaceCropRectByEarsStrategy(config),
                FaceCropRectByShoulderStrategy(config),
            )

        /**
         * 边界夹紧 + 最小尺寸纠正（与原 FourSportGb correctRect 一致）。
         */
        fun correctRect(rect: Rect, width: Int, height: Int, minSize: Int = 40) {
            if (rect.width() < minSize) {
                val centerX = (rect.left + rect.right) / 2
                rect.left = centerX - minSize / 2
                rect.right = rect.left + minSize
            }
            if (rect.height() < minSize) {
                val centerY = (rect.top + rect.bottom) / 2
                rect.top = centerY - minSize / 2
                rect.bottom = rect.top + minSize
            }
            if (rect.left < 0) {
                rect.right -= rect.left
                rect.left = 0
            }
            if (rect.right > width) {
                val overflow = rect.right - width
                rect.left -= overflow
                rect.right = width
            }
            if (rect.top < 0) {
                rect.bottom -= rect.top
                rect.top = 0
            }
            if (rect.bottom > height) {
                val overflow = rect.bottom - height
                rect.top -= overflow
                rect.bottom = height
            }
            rect.left = rect.left.coerceIn(0, width)
            rect.right = rect.right.coerceIn(0, width)
            rect.top = rect.top.coerceIn(0, height)
            rect.bottom = rect.bottom.coerceIn(0, height)
        }
    }
}
