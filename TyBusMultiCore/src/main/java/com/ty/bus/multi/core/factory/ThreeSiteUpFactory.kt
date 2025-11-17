package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupcrunch.GroupCrunchParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType
import com.ty.bus.multi.R
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.expand.getString

class ThreeSiteUpFactory:AbstractMultiSportFactory() {
    override fun getPoseModelId(): Int = ModelIdConstants.SIT_UP_POSE_MODEL_ID

    override fun getStartTip(): String = getString(R.string.pull_up_start_tips)

    override fun getInTip(): String = getString(R.string.sit_on_mat_and_look_at_screen)

    override fun getReadyTip(): String = getString(R.string.lie_on_back_and_face_screen)

    override fun getParser(): MultiSportParser<*> = GroupCrunchParser()

    override fun getMultiSportType(): MultiSportType = MultiSportType.CRUNCH_3_PERSONS
}