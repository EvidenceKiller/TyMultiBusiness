package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.StudentEntity
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.BluetoothSportConfig
import com.ty.bus.multi.core.entity.BluetoothSportResultInfo
import com.ty.bus.multi.core.factory.bluetoothsport.AbstractBluetoothSportFactory
import com.ty.bus.multi.core.factory.bluetoothsport.BluetoothSportProduceFactory
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.utils.LogUtil
import com.zy.callback.DeviceResultCallback
import com.zy.callback.OnDeviceConnectChangedListener
import com.zy.callback.OnScanCallback
import com.zy.clazz.MultiDeviceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class BluetoothSportViewModel(application: Application) : BaseSportViewModel(application) {

    private var sportState = MultiSportState.IDLE

    var sportCardState =
        MutableSharedFlow<Pair<Int, BluetoothSportCardState>>()

    //成绩回调
    var sportResultState =
        MutableSharedFlow<Pair<Int, String>>()

    //测试结束
    val testFinishFlow = MutableSharedFlow<Boolean>()

    var bluetoothSportConfig: BluetoothSportConfig? = null

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
                    //可能手动结束后开始指令才回来
                    if (sportState != MultiSportState.PREPARING) {
                        return
                    }
                    val blueInfo =
                        factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
                    LogUtil.d("${blueInfo?.index}号位发送蓝牙配置成功, 开始测试")
                    mHelper.startSingleTest(address)
                    //延迟1秒，等待设备清楚上一次成绩；出现肺活量下一次开始，但是查询到的还是上一次的成绩
                    uiHandler.postDelayed({
                        sportState = MultiSportState.SPORTING
                    }, 1000)
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

    @Synchronized
    private fun handleBlueResult(address: String?, isEnd: Boolean, score: Array<out String?>) {
        //不是运动状态，则不处理
        if (sportState != MultiSportState.SPORTING) {
            return
        }
        if (isEnd) {
            LogUtil.d("收到蓝牙成绩: $address,${score.joinToString(",")}")
            val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
            if (blueInfo != null) {
                val resultInfo = sportPersonArray[blueInfo.index]
                if (resultInfo != null) {
                    if (resultInfo.score.isEmpty()) {
                        resultInfo.score = score.joinToString(",")
                        uploadResult(resultInfo)
                        emitSportResultState(blueInfo.index, resultInfo.score)
                    }
                }
            }
            //判断是否本次测试结束，所有测试人员都已经完成测试表示本次测试完成
            val isEnd = isTestEnd()
            if (isEnd) {
                //先结束成绩查询
                mHelper.stopTest()
                sportState = MultiSportState.IDLE
                currentTestTimes++
                LogUtil.d("本次测试结束,当前测试次数：${currentTestTimes}--总测试次数：${testCount}")
                //重置本次成绩
                resetResult()
                //如果本次结束，则判断是否是本场比赛结束
                if (currentTestTimes >= testCount) {
                    //本场比赛结束
                    toStatusSportEnd()
                } else {
                    //本次结束，开始下一次测试
                    startSport()
                }
            }
        }
    }

    override fun toStatusSportEnd() {
        super.toStatusSportEnd()
        emitSportFinishState(true)
        mHelper.stopTest()
        sportState = MultiSportState.IDLE
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
        endSportTime = System.currentTimeMillis()
        val value = sportPersonArray.get(sportResultInfo.number)
        var sportResult = SportResult()
        sportResult.sportDuration = (endSportTime - startSportTime).toInt()
        sportResult.maxComboCount = 0
        sportResult.perfectCount = 0
        sportResult.goodCount = 0
        sportResult.comeOnCount = 0
        sportResult.perfectScore = 0
        sportResult.goodScore = 0
        sportResult.comeOnScore = 0
        sportResult.userId = value.studentEntity.accountId
        sportResult.startTime = startSportTime
        sportResult.endTime = endSportTime
        sportResult.sportResult = factory.getUploadResult(value.score)
//                sportResult.sportViolationStatus = 1
//                sportResult.sportViolationNumber = "1"
        var recordPlan: SportRecordEntity.RecordPlan? = null
        if (sceneId == SceneConstants.ORGANIZE_TEST) {
            recordPlan = SportRecordEntity.RecordPlan().apply {
                ptPlanId = this@BluetoothSportViewModel.ptPlanId
                ptStandardId = this@BluetoothSportViewModel.ptStandardId
            }
        }
        uploadSportResult(
            sceneId ?: SceneConstants.INDEPENDENT_TEST,
            sportResult,
            sportResult.userId,
            recordPlan = recordPlan
        )

    }

    /**
     * 处理蓝牙连接成功
     */
    private fun handleConnectSuccess(address: String?) {
        LogUtil.d("蓝牙连接成功: $address")
        val blueInfo = factory.getBluetoothInfoList().firstOrNull { it.blueAddress == address }
        if (blueInfo != null) {
            blueInfo.isConnected = true
            if (sportState == MultiSportState.SPORTING || sportState == MultiSportState.PREPARING) {
                emitSportCardState(blueInfo.index, BluetoothSportCardState.SPORTING)
            } else {
                emitSportCardState(blueInfo.index, BluetoothSportCardState.CONNECTED)
            }
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
//            emitSportCardState(it.index, BluetoothSportCardState.CONNECTED)
        }

        mHelper.scanBleDeviceWithFilter(
            factory.getBluetoothInfoList().map { it -> it.blueAddress },
            object : OnScanCallback {
                override fun onFinish() {
                    val filterAddress =
                        factory.getBluetoothInfoList()
                            .filter { tempAddressList.contains(it.blueAddress) }
                            .map { android.util.Pair(factory.getDeviceType(), it.blueAddress!!) }
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
                                        factory.getDeviceType(),
                                        it.blueAddress!!
                                    )
                                })
                        }
                    }
                }
            })

//        mHelper.connect(
//            factory.getBluetoothInfoList().map {
//                android.util.Pair(
//                    factory.getDeviceType(),
//                    it.blueAddress!!
//                )
//            })
    }

    fun startSport() {
        factory.getBluetoothInfoList().forEach {
//            emitSportCardState(it.index, BluetoothSportCardState.SPORTING)
            if (it.isConnected && sportPersonArray[it.index] != null) {
                emitSportCardState(it.index, BluetoothSportCardState.SPORTING)
            } else {
                emitSportCardState(it.index, BluetoothSportCardState.IDLE)
            }
        }
        sportState = MultiSportState.PREPARING
        mHelper.config()
        startSportTime = System.currentTimeMillis()
//        mockResult()
    }

    private fun mockResult() {
        viewModelScope.launch(Dispatchers.IO) {
            SystemClock.sleep(1000)
            factory.getBluetoothInfoList().forEach {
                handleBlueResult(it.blueAddress, true, arrayOf("66.8", "178.8"))
            }
        }
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
//            emitSportCardState(it.index, BluetoothSportCardState.CONNECTED)
            if (it.isConnected) {
                emitSportCardState(it.index, BluetoothSportCardState.CONNECTED)
            } else {
                emitSportCardState(it.index, BluetoothSportCardState.DISCONNECTED)
            }
        }
        sportState = MultiSportState.IDLE
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
        sportState = MultiSportState.IDLE
        testCount = 1
        currentTestTimes = 0
        resetResult()
        startSport()
    }
}