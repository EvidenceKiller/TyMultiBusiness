package com.ty.bus.multi.core.entity

open class BluetoothAddressParam(
    private var bluetoothList: MutableList<SportBluetoothInfo> = mutableListOf<SportBluetoothInfo>()
) {
    fun addBluetoothInfo(info: SportBluetoothInfo) {
        val jk = bluetoothList.firstOrNull { it.index == info.index }
        if (jk == null) {
            bluetoothList.add(info)
        } else {
            jk.blueAddress = info.blueAddress
        }
    }

    fun addBluetoothInfo(index: Int, address: String) {
        val jk = bluetoothList.firstOrNull { it.index == index }
        if (jk == null) {
            bluetoothList.add(SportBluetoothInfo(index, address))
        } else {
            jk.blueAddress = address
        }
    }

    fun removeBluetoothInfo(index: Int) {
        val jk = bluetoothList.firstOrNull { it.index == index }
        if (jk != null) {
            bluetoothList.remove(jk)
        }
    }

    fun getBluetoothList(): MutableList<SportBluetoothInfo> {
        return bluetoothList
    }
}