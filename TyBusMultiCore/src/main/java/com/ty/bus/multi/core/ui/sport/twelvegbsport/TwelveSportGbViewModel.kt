package com.ty.bus.multi.core.ui.sport.twelvegbsport

import adl.ssp.common.report.model.IndicatorsResultDto
import android.app.Application
import android.graphics.Rect
import android.media.AsyncPlayer
import android.media.AudioManager
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.util.SparseArray
import androidx.core.net.toUri
import androidx.core.util.forEach
import androidx.lifecycle.viewModelScope
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SportIndicatorsEntity
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.SportRecordEntity.RecordMeeting
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.service.web.SportRecordService
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.standard.ExercisePrescription
import com.adl.sport.standard.jumpcore.Jumper
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiPipelineIdentifyViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.FourSportConfig
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.ui.sport.thirtysport.CollaborationAreaState
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.IndicatorsConstants
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
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
import com.ty.tybusbase.helper.IndicatorsHelper
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.SportHelper
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.SoundPlayUtils
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TwelveSportGbViewModel(application: Application) : BaseMultiPipelineIdentifyViewModel(application) {

    companion object {
        private const val TAG = "ZXN_TEST"

        private const val DEFAULT_COUNTDOWN_TIME: Long = 10L
        private const val DEFAULT_SPORT_TIME: Long = 60L

        private const val SPORT_NUMBER = 4

        /** 裁剪框最小边长 */
        private const val MIN_RECT_SIZE = 40

        /** 无头顶点时，由肩线向上反推头高的系数（2倍肩宽） */
        private const val SHOULDER_WIDTH_TO_HEAD_RATIO = 1.5f
    }

    private enum class CameraSize(val size: Size, val hkCode: Byte) {
        SIZE_1K(Size(1920, 1080), 27),
        SIZE_2K(Size(2560, 1440), 70),
        SIZE_4K(Size(3840, 2160), 64),
    }

    enum class HandUpType {
        LEFT, RIGHT, BOTH, NONE
    }

    var fourSportConfig: FourSportConfig? = null
    private var createTime: Long = 0

    private val multiProcessor by lazy { OnceCacheProcessor() }

    val sportResultMap: MutableMap<Int, MultiSportResult> by lazy { mutableMapOf() }
    private val cardStateMap: MutableMap<Int, MultiSportCardViewState> = mutableMapOf()

    private var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    private val indicatorsMap by lazy {
        ConcurrentHashMap<Int, IndicatorsResultDto>()
    }

    private val prescriptions = SparseArray<ExercisePrescription>()

    //随堂赛的组号
    var personIndex = 0

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    /**
     * 举手判定手型：0左手 / 1右手 / 2左右手皆可 / 3不检测举手。
     * 只有判定到举手，对应道次才会送人脸识别。
     */
    private var handUpType: HandUpType = HandUpType.BOTH

    /** 记录每个道次最近一次判定到的手型，key=道次 */
    private val handRaisedMap by lazy { SparseArray<HandUpType>() }

    //记录开始和结束时间
    var startTime: Long = 0
    var endTime: Long = 0

    //模型加载事件
    private val _modelLoadLD = eventFlow<Boolean>()
    val modelLoadLD: SharedFlow<Boolean> = _modelLoadLD.asSharedFlow()

    //开启预览事件
    private val _startPreviewLD = eventFlow<Pair<AdlCamera, AdlCameraFrameProcessor>>()
    val startPreviewLD: SharedFlow<Pair<AdlCamera, AdlCameraFrameProcessor>> = _startPreviewLD.asSharedFlow()

    //停止预览事件
    private val _stopPreviewLD = eventFlow<AdlCameraFrameProcessor>()
    val stopPreviewLD: SharedFlow<AdlCameraFrameProcessor> = _stopPreviewLD.asSharedFlow()

    //运动是否开始（状态，需支持同步读取当前值，供开始条件/导航文案判断）
    private val _sportStartedLD = eventFlow<Unit>()
    val sportStartedLD: SharedFlow<Unit> = _sportStartedLD.asSharedFlow()

    //运动是否开始（状态，需支持同步读取当前值，供开始条件/导航文案判断）
    private val _sportStopLD = eventFlow<Unit>()
    val sportStopLD: SharedFlow<Unit> = _sportStopLD.asSharedFlow()

    //成绩结果（区域 → 已识别人员列表）
    private val _resultLD = eventFlow<Pair<DisplayType, MutableList<GroupSportBaseMan>>>()
    val resultLD: SharedFlow<Pair<DisplayType, MutableList<GroupSportBaseMan>>> = _resultLD.asSharedFlow()

    //单人次成绩结果（区域 → (位置, 成绩)）
    private val _singleResultLD = eventFlow<Pair<DisplayType, Pair<Int, Int>>>()
    val singleResultLD: SharedFlow<Pair<DisplayType, Pair<Int, Int>>> = _singleResultLD.asSharedFlow()

    //竞技开始倒计时完成事件（区域 → 是否完成）
    private val _countDownFinishLD = eventFlow<Pair<DisplayType, Boolean>>()
    val countDownFinishLD: SharedFlow<Pair<DisplayType, Boolean>> = _countDownFinishLD.asSharedFlow()

    //比赛结束事件（一次性消息：非粘性，返回页面不会重放，避免重复 clearScreen/addResultView）
    private val _toDoneLD = eventFlow<List<ThirtySportResultItem>>()
    val toDoneLD: SharedFlow<List<ThirtySportResultItem>> = _toDoneLD.asSharedFlow()

    //切换运动卡片状态（区域 → (位置, 卡片状态)）
    private val _changeSportViewStateLD = eventFlow<Pair<DisplayType, Pair<Int, MultiSportCardViewState>>>()
    val changeSportViewStateLD: SharedFlow<Pair<DisplayType, Pair<Int, MultiSportCardViewState>>> = _changeSportViewStateLD.asSharedFlow()

    //已识别人员（区域 → (位置, 人员信息)）
    private val _identifiedLD = eventFlow<Pair<DisplayType, Pair<Int, AccountInfo>>>()
    val identifiedLD: SharedFlow<Pair<DisplayType, Pair<Int, AccountInfo>>> = _identifiedLD.asSharedFlow()

    //人员报告信息展示（一次性跳转消息：非粘性，避免从报告页返回时重放导致再次跳转）
    private val _showReportLD = eventFlow<String>()
    val showReportLD: SharedFlow<String> = _showReportLD.asSharedFlow()

    //改变时间显示UI的背景色（区域 → 背景色资源）
    private val _changeTimeTextBgLD = eventFlow<Pair<DisplayType, Int>>()
    val changeTimeTextBgLD: SharedFlow<Pair<DisplayType, Int>> = _changeTimeTextBgLD.asSharedFlow()

    //相机登录状态
    private val _cameraLoginStateLD = eventFlow<Boolean>()
    val cameraLoginStateLD: SharedFlow<Boolean> = _cameraLoginStateLD.asSharedFlow()

    var countDownTime: Long = DEFAULT_COUNTDOWN_TIME

    //竞技倒计时过程中更新UI（每秒一次，高频事件流）
    private val _countDownNoticeLD = eventFlow<Pair<DisplayType, String>>()
    val countDownNoticeLD: SharedFlow<Pair<DisplayType, String>> = _countDownNoticeLD.asSharedFlow()

    private val asyncPlayer by lazy { AsyncPlayer("CutDownAsyncPlayer") }

    lateinit var ipcHkSource: NsCameraHKSource
    var focalHandler = Handler(Looper.getMainLooper())

    //X秒竞技倒计时的Timer任务
    private var startCountDownTimer: CountDownTimer? = null

    //当前播放音效的索引
    var soundIndex = 0

    //总音效数
    val soundCount = 5

    var sportCountDownTime = DEFAULT_SPORT_TIME

    //运动倒计时文本（区域 → 文案，每秒一次，高频事件流）
    private val _sportCountDownLD = eventFlow<Pair<DisplayType, Long>>()
    val sportCountDownLD: SharedFlow<Pair<DisplayType, Long>> = _sportCountDownLD.asSharedFlow()

    //运动倒计时任务
    private var sportingCountDownTimer: CountDownTimer? = null

    private val GRPC_KEY = "TwelveSportGbViewModel"

    private val leftSubScreenIp by lazy { BusiBaseConfig.readLeftSubScreenIp() }
    private val rightSubScreenIp by lazy { BusiBaseConfig.readRightSubScreenIp() }

    val leftPersonArray by lazy { SparseArray<MultiSportResult>() }
    val rightPersonArray by lazy { SparseArray<MultiSportResult>() }

    private val collaborationStateMap = mutableMapOf(
        DisplayType.LEFT to CollaborationAreaState.UNCONNECTED,
        DisplayType.RIGHT to CollaborationAreaState.UNCONNECTED
    )
    private val _collaborationStateLD = MutableStateFlow(collaborationStateMap.toMap())
    val collaborationStateLD: StateFlow<Map<DisplayType, CollaborationAreaState>> = _collaborationStateLD.asStateFlow()

    val identifiedCountMap = mutableMapOf<DisplayType, Int>()
    private val _identifiedCountLD = eventFlow<Unit>()
    val identifiedCountLD = _identifiedCountLD.asSharedFlow()

    private val occupiedSubScreens = mutableSetOf<DisplayType>()

    private val _subScreenOccupiedLD = eventFlow<Set<DisplayType>>()
    val subScreenOccupiedLD = _subScreenOccupiedLD.asSharedFlow()

    //协同开始条件跟踪：已倒计时结束的副屏
    private val subScreenCountDownEnded = mutableSetOf<DisplayType>()
    private val subScreenSportFinished = mutableSetOf<DisplayType>()

    //协同开始条件跟踪：开始运动指令是否已发送
    private var startCommandSent = false

    private var sportStartedFlag = false

    //协同开始条件跟踪：主屏倒计时是否结束
    private var mainCountDownEnded = false

    //协同开始条件跟踪：主屏是否真正开始
    private var mainSportStarted = false

    /** 主屏运动是否已结束（等待副屏结束后再出成绩） */
    private var mainSportFinished = false

    private val _waitOtherAreaLD = eventFlow<Unit>()
    val waitOtherAreaLD: SharedFlow<Unit> = _waitOtherAreaLD.asSharedFlow()

    override fun toStatusPrepare() {
        super.toStatusPrepare()
        Log.i(TAG, "FourSportGbViewModel::toStatusPrepare")
        sportState = MultiSportState.PREPARING
        reset()
    }

    override fun toStatusIdentifyFace() {
        super.toStatusIdentifyFace()
        Log.i(TAG, "FourSportGbViewModel::toStatusIdentifyFace")
        sportState = MultiSportState.IDENTIFYING
        cardStateMap.clear()
        for (index in 0 until SPORT_NUMBER) {
            cardStateMap[index] = MultiSportCardViewState.NOBODY
            changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
        }
    }

    override fun toStatusIdentifySuccess() {
        super.toStatusIdentifySuccess()
        Log.i(TAG, "FourSportGbViewModel::toStatusIdentifySuccess")
        sportState = MultiSportState.WAITING_START
        startCountDownTimer = null
    }

    override fun toStatusSportCountDown() {
        super.toStatusSportCountDown()
        Log.i(TAG, "FourSportGbViewModel::toStatusSportCountDown")
        sportState = MultiSportState.COUNTDOWN
    }

    override fun toStatusSportStart() {
        super.toStatusSportStart()
        Log.i(TAG, "FourSportGbViewModel::toStatusSportStart")
        sportState = MultiSportState.SPORTING
        startCountDownTimer = null
        _sportStartedLD.tryEmit(Unit)
        startTime = System.currentTimeMillis()
        sportCountDownTime = sportInfo?.endValue?.toLong()?.div(1000) ?: DEFAULT_SPORT_TIME
        sportingCountDownTimer = object : CountDownTimer(sportCountDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sportCountDownTime--
                //每隔10秒，且剩余时间大于等于5秒，播放音效,millisUntilFinished > 0.5过滤掉开始时间也能整除10的情况
                if (sportCountDownTime % 10 == 0L && sportCountDownTime >= 5) {
                    SoundPlayUtils.play(25 + soundIndex++ % soundCount)
                }
                if (sportCountDownTime == 10L) {
                    _changeTimeTextBgLD.tryEmit(DisplayType.MAIN to R.drawable.pk_skip_rose_time_warning_shape)
                }

                _sportCountDownLD.tryEmit(DisplayType.MAIN to sportCountDownTime)
            }

            override fun onFinish() {
                //比赛结束
                _sportCountDownLD.tryEmit(DisplayType.MAIN to 0L)
                toStatusSportEnd()
            }
        }
        sportingCountDownTimer?.start()

        //比赛开始设置卡片状态为运动状态
        cardStateMap.forEach { (index, state) ->
            val personResult = sportResultMap[index]
            if (personResult == null) {
                changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY_SPORT)
            } else {
                changeMultiIdentityCardState(index, MultiSportCardViewState.SPORTING)
            }
        }

        sportStartedFlag = true
    }

    override fun toStatusSportEnd() {
        super.toStatusSportEnd()
        Log.i(TAG, "FourSportGbViewModel::toStatusSportEnd")
        endTime = System.currentTimeMillis()
        sportState = MultiSportState.IDLE
        _sportStopLD.tryEmit(Unit)

        SoundPlayUtils.play(24)

        // 主屏结束：等已加入协同的副屏都结束后再统一出成绩
        mainSportFinished = true
        checkAllSportFinished()
    }

    fun initCamera() {
        Log.i(TAG, "FourSportGbViewModel::initCamera")
        val cameraConfig = BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
        ipcHkSource = AdlCameraConfig.builder()
            .setPreviewSize(getPreviewSize().width, getPreviewSize().height)
            .setCameraType(AdlCameraConfig.CameraType.HK)
            .setCameraId(AdlCameraConfig.CameraId.Back)
            .setConnect(cameraConfig.ip, BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT, cameraConfig.account, cameraConfig.psw)
            .setPreviewFps(20)
            .setPreviewEnable(true)
            .setAutoFocus(true)
            .setStreamType(0)
            .build() as NsCameraHKSource

        ipcHkSource.login(object : AdlCameraListener {
            override fun onOpenSucceed(p0: Boolean) {}

            override fun onLoginSucceed() {
                viewModelScope.launch(Dispatchers.IO) {
                    setResolution(ipcHkSource, CameraSize.SIZE_1K)
                    SystemClock.sleep(100)
                    _cameraLoginStateLD.tryEmit(true)
                    _startPreviewLD.tryEmit(Pair(ipcHkSource, multiProcessor))
                }
            }

            override fun onLoginFail() {
                _cameraLoginStateLD.tryEmit(false)
            }

            override fun onOpenStatus(p0: String?) {}

            override fun onError(p0: Exception?) {
                _cameraLoginStateLD.tryEmit(false)
            }

            override fun onStreamLoss() {}

            override fun onOpenFail() {}
        })
        timeOutHelper.start()
    }

    fun addFrameProcessor(processor: AdlCameraFrameProcessor) {
        Log.i(TAG, "FourSportGbViewModel::addFrameProcessor")
        multiProcessor.addFrameProcessor(processor)
    }

    fun getFrameProcessor(): AdlCameraFrameProcessor = multiProcessor

    fun getPreviewSize() = CameraSize.SIZE_1K.size

    fun modelLoaded() {
        _modelLoadLD.tryEmit(true)
    }

    fun loadPerson(data: ArrayList<MeetGroupDetailsEntity>?) {
        if (data.isNullOrEmpty()) return
        data.forEachIndexed { index, meetGroupDetailsEntity ->
            if (index !in 0 until SPORT_NUMBER) {
                Log.w(TAG, "TwelveSportGbViewModel::loadPerson: skip out-of-range index=$index")
                return@forEachIndexed
            }
            //给accountInfo赋值，用来获取存储视频的地址
            val accountInfo = AccountInfo().apply {
                studentEntity = StudentEntity().apply {
                    accountId = meetGroupDetailsEntity.accountId
                    accountName = meetGroupDetailsEntity.accountName
                    userFaceImgUrl = AdlService.getService().facePrefix + meetGroupDetailsEntity.accountAvatar
                }
            }
            if (accountInfo.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                accountInfoLD.postValue(accountInfo)
            }
            sportResultMap[index] = MultiSportResult(accountInfo, index, meetGroup = meetGroupDetailsEntity)
            _identifiedLD.tryEmit(DisplayType.MAIN to Pair(index, accountInfo))
            changeMultiIdentityCardState(index, MultiSportCardViewState.PRECAST_PERSON)
        }
    }

    @Synchronized
    fun processFrame(jumpers: List<Jumper>) {
        val list = jumpers.toList()
        when (sportState) {
            MultiSportState.IDENTIFYING -> handleIdentify(list)
            MultiSportState.SPORTING -> handleSportResult(list)
            else -> {}
        }
    }

    private fun createCountDownTimer() = object : CountDownTimer(DEFAULT_COUNTDOWN_TIME * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            _countDownNoticeLD.tryEmit(DisplayType.MAIN to "${countDownTime--}")
        }

        override fun onFinish() {
            //主屏识别倒计时结束，统一检查协同开始条件
            mainCountDownEnded = true
            sportState = MultiSportState.WAITING_START
            checkCollaborationSportStart()
            if (needWaitOtherAreaJoin()) {
                _waitOtherAreaLD.tryEmit(Unit)
            }
        }
    }



    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun setResolution(camera: AdlCamera, cameraSize: CameraSize) {
        val hkSource = camera as NsCameraHKSource
        val deviceItem = hkSource.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, cameraSize.hkCode, 27, 17)
    }

    fun isSportStarted(): Boolean = sportStartedFlag

    fun isSubScreenJoined(displayType: DisplayType): Boolean = displayType != DisplayType.MAIN && _collaborationStateLD.value[displayType] == CollaborationAreaState.NORMAL

    fun registerGrpcCmd() {
        Log.i(TAG, "TwelveSportGbViewModel::registerGrpcCmd")
        registerSportCardStatus()
        registerAccountInfo()
        registerResult()
        registerCollaborationStatus()
        registerIdentifiedCount()
        registerCountDownEnd()
        registerGrpcSportStatusHandler()
    }

    fun initCollaboration() {
        Log.i(TAG, "TwelveSportGbViewModel::initCollaboration: leftIp=$leftSubScreenIp, rightIp=$rightSubScreenIp")
        checkSubScreen(DisplayType.LEFT, leftSubScreenIp)
        checkSubScreen(DisplayType.RIGHT, rightSubScreenIp)
    }

    private fun checkSubScreen(displayType: DisplayType, ip: String?) {
        val clientId = ip?.let { GrpcManager.getClientIdByIp(it) }
        if (clientId == null) {
            Log.w(TAG, "TwelveSportGbViewModel::checkSubScreen: $displayType unconnected, ip=$ip")
            updateCollaborationState(displayType, CollaborationAreaState.UNCONNECTED)
            return
        }
        Log.i(TAG, "TwelveSportGbViewModel::checkSubScreen: $displayType connected, clientId=$clientId, send START")
        sendCollaborationCommand(clientId, CollaborationCmdEnum.START)
    }

    private fun sendCollaborationCommand(clientId: String, cmd: CollaborationCmdEnum) {
        val payload = GrpcCollaborationSportInfo(
            cmd = cmd.ordinal,
            sportSkuId = sportInfo?.sportSkuId ?: "",
            testCount = sportInfo?.testCount?.toInt() ?: 12,
            sceneId = sceneId ?: SceneConstants.PK,
            isDebug = BusiBaseConfig.getDebugEnable(),
            skeletonEnable = BusiBaseConfig.getSkeletonEnable()
        )
        Log.i(
            TAG,
            "TwelveSportGbViewModel::sendCollaborationCommand: clientId=$clientId, cmd=$cmd, " +
                "skuId=${payload.sportSkuId}, testCount=${payload.testCount}, " +
                "isDebug=${payload.isDebug}, skeletonEnable=${payload.skeletonEnable}"
        )
        GrpcManager.sendToClient(
            clientId, GrpcCmdCode.CMD_COLLABORATION_SPORT, payload.toJson(), false, null, null
        )
    }

    fun sendCollaborationFinish() {
        Log.i(TAG, "TwelveSportGbViewModel::sendCollaborationFinish")
        leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
        }
        rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
        }
    }

    private fun updateCollaborationState(displayType: DisplayType, state: CollaborationAreaState) {
        Log.i(TAG, "TwelveSportGbViewModel::updateCollaborationState: display=$displayType, state=$state")
        uiHandler.post {
            collaborationStateMap[displayType] = state
            _collaborationStateLD.value = collaborationStateMap.toMap()
        }
    }

    private fun sendSportCommand() {
        if (startCommandSent) {
            Log.w(TAG, "TwelveSportGbViewModel::sendSportCommand: already sent, skip")
            return
        }
        startCommandSent = true
        Log.i(TAG, "TwelveSportGbViewModel::sendSportCommand: send SPORT to joined sub screens")
        leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.SPORT)
        }
        rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.SPORT)
        }
    }

    fun registerSportCardStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_CARD_STATUS, GRPC_KEY) {
            val card = it.payload.toObject<GrpcSportCardStatus>() ?: return@registerServerHandler
            val type = it.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            MultiSportCardViewState.fromValue(card.status)?.let { state ->
                Log.i(TAG, "TwelveSportGbViewModel::registerSportCardStatus: display=$type, index=${card.index}, state=$state")
                uiHandler.post {
                    _changeSportViewStateLD.tryEmit(type to (card.index to state))
                }
            }
        }
    }

    fun registerAccountInfo() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_ACCOUNT_INFO, GRPC_KEY) { data ->
            val info = data.payload.toObject<GrpcAccountInfo>() ?: return@registerServerHandler
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val account = AccountInfo().apply {
                studentEntity = StudentEntity().apply {
                    accountName = info.name
                    userFaceImgUrl = info.avatar
                    accountId = info.accountId
                }
            }
            Log.i(TAG, "TwelveSportGbViewModel::registerAccountInfo: display=$displayType, index=${info.index}, name=${info.name}")
            if (displayType != DisplayType.LEFT && displayType != DisplayType.RIGHT) return@registerServerHandler
            uiHandler.post {
                when (displayType) {
                    DisplayType.LEFT -> {
                        leftPersonArray.put(info.index, MultiSportResult(account, info.index))
                        _identifiedLD.tryEmit(DisplayType.LEFT to (info.index to account))
                        syncIdentifiedCountWhileTip(DisplayType.LEFT)
                        checkCollaborationSportStart()
                    }
                    DisplayType.RIGHT -> {
                        rightPersonArray.put(info.index, MultiSportResult(account, info.index))
                        _identifiedLD.tryEmit(DisplayType.RIGHT to (info.index to account))
                        syncIdentifiedCountWhileTip(DisplayType.RIGHT)
                        checkCollaborationSportStart()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun registerResult() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_RESULT, GRPC_KEY) { data ->
            val result = data.payload.toObject<GrpcSportResult>() ?: return@registerServerHandler
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val score = result.sportResult.toIntOrNull() ?: 0
            Log.i(TAG, "TwelveSportGbViewModel::registerResult: display=$displayType, index=${result.index}, score=$score")
            uiHandler.post {
                when (displayType) {
                    DisplayType.LEFT -> {
                        leftPersonArray[result.index]?.count = score
                        leftPersonArray[result.index]?.recordId = result.recordId
                    }
                    DisplayType.RIGHT -> {
                        rightPersonArray[result.index]?.count = score
                        rightPersonArray[result.index]?.recordId = result.recordId
                    }
                    else -> {}
                }
                _singleResultLD.tryEmit(displayType to (result.index to score))
            }
        }
    }

    private fun registerCollaborationStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COLLABORATION_STATUS, GRPC_KEY) { data ->
            val status = data.payload.toObject<GrpcCollaborationStatus>()?.status
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val statusEnum = CollaborationStatusEnum.fromCode(status ?: -1)
            val state = when (statusEnum) {
                CollaborationStatusEnum.SPORTING -> CollaborationAreaState.OCCUPIED
                else -> CollaborationAreaState.NORMAL
            }
            Log.i(TAG, "TwelveSportGbViewModel::registerCollaborationStatus: display=$displayType, statusEnum=$statusEnum, state=$state")
            updateCollaborationState(displayType, state)
            updateOccupiedSubScreens(displayType, state == CollaborationAreaState.OCCUPIED)
        }
    }

    private fun registerIdentifiedCount() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_IDENTIFIED_COUNT, GRPC_KEY) { data ->
            val count = data.payload.toObject<GrpcSportIdentifiedCount>()?.count ?: 0
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            Log.i(TAG, "TwelveSportGbViewModel::registerIdentifiedCount: display=$displayType, count=$count")
            uiHandler.post {
                if (displayType == DisplayType.LEFT || displayType == DisplayType.RIGHT) {
                    identifiedCountMap[displayType] = count
                    _identifiedCountLD.tryEmit(Unit)
                }
            }
        }
    }

    private fun registerCountDownEnd() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COUNTDOWN_END, GRPC_KEY) {
            val displayType = it.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            Log.i(TAG, "TwelveSportGbViewModel::registerCountDownEnd: display=$displayType")
            uiHandler.post {
                subScreenCountDownEnded.add(displayType)
                checkCollaborationSportStart()
            }
        }
    }

    private fun registerGrpcSportStatusHandler() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_STATUS, GRPC_KEY) { data ->
            val info = data.payload.toObject<GrpcSportStatus>() ?: return@registerServerHandler
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            if (info.skuId != sportInfo?.sportSkuId) {
                Log.w(
                    TAG,
                    "TwelveSportGbViewModel::registerGrpcSportStatusHandler: sku mismatch grpc=${info.skuId}, local=${sportInfo?.sportSkuId}, still mark FINISHED if applicable display=$displayType"
                )
            }
            if (GrpcSportStatusEnum.fromCode(info.status) == GrpcSportStatusEnum.FINISHED) {
                if (displayType == DisplayType.LEFT || displayType == DisplayType.RIGHT) {
                    Log.i(TAG, "TwelveSportGbViewModel::registerGrpcSportStatusHandler: FINISHED from $displayType")
                    LogUtil.d("收到${displayType.name}副屏运动结束")
                    uiHandler.post {
                        subScreenSportFinished.add(displayType)
                        checkAllSportFinished()
                    }
                }
            }
        }
    }

    fun confirmClearIdentifyInfo() {
        Log.i(TAG, "TwelveSportGbViewModel::confirmClearIdentifyInfo: targets=${identifiedCountMap.keys}")
        identifiedCountMap.keys.forEach { displayType ->
            val ip = when (displayType) {
                DisplayType.LEFT -> leftSubScreenIp
                DisplayType.RIGHT -> rightSubScreenIp
                else -> null
            }
            ip?.let { GrpcManager.getClientIdByIp(it) }?.let { clientId ->
                GrpcManager.sendToClient(
                    clientId, GrpcCmdCode.CMD_CLEAR_IDENTIFY_INFO, "", false, null, null
                )
            }
            clearSubScreenIdentifiedData(displayType)
        }
        identifiedCountMap.clear()
    }

    fun cancelClearIdentifyInfo() { identifiedCountMap.clear() }

    fun confirmForceJoin() {
        Log.i(TAG, "TwelveSportGbViewModel::confirmForceJoin: occupied=$occupiedSubScreens")
        occupiedSubScreens.forEach { displayType ->
            val ip = when (displayType) {
                DisplayType.LEFT -> leftSubScreenIp
                DisplayType.RIGHT -> rightSubScreenIp
                else -> null
            }
            ip?.let { GrpcManager.getClientIdByIp(it) }?.let { clientId ->
                sendCollaborationCommand(clientId, CollaborationCmdEnum.FORCE_JOIN)
            }
        }
    }

    fun cancelForceJoin() {}

    private fun clearSubScreenIdentifiedData(displayType: DisplayType) {
        val personArray = when (displayType) {
            DisplayType.LEFT -> leftPersonArray
            DisplayType.RIGHT -> rightPersonArray
            else -> return
        }
        val indexes = mutableListOf<Int>()
        for (i in 0 until personArray.size()) indexes.add(personArray.keyAt(i))
        personArray.clear()
        uiHandler.post {
            indexes.forEach { index ->
                _changeSportViewStateLD.tryEmit(displayType to (index to MultiSportCardViewState.NOBODY))
            }
        }
    }

    private fun syncIdentifiedCountWhileTip(displayType: DisplayType) {
        uiHandler.post {
            if (identifiedCountMap.containsKey(displayType)) {
                identifiedCountMap[displayType] =
                    if (displayType == DisplayType.LEFT) leftPersonArray.size()
                    else rightPersonArray.size()
                _identifiedCountLD.tryEmit(Unit)
            }
        }
    }

    private fun updateOccupiedSubScreens(displayType: DisplayType, occupied: Boolean) {
        if (displayType != DisplayType.LEFT && displayType != DisplayType.RIGHT) return
        uiHandler.post {
            val changed = if (occupied) occupiedSubScreens.add(displayType)
            else occupiedSubScreens.remove(displayType)
            if (changed) _subScreenOccupiedLD.tryEmit(occupiedSubScreens.toSet())
        }
    }


    override fun identifyFail(index: Int) {
        Log.w(TAG, "FourSportGbViewModel::identifyFail: $index")
        changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFY_FAIL)
        //处理下一道是否有人
        var tempNumber = (currentIdentifyNumber + 1) % SPORT_NUMBER
        var findOk = false
        while (tempNumber < SPORT_NUMBER) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (sportResultMap[tempNumber] == null) {
                currentIdentifyNumber = tempNumber
                changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFYING)
                findOk = true
                break
            }
            tempNumber++
        }
        //没有找到下一道，置为-1，再从头开始识别
        if (!findOk) {
            currentIdentifyNumber = -1
        }

    }

    override fun identifyOk(accountIndex: Int, accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            Log.w(TAG, "FourSportGbViewModel::identifyOk: 识别到人脸：但已不是识别状态 $accountIndex")
            return
        }
        Log.w(TAG, "FourSportGbViewModel::identifyOk: index: $accountIndex, accountName: ${accountInfo.studentEntity?.accountName}")
        synchronized(this) {
            viewModelScope.launch(Dispatchers.Main) {
                val student = accountInfo.studentEntity ?: run {
                    Log.w(TAG, "TwelveSportGbViewModel::identifyOk: studentEntity null, skip index=$accountIndex")
                    return@launch
                }
                //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
                if (student.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                    val duplicated = sportResultMap
                        .filter { it.value.accountInfo.studentEntity?.accountId == student.accountId }
                        .keys
                        .toList()
                    duplicated.forEach { index ->
                        sportResultMap.remove(index)
                        changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
                    }
                }

                //如果是组织测试，校验学生是否在计划中
                val checkResult = withContext(Dispatchers.IO) {
                    checkStudent(student.accountId, ptPlanId)
                }
                if (checkResult) {
                    //存已识别的人信息
                    //识别到人，且有效时，取消倒计时再处理显示逻辑
//                    startCountDownTimer.cancel()
                    SpeechWrapper.speech(getString(R.string.identified_someone, student.accountName))
                    sportResultMap[accountIndex] = MultiSportResult(accountInfo, accountIndex)
                    Log.i(TAG, "FourSportGbViewModel::identifyOk: 人脸识别结果：姓名：${student.accountName}; 道数 $accountIndex")
                    _identifiedLD.tryEmit(DisplayType.MAIN to Pair(accountIndex, accountInfo))
                    //给accountInfo赋值，用来获取存储视频的地址
                    if (student.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                        accountInfoLD.postValue(accountInfo)
                    }
                    changeMultiIdentityCardState(accountIndex, MultiSportCardViewState.PREPARED)

                    currentIdentifyNumber = -1
                    //处理竞技倒计时
                    if (sportResultMap.size == 1 && startCountDownTimer == null) {
                        startCountDownTimer = createCountDownTimer()
                        startCountDownTimer?.start()
//                        playCutDown()
                    }
                    //如果已识别满员，统一检查协同开始条件
                    if (sportResultMap.size == SPORT_NUMBER) {
                        checkCollaborationSportStart()
                        return@launch
                    }
                    //重新复位倒计时时间
//                    countDownTime = getDefaultCountDownTime()
//                    countDownNoticeLD.value =
//                        "${getString(R.string.race_start_countdown)}：${countDownTime}"
                    //第一个人识别成功，则开始倒计时

                    //识别到人，取消超时机制
                    timeOutHelper.cancel()
                } else {
                    currentIdentifyNumber = -1
                    ToastUtil.getInstance(getApplication(), getString(R.string.busi_base_name_without_plan, student.accountName)).show()
                }
            }

        }
    }

    /**
     * 处理成绩，通知UI层更新成绩UI
     */
    private fun handleSportResult(jumpers: List<Jumper>) {
        //运动中才更新成绩，否则结束成绩回调会是0
        if (sportState != MultiSportState.SPORTING) return
        jumpers.forEachIndexed { index, jumper ->
            val personResult = sportResultMap[index]
            if (jumper.skeleton == null) {
                if (personResult == null) {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY_SPORT)
                } else {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.LEAVE)
                }
            } else {
                if (personResult != null) {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.SPORTING)
                } else {
                    // 其他人到了空闲位置，不做处理
                }
            }
        }
        jumpers.forEachIndexed { index, jumper ->
            if (jumper.skeleton == null) return@forEachIndexed
            val personResult = sportResultMap[index] ?: return@forEachIndexed
            personResult.count = jumper.score
            jumper.exercisePrescription?.let { pre -> prescriptions.put(index, pre) }
            _singleResultLD.tryEmit(DisplayType.MAIN to (index to jumper.score))
        }
    }

    /**
     * 记录每个位置的状态，并通知UI层更新卡片状态
     * 只处理卡片状态，此函数回调，一定有人在框里
     */
    @Synchronized
    private fun handlePersonState(jumpers: List<Jumper>) {
        jumpers.forEachIndexed { index, jumper ->
            val personResult = sportResultMap[index]
            if (sportState == MultiSportState.IDENTIFYING) {
                if (jumper.skeleton == null) {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
                } else {
                    if (personResult == null) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.IDENTIFYING)
                    } else {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.IDENTIFIED)
                    }
                }
            } else if (sportState == MultiSportState.SPORTING) {
                if (jumper.skeleton == null) {
                    if (personResult == null) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY_SPORT)
                    } else {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.LEAVE_TO_SPORTING)
                    }
                } else {
                    if (personResult != null) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.SPORTING)
                    }
                }
            }
        }
    }


    @Synchronized
    private fun handleIdentify(jumpers: List<Jumper>) {
        if (sportState != MultiSportState.IDENTIFYING) {
            return
        }
        if (currentIdentifyNumber != -1) {
            Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            if (currentIdentifyNumber !in jumpers.indices) {
                currentIdentifyNumber = -1
                return
            }
            val jumper = jumpers[currentIdentifyNumber]
            //没找到人，说明，人已离开位置，则重置状态
            if (jumper.skeleton == null) {
                Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.NOBODY)
                currentIdentifyNumber = -1
            } else {
                if (judgeHandUp(currentIdentifyNumber, jumper.skeleton)) {
                    //送数据进行人脸识别
                    Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：送数据进行人脸识别: 道数 $currentIdentifyNumber")
                    val currentYuvData = multiProcessor.getCurrentYuvData()
                    val yuvBytes = currentYuvData.yuv ?: return
                    val skeleton = jumper.skeleton ?: return
                    identify(
                        getIdentityRect(skeleton, currentYuvData.width, currentYuvData.height),
                        yuvBytes, currentYuvData.width, currentYuvData.height, currentIdentifyNumber)
                } else {
                    //手已放下：让出识别通道（不算识别失败，人还在），让其他举手的位置优先识别
                    Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：手已放下，让出识别通道; 道数 $currentIdentifyNumber")
                    changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.STANDBY)
                    currentIdentifyNumber = -1
                }
            }
            return
        }
        //如果正在识别，则等待识别完再处理下一个人脸识别
        if (isIdentifying()) {
            Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：人已离开位置，但正在识别人脸; 道数 $currentIdentifyNumber")
            return
        }

        run identify@{
            jumpers.forEachIndexed { index, jumper ->
                if (jumper.skeleton != null && sportResultMap[index] == null && judgeHandUp(index, jumper.skeleton)) {
                    //只有举手的位置才送人脸识别
                    currentIdentifyNumber = index
                    val yuvData = multiProcessor.getCurrentYuvData()
                    val yuvBytes = yuvData.yuv ?: return
                    identify(getIdentityRect(jumper.skeleton, yuvData.width, yuvData.height), yuvBytes, yuvData.width, yuvData.height,
                        currentIdentifyNumber)
                    changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFYING)
                    Log.i(TAG, "FourSportGbViewModel::handleIdentify: 处理人脸识别：人位置状态OK，开始识别该位置，道数 $currentIdentifyNumber")
                    return@identify
                }
            }
        }
        jumpers.forEachIndexed { index, jumper ->
            if (sportResultMap[index] != null) {
                return@forEachIndexed
            }
            if (jumper.skeleton == null) {
                changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
            } else {
                if (index == currentIdentifyNumber) {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.IDENTIFYING)
                } else {
                    changeMultiIdentityCardState(index, MultiSportCardViewState.STANDBY)
                }
            }
        }
    }

    /**
     * 举手判定：手腕低于肩膀即认为该侧手已举起
     *
     * 与 SingleIdentityProcessor.judgeHandUp 逻辑保持一致，区别是：
     * 1. 按道次记录手型，避免多道次互相覆盖；
     * 2. [HAND_UP_TYPE_NONE] 直接返回 true（不检测举手即放行），而不是返回 false。
     *
     * @param index 道次
     * @param skeleton 该道次人体骨骼
     * @return true 表示判定到举手，可以继续人脸识别
     */
    private fun judgeHandUp(index: Int, skeleton: AdlPoseSkeleton?): Boolean {
        if (skeleton == null) return false
        return when (handUpType) {
            HandUpType.LEFT -> {
                // 判断左手
                if (skeleton.lWrist == null || skeleton.lShoulder == null) return false
                val isLeftHandUp = skeleton.lWrist.y < skeleton.lShoulder.y
                if (isLeftHandUp) {
                    handRaisedMap.put(index, HandUpType.LEFT)
                }
                isLeftHandUp
            }
            HandUpType.RIGHT -> {
                // 判断右手
                if (skeleton.rWrist == null || skeleton.rShoulder == null) return false
                val isRightHandUp = skeleton.rWrist.y < skeleton.rShoulder.y
                if (isRightHandUp) {
                    handRaisedMap.put(index, HandUpType.RIGHT)
                }
                isRightHandUp
            }
            HandUpType.BOTH -> {
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
                if (leftHandUp || rightHandUp) {
                    handRaisedMap.put(index, HandUpType.BOTH)
                }
                leftHandUp || rightHandUp
            }

            HandUpType.NONE -> true
        }
    }

    /**
     * 获取某个道次最近一次判定到的手型，未举手过返回 [HAND_UP_TYPE_NONE]
     */
    fun getHandRaisedType(index: Int): HandUpType = handRaisedMap.get(index, HandUpType.NONE)

    /**
     * 主屏手动开始：直接进入协同开始流程
     */
    fun readyGo() {
        startSport()
    }

    /**
     * 协同开始条件变化时统一调用：满足则开始主屏 3-2-1，并向副屏发 SPORT
     */
    private fun checkCollaborationSportStart() {
        val canStart = canStartCollaborationSport()
        Log.i(
            TAG,
            "TwelveSportGbViewModel::checkCollaborationSportStart: canStart=$canStart, " +
                "mainCount=${sportResultMap.size}, leftCount=${leftPersonArray.size()}, " +
                "rightCount=${rightPersonArray.size()}, mainCountDownEnded=$mainCountDownEnded, " +
                "subCountDownEnded=$subScreenCountDownEnded, collabState=$collaborationStateMap"
        )
        if (!canStart) {
            Log.w(TAG, "TwelveSportGbViewModel::checkCollaborationSportStart: conditions not met")
            return
        }
        startSport()
    }

    private fun startSport() {
        Log.i(TAG, "TwelveSportGbViewModel::startSport")
        tryStartSport()
        sendSportCommand()
    }

    /**
     * 协同运动开始条件（任一满足即可开始）：
     * 条件1：所有屏（主屏+已加入副屏）都识别满员
     * 条件2：所有屏（主屏+已加入副屏）都倒计时结束
     */
    private fun canStartCollaborationSport(): Boolean {
        val connectedSubScreens = connectedSubScreenTypes()
        if (sportResultMap.isEmpty()) return false
        if (DisplayType.LEFT in connectedSubScreens &&
            collaborationStateMap[DisplayType.LEFT] == CollaborationAreaState.NORMAL &&
            leftPersonArray.size() <= 0
        ) return false
        if (DisplayType.RIGHT in connectedSubScreens &&
            collaborationStateMap[DisplayType.RIGHT] == CollaborationAreaState.NORMAL &&
            rightPersonArray.size() <= 0
        ) return false
        val allScreenFull = sportResultMap.size >= SPORT_NUMBER &&
            (DisplayType.LEFT !in connectedSubScreens || leftPersonArray.size() >= SPORT_NUMBER) &&
            (DisplayType.RIGHT !in connectedSubScreens || rightPersonArray.size() >= SPORT_NUMBER)
        if (allScreenFull) return true
        if (!mainCountDownEnded) return false
        return connectedSubScreens.all {
            it in subScreenCountDownEnded ||
                collaborationStateMap[it] != CollaborationAreaState.NORMAL
        }
    }

    private fun needWaitOtherAreaJoin(): Boolean {
        if (!mainCountDownEnded) return false
        return connectedSubScreenTypes().any {
            collaborationStateMap[it] == CollaborationAreaState.NORMAL &&
                it !in subScreenCountDownEnded
        }
    }

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
     * 检查并真正开始主屏 3-2-1 倒计时，只执行一次
     */
    private fun tryStartSport() {
        if (mainSportStarted) {
            Log.w(TAG, "TwelveSportGbViewModel::tryStartSport: already started, skip")
            return
        }
        mainSportStarted = true
        Log.i(TAG, "TwelveSportGbViewModel::tryStartSport: enter 3-2-1 countdown")
        //停止倒计时音效
        stopPlayCutDown()
        startCountDownTimer?.cancel()
        _countDownFinishLD.tryEmit(DisplayType.MAIN to true)
        //开始3-2-1倒计时就表示比赛已经开始
        sportState = MultiSportState.COUNTDOWN
    }

    private fun changeTo4K() {
        ipcHkSource.pauseProcessor()
        setResolution(ipcHkSource, CameraSize.SIZE_4K)
        SystemClock.sleep(100)
        _stopPreviewLD.tryEmit(multiProcessor)
        SystemClock.sleep(100)
        ipcHkSource.restartProcessor()
        _startPreviewLD.tryEmit(Pair(ipcHkSource, multiProcessor))
        SystemClock.sleep(200)
    }

    private fun changeTo2K() {
        ipcHkSource.pauseProcessor()
        setResolution(ipcHkSource, CameraSize.SIZE_2K)
        SystemClock.sleep(100)
        _stopPreviewLD.tryEmit(multiProcessor)
        SystemClock.sleep(100)
        ipcHkSource.restartProcessor()
        _startPreviewLD.tryEmit(Pair(ipcHkSource, multiProcessor))
        SystemClock.sleep(200)
    }

    private fun changeMultiIdentityCardState(index: Int, state: MultiSportCardViewState) {
        val currentState = cardStateMap[index]
        if (currentState == state) return
        cardStateMap[index] = state
        uiHandler.post {
            Log.i(TAG, "FourSportGbViewModel::changeMultiIdentityCardState: index $index state ${state.name}")
            _changeSportViewStateLD.tryEmit(DisplayType.MAIN to Pair(index, state))
        }
    }

    /**
     * 运动结束统一检查：主屏结束且所有已加入协同的副屏都结束后，再合并成绩展示
     */
    private fun checkAllSportFinished() {
        if (!mainSportFinished) {
            Log.w(TAG, "TwelveSportGbViewModel::checkAllSportFinished: main not finished yet")
            return
        }
        val joinedSubScreens =
            listOf(DisplayType.LEFT, DisplayType.RIGHT).filter { isSubScreenJoined(it) }
        if (joinedSubScreens.any { it !in subScreenSportFinished }) {
            Log.w(TAG, "TwelveSportGbViewModel::checkAllSportFinished: waiting, joined=$joinedSubScreens, finished=$subScreenSportFinished")
            LogUtil.d("等待副屏运动结束：已加入=$joinedSubScreens，已结束=$subScreenSportFinished")
            return
        }
        Log.i(TAG, "TwelveSportGbViewModel::checkAllSportFinished: all finished, merge results")
        handleMergedResult()
    }

    /**
     * 合并主屏与已连接副屏成绩，生成带区域信息的结果项，按高优排序并计算总排名
     */
    private fun handleMergedResult() {
        if (sportInfo == null) return
        val mergedItems = mergeAllAreaResults()
        if (mergedItems.isEmpty()) {
            Log.w(TAG, "TwelveSportGbViewModel::handleMergedResult: empty result list")
            return
        }
        val topSummary = mergedItems.take(3).joinToString { "${it.name}:${it.count}" }
        Log.i(TAG, "TwelveSportGbViewModel::handleMergedResult: total=${mergedItems.size}, top=$topSummary")
        _toDoneLD.tryEmit(mergedItems)
        // 主屏本地成绩仍按本屏道次上传
        val mainUpload = Array(SPORT_NUMBER) { MultiSportResult(AccountInfo(), 0) }
        sportResultMap.forEach { (index, value) ->
            if (index in mainUpload.indices) {
                mainUpload[index] = value
            }
        }
        handleUploadResult(mainUpload.toMutableList())
    }

    /**
     * 合并主屏与已连接副屏成绩，生成结果页统一数据项，按成绩高优排序并重新计算总排名
     */
    private fun mergeAllAreaResults(): List<ThirtySportResultItem> {
        val areaNameMap = mapOf(
            DisplayType.MAIN to getString(R.string.area_main),
            DisplayType.LEFT to getString(R.string.area_left),
            DisplayType.RIGHT to getString(R.string.area_right)
        )
        val merged = mutableListOf<ThirtySportResultItem>()
        sportResultMap.values.forEach { result ->
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
        merged.sortWith(compareBy { -it.count })
        var preRank = 0
        var preScore = -1
        merged.forEach { item ->
            val rank = if (item.count == preScore) preRank else preRank + 1
            preRank = rank
            preScore = item.count
            item.result.rank = rank
        }
        return merged
    }

    /**
     * 处理上传成绩
     */
    private fun handleUploadResult(resultList: MutableList<MultiSportResult>) {
        GlobalScope.launch(Dispatchers.IO) {
            val multiId = if (personIndex > 0) {
                "${resultList[0].meetGroup?.classId}${personIndex}"
            } else {
                System.currentTimeMillis().toString()
            }
            SystemClock.sleep(1500)
            for (index in resultList.indices) {
                var multiResult = resultList[index]
                if (multiResult.accountInfo.studentEntity == null) {
                    continue
                }
                var sportResult = SportResult()
                val duration = sportInfo?.endValue?.toIntOrNull()
                if (duration == null) {
                    Log.w(TAG, "TwelveSportGbViewModel::handleUploadResult: invalid endValue=${sportInfo?.endValue}, skip upload")
                    return@launch
                }
                sportResult.sportDuration = duration
                sportResult.maxComboCount = 0
                sportResult.sportAchievement = multiResult.count
                sportResult.perfectCount = 0
                sportResult.goodCount = 0
                sportResult.comeOnCount = 0
                sportResult.perfectScore = 0
                sportResult.goodScore = 0
                sportResult.comeOnScore = 0
                sportResult.userId = multiResult.accountInfo.studentEntity?.accountId
                sportResult.startTime = startTime
                sportResult.endTime = endTime
                sportResult.rank = multiResult.rank
                sportResult.multiPersonIndex = personIndex
                sportResult.multiPersonId = multiId
                sportResult.sportResult = multiResult.count.toString()
                sportResult.sportNo = (multiResult.number + 1).toString()
                sportResult.videoCover = videoCover
                sportResult.videoFile = videoFile
                sportResult.reportVersion = BusiBaseConfig.getReportVersion()

                var indicatorsList: MutableList<SportIndicatorsEntity<*, *>>? = null
                val prescription = prescriptions.get(multiResult.number)
                indicatorsList = IndicatorsHelper.getSkipIndicatorsList(
                    prescription, startTime, endTime, null
                )
                if (indicatorsList != null) {
                    IndicatorsHelper.createIndicatorsResultDto(
                        IndicatorsConstants.SKIP_CODE, indicatorsList, startTime, endTime,
                        multiResult.count, videoFile, videoCover,
                        multiResult.accountInfo.studentEntity?.accountName,
                        multiResult.accountInfo.studentEntity?.userSex?.toInt(),
                        multiResult.accountInfo.studentEntity?.userFaceImgUrl
                    )?.let { indicatorsMap.put(multiResult.number, it) }
                }

                var recordPlan: SportRecordEntity.RecordPlan? = null
                if (sceneId == SceneConstants.ORGANIZE_TEST) {
                    recordPlan = SportRecordEntity.RecordPlan()
                        .apply {
                            ptPlanId = this@TwelveSportGbViewModel.ptPlanId
                            ptStandardId = this@TwelveSportGbViewModel.ptStandardId
                        }
                }
                var sportMeet: RecordMeeting? = null
                recordMeet?.let {
                    sportMeet = RecordMeeting().apply {
                        meetId = recordMeet?.meetId
                        meetGroupType = recordMeet?.meetGroupType
                        meetSportSkuId = recordMeet?.meetSportSkuId
                        meetRound = recordMeet?.meetRound
                        teamUnitCode = recordMeet?.teamUnitCode
                        meetTeamNo = recordMeet?.meetTeamNo
                        meetNumberCode = multiResult.meetGroup?.numberCode
                        meetRunwayCode = multiResult.meetGroup?.runwayCode
                        serialNumber = multiResult.meetGroup?.serialCode
                    }
                }
                uploadSportResult(
                    sceneId ?: SceneConstants.PK,
                    sportResult,
                    sportResult.userId,
                    indicatorList = indicatorsList,
                    recordPlan = recordPlan,
                    recordMeeting = sportMeet,
                )
            }
        }
    }

    private fun playCutDown() {
        asyncPlayer.play(getApplication(), getCountDownBgmUri(), false, AudioManager.STREAM_MUSIC)
    }

    private fun stopPlayCutDown() {
        asyncPlayer.stop()
    }

    fun getCountDownBgmUri(): Uri = ("android.resource://" + ApplicationUtil.getContext().packageName + "/" + R.raw.bgm_15_prepare).toUri()


    private fun getIdentityRect(skeleton: AdlPoseSkeleton, width: Int, height: Int): Rect {
//        val rect = Rect(0, 0, 50, 50)
//        val chin = skeleton.chin
//        val headTop = skeleton.headTop
//        if (chin != null && headTop != null) {
//            val faceH = (chin.y - headTop.y) * 1.2f
//            rect.top = headTop.y.toInt().coerceAtLeast(0)
//            rect.bottom = (chin.y + faceH / 8).toInt().coerceAtMost(height)
//            rect.left = (headTop.x - faceH / 2).toInt().coerceAtLeast(0)
//            rect.right = (headTop.x + faceH / 2).toInt().coerceAtMost(width)
//            return rect
//        }
//        return rect


        if (width <= 0 || height <= 0) return Rect()

        if (skeleton == null) {
            LogUtil.d("CustomIdentifyRectHelper: skeleton is null, use fallback rect", true)
            return fallbackRect()
        }

        val rect = faceRect(skeleton)
            ?: headRect(skeleton)
            ?: shoulderRect(skeleton)
            ?: fallbackRect().also {
                LogUtil.d("CustomIdentifyRectHelper: key point is missing, use fallback rect", true)
            }

        correctRect(rect, width, height)
        return rect
    }

    /**
     * 脸部框：鼻子 + 双眼可用时优先，人脸占比最高。
     * 以鼻子为中心，2倍眼距为边长。
     */
    private fun faceRect(skeleton: AdlPoseSkeleton): Rect? {
        val nose = skeleton.nose ?: return null
        val lEye = skeleton.lEye
        val rEye = skeleton.rEye
        if (lEye == null && rEye == null) return null
        val eyeSpan = (if (lEye != null && rEye != null) {
            max(SportHelper.getDistance(nose, lEye), SportHelper.getDistance(nose, rEye)) * 2f
        } else if (lEye != null) {
            SportHelper.getDistance(nose, lEye) * 2f
        } else {
            SportHelper.getDistance(nose, rEye) * 2f
        }).toFloat()
        if (eyeSpan <= 1f) return null
        val left = nose.x - eyeSpan * 2f
        val right = nose.x + eyeSpan * 2f
        val estimatedTop = if (lEye != null && rEye != null) {
            min(nose.y, min(lEye.y, rEye.y)) - eyeSpan * 2f
        } else if (lEye != null) {
            min(nose.y, lEye.y) - eyeSpan * 2f
        } else {
            min(nose.y, rEye.y) - eyeSpan * 2f
        }
        val top = skeleton.headTop?.y?.let { min(it, estimatedTop) } ?: estimatedTop
        val estimatedBottom = if (lEye != null && rEye != null) {
            max(nose.y, max(lEye.y, rEye.y)) + eyeSpan * 2f
        } else if (lEye != null) {
            max(nose.y, lEye.y) + eyeSpan * 2f
        } else {
            max(nose.y, rEye.y) + eyeSpan * 2f
        }
        val bottom = skeleton.chin?.y?.let { max(it, estimatedBottom) } ?: estimatedBottom
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    /**
     * 头部框：鼻子 + 双耳可用时优先，人脸占比最高。
     * 以鼻子为中心，1.4倍耳距为边长。
     */
    private fun headRect(skeleton: AdlPoseSkeleton): Rect? {
        val nose = skeleton.nose ?: return null
        val lEar = skeleton.lEar
        val rEar = skeleton.rEar
        if (lEar == null && rEar == null) return null
        val earSpan = if (lEar != null && rEar != null) {
            max(abs(nose.x - lEar.x), abs(nose.x - rEar.x)) * 2f
        } else if (lEar != null) {
            abs(nose.x - lEar.x) * 2f
        } else {
            abs(nose.x - rEar.x) * 2f
        }
        if (earSpan <= 1f) return null
        val left = nose.x - earSpan * 1.4f
        val right = nose.x + earSpan * 1.4f
        val estimatedTop = if (lEar != null && rEar != null) {
            min(nose.y, min(lEar.y, rEar.y)) - earSpan * 1.4f
        } else if (lEar != null) {
            min(nose.y, lEar.y) - earSpan * 1.4f
        } else {
            min(nose.y, rEar.y) - earSpan * 1.4f
        }
        val top = skeleton.headTop?.y?.let { min(it, estimatedTop) } ?: estimatedTop
        val estimatedBottom = if (lEar != null && rEar != null) {
            max(nose.y, max(lEar.y, rEar.y)) + earSpan * 1.4f
        } else if (lEar != null) {
            max(nose.y, lEar.y) + earSpan * 1.4f
        } else {
            max(nose.y, rEar.y) + earSpan * 1.4f
        }
        val bottom = skeleton.chin?.y?.let { max(it, estimatedBottom) } ?: estimatedBottom
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    /**
     * 头肩框：双肩可用时使用。
     * left/right = 两肩的 x 范围，bottom = 较低的肩点（肩线），
     * top = 头顶；无头顶点时由肩线向上反推 1.5 倍肩宽。
     */
    private fun shoulderRect(skeleton: AdlPoseSkeleton): Rect? {
        val lShoulder = skeleton.lShoulder ?: return null
        val rShoulder = skeleton.rShoulder ?: return null
        val shoulderWidth = abs(lShoulder.x - rShoulder.x)
        if (shoulderWidth <= 0f) return null

        val shoulderWidthRedundancy = shoulderWidth * 0.2f
        val left = min(lShoulder.x, rShoulder.x) - shoulderWidthRedundancy
        val right = max(lShoulder.x, rShoulder.x) + shoulderWidthRedundancy
        val bottom = max(lShoulder.y, rShoulder.y) + shoulderWidthRedundancy
        val expectTop = bottom - shoulderWidth * SHOULDER_WIDTH_TO_HEAD_RATIO
        val top = skeleton.headTop?.let { min(it.y, expectTop) } ?: expectTop
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    /**
     * 兜底框：默认整帧。
     * 若业务不允许「全帧识别」（怕误识别/耗时长），重写返回 Rect() 空框，
     * 并配合调用处的 rect.isEmpty 判断跳过本次识别。
     */
    private fun fallbackRect(): Rect = Rect(0, 0, 50, 50)

    /**
     * 边界夹紧 + 最小尺寸纠正。
     * 同时保证 left <= right、top <= bottom，避免传给 detectFace 越界。
     */
    private fun correctRect(rect: Rect, width: Int, height: Int) {
        if (rect.width() < MIN_RECT_SIZE) {
            val centerX = (rect.left + rect.right) / 2
            rect.left = centerX - MIN_RECT_SIZE / 2
            rect.right = rect.left + MIN_RECT_SIZE
        }
        if (rect.height() < MIN_RECT_SIZE) {
            val centerY = (rect.top + rect.bottom) / 2
            rect.top = centerY - MIN_RECT_SIZE / 2
            rect.bottom = rect.top + MIN_RECT_SIZE
        }
        if (rect.left < 0) {
            rect.right -= rect.left
            rect.left = 0
        }
        if (rect.right > width) {
            val overflow = rect.right - width
            rect.left -= overflow
            rect.right = width
        }
        if (rect.top < 0) {
            rect.bottom -= rect.top
            rect.top = 0
        }
        if (rect.bottom > height) {
            val overflow = rect.bottom - height
            rect.top -= overflow
            rect.bottom = height
        }
        rect.left = rect.left.coerceIn(0, width)
        rect.right = rect.right.coerceIn(0, width)
        rect.top = rect.top.coerceIn(0, height)
        rect.bottom = rect.bottom.coerceIn(0, height)
    }


    fun getIndicatorsResultDto(number: Int): IndicatorsResultDto? = indicatorsMap.getOrDefault(number, null)

    override fun initFaceIdentify() {
        super.initFaceIdentify()
        //0~9索引对应1~10道
        multiIdentityEngine.addIdentityConfig(0, PipelineFaceIdentifyConfig(2, 0.55f))
        multiIdentityEngine.addIdentityConfig(1, PipelineFaceIdentifyConfig(2, 0.55f))
        multiIdentityEngine.addIdentityConfig(2, PipelineFaceIdentifyConfig(2, 0.55f))
        multiIdentityEngine.addIdentityConfig(3, PipelineFaceIdentifyConfig(2, 0.55f))
    }

    override fun reset() {
        super.reset()
        //重置，重新开始运动流程
        //清除所有数据
        indicatorsMap.clear()
        cardStateMap.clear()
        for (index in 0 until SPORT_NUMBER) {
            cardStateMap[index] = MultiSportCardViewState.NOBODY
        }
        sportResultMap.clear()
        handRaisedMap.clear()
        leftPersonArray.clear()
        rightPersonArray.clear()
        subScreenCountDownEnded.clear()
        subScreenSportFinished.clear()
        startCommandSent = false
        sportStartedFlag = false
        mainCountDownEnded = false
        mainSportStarted = false
        mainSportFinished = false

        //重置人脸识别
        initFaceIdentify()
    }

    fun isSupportReport(): Boolean {
        return fourSportConfig?.isSupportReport != false
    }

    /**
     *
     */
    fun queryOnlineReport(recordId: String?) {
        if (recordId.isNullOrEmpty()) {
            ToastUtil.getInstance(getApplication(), "暂无报告")
                .show()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val recordUrl = SportRecordService.querySportOnlineReport(recordId)
            if (recordUrl?.startsWith("http") == true) {
                _showReportLD.tryEmit(recordUrl)
            } else {
                ToastUtil.getInstance(getApplication(), "暂无报告").show()
            }
            hideLoading()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sendCollaborationFinish()
        GrpcManager.unregisterHandler(GRPC_KEY)
        sportingCountDownTimer?.cancel()
        startCountDownTimer?.cancel()
        multiProcessor.onRelease()
        sportState = MultiSportState.IDLE
        uiHandler.removeCallbacksAndMessages(null)
        focalHandler.removeCallbacksAndMessages(null)
        videoRecorder.onStop()
        videoRecorder.onRelease()
    }
}
