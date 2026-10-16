package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupcrunch.GroupCrunchParser
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType
import com.adl.sport.general.situp.EuSiteRule
import com.adl.sport.general.situp.SitUpMultiParser
import com.ty.bus.multi.R
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.expand.getString


class ThreeSiteUpFactory : AbstractMultiSportFactory() {
    override fun getBodyModelId(): Int = ModelIdConstants.SIT_UP_BODY_MODEL_ID
    override fun getPoseModelId(): Int = ModelIdConstants.SIT_UP_POSE_MODEL_ID

    override fun getFaceBodyModelId(): Int = ModelIdConstants.COMMON_BODY_MODEL_ID

    override fun getFacePoseModelId(): Int = ModelIdConstants.COMMON_POSE_MODEL_ID

    override fun getStartTip(): String = getString(R.string.pull_up_start_tips)

    override fun getInTip(): String = getString(R.string.sit_on_mat_and_look_at_screen)

    override fun getReadyTip(): String = getString(R.string.lie_on_back_and_face_screen)

    override fun getParser(): MultiSportParser<*> {
        val par = BusiBaseConfig.getSiteUpParamBean()
        val rule = EuSiteRule()
        rule.setNumberOfPerson(3) // 3人
        rule.setEnableAutoEnd(false)
        rule.setEnableHandOk(par.enableHandOk) // 双手抱头
        rule.setEnableKneeAngleOk(par.enableKneeAngleOk) // 屈膝呈90
        rule.setEnableElbowToKnee(false) // 双肘触膝
        rule.setEnableUpliftHip(par.enableHipUp) // 垫臀
        rule.setEnableShoulderToGround(false) // 肩膀触地
        rule.setShoulderToGroundAngle(15f) // （2-24）
        rule.setEarHandOk(0.15f) // 双手护耳严格程度（默认0.6 , 0.9宽松 - 0.1严格）
        return SitUpMultiParser(rule, 3)
    }

    override fun getMultiSportType(): MultiSportType = MultiSportType.SIT_UP_3_PERSONS

    override fun enableSmooth(): Boolean = true

    override fun enableHistoryBody(): Boolean = true

    override fun liteBodyDetect(): Boolean = false
}