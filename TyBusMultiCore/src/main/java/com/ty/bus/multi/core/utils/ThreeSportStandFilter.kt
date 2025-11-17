package com.ty.bus.multi.core.utils

import com.adl.lib.pose.AdlObject
import com.adl.mount.core.TsTaskBodyFilter
import com.adl.mount.sport.TsSportRegion
import kotlin.math.abs

class ThreeSportStandFilter : TsTaskBodyFilter {
    override fun onFilter(list: MutableList<AdlObject>?, region: TsSportRegion?): AdlObject? {
        var personRect: AdlObject? = null
        val iterator = list?.iterator()

        while (iterator?.hasNext() == true) {
            val obj = iterator.next()
            if (region != null && region.pointIn(obj.centerX(), obj.bottom(), obj.h)) {
                if (personRect == null) {
                    personRect = obj
                } else if (this.square(abs(region.centerX() - personRect.centerX())) + this.square(
                        abs(
                            region.centerY() - personRect.centerY()
                        )
                    ) > this.square(abs(region.centerX() - obj.centerX())) + this.square(
                        abs(
                            region.centerY() - obj.centerY()
                        )
                    )
                ) {
                    personRect = obj
                }
            }
        }

        return personRect
    }

    private fun square(num: Int): Int {
        return num * num
    }
}