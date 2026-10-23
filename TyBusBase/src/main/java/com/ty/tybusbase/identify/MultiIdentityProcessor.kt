package com.ty.tybusbase.identify

import android.graphics.Rect
import android.util.SparseArray
import com.adl.base.overlay.SportOverlay
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.mount.sport.TsSportChannel
import com.adl.mount.sport.TsSportRegion
import com.adl.mount.sport.TsYuvFrame
import com.adl.processor.SportProcessorStatus
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.adl.sport.general.multi.MultiSportChannel
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.adl.sport.general.multi.MultiSportResult
import com.adl.sport.general.multi.MultiSportType
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.Action2
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 多人人脸识别处理类
 *
 * 与 [SingleIdentityProcessor]（单人人脸识别）同构，区别在于：
 * 1. 人体/骨骼检测由 SportProcessorSingle 换成 MultiSportProcessor（多人多道次）。
 * 2. 人脸识别引擎由 MultiIdentityEngine（全局 isOK，一个人成功即锁死全部）
 *    换成 MultiPipelineIdentityEngine（按道次 index 独立投票，互不影响）。
 * 3. 回调由 IdentifyMachineListener 换成带 index 的 [MultiIdentifyMachineListener]。
 * 4. 举手判定、识别状态、超时失败均按道次独立维护。
 *
 * 识别调度沿用线上多人运动的分时复用策略：
 *   同一时刻只有一个道次在送人脸（商汤 SDK 单实例串行），
 *   当前道次识别成功 / 人离开 / 超时后，再轮询下一个"已就位且未识别"的道次。
 *
 * @param sportType   多人运动类型（决定人数与模型 ID）
 * @param parser      该项目对应的成绩解析器（MultiSportProduceFactory.getFactory(...).getParser()）
 * @param channelCount 道次数量，默认取 [MultiSportType.playerNum]
 * @param customIdentifyRect 人脸裁剪框计算策略
 */
