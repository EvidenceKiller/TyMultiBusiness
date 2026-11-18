package com.ty.bus.multi.core.ui.sport.sitreacheighteen

import android.app.Application
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.viewModelScope
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlYUVUtil
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.StudentEntity
import com.adl.sport.sitandreach.SitAndReachObject
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiPipelineIdentifyViewModel
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.core.ui.sport.thirtysport.CollaborationAreaState
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.expand.eventFlow
import com.ty.tybusbase.expand.getDisplayType
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.toJson
import com.ty.tybusbase.expand.toObject
import com.ty.tybusbase.grpc.CollaborationCmdEnum
import com.ty.tybusbase.grpc.CollaborationStatusEnum
import com.ty.tybusbase.grpc.GrpcAccountInfo
import com.ty.tybusbase.grpc.GrpcCmdCode
import com.ty.tybusbase.grpc.GrpcCollaborationSportInfo
import com.ty.tybusbase.grpc.GrpcCollaborationStatus
import com.ty.tybusbase.grpc.GrpcSportCardStatus
import com.ty.tybusbase.grpc.GrpcSportIdentifiedCount
import com.ty.tybusbase.grpc.GrpcSportResult
import com.ty.tybusbase.grpc.GrpcSportStatus
import com.ty.tybusbase.grpc.GrpcSportStatusEnum
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.identify.facecroprect.FaceCropRectHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

/**
 * 18人坐位体前屈 ViewModel：主屏 + 左副屏 + 右副屏，每屏6人，共18人。
 *
 * 业务逻辑在六人坐位体前屈（SitReachSixViewModel/Activity）的基础上，
 * 协同开赛时机对齐 12 人跳绳（TwelveSportGbViewModel）：
 * - 副屏识别/卡片状态/成绩通过 gRPC 上报，主屏统一渲染三个区域
 * - 主屏本区首人识别后启动 10s 识别倒计时；副屏识别只触发协同开赛检查
 * - 开赛条件：各屏满员，或主屏倒计时结束且已加入副屏均倒计时结束（已加入副屏不得为 0 人）
 * - 手动「立即开始」对齐 readyGo，跳过条件直接发令
 * - 运动结束条件：主屏与所有已加入副屏均结束，才展示合并后的18人成绩结果页
 */
