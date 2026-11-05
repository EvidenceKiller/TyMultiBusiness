package com.ty.tybusbase.identify.facecroprect

import android.graphics.Rect
import com.adl.lib.pose.AdlPoseSkeleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 头肩框：双肩可用时使用（与原 FourSportGb shoulderRect 公式一致）。
 */
class FaceCropRectByShoulderStrategy(
    private val config: FaceCropRectConfig = FaceCropRectConfig(),
) : FaceCropRectStrategy {

    override fun build(skeleton: AdlPoseSkeleton): Rect? {
        val lShoulder = skeleton.lShoulder ?: return null
        val rShoulder = skeleton.rShoulder ?: return null
        val shoulderWidth = abs(lShoulder.x - rShoulder.x)
        if (shoulderWidth <= 0f) return null

        val shoulderWidthRedundancy = shoulderWidth * config.shoulderWidthPaddingRatio
        val left = min(lShoulder.x, rShoulder.x) - shoulderWidthRedundancy
        val right = max(lShoulder.x, rShoulder.x) + shoulderWidthRedundancy
        val bottom = max(lShoulder.y, rShoulder.y) + shoulderWidthRedundancy
        val expectTop = bottom - shoulderWidth * config.shoulderHeadRatio
        val top = skeleton.headTop?.let { min(it.y, expectTop) } ?: expectTop
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
