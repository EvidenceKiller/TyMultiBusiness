package com.ty.tybusbase.base

import androidx.viewbinding.ViewBinding
import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.sport.general.EuBase
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.gesture.GestureProcessor
import com.ty.tybusbase.helper.GlobalTouchHelper

abstract class BaseSportStatusActivity<VB : ViewBinding, VM : BaseSportViewModel> :
    BaseExitActivity<VB, VM>(),
    ISportView {
    override fun initData() {
        viewModel.sceneId = intent.getStringExtra("scene_id")
        viewModel.sportInfo = intent.getSerializableExtra("sport_info") as? SceneSportInfoEntity
    }

    override fun initObserver() {
        // 人脸识别成功
        viewModel.accountInfoLD.observe(this) { v ->
            if (v != null) {
                viewModel.toStatusIdentifySuccess()
            }
        }
        // 运动状态监听
        viewModel.statusLD.observe(this) { newStatus ->
            when (newStatus) {
                SportStatusEnum.PREPARE -> onStatusPrepare()
                SportStatusEnum.IDENTIFY_FACE -> onStatusIdentify()
                SportStatusEnum.IDENTIFY_MAT -> onStatusIdentifyMat()
                SportStatusEnum.IDENTIFY_FACE_SUCCESS -> onStatusIdentifySuccess()
                SportStatusEnum.CHECK_AREA -> {}
                SportStatusEnum.SPORT_COUNTDOWN -> onStatusCountDown()
                SportStatusEnum.SPORT_START -> onStatusSport()
                SportStatusEnum.SPORT_END -> onStatusEnd()
                SportStatusEnum.SPORT_SETTLEMENT -> onStatusSettlement()
                SportStatusEnum.SPORT_RESULT -> onStatusResult()
                else -> {}
            }
        }
    }

    override fun onStatusCountDown() {
    }

    override fun onStatusSport() {
        GlobalTouchHelper.pause()
        GestureProcessor.getInstance().pause()
    }

    override fun onStatusEnd() {
        GlobalTouchHelper.restart()
        GestureProcessor.getInstance().restart()
    }

    override fun onStatusSettlement() {
    }

    override fun onStatusResult() {
    }


    override fun onStatusPrepare() {
    }

    override fun onStatusIdentify() {

    }

    override fun onStatusIdentifyMat() {

    }

    override fun onStatusIdentifySuccess() {

    }

    override fun createEuBase(): TsSingleProcessor {
        return object:TsSingleProcessor{
            override fun onAnalyzer(frame: TsSportFrameSingle?) {
            }

            override fun onInit() {
            }

            override fun onReset() {
            }

            override fun onRelease() {
            }

            override fun version(): String? = ""
        }
    }

    override fun getRegion(): TsSportRegion {
        return TsSportRegion.defCenterRegion(1920,1080)
    }

    override fun initSportEngine() {
    }

    override fun onCheckArea() {
    }
}