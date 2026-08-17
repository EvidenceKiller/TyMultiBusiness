package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupskippingremix.GroupSkippingLRemixParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

class EtTenSkipRopeFactory:AbstractMultiSportFactory() {
    override fun getPoseModelId(): Int = 2030

    override fun getParser(): MultiSportParser<*> = GroupSkippingLRemixParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.SKIPPING_10_PERSONS

    override fun isSupportPrescription(): Boolean = true
}
