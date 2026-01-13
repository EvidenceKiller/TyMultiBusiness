package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.entity.LungParam
import com.ty.bus.multi.core.entity.SportBluetoothInfo

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
}