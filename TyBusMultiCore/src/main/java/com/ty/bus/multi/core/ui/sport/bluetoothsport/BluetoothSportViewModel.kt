package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.viewModelScope
import com.ty.bus.multi.core.factory.bluetoothsport.AbstractBluetoothSportFactory
import com.ty.bus.multi.core.factory.bluetoothsport.BluetoothSportProduceFactory
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.utils.LogUtil
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

    //成绩回调
    var sportResultState =
        MutableSharedFlow<Pair<Int, String>>()

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
                    val blueInfo =
                        factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
                    LogUtil.d("${blueInfo?.index}号位发送蓝牙配置成功, 开始测试")
                    mHelper.startSingleTest(address)
                }

                override fun onResult(
                    address: String?,
                    isEnd: Boolean,
                    vararg score: String?
                ) {

                    handleBlueResult(address, isEnd, score)
                }

                override fun onStart(address: String?, isSuccess: Boolean) {
                }

                override fun onCancel(address: String?, isSuccess: Boolean) {
                }

            })
        }
    }

    private fun handleBlueResult(address: String?, isEnd: Boolean, score: Array<out String?>) {
        LogUtil.d("收到蓝牙数据: $address, $isEnd, ${score.joinToString(",")}")
        if (isEnd) {
            val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
            if (blueInfo != null) {
                emitSportResultState(blueInfo.index, score.joinToString(","))
            }
        }
    }

    /**
     * 处理蓝牙连接成功
     */
    private fun handleConnectSuccess(address: String?) {
        LogUtil.d("蓝牙连接失败: $address")
        val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
        if (blueInfo != null) {
            blueInfo.isConnected = true
            emitSportCardState(blueInfo.index, BluetoothSportCardState.CONNECTED)
        }
    }

    /**
     * 处理蓝牙断开
     */
    private fun handleConnectFailure(address: String?) {
        LogUtil.d("蓝牙连接失败: $address")
        val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
        if (blueInfo != null) {
            blueInfo.isConnected = false
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

    fun startSport() {
        factory.getBluetoothInfoList().forEach {
            if (it.isConnected) {
                emitSportCardState(it.index, BluetoothSportCardState.SPORTING)
            } else {
                emitSportCardState(it.index, BluetoothSportCardState.IDLE)
            }
        }
        mHelper.config()
    }

    private fun emitSportCardState(index: Int, state: BluetoothSportCardState) {
        viewModelScope.launch(Dispatchers.IO) {
            sportCardState.emit(Pair(index, state))
        }
    }

    private fun emitSportResultState(index: Int, score: String) {
        viewModelScope.launch(Dispatchers.IO) {
            sportResultState.emit(Pair(index, score))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHelper.release()
    }
}