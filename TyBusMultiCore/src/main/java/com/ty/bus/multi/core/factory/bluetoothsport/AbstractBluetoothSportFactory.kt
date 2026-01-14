package com.ty.bus.multi.core.factory.bluetoothsport

import android.content.Context
import com.ty.bus.multi.core.entity.SportBluetoothInfo
import com.ty.bus.multi.core.view.bluetooth.AbstractBluetoothResultView
import com.zy.entity.DeviceType

abstract class AbstractBluetoothSportFactory {
    abstract fun getBluetoothInfoList(): MutableList<SportBluetoothInfo>
    open fun getTestCount(): Int = 1

    abstract fun getResultView(): Class<out AbstractBluetoothResultView>

    abstract fun getDeviceType(): DeviceType

    open fun getUploadResult(score: String): String = score
}