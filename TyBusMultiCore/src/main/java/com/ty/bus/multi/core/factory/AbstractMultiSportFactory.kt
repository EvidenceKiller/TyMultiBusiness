package com.ty.bus.multi.core.factory

import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType
import com.ty.tybusbase.constants.ModelIdConstants

abstract class AbstractMultiSportFactory {
    open fun getBodyModelId(): Int = ModelIdConstants.THREE_BODY_MODEL_ID
    abstract fun getPoseModelId(): Int

    //通用ID
    open fun getFaceBodyModelId(): Int = ModelIdConstants.COMMON_BODY_MODEL_ID

    //通用ID
    open fun getFacePoseModelId(): Int = ModelIdConstants.COMMON_POSE_MODEL_ID
    open fun getStartTip(): String = ""
    open fun getInTip(): String = " "
    open fun getReadyTip(): String = ""
    abstract fun getParser(): MultiSportParser<*>

    abstract fun getMultiSportType(): MultiSportType

    open fun isSupportPrescription(): Boolean = false

    open fun enableSmooth() = false

    //是否使用历史人框
    open fun enableHistoryBody() = false

    open fun liteBodyDetect() = true
}