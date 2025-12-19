package com.ty.bus.multi.core.callback

import com.adl.mount.sport.TsSportChannel
import com.adl.sport.general.EuCallback
import com.ty.tybusbase.sport.bean.SportDetectResult

abstract class DoubleBaseCallback: EuCallback() {
    abstract fun onLeftCompleteOnce(result: SportDetectResult)
    open fun onLeftPrepared(){}

    abstract fun onRightCompleteOnce(result: SportDetectResult)
    open fun onRightPrepared(){}

    abstract fun onChannel(left: TsSportChannel?, right: TsSportChannel?)
}