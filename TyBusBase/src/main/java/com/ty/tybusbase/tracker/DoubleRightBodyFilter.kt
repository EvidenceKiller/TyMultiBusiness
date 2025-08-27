package com.ty.tybusbase.tracker

import android.graphics.Rect
import com.adl.lib.pose.AdlObject
import com.adl.mount.core.TsTaskBodyMultiFilter
import com.adl.mount.sport.TsSportRegion

class DoubleRightBodyFilter : TsTaskBodyMultiFilter {
    override fun onFilter(
        list: MutableList<AdlObject>?,
        region: TsSportRegion?,
        score: Float
    ): MutableList<AdlObject>? {
        if (region == null || list.isNullOrEmpty()) return null

        val left = region.pA.x.coerceAtMost(region.pC.x)
        val top = region.pA.y.coerceAtMost(region.pB.y)
        val right = region.pB.x.coerceAtLeast(region.pD.x)
        val bottom = region.pC.y.coerceAtLeast(region.pD.y)
        val rect = Rect(left, top, right, bottom)

//        val list = list.filter { rect.contains(it.right(), it.centerY()) }?.toMutableList()

        return list
    }
}