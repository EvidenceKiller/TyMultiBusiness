package com.ty.bus.multi.core.ui.set.bmi

import android.os.Bundle
import com.ty.bus.multi.R
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.dialog.BleBluetoothChooseDialog
import com.ty.bus.multi.core.entity.BmiParam
import com.ty.bus.multi.databinding.ActivityBmiSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible

class BmiSettingActivity : BaseActivity<ActivityBmiSettingBinding, BaseViewModel>() {
    private lateinit var bmiParam: BmiParam
    override fun initObserver() {
    }

    override fun initData() {
        bmiParam = MultiCoreConfig.getBmiTestParam()
    }

    override fun initView(savedInstanceState: Bundle?) {
        bmiParam.getBluetoothList().forEach {
            dispatchBluetoothAddress(it.index, it.blueAddress)
        }
        mBinding.llBack.setOnClickListener { finish() }

        mBinding.tvBle1.setOnClickListener {
            addBluetoothAddress(1)
        }
        mBinding.tvBle2.setOnClickListener {
            addBluetoothAddress(2)
        }
        mBinding.tvBle3.setOnClickListener {
            addBluetoothAddress(3)
        }

        mBinding.tvBleDisconnect1.setOnClickListener {
            dispatchBluetoothAddress(1, null)
        }
        mBinding.tvBleDisconnect2.setOnClickListener {
            dispatchBluetoothAddress(2, null)
        }
        mBinding.tvBleDisconnect3.setOnClickListener {
            dispatchBluetoothAddress(3, null)
        }
    }

    private fun addBluetoothAddress(index: Int) {
        val dialog = BleBluetoothChooseDialog().setOnBlueChooseListener {
            dispatchBluetoothAddress(index, it)
        }
        bmiParam.getBluetoothList().forEach {
            if (it.blueAddress.isNullOrEmpty().not()) {
                dialog.addFilterDevice(it.blueAddress)
            }
        }
        dialog.show(supportFragmentManager, "choose bluetooth")
    }

    private fun dispatchBluetoothAddress(index: Int, address: String?) {
        when (index) {
            1 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle1.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle1.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect1.gone()
                    bmiParam.removeBluetoothInfo(1)
                } else {
                    mBinding.tvBle1.text = address
                    mBinding.tvBle1.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect1.visible()
                    bmiParam.addBluetoothInfo(1, address)
                }

            }

            2 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle2.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle2.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect2.gone()
                    bmiParam.removeBluetoothInfo(2)
                } else {
                    mBinding.tvBle2.text = address
                    mBinding.tvBle2.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect2.visible()
                    bmiParam.addBluetoothInfo(2, address)
                }
            }

            3 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle3.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle3.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect3.gone()
                    bmiParam.removeBluetoothInfo(3)
                } else {
                    mBinding.tvBle3.text = address
                    mBinding.tvBle3.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect3.visible()
                    bmiParam.addBluetoothInfo(3, address)
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MultiCoreConfig.updateBmiTestParam(bmiParam)
    }

}