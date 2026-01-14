package com.ty.bus.multi.core.view.bluetooth

import android.content.Context
import android.view.LayoutInflater
import com.ty.bus.multi.core.utils.BmiUtil
import com.ty.bus.multi.databinding.ViewBmiBluetoothResultBinding

class BmiBluetoothResultView(context: Context) :
    AbstractBluetoothResultView(context) {

    private var mBinding = ViewBmiBluetoothResultBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    /**
     * 066.7,159.0
     */
    override fun addResult(result: String) {
        val split = result.split(",")
        val weight = split[0].toDouble()
        val height = split[1].toDouble()
        mBinding.tvHeight.text = height.toString()
        mBinding.tvWeight.text = weight.toString()
        mBinding.tvBmi.text = BmiUtil.computeBMI(height.div(100), weight)
    }


}