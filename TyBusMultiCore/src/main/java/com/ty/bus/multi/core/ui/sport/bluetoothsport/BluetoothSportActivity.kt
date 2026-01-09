package com.ty.bus.multi.core.ui.sport.bluetoothsport

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.databinding.ActivityBluetoothSportBinding
import com.ty.tybusbase.base.BaseExitActivity
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BluetoothSportActivity :
    BaseExitActivity<ActivityBluetoothSportBinding, BluetoothSportViewModel>() {
    override fun initObserver() {
        lifecycleScope.launch {
            viewModel.sportCardState.collectLatest { (index, state) ->
                // 如果更新速度很快，只处理最新的
                mBinding.blueSportView.changeState(index, state)
            }
        }

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
    }
}