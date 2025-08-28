package com.ty.tybusbase.sport.base

import com.adl.sport.general.EuCallback
import com.adl.sport.general.EuDoubleBase

abstract class CommonEuDoubleBase(callback: EuCallback) : EuDoubleBase(callback) {
    override fun version(): String = "V2.0.3.0"
}