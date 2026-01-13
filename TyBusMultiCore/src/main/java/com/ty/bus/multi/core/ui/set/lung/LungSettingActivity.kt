package com.ty.bus.multi.core.ui.set.lung

import android.os.Bundle
import android.text.TextUtils
import com.ty.bus.multi.R
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.dialog.BleBluetoothChooseDialog
import com.ty.bus.multi.core.entity.LungParam
import com.ty.bus.multi.databinding.ActivityLungSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible

class LungSettingActivity : BaseActivity<ActivityLungSettingBinding, BaseViewModel>() {
    private lateinit var lungParam: LungParam

    private var minTestTimes = 1 //最小次数

    private var maxTestTimes = 3 //最大次数
    override fun initObserver() {
    }

    override fun initData() {
        lungParam = MultiCoreConfig.getLungTestParam()
    }

    override fun initView(savedInstanceState: Bundle?) {
        //初始化设置信息
        mBinding.tvTime.text = lungParam.testCount.toString()

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.ivTimeMinus.setOnClickListener {
            if (lungParam.testCount == minTestTimes) {
                return@setOnClickListener
            }
            lungParam.testCount--
            mBinding.tvTime.text = lungParam.testCount.toString()
        }

        mBinding.ivTimeAdd.setOnClickListener {
            if (lungParam.testCount == maxTestTimes) {
                return@setOnClickListener
            }
            lungParam.testCount++
            mBinding.tvTime.text = lungParam.testCount.toString()
        }


        if (!TextUtils.isEmpty(lungParam.blueAddress1)) {
            dispatchBluetoothAddress(1, lungParam.blueAddress1)
        }
        if (!TextUtils.isEmpty(lungParam.blueAddress2)) {
            dispatchBluetoothAddress(2, lungParam.blueAddress2)
        }
        if (!TextUtils.isEmpty(lungParam.blueAddress3)) {
            dispatchBluetoothAddress(3, lungParam.blueAddress3)
        }

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.tvBle1.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(1, it)
            }.addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle2.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(2, it)
            }.addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle3.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                dispatchBluetoothAddress(3, it)
            }
                .addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
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
                lungParam.blueAddress1 = address
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
                lungParam.blueAddress2 = address
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
                lungParam.blueAddress3 = address
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MultiCoreConfig.updateLungTestParam(lungParam)
    }

}