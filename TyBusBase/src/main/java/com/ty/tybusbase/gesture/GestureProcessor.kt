package com.ty.tybusbase.gesture

import android.os.Handler
import android.os.Looper
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.overlay.OnRenderScene
import com.adl.base.overlay.SportOverlayRopeSkip
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.impl.TsSportLifecycleSingle
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigSingle
import com.adl.processor.SportModelType
import com.adl.processor.SportProcessorSingle
import com.adl.processor.SportProcessorStatus
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.LogUtil

class GestureProcessor private constructor() : AdlCameraFrameProcessor {

    companion object {
        const val GESTURE_REGION_CONFIG_KEY = "gesture_region_config_key"
        private var instance: GestureProcessor? = null

        fun getInstance(): GestureProcessor {
            if (instance == null) {
                synchronized(GestureProcessor::class.java) {
                    if (instance == null) {
                        instance = GestureProcessor()
                    }
                }
            }
            return instance!!
        }
    }

    private val singleTracker = SingleTracker()

    private val gestureListeners by lazy { mutableListOf<GestureListener>() }

    private var noGestureListener: Action? = null

    //无手势的判定时间
    private var noGestureCheckTime = 1 * 60 * 1000L

    private val mHandler = Handler(Looper.getMainLooper())

    private val noGestureRunnable = object : Runnable {
        override fun run() {
            noGestureListener?.invoke()
        }

    }

    private lateinit var processorSingle: SportProcessorSingle

    private lateinit var tsSportLifecycle: TsSportLifecycle<*>

    private var hkipcCamera: HKIPCCamera? = null

    //手势标识，为true时才检测手势
    private var gestureFlag = true

    //骨骼回调
    private var skeletonCallback: Action1<AdlPoseSkeleton>? = null

    private val tsConfig by lazy { SportConfigSingle() }

    private val gestureHelper by lazy {
        SkeletonGestureHelper3(object : GestureListener {
            override fun onGesture(command: GestureCommand, type: GestureType) {
                gestureCallback(command, type)
            }
        })
    }

    @Synchronized
    private fun gestureCallback(command: GestureCommand, type: GestureType) {
        if (gestureFlag.not()) return
        LogUtil.d("gestureCallback: $command, $type")
        gestureListeners.forEach {
            it.onGesture(command, type)
        }
        //无手势提供回调
        mHandler.removeCallbacks(noGestureRunnable)
        mHandler.postDelayed(noGestureRunnable, noGestureCheckTime)
    }

    fun setNoGestureListener(listener: Action) {
        noGestureListener = listener
    }

    fun setNoGestureCheckTime(time: Long) {
        noGestureCheckTime = time
    }

    fun setSkeletonCallback(callback: Action1<AdlPoseSkeleton>?) {
        skeletonCallback = callback
    }

    fun addGestureListener(listener: GestureListener) {
        if (gestureListeners.contains(listener).not()) {
            gestureListeners.add(listener)
        }
    }

    fun removeGestureListener(listener: GestureListener) {
        gestureListeners.remove(listener)
    }

    @Synchronized
    override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
        if (gestureListeners.isNotEmpty() && gestureFlag) {
            processorSingle.onFrame(p0, p1)
        }
    }

    fun bindHKIPCamera(camera: HKIPCCamera) {
        hkipcCamera = camera
    }

    fun initSportEngine(region: TsSportRegion) {
        val isDebug = BusiBaseConfig.getDebugEnable()
        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = isDebug
        tsConfig.drawSingleBody = false

        val tsSportLifecycleSingle = TsSportLifecycleSingle(object : TsSingleProcessor {
            override fun onInit() {

            }

            override fun onReset() {

            }

            override fun onRelease() {

            }

            override fun version(): String {
                return ""
            }

            override fun onAnalyzer(frame: TsSportFrameSingle?) {
                frame?.skeleton?.let {
                    gestureHelper.updateSkeleton(it)
                    skeletonCallback?.invoke(it)
                }
            }
        }).apply { tsSportLifecycle = this }

        processorSingle = SportProcessorSingle().apply {
            setSportLifecycle(tsSportLifecycleSingle)
            setConfig(tsConfig)
            setBodyTracker(singleTracker)
            setPoseType(if (DeviceUtil.isTsRk3588()) SportModelType.TS else SportModelType.KunL)
            setSmooth(false)
            setAutoDeleteFrame(false)
        }
        processorSingle.open(null)
        processorSingle.changeTo(SportProcessorStatus.Sporting)
        processorSingle.onInit()
        tsSportLifecycle.toSporting()
        tsConfig.sportRegion = region
        singleTracker.setInitPointF(
            tsConfig.sportRegion.centerX().toFloat(),
            tsConfig.sportRegion.centerY().toFloat()
        )
    }

    fun refreshDebug() {
        val isDebug = BusiBaseConfig.getDebugEnable()
        tsConfig.debug = isDebug
    }

    fun reloadRegion(region: TsSportRegion) {
        tsConfig.sportRegion = region
    }

    /**
     * 暂停检测手势，运动时，可节约性能
     */
    @Synchronized
    fun pause() {
        hkipcCamera?.pause()
        LogUtil.d("GestureProcessor pause")
        gestureFlag = false
        mHandler.removeCallbacks(noGestureRunnable)
    }

    /**
     * 恢复检测手势
     */
    @Synchronized
    fun restart() {
        LogUtil.d("GestureProcessor restart")
        hkipcCamera?.restart()
        mHandler.postDelayed(noGestureRunnable, noGestureCheckTime)
        gestureFlag = true
    }

    fun release() {
        gestureFlag = false
        tsSportLifecycle.toIdAuth()
        tsSportLifecycle.onRelease()
        processorSingle.changeTo(SportProcessorStatus.Idle)
        mHandler.removeCallbacksAndMessages(null)
        processorSingle.onRelease()
        gestureListeners.clear()
        hkipcCamera = null
    }

}