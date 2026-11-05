package com.ty.tybusbase.identify.facecroprect

import android.graphics.Rect
import com.adl.lib.pose.AdlPoseSkeleton

/**
 * 单步裁剪框策略：能算出则返回框，否则 null 交给下一策略。
 * 业务可自行实现并注入 [FaceCropRectHelper]。
 */
fun interface FaceCropRectStrategy {
    fun build(skeleton: AdlPoseSkeleton): Rect?
}
