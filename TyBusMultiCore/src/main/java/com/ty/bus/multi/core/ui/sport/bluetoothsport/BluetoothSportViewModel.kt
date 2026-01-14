package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.StudentEntity
import com.ty.bus.multi.core.entity.BluetoothSportResultInfo
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BluetoothSportViewModel(application: Application) : BaseSportViewModel(application) {

    var sportCardState =
        MutableSharedFlow<Pair<Int, BluetoothSportCardState>>()

    //成绩回调
    var sportResultState =
        MutableSharedFlow<Pair<Int, String>>()

    //测试结束
    val testFinishFlow = MutableSharedFlow<Boolean>()

    //人员信息
    private val sportPersonArray by lazy { SparseArray<BluetoothSportResultInfo>() } //存储已识别的位置和人员信息

    //测试次数
    private var testCount = 0

    //当前测试次数
    private var currentTestTimes = 0

    lateinit var factory: AbstractBluetoothSportFactory

    private val mHelper by lazy { MultiDeviceHelper(getApplication()) }

    private val tempAddressList = HashSet<String>()

    fun initData() {
        factory = BluetoothSportProduceFactory.getFactory(sportInfo?.sportItemCode)
        testCount = factory.getTestCount()
        val clazz = factory.getResultView()
        LogUtil.d("初始化蓝牙数据: ${clazz.simpleName}")
        LogUtil.d("初始化蓝牙数据2222: ${clazz.superclass.name}")
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
                val resultInfo = sportPersonArray[blueInfo.index]
                if (resultInfo.score.isEmpty()) {
                    resultInfo.score = score.joinToString(",")
                    uploadResult(resultInfo)
                    emitSportResultState(blueInfo.index, resultInfo.score)
                }
            }
            //判断是否本次测试结束，所有测试人员都已经完成测试表示本次测试完成
            val isEnd = isTestEnd()
            if (isEnd) {
                //先结束成绩查询
                mHelper.stopQueryResult()
                currentTestTimes++
                LogUtil.d("本次测试结束,当前测试次数：${currentTestTimes}--总测试次数：${testCount}")
                //重置本次成绩
                resetResult()
                //如果本次结束，则判断是否是本场比赛结束
                if (currentTestTimes >= testCount) {
                    //本场比赛结束
                    emitSportFinishState(true)
                } else {
                    //本次结束，开始下一次测试
                    startSport()
                }
            }

        }
    }

    /**
     * 重置成绩
     */
    private fun resetResult() {
        sportPersonArray.forEach { index, value ->
            value.score = ""
        }
    }

    /**
     * 判断本次测试是否结束
     */
    private fun isTestEnd(): Boolean {
        sportPersonArray.forEach { index, value ->
            if (value.score.isEmpty()) {
                return false
            }
        }
        return true
    }

    /**
     * 上传成绩
     */
    private fun uploadResult(sportResultInfo: BluetoothSportResultInfo) {

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
//        emitSportCardState(1, BluetoothSportCardState.CONNECTED)
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

    private fun emitSportFinishState(isFinish: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            testFinishFlow.emit(isFinish)
        }
    }

    fun addUser(index: Int, student: StudentEntity) {
        sportPersonArray.put(index, BluetoothSportResultInfo(student, index))
    }

    fun removeUser(index: Int) {
        sportPersonArray.remove(index)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHelper.release()
    }

    /**
     * 下一组
     */
    fun next() {
        //如果已连上，则开始选人，未连上，则连接蓝牙
        factory.getBluetoothInfoList().forEach {
            if (it.isConnected) {
                emitSportCardState(it.index, BluetoothSportCardState.CONNECTED)
            } else {
                emitSportCardState(it.index, BluetoothSportCardState.DISCONNECTED)
            }
        }
        //重置次数
        testCount = factory.getTestCount()
        currentTestTimes = 0
        //清除上次人员信息
        sportPersonArray.clear()
    }

    /**
     * 重新测试一次
     */
    fun retest() {
        testCount = 1
        currentTestTimes = 0
        startSport()
    }
}