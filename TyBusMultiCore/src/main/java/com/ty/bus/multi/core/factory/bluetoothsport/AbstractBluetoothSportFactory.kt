package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.bus.multi.core.entity.SportBluetoothInfo

abstract class AbstractBluetoothSportFactory {
    abstract fun getBluetoothInfoList(): MutableList<SportBluetoothInfo>
    open fun getTestCount(): Int = 1
}