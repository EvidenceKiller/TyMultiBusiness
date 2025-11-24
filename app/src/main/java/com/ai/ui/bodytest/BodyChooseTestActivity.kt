package com.ai.ui.bodytest

import android.os.Bundle
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.impl.TsSportLifecycleSingle
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigSingle
import com.adl.processor.SportModelType
import com.adl.processor.SportProcessorSingle
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.demo.databinding.ActivityBodyChooseTestBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.MMKVUtil

class BodyChooseTestActivity : BaseActivity<ActivityBodyChooseTestBinding, BaseViewModel>() {

    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/localVideo/0401010001_1763692041699.mp4"
    protected lateinit var tsSportLifecycle: TsSportLifecycle<*>
    private val singleTracker by lazy { PullUpTracker() }

    protected lateinit var processorSingle: SportProcessorSingle
    override fun initObserver() {
    }

    override fun initData() {
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.bntStart.setOnClickListener {
            processorSingle.changeTo(SportProcessorStatus.Sporting)
            videoReader?.onStart()
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
        initOverlay()
        initSportEngine()
        initVideo()
    }

    private fun initOverlay() {
        val drawSkeleton = true
        val isDebug: Boolean = true
        mBinding.overlay.enableDrawSkeleton(drawSkeleton)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(isDebug)
    }

    fun initVideo() {
        //processor.changeTo(SportProcessorStatus.Idle);
        videoReader = AdlVideoReader()
        videoReader?.setPlayMode(playMode)
        videoReader?.setSurface(mBinding.surfaceView.holder.surface)
        videoReader?.enableRender(enableRender)
        videoReader?.onReset(videoFilePath)
        videoReader?.setCallback(object : VideoReaderCallback {
            override fun onError(e: Exception?) {
            }

            override fun onFrame(yuv: ByteArray?, width: Int, height: Int, time: Long) {
                frameTime += 30
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
                processorSingle.onFrame(yuv, info)
            }
        })
    }

    fun initSportEngine() {
        val isDebug: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        // 运动配置
        val tsConfig = SportConfigSingle()
        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = true
        tsConfig.drawSingleBody = isDebug

        processorSingle = SportProcessorSingle().apply {
            setSportLifecycle(TsSportLifecycleSingle(null).apply { tsSportLifecycle = this })
            setConfig(tsConfig)
            setBodyTracker(singleTracker.apply {
                singleTracker.changeStatus(SportStatusEnum.SPORT_START)
            })
            setPoseType(if (DeviceUtil.isTsRk3588()) SportModelType.TS else SportModelType.KunL)
            setSmooth(false)
            setAutoDeleteFrame(false)
        }
        processorSingle.open(mBinding.overlay)
        processorSingle.onInit()
        mBinding.overlay.setListener {
            tsConfig.sportRegion = TsSportRegion.defCenterRegion(1920, 1080)
            tsSportLifecycle.toSporting()
            processorSingle.changeTo(SportProcessorStatus.Sporting)
        }
    }
}