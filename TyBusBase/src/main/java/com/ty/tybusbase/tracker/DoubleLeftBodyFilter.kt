package com.ty.tybusbase.tracker

import com.adl.lib.pose.AdlObject
import com.adl.mount.core.TsTaskBodyMultiFilter
import com.adl.mount.sport.TsSportRegion

class DoubleLeftBodyFilter : TsTaskBodyMultiFilter {
    override fun onFilter(
        list: MutableList<AdlObject>?,
        region: TsSportRegion?,
        score: Float
    ): MutableList<AdlObject> {
        val list = list?.filter { true }?.toMutableList()
        return list ?: mutableListOf()
    }
}