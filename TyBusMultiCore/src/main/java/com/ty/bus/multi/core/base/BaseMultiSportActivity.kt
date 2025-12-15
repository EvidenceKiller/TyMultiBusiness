package com.ty.bus.multi.core.base

import androidx.viewbinding.ViewBinding
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.R
import com.ty.tybusbase.base.BaseExitActivity
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.helper.TimeOutHelper
import com.ty.tybusbase.utils.ToastUtil


abstract class BaseMultiSportActivity<VB : ViewBinding, VM : BaseMultiSportViewModel> :
    BaseExitActivity<VB, VM>(),
    IMultiSportView {

    protected val timeOutHelper = TimeOutHelper({
        ToastUtil.getInstance(
            applicationContext,
            getString(R.string.identify_time_out)
        ).show()
        finish()
    }, BusiBaseConstantsValue.IDENTITY_TIME_OUT)

    override fun initData() {
        viewModel.sportInfo = intent.getSerializableExtra("sport_info") as? SceneSportInfoEntity
        viewModel.matchId = intent.getStringExtra("match_id")
        viewModel.sceneId = intent.getStringExtra("scene_id")
        viewModel.accountInfoLD.value = intent.getParcelableExtra("account_info")
        timeOutHelper.start()
    }

    override fun initObserver() {
        // 运动状态监听
        viewModel.statusLD.observe(this) { newStatus ->
            when (newStatus) {
                SportStatusEnum.PREPARE -> onStatusPrepare()
                SportStatusEnum.IDENTIFY_FACE -> onStatusIdentify()
                SportStatusEnum.IDENTIFY_FACE_SUCCESS -> onStatusIdentifySuccess()
                SportStatusEnum.SPORT_START -> onStatusSport()
                SportStatusEnum.SPORT_END -> onStatusEnd()
                else -> {}
            }
        }
    }

    override fun onStatusSport() {
        timeOutHelper.cancel()
    }


}