package com.ty.bus.multi.core.ui.set.lung

import android.os.Bundle
import android.text.TextUtils
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.dialog.BleBluetoothChooseDialog
import com.ty.bus.multi.core.entity.LungParam
import com.ty.bus.multi.databinding.ActivityLungSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.utils.MMKVUtil

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
        if (!TextUtils.isEmpty(lungParam.blueAddress1)) {
            mBinding.tvBle1.text = lungParam.blueAddress1
        }
        if (!TextUtils.isEmpty(lungParam.blueAddress2)) {
            mBinding.tvBle2.text = lungParam.blueAddress2
        }
        if (!TextUtils.isEmpty(lungParam.blueAddress3)) {
            mBinding.tvBle3.text = lungParam.blueAddress3
        }
        mBinding.tvTime.text = lungParam.testCount.toString()


        mBinding.llBack.setOnClickListener { finish() }
        mBinding.tvBle1.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle1.text = it
                lungParam.blueAddress1 = it
            }.addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle2.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle2.text = it
                lungParam.blueAddress2 = it
            }.addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }
        mBinding.tvBle3.setOnClickListener {
            BleBluetoothChooseDialog().setOnBlueChooseListener {
                mBinding.tvBle3.text = it
                lungParam.blueAddress3 = it
            }
                .addFilterDevice(lungParam.blueAddress1)
                .addFilterDevice(lungParam.blueAddress2)
                .addFilterDevice(lungParam.blueAddress3)
                .show(supportFragmentManager, "choose bluetooth")
        }

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

    }

    override fun onDestroy() {
        super.onDestroy()
        MultiCoreConfig.updateLungTestParam(lungParam)
    }

}