package com.ty.bus.multi.core.factory

import com.adl.sport.general.grouppushup.GroupPushUpParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType
import com.ty.bus.multi.R
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.expand.getString

class ThreePushUpFactory : AbstractMultiSportFactory() {
    override fun getPoseModelId(): Int = ModelIdConstants.PUSH_UP_POSE_MODEL_ID

    override fun getStartTip(): String = getString(R.string.move_in_marked_area)

    override fun getInTip(): String = getString(R.string.enter_identification_area)

    override fun getReadyTip(): String = getString(R.string.prepare_side_facing_screen)

    override fun getParser(): MultiSportParser<*> = GroupPushUpParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.PUSHUP_3_PERSONS
}