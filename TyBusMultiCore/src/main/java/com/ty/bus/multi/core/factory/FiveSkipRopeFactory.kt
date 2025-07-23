package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupskippingremix.GroupSkippingRemixParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

class FiveSkipRopeFactory : AbstractMultiSportFactory() {

    override fun getBodyModelId(): Int = 1000

    override fun getPoseModelId(): Int = 2018

    override fun getParser(): MultiSportParser<*> = GroupSkippingRemixParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.SKIPPING_5_PERSONS_BATCH

    override fun isSupportPrescription(): Boolean = true
}