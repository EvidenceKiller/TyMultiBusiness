package com.ty.bus.multi.core.factory

import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportType

abstract class AbstractMultiSportFactory {
    open fun getBodyModelId(): Int = 1054
    abstract fun getPoseModelId(): Int

    //通用ID
    open fun getFaceBodyModelId(): Int = 1003

    //通用ID
    open fun getFacePoseModelId(): Int = 2004
    open fun getStartTip(): String = ""
    open fun getInTip(): String = " "
    open fun getReadyTip(): String = ""
    abstract fun getParser(): MultiSportParser<*>

    abstract fun getMultiSportType(): MultiSportType

    open fun isSupportPrescription(): Boolean = false

    open fun enableSmooth() = false
}