open class MultiIdentityProcessor(
    private val sportType: MultiSportType,
    private val parser: MultiSportParser<*>,
    private val channelCount: Int = sportType.playerNum(),
    val customIdentifyRect: ICustomIdentifyRect = DefaultCustomRect()
) : AdlCameraFrameProcessor {

    private val mFrameProcessors by lazy { ArrayList<AdlCameraFrameProcessor>() }

    private val yuvData by lazy { TsYuvFrame() }

    private val multiConfig by lazy { MultiSportConfig() }

    protected lateinit var processorMulti: MultiSportProcessor

    protected lateinit var multiSportParser: MultiSportParser<*>

    private var mIdentityListener: MultiIdentifyMachineListener? = null

    private var startGestureDetection: IStartGestureDetection? = null

    private var startGestureDetectCallback: Action? = null

    /** 全部道次识别完成的回调 */
    private var allIdentifiedCallback: Action? = null

    /** 举手的回调：(道次, 举手类型) */
    private var handUpRaiseCallback: Action2<Int, Int>? = null

    /** 每个道次是否已识别成功 */
    private val identityOkMap by lazy { SparseArray<Boolean>() }

    /** 每个道次识别成功时的举手手型 */
    private val handRaisedMap by lazy { SparseArray<Int>() }

    /** 当前正在识别的道次，-1 表示空闲 */
    private var currentIdentifyNumber = -1

    /** 是否镜像，默认不镜像 */
    var isMirror = false

    /** 0:右手，1：左手：2：左右手皆可 3:不检测举手（多人默认不要求举手） */
    var handUpType = HAND_UP_TYPE_NONE

    private var overlay: SportOverlay? = null

    /**
     * 内部使用：多人流水线人脸识别引擎（按道次独立投票）
     */
    private val identityEngine by lazy { MultiPipelineIdentityEngine() }

    private var isStarting = false

    var enableSmooth = true
        set(value) {
            field = value
            if (::processorMulti.isInitialized) {
                processorMulti.setSmooth(value)
            }
        }

    override fun onFrame(yuv: ByteArray, info: AdlCameraPreviewInfo) {
        mFrameProcessors.forEach {
            it.onFrame(yuv, info)
        }
        yuvData.copy(yuv, info.width, info.height, info.frameTimestamp)
        if (isStarting) {
            processorMulti.onFrame(yuv, info)
        }
    }

    /**
     * 初始化多人运动引擎 + 人脸识别引擎
     *
     * @param overlay 多人区域overlay（推荐传 SportOverlayMultiRegion，
     *                引擎会自动从 overlay 的分区生成道次区域；
     *                若传普通 SportOverlay，需再调用 [resetSportRegion] 手动下发区域）
     */
    fun initSportEngine(overlay: SportOverlay) {
        val isDebug = BusiBaseConfig.getDebugEnable()
        this.overlay = overlay

        multiConfig.debug = isDebug
        multiConfig.drawSkeleton = true
        multiConfig.drawBody = isDebug
        multiConfig.drawAllBody = isDebug
        multiConfig.sportType = sportType
        multiConfig.liteBodyDetect = true
        multiConfig.checkExercisePrescription = false
        multiConfig.loadModel(null)

        multiSportParser = parser

        processorMulti = MultiSportProcessor().apply {
            setConfig(multiConfig)
            setParser(multiSportParser)
            setSmooth(enableSmooth)
            // 人脸裁剪由本类自己按道次分时处理，关闭引擎内部裁剪
            setEnableCropFace(false)
            setCallback(object : MultiSportCallbackImpl() {
                override fun onResult(result: MultiSportResult) {
                    processResult(result.groupManList)
                }

                override fun onError(index: Int, code: Int, error: String?) {
                    LogUtil.d("MultiIdentityProcessor onError: index=$index code=$code msg=$error")
                }
            })
        }
        processorMulti.open(overlay)
        processorMulti.onInit()

        openIdentityEngine()
        setIdentityRound(1)
    }

    /**
     * 初始化多人运动引擎 + 人脸识别引擎，并手动下发道次区域
     *
     * 注意：overlay 为 SportOverlayMultiRegion 时，引擎会在每次 changeTo(Sporting)
     * 时从 overlay 分区重新生成道次，手动下发的区域会被覆盖。
     */
    fun initSportEngine(overlay: SportOverlay, sportRegions: List<TsSportRegion>) {
        initSportEngine(overlay)
        resetSportRegion(sportRegions)
    }

    /**
     * 手动下发道次区域（overlay 非 SportOverlayMultiRegion 时使用）
     */
    fun resetSportRegion(sportRegions: List<TsSportRegion>) {
        val channels = ArrayList<MultiSportChannel>()
        sportRegions.forEachIndexed { index, region ->
            channels.add(
                MultiSportChannel(
                    index,
                    multiConfig.getChannelPoseModel(index),
                    region,
                    region
                )
            )
        }
        multiConfig.channels = channels
    }

    private fun openIdentityEngine() {
        // 与线上多人运动保持一致：每帧都送检
        FaceDetectorHelper.getInstance().setFrameInterval(1)
        identityEngine.openMachine(object : MultiIdentifyMachineListener {

            override fun onIdentityMachineInit(initResult: Boolean) {
                mIdentityListener?.onIdentityMachineInit(initResult)
            }

            override fun onIdentityTimeOn() {
                mIdentityListener?.onIdentityTimeOn()
            }

            override fun onIdentityOk(index: Int, accountInfo: AccountInfo) {
                handleIdentityOk(index, accountInfo)
            }

            override fun onIdentityTimeFail(index: Int) {
                handleIdentifyFail(index)
            }
        })
    }

    /**
     * 每帧多人结果的统一入口
     */
    @Synchronized
    private fun processResult(manList: MutableList<GroupSportBaseMan>?) {
        if (!isStarting || manList.isNullOrEmpty()) {
            handleNoPerson()
            return
        }
        handleStartGesture(manList)
        handleIdentify(manList)
    }

    /**
     * 识别成功后，检测该道次人员是否做出开始手势
     */
    private fun handleStartGesture(manList: List<GroupSportBaseMan>) {
        val detection = startGestureDetection ?: return
        for (man in manList) {
            if (!isIdentified(man.positionNum)) continue
            val skeleton = man.skeleton ?: continue
            if (detection.startGestureDetection(skeleton, handRaisedMap.get(man.positionNum, handUpType))) {
                startGestureDetectCallback?.invoke()
                return
            }
        }
    }

    /**
     * 分时调度各道次的人脸识别
     */
    @Synchronized
    private fun handleIdentify(manList: List<GroupSportBaseMan>) {
        // 全部识别完成，不再送检
        if (identifiedCount() >= channelCount) return

        // 有道次正在识别：人还在就继续送帧，人离开就判失败并让出通道
        if (currentIdentifyNumber >= 0) {
            val man = manList.firstOrNull { it.positionNum == currentIdentifyNumber }
            if (man == null || man.channelStatus != GroupSportsBaseChannelStatus.checkDown) {
                LogUtil.d("多人识别：道次 $currentIdentifyNumber 人员已离开，识别失败")
                handleIdentifyFail(currentIdentifyNumber)
                return
            }
            dispatchIdentify(man)
            return
        }

        // 引擎还在处理上一张人脸，等待
        if (identityEngine.isIdentifying()) return

        // 轮询下一个"已就位且未识别"的道次
        for (index in 0 until channelCount) {
            if (isIdentified(index)) continue
            val man = manList.firstOrNull { it.positionNum == index } ?: continue
            if (man.channelStatus != GroupSportsBaseChannelStatus.checkDown) continue
            currentIdentifyNumber = index
            mIdentityListener?.onIdentityTimeOn()
            LogUtil.d("多人识别：开始识别道次 $index")
            dispatchIdentify(man)
            return
        }
    }

    /**
     * 送一帧人脸数据到识别引擎
     */
    private fun dispatchIdentify(man: GroupSportBaseMan) {
        val index = man.positionNum
        if (handUpType != HAND_UP_TYPE_NONE && !judgeHandUp(index, man.skeleton)) {
            return
        }
        val yuv = yuvData.yuv ?: return
        identityEngine.identify(
            customIdentifyRect.calcRect(man, yuvData.width, yuvData.height, index),
            yuv,
            yuvData.width,
            yuvData.height,
            index
        )
    }

    /**
     * 无人时的状态复位：让出识别通道
     */
    @Synchronized
    private fun handleNoPerson() {
        if (currentIdentifyNumber >= 0) {
            handleIdentifyFail(currentIdentifyNumber)
        }
    }

    /**
     * 某个道次识别成功
     */
    @Synchronized
    fun handleIdentityOk(index: Int, accountInfo: AccountInfo) {
        if (isIdentified(index)) return
        identityOkMap.put(index, true)
        currentIdentifyNumber = -1

        var raised = handRaisedMap.get(index, handUpType)
        if (isMirror) {
            raised = if (raised == HAND_UP_TYPE_RIGHT) HAND_UP_TYPE_LEFT else HAND_UP_TYPE_RIGHT
        }
        handUpRaiseCallback?.invoke(index, raised)
        mIdentityListener?.onIdentityOk(index, accountInfo)

        LogUtil.d("多人识别：道次 $index 识别成功，已识别 ${identifiedCount()}/$channelCount")
        if (identifiedCount() >= channelCount) {
            allIdentifiedCallback?.invoke()
        }
    }

    /**
     * 某个道次识别失败（超时 / 人离开）
     */
    @Synchronized
    fun handleIdentifyFail(index: Int) {
        if (currentIdentifyNumber == index) {
            currentIdentifyNumber = -1
        }
        mIdentityListener?.onIdentityTimeFail(index)
    }

    /**
     * 强制切换到下一个待识别道次（业务层可主动跳过当前道次）
     */
    @Synchronized
    fun skipCurrentChannel() {
        if (currentIdentifyNumber >= 0) {
            handleIdentifyFail(currentIdentifyNumber)
        }
    }

    /**
     * 外部预置已识别人员（赛事模式预先编排人员），该道次不再识别
     */
    @Synchronized
    fun setIdentified(index: Int) {
        identityOkMap.put(index, true)
        if (currentIdentifyNumber == index) {
            currentIdentifyNumber = -1
        }
    }

    /**
     * 判断某个道次是否举手
     */
    private fun judgeHandUp(index: Int, skeleton: AdlPoseSkeleton?): Boolean {
        if (skeleton == null) return false
        return when (handUpType) {
            HAND_UP_TYPE_LEFT -> {
                if (skeleton.LWrist == null || skeleton.LShoulder == null) return false
                val isLeftHandUp = skeleton.LWrist.y < skeleton.LShoulder.y
                if (isLeftHandUp) handRaisedMap.put(index, HAND_UP_TYPE_LEFT)
                isLeftHandUp
            }

            HAND_UP_TYPE_RIGHT -> {
                if (skeleton.RWrist == null || skeleton.RShoulder == null) return false
                val isRightHandUp = skeleton.RWrist.y < skeleton.RShoulder.y
                if (isRightHandUp) handRaisedMap.put(index, HAND_UP_TYPE_RIGHT)
                isRightHandUp
            }

            HAND_UP_TYPE_BOTH -> {
                val leftHandUp = if (skeleton.LWrist != null && skeleton.LShoulder != null) {
                    skeleton.LWrist.y < skeleton.LShoulder.y
                } else {
                    false
                }
                val rightHandUp = if (skeleton.RWrist != null && skeleton.RShoulder != null) {
                    skeleton.RWrist.y < skeleton.RShoulder.y
                } else {
                    false
                }
                if (leftHandUp) {
                    handRaisedMap.put(index, HAND_UP_TYPE_LEFT)
                } else if (rightHandUp) {
                    handRaisedMap.put(index, HAND_UP_TYPE_RIGHT)
                }
                leftHandUp || rightHandUp
            }

            else -> false
        }
    }

    fun setIdentityListener(listener: MultiIdentifyMachineListener) {
        mIdentityListener = listener
    }

    /**
     * 举手回调：(道次, 举手类型)
     */
    fun setHandUpRaiseCallback(callback: Action2<Int, Int>) {
        handUpRaiseCallback = callback
    }

    /**
     * 全部道次识别完成回调
     */
    fun setAllIdentifiedCallback(callback: Action) {
        allIdentifiedCallback = callback
    }

    fun setStartGestureDetection(startGestureDetection: IStartGestureDetection) {
        this.startGestureDetection = startGestureDetection
    }

    fun setStartGestureDetectCallback(callback: Action) {
        startGestureDetectCallback = callback
    }

    /**
     * 添加子帧处理器（录像、副屏推流等）
     */
    fun addFrameProcessor(processor: AdlCameraFrameProcessor) {
        mFrameProcessors.add(processor)
    }

    fun start() {
        identityOkMap.clear()
        handRaisedMap.clear()
        currentIdentifyNumber = -1
        isStarting = true
        overlay?.visible()
        multiSportParser.checkChannelStatus = true
        processorMulti.changeTo(SportProcessorStatus.Sporting)
    }

    fun pause() {
        isStarting = false
        currentIdentifyNumber = -1
        overlay?.clearScreen()
        overlay?.gone()
        multiSportParser.checkChannelStatus = false
        processorMulti.changeTo(SportProcessorStatus.Idle)
    }

    fun reset() {
        identityOkMap.clear()
        handRaisedMap.clear()
        currentIdentifyNumber = -1
        identityEngine.reset()
        startGestureDetection?.reset()
        if (::processorMulti.isInitialized) {
            processorMulti.onReset()
        }
    }

    /**
     * 清空所有道次的识别结果
     */
    fun clearCount() {
        identityOkMap.clear()
        currentIdentifyNumber = -1
        identityEngine.reset()
    }

    /**
     * 清空单个道次的识别结果
     */
    fun clearCount(index: Int) {
        identityOkMap.remove(index)
        if (currentIdentifyNumber == index) {
            currentIdentifyNumber = -1
        }
    }

    fun release() {
        isStarting = false
        currentIdentifyNumber = -1
        if (::processorMulti.isInitialized) {
            processorMulti.changeTo(SportProcessorStatus.Idle)
            processorMulti.onRelease()
        }
        identityEngine.closeMachine()
        mFrameProcessors.clear()
        yuvData.yuv = null
    }

    fun enableVisitor(enable: Boolean) {
        identityEngine.visitorAble = enable
    }

    fun resetIdentityEngine() {
        identityEngine.reset()
    }

    /**
     * 统一设置每个道次的识别轮次
     */
    fun setIdentityRound(round: Int) {
        for (index in 0 until channelCount) {
            setIdentityRound(index, round)
        }
    }

    /**
     * 设置单个道次的识别轮次
     */
    fun setIdentityRound(index: Int, round: Int) {
        identityEngine.addIdentityConfig(
            index,
            PipelineFaceIdentifyConfig(
                visitorRoundCount = round,
                minScoreThreshold = BusiBaseConstantsValue.DEFAULT_MULTI_FACE_THRESHOLD
            )
        )
    }

    /**
     * 设置单个道次的完整识别配置
     */
    fun setIdentityConfig(index: Int, config: PipelineFaceIdentifyConfig) {
        identityEngine.addIdentityConfig(index, config)
    }

    fun setIdentityConfigMap(map: HashMap<Int, PipelineFaceIdentifyConfig>) {
        identityEngine.setIdentityConfigMap(map)
    }

    fun isIdentified(index: Int) = identityOkMap.get(index, false)

    fun identifiedCount() = identityOkMap.size()

    fun isIdentifying() = identityEngine.isIdentifying()

    fun getCurrentIdentifyNumber() = currentIdentifyNumber

    /**
     * 人脸裁剪框计算，按道次回调
     */
    interface ICustomIdentifyRect {
        fun calcRect(channel: TsSportChannel?, width: Int, height: Int, index: Int): Rect
    }

    /**
     * 默认裁剪框：以人框为主，若存在骨骼点则用肩宽+头顶修正，过小则兜底放大
     */
    class DefaultCustomRect : ICustomIdentifyRect {
        override fun calcRect(
            channel: TsSportChannel?,
            width: Int,
            height: Int,
            index: Int
        ): Rect {
            if (channel == null || channel.body == null || channel.skeleton == null) {
                LogUtil.d("channel.body / skeleton is Null ------ index=$index", true)
                return Rect(0, 0, 50, 50)
            }
            val body = channel.body
            val skeleton = channel.skeleton
            var rect = Rect(body.x, body.y, body.right(), body.bottom())
            if (skeleton.LShoulder != null && skeleton.RShoulder != null) {
                val shoulderWidth = abs(skeleton.LShoulder.x - skeleton.RShoulder.x)
                rect.bottom = max(skeleton.LShoulder.y, skeleton.RShoulder.y).toInt()
                rect.left = max(
                    min(skeleton.LShoulder.x, skeleton.RShoulder.x).toInt(),
                    0
                )
                rect.right = min(rect.left + shoulderWidth.toInt(), width)
                rect.top = if (skeleton.headTop != null) {
                    skeleton.headTop.y.toInt()
                } else {
                    max((rect.bottom - 1.8 * shoulderWidth).toInt(), 0)
                }
            }

            // 框体纠正
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
