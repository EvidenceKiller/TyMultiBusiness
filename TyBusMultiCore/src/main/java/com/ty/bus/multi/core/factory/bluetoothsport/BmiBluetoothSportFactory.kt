package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.entity.BluetoothAddressParam
import com.ty.bus.multi.core.entity.BmiParam

class BmiBluetoothSportFactory: AbstractBluetoothSportFactory() {
    private var bmiParam: BmiParam? = null
    override fun getAddress(): BluetoothAddressParam {
        if (bmiParam == null){
            bmiParam = MultiCoreConfig.getBmiTestParam()
        }
        return bmiParam!!
    }

}