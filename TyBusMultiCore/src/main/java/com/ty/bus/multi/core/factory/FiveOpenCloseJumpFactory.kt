package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupjumpingjack.GroupJumpingJackParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

class FiveOpenCloseJumpFactory: AbstractMultiSportFactory() {

    override fun getBodyModelId(): Int = 1000

    override fun getPoseModelId(): Int = 2018

    override fun getParser(): MultiSportParser<*> = GroupJumpingJackParser()
    override fun getMultiSportType(): MultiSportType = MultiSportType.JUMPINGJACK_5_PERSONS
}