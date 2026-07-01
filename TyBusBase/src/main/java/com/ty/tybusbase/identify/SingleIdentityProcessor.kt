package com.ty.tybusbase.identify

import com.adl.base.overlay.SportOverlay
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.impl.TsSportLifecycleSingle
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.mount.sport.TsYuvFrame
import com.adl.processor.SportConfigSingle
import com.adl.processor.SportModelType
import com.adl.processor.SportProcessorSingle
import com.adl.processor.SportProcessorStatus
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.FaceIdentifyConfig
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.DeviceUtil

class SingleIdentityProcessor : AdlCameraFrameProcessor {
    private val mFrameProcessors by lazy { ArrayList<AdlCameraFrameProcessor>() }
    private val yuvData by lazy { TsYuvFrame() }
    protected lateinit var processorSingle: SportProcessorSingle
    protected lateinit var tsSportLifecycle: TsSportLifecycle<*>
    protected val singleTracker by lazy { SingleTracker() }
    private var mIdentityListener: IdentifyMachineListener? = null
    private var overlay: SportOverlay? = null
    private val identityEngine by lazy {
        MultiIdentityEngine()
    }
    private var isStarting = false
    var enableSmooth = true
        set(value) {
            singleTracker.setEnableSmooth(value)
            processorSingle.setSmooth(value)
            field = value
        }

    override fun onFrame(yuv: ByteArray, info: AdlCameraPreviewInfo) {
        mFrameProcessors.forEach {
            it.onFrame(yuv, info)
        }
        yuvData.copy(yuv, info.width, info.height, info.frameTimestamp)
        if (isStarting) {
            processorSingle.onFrame(yuv, info)
        }
    }

    fun initSportEngine(overlay: SportOverlay, sportRegion: TsSportRegion) {
        val isDebug: Boolean = BusiBaseConfig.getDebugEnable()
        // 运动配置
        val tsConfig = SportConfigSingle()
        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = true
        tsConfig.drawSingleBody = isDebug
        this.overlay = overlay
        val tsSportLifecycleSingle = object : TsSportLifecycleSingle(null) {
            override fun onIdAuth(frame: TsSportFrameSingle) {
                frame.skeleton?.let {
                    identityEngine.identify(
                        frame.skeleton,
                        yuvData.yuv,
                        yuvData.width,
                        yuvData.height
                    )
                }
            }
        }.apply { tsSportLifecycle = this }

        processorSingle = SportProcessorSingle().apply {
            setSportLifecycle(tsSportLifecycleSingle)
            setConfig(tsConfig)
            setBodyTracker(singleTracker)
            setPoseType(if (DeviceUtil.isTsRk3588()) SportModelType.TS else SportModelType.KunL)
            setAutoDeleteFrame(false)
        }
        processorSingle.open(overlay)
        processorSingle.onInit()
        tsConfig.sportRegion = sportRegion

        identityEngine.openMachine(object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {
                mIdentityListener?.onIdentityMachineInit(initResult)
            }

            override fun onIdentityTimeOn() {
                mIdentityListener?.onIdentityTimeOn()
            }

            override fun onIdentityOk(accountInfo: AccountInfo?) {
                mIdentityListener?.onIdentityOk(accountInfo)
            }

            override fun onIdentityTimeFail() {
                mIdentityListener?.onIdentityTimeFail()
            }

        })
        setIdentityRound(1)
    }

    fun setIdentityListener(listener: IdentifyMachineListener) {
        mIdentityListener = listener
    }

    fun start() {
        isStarting = true
        overlay?.visible()
        tsSportLifecycle.toIdAuth()
        processorSingle.changeTo(SportProcessorStatus.Sporting)
    }

    fun pause() {
        isStarting = false
        overlay?.clearScreen()
        overlay?.gone()
        tsSportLifecycle.toIdle()
        processorSingle.changeTo(SportProcessorStatus.Idle)
    }

    fun release() {
        isStarting = false
        tsSportLifecycle.toIdle()
        tsSportLifecycle.onRelease()

        processorSingle.changeTo(SportProcessorStatus.Idle)
        processorSingle.onRelease()
    }

    fun enableVisitor(enable: Boolean) {
        identityEngine.visitorAble = enable
    }

    fun resetIdentityEngine() {
        identityEngine.reset()
    }

    fun setIdentityRound(round: Int) {
        identityEngine.addIdentityConfig(
            0,
            FaceIdentifyConfig(round, BusiBaseConstantsValue.DEFAULT_SINGLE_FACE_THRESHOLD)
        )
    }

}