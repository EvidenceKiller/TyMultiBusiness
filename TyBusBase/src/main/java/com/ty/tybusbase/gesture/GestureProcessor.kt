package com.ty.tybusbase.gesture

import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.overlay.OnRenderScene
import com.adl.base.overlay.SportOverlayRopeSkip
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.impl.TsSportLifecycleSingle
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigSingle
import com.adl.processor.SportModelType
import com.adl.processor.SportProcessorSingle
import com.adl.processor.SportProcessorStatus
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.utils.DeviceUtil

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

    private val gestureListeners by lazy { mutableListOf<GestureListener>() }

    private var overlayVessel: SportOverlayRopeSkip? = null


    private lateinit var processorSingle: SportProcessorSingle

    private lateinit var tsSportLifecycle: TsSportLifecycle<*>

    //手势标识，为true时才检测手势
    private var gestureFlag = true

    private val tsConfig by lazy { SportConfigSingle() }

    private val gestureHelper by lazy {
        SkeletonGestureHelper2(object : GestureListener {
            override fun onGesture(command: GestureCommand, type: GestureType) {
                gestureListeners.forEach {
                    it.onGesture(command, type)
                }
            }
        })
    }

    fun initEngine(overlayVessel: SportOverlayRopeSkip) {
        this.overlayVessel = overlayVessel
        initSportEngine()
    }

    fun addGestureListener(listener: GestureListener) {
        if (gestureListeners.contains(listener).not()) {
            gestureListeners.add(listener)
        }
    }

    fun removeGestureListener(listener: GestureListener) {
        gestureListeners.remove(listener)
    }

    override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
        if (gestureListeners.size > 0 && gestureFlag) {
            processorSingle.onFrame(p0, p1)
        }
    }

    private fun initSportEngine() {
        val isDebug = true
        overlayVessel?.enableDrawSkeleton(isDebug)
        overlayVessel?.enableDrawBox(isDebug)
        overlayVessel?.enableDraw(isDebug)
        overlayVessel?.enableDebug(isDebug)
        overlayVessel?.enableTouch(false)
        // 运动配置

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
                frame?.skeleton?.let { gestureHelper.updateSkeleton(it) }
            }
        }).apply { tsSportLifecycle = this }

        processorSingle = SportProcessorSingle().apply {
            setSportLifecycle(tsSportLifecycleSingle)
            setConfig(tsConfig)
//            setBodyTracker(singleTracker)
            setPoseType(if (DeviceUtil.isTsRk3588()) SportModelType.TS else SportModelType.KunL)
            setSmooth(false)
            setAutoDeleteFrame(false)
        }
        processorSingle.open(overlayVessel)
        processorSingle.changeTo(SportProcessorStatus.Sporting)
        processorSingle.onInit()
        val uiConfig = AdlUIConfig.instance().readConfig(
            GESTURE_REGION_CONFIG_KEY,
            RoleConfigRopeSkip::class.java
        )
        overlayVessel?.setConfig(uiConfig,OnRenderScene.StatusSportSet)
        overlayVessel?.setListener {
            tsSportLifecycle.toSporting()
            tsConfig.sportRegion = TsSportRegion(overlayVessel?.sportRegion?.previewPoints)
        }
    }

    fun reloadRegion(){
        val uiConfig = AdlUIConfig.instance().readConfig(
            GESTURE_REGION_CONFIG_KEY,
            RoleConfigRopeSkip::class.java
        )
        overlayVessel?.setConfig(uiConfig,OnRenderScene.StatusSportSet)
        overlayVessel?.refresh()
        tsConfig.sportRegion = TsSportRegion(overlayVessel?.sportRegion?.previewPoints)
    }

    /**
     * 暂停检测手势，运动时，可节约性能
     */
    fun pause(){
        gestureFlag = false
    }

    /**
     * 恢复检测手势
     */
    fun restart(){
        gestureFlag = true
    }

    fun release() {
        gestureFlag = false
        tsSportLifecycle.toIdAuth()
        tsSportLifecycle.onRelease()
        processorSingle.changeTo(SportProcessorStatus.Idle)
        processorSingle.onRelease()
        gestureListeners.clear()
    }

}