package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupsidetosidejumps.GroupSideToSideJumpsParser
import com.adl.sport.general.groupskippingremix.GroupSkippingLRemixParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

class TenSideToSideJumpFactory:AbstractMultiSportFactory() {
    override fun getPoseModelId(): Int = 2030

    override fun getParser(): MultiSportParser<*> = GroupSideToSideJumpsParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.SIDE_TO_SIDE_10_PERSONS

    override fun isSupportPrescription(): Boolean = false
}