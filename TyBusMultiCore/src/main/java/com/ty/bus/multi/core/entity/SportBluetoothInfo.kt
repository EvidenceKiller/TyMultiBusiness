package com.ty.bus.multi.core.entity

import java.io.Serializable

data class SportBluetoothInfo(
    var index: Int = 0,
    var blueAddress: String? = null
) : Serializable