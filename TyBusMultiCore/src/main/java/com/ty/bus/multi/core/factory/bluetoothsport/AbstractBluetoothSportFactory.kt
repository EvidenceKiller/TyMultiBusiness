package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.bus.multi.core.entity.BluetoothAddressParam

abstract class AbstractBluetoothSportFactory {
    abstract fun getAddress(): BluetoothAddressParam
    open fun getTestCount(): Int = 1
}