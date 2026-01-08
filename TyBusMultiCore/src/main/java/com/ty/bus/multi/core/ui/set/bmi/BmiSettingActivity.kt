package com.ty.bus.multi.core.ui.set.bmi

import android.os.Bundle
import android.text.TextUtils
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.dialog.BleBluetoothChooseDialog
import com.ty.bus.multi.core.entity.BmiParam
import com.ty.bus.multi.databinding.ActivityBmiSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel

class BmiSettingActivity : BaseActivity<ActivityBmiSettingBinding, BaseViewModel>() {
    private lateinit var bmiParam: BmiParam
    override fun initObserver() {
    }

    override fun initData() {
        bmiParam = MultiCoreConfig.getBmiTestParam()
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (!TextUtils.isEmpty(bmiParam.blueAddress1)) {
            mBinding.tvBle1.text = bmiParam.blueAddress1
        }
        if (!TextUtils.isEmpty(bmiParam.blueAddress2)) {
            mBinding.tvBle2.text = bmiParam.blueAddress2
        }
        if (!TextUtils.isEmpty(bmiParam.blueAddress3)) {
            mBinding.tvBle3.text = bmiParam.blueAddress3
        }

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.tvBle1.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle1.text = it
                bmiParam.blueAddress1 = it
            }.addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle2.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle2.text = it
                bmiParam.blueAddress2 = it
            }.addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle3.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle3.text = it
                bmiParam.blueAddress3 = it
            }
                .addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MultiCoreConfig.updateBmiTestParam(bmiParam)
    }

}