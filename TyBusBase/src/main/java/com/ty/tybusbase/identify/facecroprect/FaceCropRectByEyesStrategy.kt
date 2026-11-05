package com.ty.tybusbase.identify.facecroprect

import android.graphics.Rect
import com.adl.lib.pose.AdlPoseSkeleton
import com.ty.tybusbase.helper.SportHelper
import kotlin.math.max
import kotlin.math.min

/**
 * 脸部框：鼻子 + 双眼可用时优先。
 * 以鼻子为中心，按眼距扩边（与原 FourSportGb faceRect 公式一致）。
 */
class FaceCropRectByEyesStrategy(
    private val config: FaceCropRectConfig = FaceCropRectConfig(),
) : FaceCropRectStrategy {

    override fun build(skeleton: AdlPoseSkeleton): Rect? {
        val nose = skeleton.nose ?: return null
        val lEye = skeleton.lEye
        val rEye = skeleton.rEye
        if (lEye == null && rEye == null) return null
        val scale = config.eyeSpanScale
        val eyeSpan = (if (lEye != null && rEye != null) {
            max(SportHelper.getDistance(nose, lEye), SportHelper.getDistance(nose, rEye)) * scale
        } else if (lEye != null) {
            SportHelper.getDistance(nose, lEye) * scale
        } else {
            SportHelper.getDistance(nose, rEye) * scale
        }).toFloat()
        if (eyeSpan <= 1f) return null
        val left = nose.x - eyeSpan * scale
        val right = nose.x + eyeSpan * scale
        val estimatedTop = if (lEye != null && rEye != null) {
            min(nose.y, min(lEye.y, rEye.y)) - eyeSpan * scale
        } else if (lEye != null) {
            min(nose.y, lEye.y) - eyeSpan * scale
        } else {
            min(nose.y, rEye.y) - eyeSpan * scale
        }
        val top = skeleton.headTop?.y?.let { min(it, estimatedTop) } ?: estimatedTop
        val estimatedBottom = if (lEye != null && rEye != null) {
            max(nose.y, max(lEye.y, rEye.y)) + eyeSpan * scale
        } else if (lEye != null) {
            max(nose.y, lEye.y) + eyeSpan * scale
        } else {
            max(nose.y, rEye.y) + eyeSpan * scale
        }
        val bottom = skeleton.chin?.y?.let { max(it, estimatedBottom) } ?: estimatedBottom
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
