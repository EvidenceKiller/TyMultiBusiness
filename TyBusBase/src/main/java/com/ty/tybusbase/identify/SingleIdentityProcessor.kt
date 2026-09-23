package com.ty.tybusbase.identify

import android.graphics.Rect
import com.adl.base.overlay.SportOverlay
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.pose.AdlPoseSkeleton
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
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class SingleIdentityProcessor(val customIdentifyRect: ICustomIdentifyRect = DefaultCustomRect()) :
    AdlCameraFrameProcessor {
    private val mFrameProcessors by lazy { ArrayList<AdlCameraFrameProcessor>() }
    private val yuvData by lazy { TsYuvFrame() }
    private val tsConfig by lazy { SportConfigSingle() }
    protected lateinit var processorSingle: SportProcessorSingle
    protected lateinit var tsSportLifecycle: TsSportLifecycle<*>
    protected val singleTracker by lazy { SingleTracker() }
    private var mIdentityListener: IdentifyMachineListener? = null
    private var startGestureDetection: IStartGestureDetection? = null
    private var startGestureDetectCallback: Action? = null

    //人脸识别是否OK
    private var identityOk = false

    //是否镜像，默认是镜像写法
    var isMirror = false
        set(value) {
            field = value
            calculateHandUpType(handUpType)
        }

    //举手的回调
    private var handUpRaiseCallback: Action1<Int>? = null

    //0:右手，1：左手：2：左右手皆可 3:不检测举手
    var handUpType = HAND_UP_TYPE_LEFT
        set(value) {
            field = calculateHandUpType(value)
        }

    // 定义全局变量，默认为null表示未确定
    var handRaised = HAND_UP_TYPE_LEFT
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
        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = true
        tsConfig.drawSingleBody = isDebug
        this.overlay = overlay
        val tsSportLifecycleSingle = object : TsSportLifecycleSingle(null) {
            override fun onIdAuth(frame: TsSportFrameSingle) {
                frame.skeleton?.let {
                    if (judgeHandUp(it)) {
                        identityEngine.identify(
                            customIdentifyRect.calcRect(
                                frame.skeleton,
                                yuvData.width,
                                yuvData.height
                            ), yuvData.yuv, yuvData.width, yuvData.height
                        )
                    }
                    if (identityOk && startGestureDetection != null && startGestureDetection?.startGestureDetection(
                            it,
                            handRaised
                        ) == true
                    ) {
                        startGestureDetectCallback?.invoke()
                    }
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
                handleIdentityOk(accountInfo)
            }

            override fun onIdentityTimeFail() {
                mIdentityListener?.onIdentityTimeFail()
            }

        })
        setIdentityRound(1)
    }

    fun resetSportRegion(sportRegion: TsSportRegion) {
        tsConfig.sportRegion = sportRegion
    }

    fun handleIdentityOk(accountInfo: AccountInfo?) {
        if (identityOk) return
        identityOk = true
        if (isMirror) {
            handRaised = if (handRaised == HAND_UP_TYPE_RIGHT) {
                HAND_UP_TYPE_LEFT
            } else {
                HAND_UP_TYPE_RIGHT
            }
        }
        handUpRaiseCallback?.invoke(handRaised)
        mIdentityListener?.onIdentityOk(accountInfo)
    }

    // 抽离计算逻辑
    private fun calculateHandUpType(value: Int): Int {
        return if (isMirror) {
            when (value) {
                HAND_UP_TYPE_LEFT -> HAND_UP_TYPE_RIGHT
                HAND_UP_TYPE_RIGHT -> HAND_UP_TYPE_LEFT
                else -> value
            }
        } else {
            value
        }
    }

    fun setIdentityListener(listener: IdentifyMachineListener) {
        mIdentityListener = listener
    }

    fun setHandUpRaiseCallback(callback: Action1<Int>) {
        handUpRaiseCallback = callback
    }

    fun setStartGestureDetection(startGestureDetection: IStartGestureDetection) {
        this.startGestureDetection = startGestureDetection
    }

    fun setStartGestureDetectCallback(callback: Action) {
        startGestureDetectCallback = callback
    }

    private fun judgeHandUp(skeleton: AdlPoseSkeleton?): Boolean {
        if (skeleton == null) return false
        return when (handUpType) {
            0 -> {
                // 判断左手
                if (skeleton.lWrist == null || skeleton.lShoulder == null) return false
                val isLeftHandUp = skeleton.lWrist.y < skeleton.lShoulder.y
                if (isLeftHandUp) {
                    handRaised = HAND_UP_TYPE_LEFT
                }
                isLeftHandUp
            }

            1 -> {
                // 判断右手
                if (skeleton.rWrist == null || skeleton.rShoulder == null) return false
                val isRightHandUp = skeleton.rWrist.y < skeleton.rShoulder.y
                if (isRightHandUp) {
                    handRaised = HAND_UP_TYPE_RIGHT
                }
                isRightHandUp
            }

            2 -> {
                // 判断左右手皆可
                val leftHandUp = if (skeleton.lWrist != null && skeleton.lShoulder != null) {
                    skeleton.lWrist.y < skeleton.lShoulder.y
                } else {
                    false
                }
                val rightHandUp = if (skeleton.rWrist != null && skeleton.rShoulder != null) {
                    skeleton.rWrist.y < skeleton.rShoulder.y
                } else {
                    false
                }
                val anyHandUp = leftHandUp || rightHandUp
                if (leftHandUp) {
                    handRaised = HAND_UP_TYPE_LEFT
                } else if (rightHandUp) {
                    handRaised = HAND_UP_TYPE_RIGHT
                }
                anyHandUp
            }

            else -> {
                false
            }
        }
    }

    fun start() {
        identityOk = false
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

    fun reset() {
        identityOk = false
        identityEngine.reset()
        startGestureDetection?.reset()
    }

    fun clearCount() {
        identityEngine.clearCountMap()
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

    interface ICustomIdentifyRect {
        fun calcRect(skeleton: AdlPoseSkeleton?, width: Int, height: Int): Rect
    }

    class DefaultCustomRect() : ICustomIdentifyRect {
        override fun calcRect(skeleton: AdlPoseSkeleton?, width: Int, height: Int): Rect {
            if (skeleton == null) {
                LogUtil.d("skeleton.bodyrect  is Null null null null ------", true)
                return Rect(0, 0, 50, 50)
            }
            var rect = Rect(0, 0, 50, 50)
            if (skeleton.LShoulder != null && skeleton.RShoulder != null) {
                val shoulderWidth = abs(skeleton.LShoulder.x - skeleton.RShoulder.x)
                rect.bottom = max(skeleton.LShoulder.y, skeleton.RShoulder.y).toInt()
                rect.left =
                    max(
                        min(
                            skeleton.LShoulder.x,
                            skeleton.RShoulder.x
                        ).toInt(), 0
                    )
                rect.right = min(rect.left + shoulderWidth.toInt(), width)
                rect.top = if (skeleton.headTop != null) {
                    skeleton.headTop.y.toInt()
                } else {
                    max((rect.bottom - 1.8 * shoulderWidth).toInt(), 0)
                }
            }

            //框体纠正
            if (rect.width() < 40 || rect.height() < 40) {
                rect.left = max(rect.left - 20, 0)
                rect.right = min(rect.left + 40, width)
                rect.top = max(rect.top - 20, 0)
                rect.bottom = min(rect.top + 40, height)
            }
            return rect
        }

    }

    companion object {
        const val HAND_UP_TYPE_LEFT = 0
        const val HAND_UP_TYPE_RIGHT = 1
        const val HAND_UP_TYPE_BOTH = 2
        const val HAND_UP_TYPE_NONE = 3
    }
}