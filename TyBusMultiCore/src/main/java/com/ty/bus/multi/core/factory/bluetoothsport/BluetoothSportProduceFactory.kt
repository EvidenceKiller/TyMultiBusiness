package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.tybusbase.constants.SportConstant

object BluetoothSportProduceFactory {
    fun getFactory(itemCode: String?): AbstractBluetoothSportFactory {
        return when (itemCode) {
            SportConstant.LUNG_CAPACITY -> {
                LungBluetoothSportFactory()
            }

            SportConstant.BODY_HEIGHT_WEIGHT -> {
                BmiBluetoothSportFactory()
            }

            else -> BmiBluetoothSportFactory()

        }
    }
}