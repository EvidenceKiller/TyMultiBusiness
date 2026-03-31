package com.ai.ui.multiface

import android.os.Bundle
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.demo.databinding.ActivityMultiFaceTestBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.utils.MMKVUtil

class MultiFaceTestActivity :
    BaseMultiSportActivity<ActivityMultiFaceTestBinding, TenMultiFaceTestViewModel>() {
    private val number = 10

    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_hhh.mp4"

    override fun initData() {
        super.initData()
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.tenSportConfig = intent.getSerializableExtra("sport_config") as? TenSportConfig
        if (viewModel.sportInfo == null) {
            viewModel.sportInfo = SceneSportInfoEntity().apply {
                sportSkuId = "0201010001"
                sportItemCode = SportConstant.SPORT_SKIP_ROPE
                endValue = "60000"
                testCount = "10"
            }
        }
        viewModel.initEngine(number)
        viewModel.getBestRankTop()
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val isDebug =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        mBinding.overlay.enableDraw(isDebug)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_ABLE,
                BusiBaseConstantsValue.DEFAULT_SKELETON
            )
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number",
            RoleConfigMulti::class.java
        )
        uiConfig.number = number
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        viewModel.combineOverlay(mBinding.overlay)

        mBinding.bntStart.setOnClickListener {
            videoReader?.onStart()
            viewModel.openTsProcess()
        }
        mBinding.btnPause.setOnClickListener {
            videoReader?.onPause()
        }
        mBinding.bntNext.setOnClickListener {
            videoReader?.nextFrame()
        }

        mBinding.bntChange.setOnClickListener {
            playMode = when (playMode) {
                AdlAvReader.PlayMode.Auto -> AdlAvReader.PlayMode.OneFrame
                AdlAvReader.PlayMode.OneFrame -> AdlAvReader.PlayMode.Auto
            }
            videoReader?.setPlayMode(playMode)
            videoReader?.onStart()
        }

        mBinding.bntReset.setOnClickListener {
            videoReader?.onReset()
        }

        initVideo()
    }

    fun initVideo() {
        videoReader = AdlVideoReader()
        videoReader?.setPlayMode(playMode)
        videoReader?.setSurface(mBinding.surfaceView.holder.surface)
        videoReader?.enableRender(enableRender)
        videoReader?.onReset(videoFilePath)
        videoReader?.setCallback(object : VideoReaderCallback {
            override fun onError(e: Exception?) {
            }

            override fun onFrame(yuv: ByteArray, width: Int, height: Int, time: Long) {
                frameTime += 30
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
                viewModel.bsSkipRoseMultiProcessor.onFrame(yuv, info)
            }

            override fun onComplete() {

            }
        })
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.identifiedLD.observe(this) {
            AdlLogger.d("identifiedLD: 道次：${it.first}--人员：${it.second.studentEntity!!.accountName}")
            mBinding.sportView.setUserInfo(it)
        }

        viewModel.changeSportViewStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
        }

        viewModel.changeTimeTextBgLD.observe(this) {
            mBinding.sportView.changeTimeTextBg(it)
        }
    }


    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
    }

    override fun onStatusEnd() {
    }

    fun reset() {
        mBinding.sportView.reset()
        viewModel.reset()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.sportView.release()
        mBinding.overlay.release()
    }
}