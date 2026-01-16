package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.StudentEntity
import com.ty.bus.multi.R
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.BluetoothSportConfig
import com.ty.bus.multi.core.view.bluetooth.BluetoothSportSportView.UserChangeListener
import com.ty.bus.multi.databinding.ActivityBluetoothSportBinding
import com.ty.tybusbase.base.BaseExitActivity
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.launch

class BluetoothSportActivity :
    BaseExitActivity<ActivityBluetoothSportBinding, BluetoothSportViewModel>() {
    override fun initObserver() {
        lifecycleScope.launch {
            viewModel.sportCardState.collect { (index, state) ->
                mBinding.blueSportView.changeState(index, state)
            }
        }

        lifecycleScope.launch {
            viewModel.sportResultState.collect { (index, result) ->
                mBinding.blueSportView.addResult(index, result)
            }
        }

        lifecycleScope.launch {
            viewModel.testFinishFlow.collect {
                if (it) {
                    LogUtil.d("结束测试")
                    mBinding.blueSportView.sportFinish()
                }
            }
        }

        /**
         * 处理蓝牙连接逻辑
         */
        viewModel.handleBluetoothConnect()
    }

    override fun initData() {
        viewModel.sportInfo = intent.getSerializableExtra("sport_info") as? SceneSportInfoEntity
        viewModel.matchId = intent.getStringExtra("match_id")
        viewModel.sceneId = intent.getStringExtra("scene_id")
        viewModel.accountInfoLD.value = intent.getParcelableExtra("account_info")
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.bluetoothSportConfig =
            intent.getSerializableExtra("sport_config") as? BluetoothSportConfig
        if (viewModel.sportInfo == null) {
            ToastUtil.getInstance(this, "运动详情为空").show()
            finish()
        }
        viewModel.initData()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.tvSportName.text = viewModel.sportInfo?.sportSkuName
        mBinding.blueSportView.listener = object : ResultViewBtnClickListener {
            override fun onRetestClick() {
                viewModel.retest()
            }

            override fun onNextClick() {
                showBackBtn()
                viewModel.next()
            }

            override fun onEndTestClick() {
                CommonTipDialog().setContent(getString(R.string.busi_multi_if_end_test))
                    .setCallback {
                        viewModel.toStatusSportEnd()
                    }
                    .show(supportFragmentManager, "ent tip")
            }

            override fun onStartClick() {
                viewModel.startSport()
                hideBackBtn()
                //添加结果页面
                mBinding.blueSportView.addResultView(viewModel.factory.getResultView())
            }
        }

        mBinding.blueSportView.setUserChangeListener(object : UserChangeListener {
            override fun onUserChange(
                index: Int,
                studentEntity: StudentEntity
            ) {
                viewModel.addUser(index, studentEntity)
            }

            override fun onUserDelete(index: Int) {
                viewModel.removeUser(index)
            }

        })

        mBinding.blueSportView.setBluetoothSportConfig(viewModel.bluetoothSportConfig)

    }
}