package com.ty.bus.multi.core.factory.bluetoothsport

import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.entity.BmiParam
import com.ty.bus.multi.core.entity.SportBluetoothInfo
import com.ty.bus.multi.core.utils.BmiUtil
import com.ty.bus.multi.core.view.bluetooth.AbstractBluetoothResultView
import com.ty.bus.multi.core.view.bluetooth.BmiBluetoothResultView
import com.ty.tybusbase.utils.LogUtil
import com.zy.entity.DeviceType
import java.lang.Exception

class BmiBluetoothSportFactory : AbstractBluetoothSportFactory() {
    private var bmiParam: BmiParam? = null
    override fun getBluetoothInfoList(): MutableList<SportBluetoothInfo> {
        if (bmiParam == null) {
            bmiParam = MultiCoreConfig.getBmiTestParam()
        }
        return bmiParam!!.getBluetoothList()
    }

    override fun getResultView(): Class<out AbstractBluetoothResultView> {
        return BmiBluetoothResultView::class.java
    }

    override fun getDeviceType(): DeviceType {
        return DeviceType.HEIGHT_WEIGHT
    }

    override fun getUploadResult(score: String): String {
        try {
            val split = score.split(",")
            val weight = split[0].toDouble()
            val height = split[1].toDouble()
            return BmiUtil.computeBMI(height.div(100), weight)
        } catch (e: Exception) {
            LogUtil.e("计算BMI出错：score = $score")
            return "20.8"
        }
    }

}