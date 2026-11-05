package com.ty.tybusbase.identify.facecroprect

import android.graphics.Rect
import com.adl.lib.pose.AdlPoseSkeleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 头部框：鼻子 + 双耳。
 * 以鼻子为中心，按耳距扩边（与原 FourSportGb headRect 公式一致）。
 */
class FaceCropRectByEarsStrategy(
    private val config: FaceCropRectConfig = FaceCropRectConfig(),
) : FaceCropRectStrategy {

    override fun build(skeleton: AdlPoseSkeleton): Rect? {
        val nose = skeleton.nose ?: return null
        val lEar = skeleton.lEar
        val rEar = skeleton.rEar
        if (lEar == null && rEar == null) return null
        // 耳跨距：与原逻辑一致先 *2，再按 earSpanScale 扩边
        val earSpan = if (lEar != null && rEar != null) {
            max(abs(nose.x - lEar.x), abs(nose.x - rEar.x)) * 2f
        } else if (lEar != null) {
            abs(nose.x - lEar.x) * 2f
        } else {
            abs(nose.x - rEar.x) * 2f
        }
        if (earSpan <= 1f) return null
        val scale = config.earSpanScale
        val left = nose.x - earSpan * scale
        val right = nose.x + earSpan * scale
        val estimatedTop = if (lEar != null && rEar != null) {
            min(nose.y, min(lEar.y, rEar.y)) - earSpan * scale
        } else if (lEar != null) {
            min(nose.y, lEar.y) - earSpan * scale
        } else {
            min(nose.y, rEar.y) - earSpan * scale
        }
        val top = skeleton.headTop?.y?.let { min(it, estimatedTop) } ?: estimatedTop
        val estimatedBottom = if (lEar != null && rEar != null) {
            max(nose.y, max(lEar.y, rEar.y)) + earSpan * scale
        } else if (lEar != null) {
            max(nose.y, lEar.y) + earSpan * scale
        } else {
            max(nose.y, rEar.y) + earSpan * scale
        }
        val bottom = skeleton.chin?.y?.let { max(it, estimatedBottom) } ?: estimatedBottom
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
