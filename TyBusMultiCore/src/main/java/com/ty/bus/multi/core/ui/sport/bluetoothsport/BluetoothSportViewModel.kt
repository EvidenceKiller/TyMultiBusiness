package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.viewModelScope
import com.adl.base.common.AdlToast
import com.ty.bus.multi.core.factory.bluetoothsport.AbstractBluetoothSportFactory
import com.ty.bus.multi.core.factory.bluetoothsport.BluetoothSportProduceFactory
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.tybusbase.base.BaseSportViewModel
import com.zy.callback.DeviceResultCallback
import com.zy.callback.OnDeviceConnectChangedListener
import com.zy.callback.OnScanCallback
import com.zy.clazz.MultiDeviceHelper
import com.zy.entity.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class BluetoothSportViewModel(application: Application) : BaseSportViewModel(application) {

    var sportCardState =
        MutableSharedFlow<Pair<Int, BluetoothSportCardState>>()
    lateinit var factory: AbstractBluetoothSportFactory

    private val mHelper by lazy { MultiDeviceHelper(getApplication()) }

    private val tempAddressList = HashSet<String>()

    fun initData() {
        factory = BluetoothSportProduceFactory.getFactory(sportInfo?.sportItemCode)
        if (factory.getBluetoothInfoList().isNotEmpty()) {
            mHelper.setConnectChangedListener(object : OnDeviceConnectChangedListener {
                override fun onConnectSuccess(address: String?) {
                }

                override fun onConnectFailure(address: String?) {
                    handleConnectFailure(address)
                }

                override fun onServiceDiscoverySucceed(address: String?) {
                    handleConnectSuccess(address)
                }
            })

            mHelper.setDeviceResultCallback(object : DeviceResultCallback {
                override fun onConfigComplete(address: String?) {

                }

                override fun onResult(
                    address: String?,
                    isEnd: Boolean,
                    vararg score: String?
                ) {
                }

                override fun onStart(address: String?, isSuccess: Boolean) {
                }

                override fun onCancel(address: String?, isSuccess: Boolean) {
                }

            })

        }
    }

    /**
     * 处理蓝牙连接成功
     */
    private fun handleConnectSuccess(address: String?) {
        val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
        if (blueInfo != null) {
            emitSportCardState(blueInfo.index, BluetoothSportCardState.CONNECTED)
        }
    }

    /**
     * 处理蓝牙断开
     */
    private fun handleConnectFailure(address: String?) {
        val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
        if (blueInfo != null) {
            emitSportCardState(blueInfo.index, BluetoothSportCardState.DISCONNECTED)
        }
    }

    /**
     * 处理蓝牙连接
     */
    fun handleBluetoothConnect() {
        factory.getBluetoothInfoList().forEach {
            emitSportCardState(it.index, BluetoothSportCardState.DISCONNECTED)
        }
        mHelper.scanBleDevice(object : OnScanCallback {
            override fun onFinish() {
                val filterAddress =
                    factory.getBluetoothInfoList()
                        .filter { tempAddressList.contains(it.blueAddress) }
                        .map { android.util.Pair(DeviceType.VITAL_CAPACITY, it.blueAddress!!) }
                        .toMutableList()
                if (filterAddress.isNotEmpty()) {
                    tempAddressList.clear()
                    mHelper.connect(filterAddress)
                }
            }

            override fun onScanning(device: BluetoothDevice?, rssi: Int) {
                device?.let {
                    tempAddressList.add(device.address)
                    if (factory.getBluetoothInfoList()
                            .all { tempAddressList.contains(it.blueAddress) }
                    ) {
                        mHelper.stopScan()
                        tempAddressList.clear()
                        mHelper.connect(
                            factory.getBluetoothInfoList().map {
                                android.util.Pair(
                                    DeviceType.VITAL_CAPACITY,
                                    it.blueAddress!!
                                )
                            })
                    }
                }
            }
        })
    }

    private fun emitSportCardState(index: Int, state: BluetoothSportCardState) {
        viewModelScope.launch(Dispatchers.IO) {
            sportCardState.emit(Pair(index, state))
        }
    }
}