class SitReachEighteenViewModel(application: Application) :
    BaseMultiPipelineIdentifyViewModel(application) {

    companion object {
        private const val TAG = "SitReachEighteen"

        /** loose≥该值(cm) 且与 max 相差过大时，改用 distanceMax，避免脏峰值 */
        private const val LOOSE_HIGH_CM = 57f
        private const val MAX_LOOSE_DIFF_CM = 15f
    }

    private val GRPC_KEY = "SitReachEighteenViewModel" //注册Grpc消息的ID
    private val leftSubScreenIp by lazy { BusiBaseConfig.readLeftSubScreenIp() }
    private val rightSubScreenIp by lazy { BusiBaseConfig.readRightSubScreenIp() }

    var ffSportConfig: ForwardFlexionConfig? = null

    private val previewSize = 2560 to 1440

    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

    /** 人脸裁剪框：与六人坐位体前屈相同 Eyes→Ears→Shoulder；空框则跳过识别 */
    private val faceCropRectHelper = FaceCropRectHelper()

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private var statusMap = mutableMapOf<Int, Boolean>()

    private val param by lazy { MultiCoreConfig.getForwardFlexionParam() }

    val checkMap = HashMap<Int, Boolean>() //记录当前道是否已检录

    //已准备的道次
    val prepareStatusList = mutableListOf<Boolean>()

    //已出成绩的道次
    val resultStatusList = mutableListOf<Boolean>()

    private var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    /** 主屏（中区）已识别的位置和人员信息 */
    val sitReachPersonArray by lazy { SparseArray<MultiSportResult>() }
    val leftPersonArray by lazy { SparseArray<MultiSportResult>() } //左副屏
    val rightPersonArray by lazy { SparseArray<MultiSportResult>() } //右副屏

    var sportManCount = 0 //主屏测试人数

    /** 左/右副屏测垫数量（设置页分区 key）；主屏齐人判断用 */
    var leftSportManCount = 0
    var rightSportManCount = 0

    var sportTimes = 0 //主屏测试次数

    var currentSportTimes = 1 //当前测试次数

    val identifyTimeOut = 10 * 1000L //识别超时时间，最后一个识别完，此时间内无新识别，则进入准备状态

    val sportTimeOut = 10 //运动超时提示时间

    var sportCountDownTimer: CountDownTimer? = null

    var sportCountDown = 0

    /** 识别倒计时时长（秒）：与 12 人一致，仅主屏本区首人识别后启动 */
    val identifyCountDownSeconds = 10

    /** 识别倒计时（"立即开始"按钮倒计时） */
    private var identifyCountDownTimer: CountDownTimer? = null

    /** 识别倒计时是否正在进行 */
    private var identifyCountDownRunning = false

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var isRetest = false //是否为重测

    /** 本轮收尾进行中，防止 handleResult 与超时 finish 重复结束 */
    @Volatile
    private var currentRoundFinishing = false

    /** 本轮开测后该道是否出现过非 END，用于屏蔽算法残留 END */
    private val laneScoreArmed = BooleanArray(7) { false }

    /** 运动中每道最新有效伸距（preferUseDistance 快照），超时无 END 时用其出分 */
    private val lastUseDistanceByLane = arrayOfNulls<Float>(7)

    //---------- 三屏协同 ----------
    //主屏是否已就绪（识别完成，进入准备状态）
    private var mainReady = false

    //主屏识别倒计时是否已结束（对齐 12 人跳绳 mainCountDownEnded）
    private var mainCountDownEnded = false

    //已就绪（倒计时结束）的副屏
    private val subScreenCountDownEnded = mutableSetOf<DisplayType>()

    //当前一轮是否已开始
    private var mainSportStarted = false

    //开始运动指令是否已发送（只发送一次，副屏后续测试次数自行处理）
    private var startCommandSent = false

    //主屏运动是否已结束
    private var mainSportFinished = false

    //已上报运动结束的副屏
    private val subScreenSportFinished = mutableSetOf<DisplayType>()

    //协同已识别人数上报记录：区域 → 已识别人数
    val identifiedCountMap = mutableMapOf<DisplayType, Int>()

    //导航区协同状态：左/右副屏各自的 未连接/设备占用中/正常
    private val collaborationStateMap = mutableMapOf(
        DisplayType.LEFT to CollaborationAreaState.UNCONNECTED,
        DisplayType.RIGHT to CollaborationAreaState.UNCONNECTED
    )
    private val _collaborationStateLD =
        MutableStateFlow<Map<DisplayType, CollaborationAreaState>>(collaborationStateMap.toMap())
    val collaborationStateLD: StateFlow<Map<DisplayType, CollaborationAreaState>> =
        _collaborationStateLD.asStateFlow()
    //---------- 三屏协同 ----------

    //已识别人员（区域 → (道次, 人员信息)）
    private val _identifiedLD = eventFlow<Pair<DisplayType, Pair<Int, AccountInfo>>>()
    val identifiedLD: SharedFlow<Pair<DisplayType, Pair<Int, AccountInfo>>> =
        _identifiedLD.asSharedFlow()

    //开始运动（当前测试次数）
    private val _sportStartLD = eventFlow<Int>()
    val sportStartLD: SharedFlow<Int> = _sportStartLD.asSharedFlow()

    //一次测试完毕
    private val _testOnceFinishLD = eventFlow<Boolean>()
    val testOnceFinishLD: SharedFlow<Boolean> = _testOnceFinishLD.asSharedFlow()

    //运动提示信息（倒计时）
    private val _sportTipsInfoLD = eventFlow<String>()
    val sportTipsInfoLD: SharedFlow<String> = _sportTipsInfoLD.asSharedFlow()

    //单个道次成绩（区域 → (道次, 成绩)）
    private val _sportResultLD = eventFlow<Pair<DisplayType, Pair<Int, MultiSportResult>>>()
    val sportResultLD: SharedFlow<Pair<DisplayType, Pair<Int, MultiSportResult>>> =
        _sportResultLD.asSharedFlow()

    //运动卡片状态（区域 → (道次, 卡片状态)）
    private val _sportCardStateLD =
        eventFlow<Pair<DisplayType, Pair<Int, ForwardFlexionCardState>>>()
    val sportCardStateLD: SharedFlow<Pair<DisplayType, Pair<Int, ForwardFlexionCardState>>> =
        _sportCardStateLD.asSharedFlow()

    //运动是否开始（状态，供区域Tab文案判断）
    private val _sportStartedLD = MutableStateFlow(false)
    val sportStartedLD: StateFlow<Boolean> = _sportStartedLD.asStateFlow()

    //识别倒计时剩余秒数：主屏首人识别后开始；结束后按协同开赛条件决定是否发令
    private val _identifyCountDownLD = eventFlow<Int>()
    val identifyCountDownLD: SharedFlow<Int> = _identifyCountDownLD.asSharedFlow()

    //主屏倒计时已结束，等待其他已加入副屏倒计时结束
    private val _waitOtherAreaLD = eventFlow<Unit>()
    val waitOtherAreaLD: SharedFlow<Unit> = _waitOtherAreaLD.asSharedFlow()

    //已识别人数变化通知（"是否清空已识别结果"提示）
    private val _identifiedCountLD = eventFlow<Unit>()
    val identifiedCountLD: SharedFlow<Unit> = _identifiedCountLD.asSharedFlow()

    //全部结束事件（合并三屏成绩，一次性消息）
    private val _toDoneLD = eventFlow<List<ThirtySportResultItem>>()
    val toDoneLD: SharedFlow<List<ThirtySportResultItem>> = _toDoneLD.asSharedFlow()

    private val recordProcess = object : AdlCameraFrameProcessor {
        override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
            videoRecorder.inputNv21(p0, "")
        }
    }

    fun initData() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        sportTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES,
            MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES_DEFAULT
        )
        leftSportManCount = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_EIGHTEEN_TEST_PEOPLE_NUMBER_LEFT,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        rightSportManCount = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_EIGHTEEN_TEST_PEOPLE_NUMBER_RIGHT,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        for (i in 1..diskNum) {
            checkMap[i] = false
            prepareStatusList.add(false)
            resultStatusList.add(false)
        }
        sportManCount = diskNum
        initFaceIdentify()
        sportState = MultiSportState.IDENTIFYING
    }

    override fun initFaceIdentify() {
        super.initFaceIdentify()
        multiIdentityEngine.addIdentityConfig(0, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(1, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(2, PipelineFaceIdentifyConfig(2, 0.6f))
        multiIdentityEngine.addIdentityConfig(3, PipelineFaceIdentifyConfig(2, 0.6f))
        multiIdentityEngine.addIdentityConfig(4, PipelineFaceIdentifyConfig(3))
        multiIdentityEngine.addIdentityConfig(5, PipelineFaceIdentifyConfig(3))
    }

    override fun identifyOk(index: Int, accountInfo: AccountInfo) {
        // 识别/准备阶段都接纳；首人成功后会进 PREPARING，后续人仍需能识别
        if (sportState != MultiSportState.IDENTIFYING && sportState != MultiSportState.PREPARING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $index, state=$sportState")
            return
        }
        if (accountInfo.studentEntity == null) {
            LogUtil.d("识别到人员为空")
            return
        }
        SpeechWrapper.speech(
            getString(
                R.string.identified_someone,
                accountInfo.studentEntity?.accountName ?: ""
            )
        )
        //取消超时任务
        timeOutHelper.cancel()
        viewModelScope.launch {
            sportState = MultiSportState.PREPARING
            //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
            if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                var rmKey = -1
                sitReachPersonArray.forEach { key, value ->
                    if (value.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity!!.accountId) {
                        LogUtil.d("已识别过${value.accountInfo.studentEntity!!.accountName}")
                        rmKey = key
                        changeSportCardState(
                            DisplayType.MAIN,
                            value.number,
                            ForwardFlexionCardState.IDLE
                        )
                        checkMap[value.number] = false
                    }
                }
                if (rmKey != -1) {
                    sitReachPersonArray.remove(rmKey)
                }
            }
            val checkResult = withContext(Dispatchers.IO) {
                checkStudent(accountInfo.studentEntity!!.accountId, ptPlanId)
            }
            if (!checkResult) {
                multiIdentityEngine.clearIdentified(index)
                currentIdentifyNumber = -1
                ToastUtil.getInstance(
                    getApplication(),
                    getString(
                        R.string.busi_base_name_without_plan,
                        accountInfo.studentEntity!!.accountName
                    )
                ).show()
                return@launch
            }
            //存已识别的人信息
            sitReachPersonArray.put(index, MultiSportResult(accountInfo, index, -1000))
            _identifiedLD.tryEmit(DisplayType.MAIN to Pair(index, accountInfo))
            checkMap[index] = true
            changeSportCardState(DisplayType.MAIN, index, ForwardFlexionCardState.IDENTIFIED)
            // 放开独占道次，由 handleIdentify 按举手再选下一道
            currentIdentifyNumber = -1

            LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $index")
            //对齐 12 人：仅主屏本区首人启动识别倒计时
            if (sitReachPersonArray.size() == 1) {
                startIdentifyCountDown()
            }
            //满员则检查协同开赛；否则继续识别，倒计时结束后再统一检查
            if (sitReachPersonArray.size() == sportManCount) {
                checkCollaborationSportStart()
                toStatusPrepare()
            } else if (sitReachPersonArray.size() == 1) {
                mHandler.postDelayed({
                    //识别倒计时已开始运动，不再进入准备状态（避免打断运动）
                    if (sportState != MultiSportState.SPORTING) {
                        toStatusPrepare()
                    }
                }, identifyTimeOut)
            }
        }
    }

    override fun identifyFail(index: Int) {
        // Pipeline 每帧未成功都会回调，勿切道/勿打断当前识别
        LogUtil.d("$TAG::identifyFail: lane=$index (ignore frame progress)")
    }

    private fun calcNextNumber(index: Int) {
        LogUtil.d("当前识别失败道次：$index")
        var findOk = false
        var tempNumber = index % sportManCount
        while (tempNumber < sportManCount) {
            //找到下一道，有人举手且还没有识别的位置
            if (statusMap[tempNumber] == true && sitReachPersonArray.get(tempNumber, null) == null) {
                currentIdentifyNumber = tempNumber
                changeSportCardState(
                    DisplayType.MAIN,
                    currentIdentifyNumber,
                    ForwardFlexionCardState.IDENTIFYING
                )
                findOk = true
                break
            }
            tempNumber++
        }
        LogUtil.d("下一道识别道次：$currentIdentifyNumber")
        //没有找到下一道，置为-1，再从头开始识别
        if (!findOk) {
            currentIdentifyNumber = -1
        }
    }

    private fun changeSportCardState(
        displayType: DisplayType,
        index: Int,
        state: ForwardFlexionCardState
    ) {
        uiHandler.post {
            _sportCardStateLD.tryEmit(displayType to Pair(index, state))
        }
    }

    fun onPostureStatus(map: MutableList<SitAndReachObject>) {
        if (statusLD.value != SportStatusEnum.PREPARE) return
        viewModelScope.launch(Dispatchers.IO) {
            handlePrepareStatus(map)
        }
    }

    fun startRecord(cameraDispatcher: DynamicSportFrameDispatcher) {
        getVideoPath(sportInfo!!.sportSkuId)
        if (videoRecorder.isStarting) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            videoRecorder.onStart(videoFile, previewSize.first, previewSize.second)
            cameraDispatcher.addFrameProcessor(recordProcess)
        }
    }

    fun stopRecord(cameraDispatcher: DynamicSportFrameDispatcher) {
        videoRecorder.onStop()
        cameraDispatcher.removeFrameProcessor(recordProcess)
    }

    /**
     * 处理姿态状态：统一检查是否可以开赛
     */
    @Synchronized
    private fun handlePrepareStatus(map: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.COUNTDOWN) return
        checkCollaborationSportStart()
    }

    /**
     * 协同识别状态变化时统一调用（对齐 12 人）：满足开赛条件则开始主屏运动并通知副屏。
     * 识别倒计时仅由主屏首人识别触发，不在此处拉起。
     */
    private fun checkCollaborationSportStart() {
        val canStart = canStartCollaborationSport()
        LogUtil.d(
            "$TAG::checkCollaborationSportStart: canStart=$canStart, " +
                "main=${sitReachPersonArray.size()}, left=${leftPersonArray.size()}, " +
                "right=${rightPersonArray.size()}, mainCountDownEnded=$mainCountDownEnded, " +
                "subEnded=$subScreenCountDownEnded, collab=$collaborationStateMap"
        )
        if (!canStart) {
            return
        }
        startSport()
    }

    private fun startSport() {
        if (!canStartCollaborationSport()) return
        stopIdentifyCountDown()
        tryStartSport()
        sendSportCommand()
    }

    /**
     * 协同运动开始条件（对齐 12 人跳绳 canStartCollaborationSport）：
     * - 主屏至少 1 人识别成功
     * - 已加入（NORMAL）的副屏不得为 0 人
     * - 条件1：主屏+已连接副屏均满员
     * - 条件2：主屏倒计时已结束，且已加入副屏均倒计时结束（或未加入）
     */
    private fun canStartCollaborationSport(): Boolean {
        val connectedSubScreens = connectedSubScreenTypes()
        if (sitReachPersonArray.size() <= 0) return false
        if (DisplayType.LEFT in connectedSubScreens &&
            collaborationStateMap[DisplayType.LEFT] == CollaborationAreaState.NORMAL &&
            leftPersonArray.size() <= 0
        ) return false
        if (DisplayType.RIGHT in connectedSubScreens &&
            collaborationStateMap[DisplayType.RIGHT] == CollaborationAreaState.NORMAL &&
            rightPersonArray.size() <= 0
        ) return false
        val allScreenFull = sitReachPersonArray.size() >= sportManCount &&
            (DisplayType.LEFT !in connectedSubScreens ||
                leftPersonArray.size() >= leftSportManCount) &&
            (DisplayType.RIGHT !in connectedSubScreens ||
                rightPersonArray.size() >= rightSportManCount)
        if (allScreenFull) return true
        if (!mainCountDownEnded) return false
        return connectedSubScreens.all {
            it in subScreenCountDownEnded ||
                collaborationStateMap[it] != CollaborationAreaState.NORMAL
        }
    }

    /**
     * 主屏识别倒计时已结束，但仍有已加入协同的副屏未倒计时结束
     */
    private fun needWaitOtherAreaJoin(): Boolean {
        if (!mainCountDownEnded) return false
        return connectedSubScreenTypes().any {
            collaborationStateMap[it] == CollaborationAreaState.NORMAL &&
                it !in subScreenCountDownEnded
        }
    }

    /**
     * 开始识别倒计时：仅主屏本区首人识别后调用；
     * 倒计时结束后按 [canStartCollaborationSport] 决定是否开赛。
     */
    private fun startIdentifyCountDown() {
        if (identifyCountDownRunning) return
        if (mainSportStarted) return
        if (mainCountDownEnded) return
        identifyCountDownRunning = true
        uiHandler.post {
            identifyCountDownTimer?.cancel()
            var remain = identifyCountDownSeconds
            _identifyCountDownLD.tryEmit(remain)
            identifyCountDownTimer =
                object : CountDownTimer(identifyCountDownSeconds * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        if (!identifyCountDownRunning) {
                            cancel()
                            return
                        }
                        remain--
                        _identifyCountDownLD.tryEmit(remain)
                    }

                    override fun onFinish() {
                        if (!identifyCountDownRunning) return
                        identifyCountDownRunning = false
                        //主屏识别倒计时结束，统一检查协同开始条件
                        mainCountDownEnded = true
                        checkCollaborationSportStart()
                        if (needWaitOtherAreaJoin()) {
                            _waitOtherAreaLD.tryEmit(Unit)
                        }
                    }
                }.start()
        }
    }

    /**
     * 取消识别倒计时
     */
    private fun stopIdentifyCountDown() {
        identifyCountDownRunning = false
        uiHandler.post {
            identifyCountDownTimer?.cancel()
            identifyCountDownTimer = null
        }
    }

    /**
     * 点击"立即开始"：与 12 人跳绳 readyGo 一致，跳过识别倒计时并直接开赛，
     * 不因已加入副屏 0 人而拦截；自动开赛仍走 [canStartCollaborationSport]。
     */
    fun startSportNow() {
        if (sitReachPersonArray.size() <= 0 &&
            leftPersonArray.size() <= 0 &&
            rightPersonArray.size() <= 0
        ) return
        mainCountDownEnded = true
        stopIdentifyCountDown()
        tryStartSport()
        sendSportCommand()
    }

    /**
     * 真正开始本屏一轮运动
     */
    private fun tryStartSport() {
        if (mainSportStarted) return
        if (currentSportTimes > sportTimes) return
        mainSportStarted = true
        if (currentSportTimes == 1) {
            startSportTime = System.currentTimeMillis()
        }
        if (sportTimes > 1) {
            SpeechWrapper.speakAfterStop(
                getString(R.string.forward_flexion_times_start_speech, currentSportTimes)
            )
        } else {
            SpeechWrapper.speakAfterStop(getString(R.string.forward_flexion_start_speech))
        }
        _sportStartLD.tryEmit(currentSportTimes)
        laneScoreArmed.fill(false)
        clearLastUseDistances()
        currentRoundFinishing = false
        startSportCountDown()
        //切换卡片状态
        if (currentSportTimes == 1 && isRetest.not()) {
            for (index in 1 until sportManCount + 1) {
                val value = sitReachPersonArray.get(index)
                if (value != null) {
                    changeSportCardState(
                        DisplayType.MAIN,
                        index,
                        ForwardFlexionCardState.FIRST_SPORTING
                    )
                } else {
                    changeSportCardState(
                        DisplayType.MAIN,
                        index,
                        ForwardFlexionCardState.SPORTING_NO_USER
                    )
                }
            }
        } else {
            sitReachPersonArray.forEach { index, _ ->
                changeSportCardState(
                    DisplayType.MAIN,
                    index,
                    ForwardFlexionCardState.SPORTING
                )
            }
        }
        sportState = MultiSportState.SPORTING
        _sportStartedLD.value = true
    }

    fun onHandleIdentity(data: MutableList<SitAndReachObject>?) {
        if (sportState != MultiSportState.IDENTIFYING && sportState != MultiSportState.PREPARING) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (data != null) {
                updateStatusMap(data)
                handleIdentify(data)
            }
        }
    }

    private fun updateStatusMap(objects: MutableList<SitAndReachObject>) {
        statusMap.clear()
        objects.forEach {
            when {
                // 无人且场地不可用才标「暂不可用」；有人仍坐在道上时按空闲提示，避免下一组铺板误判
                it.isCompliant.not() -> {
                    if (it.skeleton != null) {
                        if (checkHandUp(it.skeleton)) {
                            statusMap[it.indexNum] = true
                        } else {
                            changeSportCardState(
                                DisplayType.MAIN,
                                it.indexNum,
                                ForwardFlexionCardState.IDLE
                            )
                        }
                    } else {
                        changeSportCardState(
                            DisplayType.MAIN,
                            it.indexNum,
                            ForwardFlexionCardState.UNAVAILABLE
                        )
                    }
                }

                it.skeleton != null && checkHandUp(it.skeleton) -> {
                    statusMap[it.indexNum] = true
                }

                it.skeleton == null -> changeSportCardState(
                    DisplayType.MAIN,
                    it.indexNum,
                    ForwardFlexionCardState.IDLE
                )
            }
        }
    }

    @Synchronized
    private fun handleIdentify(data: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.IDENTIFYING && sportState != MultiSportState.PREPARING) {
            return
        }
        //如果为识别状态
        if (sportState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val flexionBody = data.find { it.indexNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (flexionBody == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                val rect = faceCropRectHelper.getRect(
                    flexionBody.skeleton,
                    currentYuvData.width,
                    currentYuvData.height
                )
                if (rect.isEmpty) {
                    LogUtil.d("$TAG::handleIdentify: 裁剪框为空，跳过识别 道数 $currentIdentifyNumber")
                } else {
                    identify(
                        rect,
                        toIdentifyYuv(currentYuvData.yuv, currentYuvData.width, currentYuvData.height),
                        currentYuvData.width,
                        currentYuvData.height,
                        currentIdentifyNumber
                    )
                }
            }
            return
        }
        //如果正在识别，则等待识别完再处理下一个人脸识别
        if (isIdentifying()) {
            LogUtil.d("处理人脸识别：人已离开位置，但正在识别人脸; 道数 $currentIdentifyNumber")
            return
        }

        data.forEach {
            if (sitReachPersonArray.get(it.indexNum, null) == null
                && it.skeleton != null
                && checkHandUp(it.skeleton)
            ) {
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                val rect = faceCropRectHelper.getRect(
                    it.skeleton,
                    currentYuvData.width,
                    currentYuvData.height
                )
                if (rect.isEmpty) {
                    LogUtil.d("$TAG::handleIdentify: 裁剪框为空，跳过道数 ${it.indexNum}")
                    return@forEach
                }
                currentIdentifyNumber = it.indexNum
                sportState = MultiSportState.IDENTIFYING
                identify(
                    rect,
                    toIdentifyYuv(currentYuvData.yuv, currentYuvData.width, currentYuvData.height),
                    currentYuvData.width,
                    currentYuvData.height,
                    currentIdentifyNumber
                )
                changeSportCardState(
                    DisplayType.MAIN,
                    currentIdentifyNumber,
                    ForwardFlexionCardState.IDENTIFYING
                )
                LogUtil.d("处理人脸识别：人位置状态OK，开始识别该位置，道数 $currentIdentifyNumber")
                return
            }
        }
    }

    /** 相机取流为 YV12 时转 NV21 再送人脸识别 */
    private fun toIdentifyYuv(yuv: ByteArray, width: Int, height: Int): ByteArray {
        if (streamType != 1) return yuv
        val nv21 = ByteArray(width * height * 3 / 2)
        AdlYUVUtil.YV12toNV21(yuv, nv21, width, height)
        return nv21
    }

    private fun checkHandUp(skeleton: AdlPoseSkeleton?): Boolean {
        val lWrist = skeleton?.lWrist
        val lShoulder = skeleton?.lShoulder
        val rWrist = skeleton?.rWrist
        val rShoulder = skeleton?.rShoulder
        if (lWrist == null || lShoulder == null || rWrist == null || rShoulder == null) return false
        return lWrist.y < lShoulder.y || rWrist.y < rShoulder.y
    }

    /**
     * 处理主屏成绩（对齐六人：prefer 伸距、armed、齐人后 finishCurrentRound）
     */
    fun handleResult(result: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.SPORTING) return
        // 快照，避免 SDK 同列表并发改写导致 CME / 半帧数据
        val snapshot = result.toList()
        LogUtil.d(
            "$TAG::handleResult: enter sportState=$sportState size=${snapshot.size} " +
                "states=${snapshot.joinToString { "${it.indexNum}:${it.state}" }}"
        )
        viewModelScope.launch(Dispatchers.IO) {
            var emittedUi = false
            snapshot.forEach { obj ->
                val lane = obj.indexNum
                val armed = lane in laneScoreArmed.indices && laneScoreArmed[lane]
                LogUtil.d(
                    "$TAG::handleResult: lane=$lane state=${obj.state} sportState=$sportState " +
                        "armed=$armed hasPerson=${sitReachPersonArray.get(lane) != null} " +
                        "max=${obj.distanceMax} loose=${obj.distanceMaxLoose}"
                )
                if (!isValidScoreLane(lane)) {
                    LogUtil.d(
                        "$TAG::handleResult: drop invalid lane=$lane, sportManCount=$sportManCount"
                    )
                    return@forEach
                }
                val person = sitReachPersonArray.get(lane)
                if (person != null) {
                    lastUseDistanceByLane[lane] = preferUseDistance(obj)
                }
                if (obj.state != SitAndReachObject.State.END) {
                    laneScoreArmed[lane] = true
                    return@forEach
                }
                if (person == null) {
                    LogUtil.d("$TAG::handleResult: drop END no person lane=$lane")
                    return@forEach
                }
                if (!laneScoreArmed[lane]) {
                    LogUtil.d("$TAG::handleResult: drop residual END lane=$lane")
                    return@forEach
                }
                val useDistance = lastUseDistanceByLane[lane] ?: preferUseDistance(obj)
                LogUtil.d(
                    "$TAG::handleResult: prefer distance lane=$lane, " +
                        "loose=${obj.distanceMaxLoose}, max=${obj.distanceMax}, use=$useDistance"
                )
                val accepted = applyScoreFromObject(lane, person, obj)
                if (!accepted) {
                    LogUtil.d("$TAG::handleResult: drop duplicate END lane=$lane")
                    return@forEach
                }
                LogUtil.d(
                    "$TAG::handleResult: accept END lane=$lane count=${person.count} " +
                        "status=${person.status} violationDesc=${person.violationDesc} " +
                        "max=${obj.distanceMax} loose=${obj.distanceMaxLoose} use=$useDistance"
                )
                withContext(Dispatchers.Main) {
                    _sportResultLD.tryEmit(DisplayType.MAIN to Pair(lane, person))
                    emittedUi = true
                }
            }
            val doneCount: Int
            val checkSize: Int
            synchronized(resultStatusList) {
                doneCount = resultStatusList.count { it }
            }
            synchronized(checkMap) {
                checkSize = checkMap.count { it.value }
            }
            if (doneCount != checkSize) {
                LogUtil.d("$TAG::handleResult: waiting scores done=$doneCount/$checkSize")
                return@launch
            }
            if (!emittedUi) {
                LogUtil.d("$TAG::handleResult: all ready but skip finish, waiting emitter UI")
                return@launch
            }
            if (currentSportTimes > sportTimes) {
                LogUtil.d("$TAG::handleResult: skip redundant, times=$currentSportTimes/$sportTimes")
                return@launch
            }
            LogUtil.d("$TAG::handleResult: all scores ready, finishCurrentRound")
            finishCurrentRound()
        }
    }

    /** 出分道号必须在 1..sportManCount，与 resultStatusList 长度对齐 */
    private fun isValidScoreLane(lane: Int): Boolean {
        return lane in 1..sportManCount && lane - 1 < resultStatusList.size
    }

    private fun clearLastUseDistances() {
        for (i in lastUseDistanceByLane.indices) {
            lastUseDistanceByLane[i] = null
        }
    }

    /** 高 loose 且与 max 差大时用 max，否则用 loose */
    private fun preferUseDistance(obj: SitAndReachObject): Float {
        val distanceMax = obj.distanceMax
        val distanceLoose = obj.distanceMaxLoose
        return if (distanceLoose >= LOOSE_HIGH_CM && abs(distanceLoose - distanceMax) >= MAX_LOOSE_DIFF_CM) {
            distanceMax
        } else {
            distanceLoose
        }
    }

    /**
     * END 出分：按对象写成绩并占坑（含违规判定）；已出分返回 false。
     */
    private fun applyScoreFromObject(
        lane: Int,
        person: MultiSportResult,
        obj: SitAndReachObject
    ): Boolean {
        if (!isValidScoreLane(lane)) return false
        synchronized(resultStatusList) {
            if (lane - 1 !in resultStatusList.indices) return false
            if (resultStatusList[lane - 1]) return false
            val useDistance = preferUseDistance(obj)
            person.count = max((useDistance * 10 - 200).toInt(), -200)
            if (person.count == -200) {
                person.status = 2
                person.violationDesc = null
            } else {
                val (status, desc) = calcViolation(obj)
                person.status = status
                person.violationDesc = desc
            }
            resultStatusList[lane - 1] = true
            return true
        }
    }

    /**
     * 超时补分：仅用缓存的有效伸距；无违规上下文，非地板值按正常分。
     */
    private fun applyScoreFromUseDistance(
        lane: Int,
        person: MultiSportResult,
        useDistance: Float
    ): Boolean {
        if (!isValidScoreLane(lane)) return false
        synchronized(resultStatusList) {
            if (lane - 1 !in resultStatusList.indices) return false
            if (resultStatusList[lane - 1]) return false
            person.count = max((useDistance * 10 - 200).toInt(), -200)
            if (person.count == -200) {
                person.status = 2
                person.violationDesc = null
            } else {
                person.status = 0
                person.violationDesc = null
            }
            resultStatusList[lane - 1] = true
            return true
        }
    }

    /**
     * @return first=status(0正常/1违规/2未完成), second=违规文案（多项时只取第一个）
     */
    private fun calcViolation(srj: SitAndReachObject): Pair<Int, String?> {
        if (srj.direction == SitAndReachObject.Direction.UNKNOWN) {
            return 2 to null
        }
        val failMsgList = srj.failMsgList
        if (failMsgList == null) {
            LogUtil.d("$TAG::calcViolation: failMsgList null, treat as normal")
            return 0 to null
        }
        LogUtil.d(
            "====================${failMsgList.getOrNull(0)}--${
                failMsgList.getOrNull(1)
            }--${failMsgList.getOrNull(2)}--${failMsgList.getOrNull(3)}"
        )
        when {
            param.checkHand && failMsgList.getOrNull(0) == false ->
                return 1 to getString(R.string.sit_reach_violation_hand)
            param.checkLeg && failMsgList.getOrNull(1) == false ->
                return 1 to getString(R.string.sit_reach_violation_leg)
            param.checkFoot && failMsgList.getOrNull(2) == false ->
                return 1 to getString(R.string.sit_reach_violation_foot)
            param.checkInertia && failMsgList.getOrNull(3) == true ->
                return 1 to getString(R.string.sit_reach_violation_inertia)
        }
        return 0 to null
    }

    private fun startSportCountDown() {
        LogUtil.d("$TAG::startSportCountDown: timeoutSec=$sportTimeOut")
        Handler(Looper.getMainLooper()).post {
            sportCountDownTimer?.cancel()
            sportCountDown = sportTimeOut
            sportCountDownTimer = object : CountDownTimer(sportTimeOut * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    sportCountDown--
                    _sportTipsInfoLD.tryEmit(
                        getString(R.string.sport_count_down, sportCountDown)
                    )
                }

                override fun onFinish() {
                    LogUtil.d("$TAG::startSportCountDown: onFinish -> finishWithTimeout")
                    finishWithTimeout()
                }
            }.start()
        }
    }

    private fun finishWithTimeout() {
        if (sportState != MultiSportState.SPORTING) {
            LogUtil.d("$TAG::finishWithTimeout: skip, state=$sportState")
            return
        }
        LogUtil.d("$TAG::finishWithTimeout: fill missing scores from last useDistance")
        val pendingLanes: List<Int>
        synchronized(resultStatusList) {
            pendingLanes = resultStatusList.indices
                .filter { !resultStatusList[it] }
                .map { it + 1 }
        }
        for (index in pendingLanes) {
            if (!isValidScoreLane(index)) continue
            val person = sitReachPersonArray.get(index) ?: continue
            val useDistance = lastUseDistanceByLane[index]
            if (useDistance != null) {
                val accepted = applyScoreFromUseDistance(index, person, useDistance)
                if (accepted) {
                    LogUtil.d(
                        "$TAG::finishWithTimeout: lane=$index from cache " +
                            "use=$useDistance count=${person.count} status=${person.status}"
                    )
                    _sportResultLD.tryEmit(DisplayType.MAIN to Pair(index, person))
                }
            } else {
                synchronized(resultStatusList) {
                    val i = index - 1
                    if (i !in resultStatusList.indices || resultStatusList[i]) return@synchronized
                    person.status = 2
                    person.violationDesc = null
                    resultStatusList[i] = true
                }
                LogUtil.d("$TAG::finishWithTimeout: lane=${person.number} unfinished, no cache")
                _sportResultLD.tryEmit(DisplayType.MAIN to Pair(index, person))
            }
        }
        // 超时后无新结果帧时也必须收尾，否则倒计时不消、协同收尾卡住
        finishCurrentRound()
    }

    /**
     * 本轮全部出分（含超时补分）后的统一收尾，与 handleResult / finishWithTimeout 共用。
     */
    @Synchronized
    private fun finishCurrentRound() {
        if (sportState != MultiSportState.SPORTING) {
            LogUtil.d("$TAG::finishCurrentRound: skip, state=$sportState")
            return
        }
        if (currentRoundFinishing) {
            LogUtil.d("$TAG::finishCurrentRound: skip, already finishing")
            return
        }
        if (currentSportTimes > sportTimes) {
            LogUtil.d("$TAG::finishCurrentRound: skip, times=$currentSportTimes/$sportTimes")
            return
        }
        currentRoundFinishing = true
        clearLastUseDistances()
        laneScoreArmed.fill(false)

        stopSportCountDown()
        _sportTipsInfoLD.tryEmit("")
        LogUtil.d(
            "$TAG::finishCurrentRound: round done, beforeInc times=$currentSportTimes/$sportTimes"
        )
        currentSportTimes++
        sitReachPersonArray.forEach { index, _ ->
            changeSportCardState(
                DisplayType.MAIN,
                index,
                ForwardFlexionCardState.SPORTING
            )
        }
        _testOnceFinishLD.tryEmit(true)

        viewModelScope.launch(Dispatchers.IO) {
            if (currentSportTimes > sportTimes) {
                LogUtil.d("$TAG::finishCurrentRound: all rounds done -> sportEnd")
                SpeechWrapper.speech(
                    getString(R.string.end_test, currentSportTimes - 1)
                )
                withContext(Dispatchers.Main) {
                    toStatusSportEnd()
                }
            } else {
                LogUtil.d("$TAG::finishCurrentRound: next round=$currentSportTimes -> prepare")
                SpeechWrapper.speech(
                    getString(R.string.forward_flexion_sport_times_end, currentSportTimes - 1)
                )
                SystemClock.sleep(2000)
                sportState = MultiSportState.PREPARING
                withContext(Dispatchers.Main) {
                    toStatusPrepare()
                }
            }
        }
    }

    private fun stopSportCountDown() {
        sportCountDownTimer?.cancel()
    }

    /**
     * 进入准备状态：本屏识别完成，主屏已就绪（识别倒计时尚未结束时由其统一触发开始）
     */
    override fun toStatusPrepare() {
        super.toStatusPrepare()
        mHandler.removeCallbacksAndMessages(null)
        sportState = MultiSportState.COUNTDOWN
        for (i in 0 until sportManCount) {
            prepareStatusList[i] = false
            resultStatusList[i] = false
        }
        //重新武装"本轮是否开始"，支持多次测试
        mainSportStarted = false
        //主屏已就绪
        mainReady = true
        checkCollaborationSportStart()
    }

    fun uploadResult() {
        endSportTime = System.currentTimeMillis()
        //延迟100ms，等待录制视频结束
        SystemClock.sleep(100)
        sitReachPersonArray.forEach { _, value ->
            //如果是status==2,表示未完成，不上传成绩
            if (value.status == 2) return@forEach
            val sportResult = SportResult()
            sportResult.sportDuration = (endSportTime - startSportTime).toInt()
            sportResult.maxComboCount = 0
            sportResult.sportAchievement = value.count
            sportResult.perfectCount = 0
            sportResult.goodCount = 0
            sportResult.comeOnCount = 0
            sportResult.perfectScore = 0
            sportResult.goodScore = 0
            sportResult.comeOnScore = 0
            sportResult.sportNo = value.number.toString()
            sportResult.userId = value.accountInfo.studentEntity?.accountId
            sportResult.startTime = startSportTime
            sportResult.endTime = endSportTime
            sportResult.sportResult = value.count.toString()
            if (value.status == 1) {
                sportResult.sportViolationStatus = 1
                sportResult.sportViolationNumber = "1"
            }
            sportResult.videoCover = videoCover
            sportResult.videoFile = videoFile
            var recordPlan: SportRecordEntity.RecordPlan? = null
            if (sceneId == SceneConstants.ORGANIZE_TEST) {
                recordPlan = SportRecordEntity.RecordPlan().apply {
                    ptPlanId = this@SitReachEighteenViewModel.ptPlanId
                    ptStandardId = this@SitReachEighteenViewModel.ptStandardId
                }
            }
            uploadSportResult(
                sceneId ?: SceneConstants.INDEPENDENT_TEST,
                sportResult,
                sportResult.userId,
                recordPlan = recordPlan
            )
        }
    }

    /**
     * 结束处理成绩：主屏结束后需等所有已加入副屏结束，才展示合并结果页
     */
    override fun toStatusSportEnd() {
        super.toStatusSportEnd()
        sportState = MultiSportState.IDLE
        _sportStartedLD.value = false
        stopIdentifyCountDown()
        mainSportFinished = true
        checkAllSportFinished()
    }

    private fun checkAllSportFinished() {
        if (!mainSportFinished) return
        val joinedSubScreens =
            listOf(DisplayType.LEFT, DisplayType.RIGHT).filter { isSubScreenJoined(it) }
        if (joinedSubScreens.any { it !in subScreenSportFinished }) {
            LogUtil.d("等待副屏运动结束：已加入=$joinedSubScreens，已结束=$subScreenSportFinished")
            return
        }
        handleFinalResult()
    }

    /**
     * 合并主屏与已连接副屏成绩，按成绩高优排序，计算名次，生成结果页统一数据
     */
    private fun handleFinalResult() {
        val merged = mergeAllAreaResults()
        if (merged.isEmpty()) return
        _toDoneLD.tryEmit(merged)
    }

    private fun mergeAllAreaResults(): List<ThirtySportResultItem> {
        val areaNameMap = mapOf(
            DisplayType.MAIN to getString(R.string.area_main),
            DisplayType.LEFT to getString(R.string.area_left),
            DisplayType.RIGHT to getString(R.string.area_right)
        )
        val merged = mutableListOf<ThirtySportResultItem>()
        //主屏成绩
        sitReachPersonArray.forEach { _, result ->
            merged.add(
                ThirtySportResultItem(
                    result = result,
                    displayType = DisplayType.MAIN,
                    areaName = areaNameMap[DisplayType.MAIN] ?: "",
                    isVisitor = result.accountInfo.studentEntity?.accountId == BusiBaseConstantsValue.VISITOR_USER_ID,
                    trackNumber = result.number
                )
            )
        }
        //左副屏成绩
        if (leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) } != null) {
            leftPersonArray.forEach { _, result ->
                merged.add(
                    ThirtySportResultItem(
                        result = result,
                        displayType = DisplayType.LEFT,
                        areaName = areaNameMap[DisplayType.LEFT] ?: "",
                        isVisitor = result.accountInfo.studentEntity?.accountId == BusiBaseConstantsValue.VISITOR_USER_ID,
                        trackNumber = result.number,
                        recordId = result.recordId
                    )
                )
            }
        }
        //右副屏成绩
        if (rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) } != null) {
            rightPersonArray.forEach { _, result ->
                merged.add(
                    ThirtySportResultItem(
                        result = result,
                        displayType = DisplayType.RIGHT,
                        areaName = areaNameMap[DisplayType.RIGHT] ?: "",
                        isVisitor = result.accountInfo.studentEntity?.accountId == BusiBaseConstantsValue.VISITOR_USER_ID,
                        trackNumber = result.number,
                        recordId = result.recordId
                    )
                )
            }
        }
        //按成绩高优排序，并列同排名
        merged.sortWith(compareBy { -it.count })
        var preRank = 0
        var preScore = Int.MAX_VALUE
        merged.forEach { item ->
            val rank = if (item.count == preScore) preRank else preRank + 1
            preRank = rank
            preScore = item.count
            item.result.rank = rank
        }
        return merged
    }

    /**
     * 重测，只测一次；本机开测并明确向副屏发 SPORT，副屏 IDLE 有人时走协同重测
     */
    fun retest() {
        isRetest = true
        sitReachPersonArray.forEach { _, result ->
            result.count = -1000
            result.violationDesc = null
            result.status = 0
        }
        leftPersonArray.forEach { _, result ->
            result.count = -1000
            result.violationDesc = null
            result.status = 0
        }
        rightPersonArray.forEach { _, result ->
            result.count = -1000
            result.violationDesc = null
            result.status = 0
        }
        for (ind in resultStatusList.indices) {
            resultStatusList[ind] = false
        }
        laneScoreArmed.fill(false)
        clearLastUseDistances()
        currentRoundFinishing = false
        startCommandSent = false
        mainSportFinished = false
        mainSportStarted = false
        mainCountDownEnded = true
        subScreenSportFinished.clear()
        sportTimes = 1
        currentSportTimes = 1
        videoFile = null
        // 不经 toStatusPrepare，避免状态被打回；直接开测并通知副屏
        tryStartSport()
        sendSportCommand()
    }

    override fun reset() {
        super.reset()
        isRetest = false
        prepareStatusList.clear()
        resultStatusList.clear()
        for (i in 1..sportManCount) {
            checkMap[i] = false
            prepareStatusList.add(false)
            resultStatusList.add(false)
        }
        laneScoreArmed.fill(false)
        clearLastUseDistances()
        currentRoundFinishing = false
        multiIdentityEngine.reset()
        sitReachPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        videoFile = null
        //重置协同状态
        stopIdentifyCountDown()
        mainReady = false
        mainCountDownEnded = false
        mainSportStarted = false
        startCommandSent = false
        mainSportFinished = false
        subScreenCountDownEnded.clear()
        subScreenSportFinished.clear()
        _sportStartedLD.value = false
    }

    //----------------------- 三屏协同 gRPC -----------------------

    fun registerGrpcCmd() {
        registerSportCardStatus()
        registerAccountInfo()
        registerResult()
        registerCollaborationStatus()
        registerIdentifiedCount()
        registerCountDownEnd()
        registerGrpcSportStatusHandler()
    }

    /**
     * 进入项目：检查左右屏连接状态，已连接的发送协同运动开始指令
     */
    fun initCollaboration() {
        checkSubScreen(DisplayType.LEFT, leftSubScreenIp)
        checkSubScreen(DisplayType.RIGHT, rightSubScreenIp)
    }

    private fun checkSubScreen(displayType: DisplayType, ip: String?) {
        val clientId = ip?.let { GrpcManager.getClientIdByIp(it) }
        if (clientId == null) {
            LogUtil.d("${displayType.name}副屏未连接：$ip")
            updateCollaborationState(displayType, CollaborationAreaState.UNCONNECTED)
            return
        }
        //已连接，发送协同运动开始指令
        sendCollaborationCommand(clientId, CollaborationCmdEnum.START)
    }

    private fun sendCollaborationCommand(clientId: String, cmd: CollaborationCmdEnum) {
        val payload = GrpcCollaborationSportInfo(
            cmd = cmd.ordinal,
            sportSkuId = sportInfo?.sportSkuId ?: "",
            testCount = sportInfo?.testCount?.toInt() ?: 1,
            sceneId = sceneId ?: SceneConstants.INDEPENDENT_TEST
        )
        GrpcManager.sendToClient(
            clientId,
            GrpcCmdCode.CMD_COLLABORATION_SPORT,
            payload.toJson(),
            false,
            null,
            null
        )
    }

    /**
     * 页面关闭：向已连接左右屏发送协同结束指令
     */
    fun sendCollaborationFinish() {
        leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
        }
        rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
        }
    }

    /**
     * 当前已连接副屏类型列表
     */
    private fun connectedSubScreenTypes(): List<DisplayType> {
        val result = mutableListOf<DisplayType>()
        if (leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) } != null) {
            result.add(DisplayType.LEFT)
        }
        if (rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) } != null) {
            result.add(DisplayType.RIGHT)
        }
        return result
    }

    /**
     * 指定副屏是否已加入协同（状态为 NORMAL 可加入）
     */
    fun isSubScreenJoined(displayType: DisplayType): Boolean =
        displayType != DisplayType.MAIN &&
                collaborationStateMap[displayType] == CollaborationAreaState.NORMAL

    /**
     * 更新单个区域协同状态，保留其他区域已有状态
     */
    private fun updateCollaborationState(displayType: DisplayType, state: CollaborationAreaState) {
        collaborationStateMap[displayType] = state
        uiHandler.post { _collaborationStateLD.value = collaborationStateMap.toMap() }
    }

    /**
     * 向已连接副屏发送开始运动指令，保证只发送一次
     */
    private fun sendSportCommand() {
        if (startCommandSent) return
        startCommandSent = true
        LogUtil.d("协同开始条件满足：向左右副屏发送开始运动指令")
        leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.SPORT)
        }
        rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.SPORT)
        }
    }

    /**
     * 副屏就绪（倒计时结束）指令
     */
    private fun registerCountDownEnd() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COUNTDOWN_END, GRPC_KEY) {
            subScreenCountDownEnded.add(it.from.getDisplayType(leftSubScreenIp, rightSubScreenIp))
            checkCollaborationSportStart()
        }
    }

    /**
     * 注册协同状态回复指令
     */
    private fun registerCollaborationStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COLLABORATION_STATUS, GRPC_KEY) { data ->
            val status = data.payload.toObject<GrpcCollaborationStatus>()?.status
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val statusEnum = CollaborationStatusEnum.fromCode(status ?: -1)
            val state = when (statusEnum) {
                //运动中、已有已识别人员 → 不能加入协同，设备占用中
                CollaborationStatusEnum.SPORTING -> CollaborationAreaState.OCCUPIED
                else -> CollaborationAreaState.NORMAL
            }
            LogUtil.d("收到协同状态回复：$displayType, status=$status, state=$state")
            updateCollaborationState(displayType, state)
        }
    }

    /**
     * 注册运动结束指令
     */
    private fun registerGrpcSportStatusHandler() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_STATUS, GRPC_KEY) { data ->
            val info = data.payload.toObject<GrpcSportStatus>()
            if (info == null || info.skuId != sportInfo?.sportSkuId) {
                LogUtil.d("当前运动状态指令不匹配：grpcskuid:${info?.skuId}--sportSkuId:${sportInfo?.sportSkuId}")
                return@registerServerHandler
            }
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val status = GrpcSportStatusEnum.fromCode(info.status)
            if (status == GrpcSportStatusEnum.FINISHED) {
                if (displayType == DisplayType.LEFT || displayType == DisplayType.RIGHT) {
                    LogUtil.d("收到${displayType.name}副屏运动结束")
                    subScreenSportFinished.add(displayType)
                    uiHandler.post { checkAllSportFinished() }
                }
            }
        }
    }

    /**
     * 注册已识别人数指令：副屏已有识别成功的人员时上报人数，用于"是否清空已识别结果"提示
     */
    private fun registerIdentifiedCount() {
        GrpcManager.registerServerHandler(
            GrpcCmdCode.CMD_SPORT_IDENTIFIED_COUNT,
            GRPC_KEY
        ) { data ->
            val count = data.payload.toObject<GrpcSportIdentifiedCount>()?.count ?: 0
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            uiHandler.post {
                if (displayType == DisplayType.LEFT || displayType == DisplayType.RIGHT) {
                    LogUtil.d("收到${displayType.name}副屏已识别人数：$count")
                    identifiedCountMap[displayType] = count
                    _identifiedCountLD.tryEmit(Unit)
                }
            }
        }
    }

    /**
     * 弹窗展示期间，副屏有新用户到达时同步累加已识别人数
     */
    private fun syncIdentifiedCountWhileTip(displayType: DisplayType) {
        uiHandler.post {
            if (identifiedCountMap.containsKey(displayType)) {
                identifiedCountMap[displayType] = when (displayType) {
                    DisplayType.LEFT -> leftPersonArray.size()
                    DisplayType.RIGHT -> rightPersonArray.size()
                    else -> 0
                }
                _identifiedCountLD.tryEmit(Unit)
            }
        }
    }

    /**
     * 用户确认清空：向已上报识别人数的区域发送清除指令，并同步清空主屏本地对应区域数据
     */
    fun confirmClearIdentifyInfo() {
        identifiedCountMap.keys.forEach { displayType ->
            val ip = when (displayType) {
                DisplayType.LEFT -> leftSubScreenIp
                DisplayType.RIGHT -> rightSubScreenIp
                else -> null
            }
            ip?.let { GrpcManager.getClientIdByIp(it) }?.let { clientId ->
                LogUtil.d("发送清除指令到${displayType.name}副屏：$ip")
                GrpcManager.sendToClient(
                    clientId,
                    GrpcCmdCode.CMD_CLEAR_IDENTIFY_INFO,
                    "",
                    false,
                    null,
                    null
                )
            }
            clearSubScreenIdentifiedData(displayType)
        }
        identifiedCountMap.clear()
    }

    /**
     * 用户取消清空：关闭本次提示
     */
    fun cancelClearIdentifyInfo() {
        identifiedCountMap.clear()
    }

    private fun clearSubScreenIdentifiedData(displayType: DisplayType) {
        val personArray = when (displayType) {
            DisplayType.LEFT -> leftPersonArray
            DisplayType.RIGHT -> rightPersonArray
            else -> return
        }
        val indexes = mutableListOf<Int>()
        for (i in 0 until personArray.size()) {
            indexes.add(personArray.keyAt(i))
        }
        personArray.clear()
        uiHandler.post {
            indexes.forEach { index ->
                _sportCardStateLD.tryEmit(
                    displayType to (index to ForwardFlexionCardState.IDLE)
                )
            }
        }
    }

    /**
     * 注册成绩指令接收
     */
    private fun registerResult() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_RESULT, GRPC_KEY) { data ->
            val result = data.payload.toObject<GrpcSportResult>()
            result?.let {
                val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
                val score = result.sportResult.toIntOrNull() ?: -1000
                uiHandler.post {
                    when (displayType) {
                        DisplayType.LEFT -> {
                            leftPersonArray[result.index]?.count = score
                            leftPersonArray[result.index]?.status = result.status
                            leftPersonArray[result.index]?.recordId = result.recordId
                            leftPersonArray[result.index]?.violationDesc = result.violationDesc
                            leftPersonArray[result.index]?.let { person ->
                                _sportResultLD.tryEmit(
                                    DisplayType.LEFT to Pair(result.index, person)
                                )
                            }
                        }

                        DisplayType.RIGHT -> {
                            rightPersonArray[result.index]?.count = score
                            rightPersonArray[result.index]?.status = result.status
                            rightPersonArray[result.index]?.recordId = result.recordId
                            rightPersonArray[result.index]?.violationDesc = result.violationDesc
                            rightPersonArray[result.index]?.let { person ->
                                _sportResultLD.tryEmit(
                                    DisplayType.RIGHT to Pair(result.index, person)
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    /**
     * 注册用户消息
     */
    private fun registerAccountInfo() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_ACCOUNT_INFO, GRPC_KEY) { data ->
            val accountInfo = data.payload.toObject<GrpcAccountInfo>()
            accountInfo?.let { info ->
                val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
                val account = AccountInfo().apply {
                    studentEntity = StudentEntity().apply {
                        accountName = info.name
                        userFaceImgUrl = info.avatar
                        accountId = info.accountId
                    }
                }
                when (displayType) {
                    DisplayType.LEFT -> {
                        removeSamePerson(DisplayType.LEFT, leftPersonArray, account)
                        leftPersonArray.put(
                            info.index,
                            MultiSportResult(account, info.index, -1000)
                        )
                        identified(DisplayType.LEFT, info.index, account)
                        syncIdentifiedCountWhileTip(DisplayType.LEFT)
                        checkCollaborationSportStart()
                    }

                    DisplayType.RIGHT -> {
                        removeSamePerson(DisplayType.RIGHT, rightPersonArray, account)
                        rightPersonArray.put(
                            info.index,
                            MultiSportResult(account, info.index, -1000)
                        )
                        identified(DisplayType.RIGHT, info.index, account)
                        syncIdentifiedCountWhileTip(DisplayType.RIGHT)
                        checkCollaborationSportStart()
                    }

                    else -> LogUtil.d("收到未知区域用户消息：clientId=${data.from}")
                }
            }
        }
    }

    /**
     * 副屏识别到已识别过的人员时，清掉上次识别的位置（非游客）
     */
    private fun removeSamePerson(
        displayType: DisplayType,
        personArray: SparseArray<MultiSportResult>,
        account: AccountInfo
    ) {
        val accountId = account.studentEntity?.accountId
        if (accountId == BusiBaseConstantsValue.VISITOR_USER_ID) return
        for (index in 0 until personArray.size()) {
            val multiResult = personArray.valueAt(index)
            if (multiResult.accountInfo.studentEntity?.accountId == accountId) {
                personArray.remove(multiResult.number)
                _sportCardStateLD.tryEmit(
                    displayType to (multiResult.number to ForwardFlexionCardState.IDLE)
                )
                break
            }
        }
    }

    private fun identified(type: DisplayType, index: Int, accountInfo: AccountInfo) {
        uiHandler.post {
            _identifiedLD.tryEmit(type to Pair(index, accountInfo))
        }
    }

    /**
     * 注册卡片状态消息
     */
    private fun registerSportCardStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_CARD_STATUS, GRPC_KEY) { data ->
            val sportCardStatus = data.payload.toObject<GrpcSportCardStatus>()
            LogUtil.d("收到客户端（${GrpcManager.getClientIp(data.from)}）消息：${data.commandCode}")
            sportCardStatus?.let { card ->
                //副屏上报的卡片状态为 ForwardFlexionCardState 的序号
                ForwardFlexionCardState.values().getOrNull(card.status)?.let { state ->
                    uiHandler.post {
                        _sportCardStateLD.tryEmit(
                            data.from.getDisplayType(
                                leftSubScreenIp,
                                rightSubScreenIp
                            ) to Pair(card.index, state)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //页面关闭，发送协同运动结束指令
        sendCollaborationFinish()
        sportCountDownTimer?.cancel()
        mHandler.removeCallbacksAndMessages(null)
        onceCacheProcessor.onRelease()
        sportState = MultiSportState.IDLE
        videoRecorder.onStop()
        videoRecorder.onRelease()
        GrpcManager.unregisterHandler(GRPC_KEY)
    }
}
