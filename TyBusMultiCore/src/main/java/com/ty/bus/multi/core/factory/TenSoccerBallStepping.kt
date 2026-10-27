package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupfootballsteponball.GroupFootballStepOnBallParser
import com.adl.sport.general.groupsquat.GroupSquatParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

class TenSoccerBallStepping: AbstractMultiSportFactory() {

    override fun getBodyModelId(): Int = 1003

    override fun getPoseModelId(): Int = 2004

    override fun getParser(): MultiSportParser<*> = GroupFootballStepOnBallParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.STEP_ON_BALL_10_PERSONS

    override fun liteBodyDetect(): Boolean = false
}