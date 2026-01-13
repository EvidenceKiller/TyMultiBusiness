package com.ty.bus.multi.core.ui.set.bmi

import android.os.Bundle
import android.text.TextUtils
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
        if (!TextUtils.isEmpty(bmiParam.blueAddress1)) {
            dispatchBluetoothAddress(1, bmiParam.blueAddress1)
        }
        if (!TextUtils.isEmpty(bmiParam.blueAddress2)) {
            dispatchBluetoothAddress(2, bmiParam.blueAddress2)
        }
        if (!TextUtils.isEmpty(bmiParam.blueAddress3)) {
            dispatchBluetoothAddress(3, bmiParam.blueAddress3)
        }

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.tvBle1.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(1, it)
            }.addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle2.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(2, it)
            }.addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle3.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(3, it)
            }
                .addFilterDevice(bmiParam.blueAddress1)
                .addFilterDevice(bmiParam.blueAddress2)
                .addFilterDevice(bmiParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
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

    private fun dispatchBluetoothAddress(index: Int, address: String?) {
        when (index) {
            1 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle1.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle1.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect1.gone()
                } else {
                    mBinding.tvBle1.text = address
                    mBinding.tvBle1.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect1.visible()
                }
                bmiParam.blueAddress1 = address
            }

            2 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle2.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle2.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect2.gone()
                } else {
                    mBinding.tvBle2.text = address
                    mBinding.tvBle2.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect2.visible()
                }
                bmiParam.blueAddress2 = address
            }

            3 -> {
                if (address.isNullOrEmpty()) {
                    mBinding.tvBle3.text = getString(R.string.busi_multi_click_add_bluetooth)
                    mBinding.tvBle3.setTextColor(resources.getColor(R.color.white_60))
                    mBinding.tvBleDisconnect3.gone()
                } else {
                    mBinding.tvBle3.text = address
                    mBinding.tvBle3.setTextColor(resources.getColor(android.R.color.white))
                    mBinding.tvBleDisconnect3.visible()
                }
                bmiParam.blueAddress3 = address
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MultiCoreConfig.updateBmiTestParam(bmiParam)
    }

}