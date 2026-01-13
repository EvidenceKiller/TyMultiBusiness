package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.databinding.ActivityBluetoothSportBinding
import com.ty.tybusbase.base.BaseExitActivity
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

            }

            override fun onNextClick() {
            }

            override fun onEndTestClick() {
                showExitDialog()
            }

            override fun onStartClick() {
                viewModel.startSport()
                //添加结果页面
                mBinding.blueSportView.addResultView(viewModel.factory.getResultView())
            }
        }

    }
}