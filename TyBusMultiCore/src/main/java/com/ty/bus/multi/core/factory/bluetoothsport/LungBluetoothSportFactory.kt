package com.ty.bus.multi.core.factory.bluetoothsport

import android.content.Context
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.entity.LungParam
import com.ty.bus.multi.core.entity.SportBluetoothInfo
import com.ty.bus.multi.core.view.bluetooth.AbstractBluetoothResultView
import com.ty.bus.multi.core.view.bluetooth.LungBluetoothResultView

class LungBluetoothSportFactory : AbstractBluetoothSportFactory() {
    private var lungParam: LungParam? = null
    override fun getBluetoothInfoList(): MutableList<SportBluetoothInfo> {
        if (lungParam == null) {
            lungParam = MultiCoreConfig.getLungTestParam()
        }
        return lungParam!!.getBluetoothList()
    }

    override fun getTestCount(): Int {
        if (lungParam == null) {
            lungParam = MultiCoreConfig.getLungTestParam()
        }
        return lungParam!!.testCount
    }

    override fun getResultView(): Class<out AbstractBluetoothResultView> {
        return LungBluetoothResultView::class.java
    }

}