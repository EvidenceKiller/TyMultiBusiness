package com.ty.bus.multi.core.base

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
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.SportOverlayMultiRegion
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.core.TsTaskBodyFilter
import com.adl.mount.sport.TsSportChannel
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.SportIndicatorsEntity
import com.adl.sport.general.groupskippingremix.GroupSkippingLRemixParser
import com.adl.sport.general.groupskippingremix.GroupSkippingRemixParser
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.hcnetsdk.control.DevConfigGuider
import com.ty.tybusbase.expand.getString
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.factory.AbstractMultiSportFactory
import com.ty.bus.multi.core.factory.MultiSportProduceFactory
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.SoundPlayUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.core.util.size
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.SportRecordEntity
import com.adl.sport.general.RegionFilter
import com.adl.sport.general.groupskippingremix.GroupSkippingManLRemix
import com.adl.sport.general.groupskippingremix.GroupSkippingManRemix
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.ty.bus.multi.core.config.SportModel

import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.IndicatorsConstants
import com.ty.tybusbase.helper.IndicatorsHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import androidx.core.util.isEmpty
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SportRecordEntity.RecordMeeting
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService



import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import androidx.core.net.toUri
import com.adl.service.web.SportRecordService
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.expand.eventFlow
import com.ty.tybusbase.expand.checkHandUp
import com.ty.tybusbase.expand.getDisplayType
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
import kotlinx.coroutines.channels.BufferOverflow


/**
 * 导航区协同状态
 */
enum class CollaborationAreaState {
    /** 正常，可点击 */
    NORMAL,

    /** 未连接 */
    UNCONNECTED,

    /** 设备占用中 */
    OCCUPIED,
}

/**
 * 多人协同运动 ViewModel 基类（主屏 + 左/右副屏 gRPC 协同）。
 *
 * 职责：识别流水线（handlePersonState/handleIdentify）、竞技倒计时、运动计时、
 * 三屏协同（开始条件/等待加入/占用/强制加入/清空识别）、成绩合并与上传、在线报告。
 *
 * 新增一种多人运动（如30人跳远）时，继承本类并按需覆写以下钩子即可，无需复制协同逻辑：
 * - 配置类：自行持有配置对象，覆写 [getSportModel]/[isSupportReport]
 * - 识别：[initFaceIdentify]（人脸阈值）、[getIdentityRect]（识别框）、[getBodyFilter]
 * - 流程：[initProcess]（处理器/解析器装配）、[readyGo]、[tryStartSport]
 * - 结果：[handleUploadResult]（指标/处方解析）、[mergeAllAreaResults]（结果页合并策略）
 *
 * 注意：主屏人员数组为 [skipPersonArray]，左右副屏为 [leftPersonArray]/[rightPersonArray]。
 */
open class BaseCollabSportViewModel(application: Application) :
    BaseMultiPipelineIdentifyViewModel(application) {

    protected val grpcKey = "BaseCollabSportViewModel" //注册Grpc消息的ID
    private val leftSubScreenIp by lazy { BusiBaseConfig.readLeftSubScreenIp() }
    private val rightSubScreenIp by lazy { BusiBaseConfig.readRightSubScreenIp() }

    /**
     * 是否支持副屏协同（默认支持；单屏运动页覆写为false）：
     * false时不向客户端发送任何Grpc消息，协同判定自动退化（开始只看主屏、结束不等副屏、成绩只取主屏）
     */
    protected open fun isSupportCollaboration(): Boolean = true

    /**
     * 参与协同的副屏区域集合（默认左右副屏）。
     * 不支持协同的运动（isSupportCollaboration=false）返回空集合，协同层自动退化。
     */
    open fun getSubScreenAreas(): List<DisplayType> =
        if (isSupportCollaboration()) {
            listOf(DisplayType.LEFT, DisplayType.RIGHT)
        } else {
            emptyList()
        }

    /** 是否有副屏参与协同 */
    protected fun hasSubScreens(): Boolean = getSubScreenAreas().isNotEmpty()

    /** 区域对应的人员成绩数组 */
    protected fun areaPersonArray(type: DisplayType): SparseArray<MultiSportResult> =
        when (type) {
            DisplayType.MAIN -> skipPersonArray
            DisplayType.LEFT -> leftPersonArray
            else -> rightPersonArray
        }

    /** 区域对应的副屏 IP（主屏返回 null） */
    private fun subScreenIp(type: DisplayType): String? = when (type) {
        DisplayType.LEFT -> leftSubScreenIp
        DisplayType.RIGHT -> rightSubScreenIp
        else -> null
    }

    //运动专属配置（如 ThirtySportConfig）由子类自行持有，基类只通过 open 钩子取值
    private var createTime: Long = 0
    protected val frameProcessor by lazy { OnceCacheProcessor() } //代理帧数据处理类
    protected val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    protected val config = MultiSportConfig()

    protected lateinit var multiSportProcessor: MultiSportProcessor
    protected lateinit var multiSportParser: MultiSportParser<*>


    val skipPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息
    val leftPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息
    val rightPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息

    protected val indicatorsMap by lazy {
        ConcurrentHashMap<Int, IndicatorsResultDto>()
    }

    //随堂赛的组号
    var personIndex = 0

    protected var currentIdentifyNumber = -1 //记录当前正在识别的位置

    protected var manCount = 0 //几人运动

    //记录开始和结束时间
    var startTime: Long = 0
    var endTime: Long = 0

    //开启预览事件
    private val _startPreviewLD = eventFlow<Pair<AdlCamera, AdlCameraFrameProcessor>>()
    val startPreviewLD: SharedFlow<Pair<AdlCamera, AdlCameraFrameProcessor>> =
        _startPreviewLD.asSharedFlow()

    //停止预览事件
    private val _stopPreviewLD = eventFlow<Boolean>()
    val stopPreviewLD: SharedFlow<Boolean> = _stopPreviewLD.asSharedFlow()

    //成绩结果（区域 → 已识别人员列表）
    private val _resultLD = eventFlow<Pair<DisplayType, MutableList<GroupSportBaseMan>>>()
    val resultLD: SharedFlow<Pair<DisplayType, MutableList<GroupSportBaseMan>>> =
        _resultLD.asSharedFlow()

    //单人次成绩结果（区域 → (位置, 成绩)）
    private val _singleResultLD = eventFlow<Pair<DisplayType, Pair<Int, Int>>>()
    val singleResultLD: SharedFlow<Pair<DisplayType, Pair<Int, Int>>> =
        _singleResultLD.asSharedFlow()

    //竞技开始倒计时完成事件（区域 → 是否完成）
    private val _countDownFinishLD = eventFlow<Pair<DisplayType, Boolean>>()
    val countDownFinishLD: SharedFlow<Pair<DisplayType, Boolean>> =
        _countDownFinishLD.asSharedFlow()

    //主屏识别倒计时已结束、等待其他区域倒计时结束（倒计时文案展示"等待其他区域加入..."）
    private val _waitOtherAreaLD = eventFlow<Unit>()
    val waitOtherAreaLD: SharedFlow<Unit> = _waitOtherAreaLD.asSharedFlow()

    //比赛结束事件（一次性消息：非粘性，返回页面不会重放，避免重复 clearScreen/addResultView）
    private val _toDoneLD = eventFlow<List<ThirtySportResultItem>>()
    val toDoneLD: SharedFlow<List<ThirtySportResultItem>> = _toDoneLD.asSharedFlow()

    //切换运动卡片状态（区域 → (位置, 卡片状态)）
    private val _changeSportViewStateLD =
        eventFlow<Pair<DisplayType, Pair<Int, MultiSportCardViewState>>>()
    val changeSportViewStateLD: SharedFlow<Pair<DisplayType, Pair<Int, MultiSportCardViewState>>> =
        _changeSportViewStateLD.asSharedFlow()

    //已识别人员（区域 → (位置, 人员信息)）
    private val _identifiedLD = eventFlow<Pair<DisplayType, Pair<Int, AccountInfo>>>()
    val identifiedLD: SharedFlow<Pair<DisplayType, Pair<Int, AccountInfo>>> =
        _identifiedLD.asSharedFlow()

    //运动是否开始（状态，需支持同步读取当前值，供开始条件/导航文案判断）
    private val _sportStartedLD = MutableStateFlow(false)
    val sportStartedLD: StateFlow<Boolean> = _sportStartedLD.asStateFlow()

    /** 当前最高展示数据：本次运动进行中，主/左/右三屏实时成绩最高者 */
    data class CurrentBestInfo(
        val displayType: DisplayType,
        val areaName: String,
        val accountName: String,
        val score: Int
    )

    //当前最高（状态，粘性，页面返回后也能恢复显示）
    private val _currentBestLD = MutableStateFlow<CurrentBestInfo?>(null)
    val currentBestLD: StateFlow<CurrentBestInfo?> = _currentBestLD.asStateFlow()

    /**
     * 刷新当前最高：运动开始或任一屏成绩变化后调用
     */
    fun refreshCurrentBest() {
        _currentBestLD.value = computeCurrentBest()
    }

    /**
     * 计算三屏实时最佳：运动未开始或无有效成绩(count>0)返回null，UI层据此隐藏/显示
     */
    private fun computeCurrentBest(): CurrentBestInfo? {
        if (!_sportStartedLD.value) return null
        var bestResult: MultiSportResult? = null
        var bestType = DisplayType.MAIN

        //局部函数会被编译器视为"变化闭包"捕获 bestResult/bestType，故判断时先取不可变快照 current，
        //避免"被捕获变量无法智能转换"的编译错误
        fun checkArea(type: DisplayType, area: SparseArray<MultiSportResult>) {
            for (index in 0 until area.size()) {
                val result = area.valueAt(index)
                if (result.count <= 0) continue
                val current = bestResult
                if (current == null || result.count > current.count) {
                    bestResult = result
                    bestType = type
                }
            }
        }
        checkArea(DisplayType.MAIN, skipPersonArray)
        checkArea(DisplayType.LEFT, leftPersonArray)
        checkArea(DisplayType.RIGHT, rightPersonArray)
        val best = bestResult
        if (best == null) return null
        val areaName = when (bestType) {
            DisplayType.LEFT -> getString(R.string.area_left)
            DisplayType.RIGHT -> getString(R.string.area_right)
            else -> getString(R.string.area_main)
        }
        return CurrentBestInfo(
            displayType = bestType,
            areaName = areaName,
            accountName = best.accountInfo.studentEntity?.accountName
                ?: getString(R.string.visitor),
            score = best.count
        )
    }

    //导航区协同状态：左/右副屏各自的 未连接/设备占用中/正常，用于UI显示（状态，需支持同步读取）
    //默认无连接：收到副屏协同状态回复后刷新为 NORMAL / OCCUPIED
    private val collaborationStateMap = mutableMapOf(
        DisplayType.LEFT to CollaborationAreaState.UNCONNECTED,
        DisplayType.RIGHT to CollaborationAreaState.UNCONNECTED
    )
    private val _collaborationStateLD =
        MutableStateFlow<Map<DisplayType, CollaborationAreaState>>(collaborationStateMap.toMap())
    val collaborationStateLD: StateFlow<Map<DisplayType, CollaborationAreaState>> =
        _collaborationStateLD.asStateFlow()

    //协同已识别人数上报记录：区域 → 已识别人数（CMD_SPORT_IDENTIFIED_COUNT），用于"是否清空已识别结果"提示
    val identifiedCountMap = mutableMapOf<DisplayType, Int>()

    //已识别人数变化通知（弹窗展示/更新，含弹窗期间新用户到达的人数同步）
    private val _identifiedCountLD = eventFlow<Unit>()
    val identifiedCountLD: SharedFlow<Unit> = _identifiedCountLD.asSharedFlow()

    //副屏占用中记录：正在运动中、无法直接加入协同的副屏
    private val occupiedSubScreens = mutableSetOf<DisplayType>()

    //副屏占用中变化通知（弹窗展示/更新，含弹窗期间另一屏也上报占用）
    private val _subScreenOccupiedLD = eventFlow<Set<DisplayType>>()
    val subScreenOccupiedLD: SharedFlow<Set<DisplayType>> = _subScreenOccupiedLD.asSharedFlow()

    //人员报告信息展示（一次性跳转消息：非粘性，避免从报告页返回时重放导致再次跳转）
    private val _showReportLD = eventFlow<String>()
    val showReportLD: SharedFlow<String> = _showReportLD.asSharedFlow()

    /**
     * 更新单个区域协同状态，保留其他区域已有状态，避免互相覆盖
     */
    private fun updateCollaborationState(displayType: DisplayType, state: CollaborationAreaState) {
        collaborationStateMap[displayType] = state
        uiHandler.post { _collaborationStateLD.value = collaborationStateMap.toMap() }
    }

    //协同开始条件跟踪：主屏倒计时是否结束
    protected var mainCountDownEnded = false

    //协同开始条件跟踪：已倒计时结束的副屏
    private val subScreenCountDownEnded = mutableSetOf<DisplayType>()

    //协同开始条件跟踪：主屏是否真正开始
    private var mainSportStarted = false

    //协同开始条件跟踪：开始运动指令是否已发送
    private var startCommandSent = false

    //运动结束条件跟踪：主屏运动是否已结束
    private var mainSportFinished = false

    //运动结束条件跟踪：已上报运动结束的副屏
    private val subScreenSportFinished = mutableSetOf<DisplayType>()

    //改变时间显示UI的背景色（区域 → 背景色资源）
    private val _changeTimeTextBgLD = eventFlow<Pair<DisplayType, Int>>()
    val changeTimeTextBgLD: SharedFlow<Pair<DisplayType, Int>> = _changeTimeTextBgLD.asSharedFlow()

    //相机登录状态
    private val _cameraLoginStateLD = eventFlow<Boolean>()
    val cameraLoginStateLD: SharedFlow<Boolean> = _cameraLoginStateLD.asSharedFlow()

    var countDownTime: Long = 10 //竞技倒计时10S，单位秒

    //竞技倒计时过程中更新UI（每秒一次，高频事件流）
    private val _countDownNoticeLD = eventFlow<Pair<DisplayType, String>>()
    val countDownNoticeLD: SharedFlow<Pair<DisplayType, String>> = _countDownNoticeLD.asSharedFlow()

    //记录每个位置状态
    protected val currentPersonState =
        mutableListOf<GroupSportsBaseChannelStatus>() //记录每个位置进入的时间，用来处理是否可以人脸识别
    protected val currentCardState = mutableListOf<MultiSportCardViewState>()

    protected var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    private val asyncPlayer by lazy { AsyncPlayer("CutDownAsyncPlayer") }

    lateinit var ipcHik: NsCameraHKSource
    var focalHandler = Handler(Looper.getMainLooper())

    //X秒竞技倒计时的Timer任务
    protected var startCountDownTimer: CountDownTimer? = null

    //当前播放音效的索引
    var soundIndex = 0

    //总音效数
    val soundCount = 5
    val simpleFormat = SimpleDateFormat("m:ss")
    var sportCountDownTime = 60L // 比赛时间 S

    //运动倒计时文本（区域 → 文案，每秒一次，高频事件流）
    private val _sportCountDownLD = eventFlow<Pair<DisplayType, String>>()
    val sportCountDownLD: SharedFlow<Pair<DisplayType, String>> = _sportCountDownLD.asSharedFlow()

    protected lateinit var factory: AbstractMultiSportFactory

    //运动倒计时任务
    protected var sportingCountDownTimer: CountDownTimer? = null
    open fun initEngine(number: Int) {
        factory =
            MultiSportProduceFactory.getFactory(sportInfo?.sportItemCode, sportInfo?.testCount)
        manCount = number
        for (index in 1..manCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        countDownTime = getDefaultCountDownTime()
        initProcess()
        if (streamType == 1) {
            initCamera()
        } else {
            cameraDispatcher.addFrameProcessor(frameProcessor)
        }
        //目前只有常规模式，才需要人脸识别，运动会模式不需要
        if (getSportModel() == SportModel.NORMAL) {
            initFaceIdentify()
        }
        //如果是赛事，请求运动详情数据，用于上传数据
        matchId?.let {
//            getFreeModelSportDetail(MatchMultiListActivity.uploadSportDetail!!.sportsDetailUid)
        }
    }

    open fun loadPerson(data: ArrayList<MeetGroupDetailsEntity>?) {
        if (data.isNullOrEmpty() || getSportModel() != SportModel.MEETING) return
        data.forEachIndexed { index, meetGroupDetailsEntity ->
            //给accountInfo赋值，用来获取存储视频的地址
            val accountInfo = AccountInfo().apply {
                studentEntity = StudentEntity().apply {
                    accountId = meetGroupDetailsEntity.accountId
                    accountName = meetGroupDetailsEntity.accountName
                    userFaceImgUrl =
                        AdlService.getService().facePrefix + meetGroupDetailsEntity.accountAvatar
                }
            }
            if (accountInfo.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                accountInfoLD.postValue(accountInfo)
            }
            skipPersonArray.put(
                index,
                MultiSportResult(accountInfo, index, meetGroup = meetGroupDetailsEntity)
            )
            identified(DisplayType.MAIN, index, accountInfo)
            changeMultiIdentityCardState(
                index,
                MultiSportCardViewState.PRECAST_PERSON
            )
        }
    }

    @Synchronized
    protected open fun processCallback(groupSkippingManList: MutableList<GroupSportBaseMan>) {
        //处理卡片和人的状态
        if (groupSkippingManList.isEmpty()) {
            handleNoPersonState()
        } else {
            handlePersonState(groupSkippingManList)
        }
        when (sportState) {
            MultiSportState.PREPARING, MultiSportState.IDENTIFYING -> {
                handleIdentify(groupSkippingManList)
            }

            MultiSportState.COUNTDOWN -> {
            }

            MultiSportState.SPORTING -> {
                postResult(groupSkippingManList)
            }

            MultiSportState.IDLE -> {
            }

            MultiSportState.WAITING_START -> {
            }
        }
    }

    protected open fun initProcess() {
        multiSportProcessor = MultiSportProcessor()
        config.debug = true
        config.drawAllBody = true
        config.drawBody = true
        config.drawSkeleton = true
        config.sportType = factory.getMultiSportType()
        config.loadModel(null)
        config.liteBodyDetect = factory.liteBodyDetect()
        config.checkExercisePrescription = isSupportReport()
        multiSportProcessor.setConfig(config)
        multiSportProcessor.setBodyFilter(getBodyFilter())
        multiSportProcessor.setSmooth(false)
        multiSportParser = factory.getParser()
        multiSportProcessor.setParser(multiSportParser)
        multiSportProcessor.setCallback(object : MultiSportCallbackImpl() {
            override fun onResult(result: com.adl.sport.general.multi.MultiSportResult) {
                processCallback(result.groupManList)
            }

            override fun onError(index: Int, code: Int, error: String?) {}
        })
        frameProcessor.addFrameProcessor(multiSportProcessor)
    }

    protected open fun createCountDownTimer() =
        object : CountDownTimer(getDefaultCountDownTime() * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _countDownNoticeLD.tryEmit(DisplayType.MAIN to "${countDownTime--}")
            }

            override fun onFinish() {
                //主屏倒计时结束，统一检查协同开始条件
                mainCountDownEnded = true
                sportState = MultiSportState.WAITING_START
                checkCollaborationSportStart()
                //主屏识别倒计时已结束，但仍有已加入副屏未倒计时结束：提示等待其他区域
                if (needWaitOtherAreaJoin()) {
                    _waitOtherAreaLD.tryEmit(Unit)
                }
            }
        }

    protected fun initCamera() {
        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(frameProcessor)
        val cameraConfig =
            BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = false
        ipcHik = hikSubConfig.setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
            cameraConfig.ip,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
            cameraConfig.account,
            cameraConfig.psw
        ).setStreamType(0).build() as NsCameraHKSource
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    protected fun setResolution(camera: AdlCamera, re: Byte, frameRate: Byte = 17) {
        val hkSource = camera as NsCameraHKSource
        val deviceItem = hkSource.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, re, 27, frameRate)
    }

    open fun openTsProcess() {
//        tsProcessor.combineRegions()
//        tsProcessor.toSporting()
//        tsProcessor.setCheckChannelStatus(true)
        multiSportParser.checkChannelStatus = true
        sportState = MultiSportState.PREPARING
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
    }

    open fun combineOverlay(overlay: SportOverlayMultiRegion) {
        multiSportProcessor.open(overlay)
        multiSportProcessor.onInit()
    }

    override fun identifyFail(index: Int) {
//        changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFY_FAIL)
        //处理下一道是否有人
        val index = currentIdentifyNumber + 1
        var tempNumber = index % manCount
        var findOk = false
        while (tempNumber < manCount) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (currentPersonState[tempNumber] == GroupSportsBaseChannelStatus.checkDown && skipPersonArray.get(
                    tempNumber,
                    null
                ) == null
            ) {
                currentIdentifyNumber = tempNumber
                changeMultiIdentityCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFYING
                )
                findOk = true
                break
            }
            tempNumber++
        }
        //没有找到下一道，置为-1，再从头开始识别
        if (!findOk) {
            resetIdentifyNumber()
        }

    }

    override fun identifyOk(accountIndex: Int, accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            AdlLogger.d("识别到人脸：但已不是识别状态 $accountIndex")
            return
        }
        SpeechWrapper.speech(
            getString(
                R.string.identified_someone,
                accountInfo.studentEntity!!.accountName
            )
        )
        synchronized(this) {
            viewModelScope.launch(Dispatchers.Main) {
                //处理结果加锁，post到主线程执行时，子线程可能会改变currentIdentifyNumber的值，或其他状态
                sportState = MultiSportState.PREPARING
                //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
                if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                    for (index in 0 until skipPersonArray.size) {
                        val multiResult = skipPersonArray.valueAt(index)
                        //已识别过此人
                        if (multiResult.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity?.accountId) {
                            skipPersonArray.removeAt(index)
                            changeMultiIdentityCardState(
                                multiResult.number,
                                MultiSportCardViewState.NOBODY
                            )
                            break
                        }
                    }
                }

                //如果是组织测试，校验学生是否在计划中
                val checkResult = withContext(Dispatchers.IO) {
                    checkStudent(accountInfo.studentEntity!!.accountId, ptPlanId)
                }
                if (checkResult) {
                    //存已识别的人信息
                    //识别到人，且有效时，取消倒计时再处理显示逻辑
//                    startCountDownTimer.cancel()
                    skipPersonArray.put(
                        accountIndex,
                        MultiSportResult(accountInfo, accountIndex)
                    )
                    AdlLogger.d("人脸识别结果：姓名：${accountInfo.studentEntity?.accountName}; 道数 $accountIndex")
                    _identifiedLD.tryEmit(DisplayType.MAIN to Pair(accountIndex, accountInfo))
                    //给accountInfo赋值，用来获取存储视频的地址
                    if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                        accountInfoLD.postValue(accountInfo)
                    }
                    LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity?.accountName};道数 $accountIndex 切换到准备状态")
                    changeMultiIdentityCardState(
                        accountIndex,
                        identifiedCardState()
                    )

                    resetIdentifyNumber()
                    //处理竞技倒计时
                    //如果已识别五人，则直接开始
                    if (skipPersonArray.size == manCount) {
//                        sportState = MultiSportState.WAITING_START
                        checkCollaborationSportStart()
                        return@launch
                    }
                    //重新复位倒计时时间
//                    countDownTime = getDefaultCountDownTime()
//                    countDownNoticeLD.value =
//                        "${getString(R.string.race_start_countdown)}：${countDownTime}"
                    //第一个人识别成功，则开始倒计时
                    if (skipPersonArray.size == 1 && startCountDownTimer == null) {
                        onFirstIdentified()
                        startCountDownTimer = createCountDownTimer()
                        startCountDownTimer?.start()
//                        playCutDown()
                    }
                    //识别到人，取消超时机制
                    timeOutHelper.cancel()
                } else {
                    resetIdentifyNumber()
                    ToastUtil.getInstance(
                        getApplication(),
                        getString(
                            R.string.busi_base_name_without_plan,
                            accountInfo.studentEntity!!.accountName
                        )
                    )
                        .show()
                }
            }

        }
    }

    @Synchronized
    protected open fun resetIdentifyNumber() {
        LogUtil.d("重置识别编号为-1")
        currentIdentifyNumber = -1
    }

    /**
     * 识别成功后的卡片状态（默认"已准备"）
     */
    protected open fun identifiedCardState(): MultiSportCardViewState =
        MultiSportCardViewState.PREPARED

    /**
     * 第一个人识别成功、竞技倒计时启动前的处理（默认无）
     */
    protected open fun onFirstIdentified() {
    }

    /**
     * 处理成绩，通知UI层更新成绩UI
     */
    protected open fun postResult(result: MutableList<GroupSportBaseMan>) {
        //运动中才更新成绩，否则结束成绩回调会是0
        if (sportState == MultiSportState.SPORTING) {
            var str = ""
            result.forEach {
                str = "$str -- ${it.score} -- ${it.positionNum} /////"
                skipPersonArray[it.positionNum]?.count = it.score
            }
//            LogUtil.d("成绩：$str")
            refreshCurrentBest()
            _resultLD.tryEmit(DisplayType.MAIN to result)
        }
    }

    /**
     * 没有人了，处理一下状态复位
     */
    @Synchronized
    protected open fun handleNoPersonState() {
        when (sportState) {
            //IDLE状态或者正在识别状态，清掉卡片状态，并复位状态到IDLE
            MultiSportState.IDENTIFYING -> {
                for (index in currentPersonState.indices) {
                    if (currentPersonState[index] != GroupSportsBaseChannelStatus.none) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
                    }
                }
                LogUtil.d("处理无人时的状态复位")
                resetIdentifyNumber()
                sportState = MultiSportState.PREPARING
            }

            //准备中，即将开始，不处理离开
            MultiSportState.PREPARING -> {

            }

            //运动中，最后一个人离开需要更新卡片到离开状态,可直接更新所有位置的卡片为离开状态
            MultiSportState.SPORTING -> {
                for (index in 0 until manCount) {
                    val roseMultiResult = skipPersonArray.get(index, null)
                    if (roseMultiResult != null) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.LEAVE)
                    }
                    currentPersonState[index] = GroupSportsBaseChannelStatus.none
                }
            }

            else -> {}
        }

    }

    /**
     * 记录每个位置的状态，并通知UI层更新卡片状态
     * 只处理卡片状态，此函数回调，一定有人在框里
     */
    @Synchronized
    protected open fun handlePersonState(skipRopeManList: MutableList<GroupSportBaseMan>) {

        //非运动状态下记录每个位置的人员进入状态
        for (skipMan in skipRopeManList) {
            val currentState = currentPersonState[skipMan.positionNum]
            val roseMultiResult = skipPersonArray.get(skipMan.positionNum, null)
            val nextState = skipMan.channelStatus
            //处理进入时间
            when (nextState) {
                GroupSportsBaseChannelStatus.none -> {
                    //如果当前为standby,下一个状态为 none
                    if (currentState == GroupSportsBaseChannelStatus.standBy) {
                        //当前比赛状态为运动中... 切换人员卡片到离开状态
                        if (sportState == MultiSportState.SPORTING && roseMultiResult != null) {
                            changeMultiIdentityCardState(
                                skipMan.positionNum,
                                MultiSportCardViewState.LEAVE
                            )
                            currentCardState[skipMan.positionNum] = MultiSportCardViewState.LEAVE
                        }
                        if (sportState != MultiSportState.SPORTING && roseMultiResult == null) {
                            changeMultiIdentityCardState(
                                skipMan.positionNum,
                                MultiSportCardViewState.NOBODY
                            )
                        }
                    }
                }

                GroupSportsBaseChannelStatus.standBy -> {
                    if (getSportModel() == SportModel.NORMAL
                        && (sportState == MultiSportState.PREPARING || sportState == MultiSportState.IDENTIFYING)
                        && roseMultiResult == null
                    ) {
                        changeMultiIdentityCardState(
                            skipMan.positionNum,
                            MultiSportCardViewState.STANDBY
                        )
                    }
                    //进入区域
                    if (currentState == GroupSportsBaseChannelStatus.none && sportState == MultiSportState.SPORTING && roseMultiResult != null) {
                        currentCardState[skipMan.positionNum] = MultiSportCardViewState.SPORTING
                        changeMultiIdentityCardState(
                            skipMan.positionNum,
                            MultiSportCardViewState.LEAVE_TO_SPORTING
                        )
                    }
                }

                GroupSportsBaseChannelStatus.checkDown -> {
                    if (getSportModel() == SportModel.MEETING) {
                        //进入区域
                        if (sportState != MultiSportState.SPORTING && sportState != MultiSportState.COUNTDOWN && sportState != MultiSportState.IDLE) {
                            val person = skipPersonArray.get(skipMan.positionNum)
                            if (person != null && !person.hasVoice) {
                                person.hasVoice = true
                                SpeechWrapper.speech("${person.accountInfo.studentEntity?.accountName}已就位")
                                changeMultiIdentityCardState(
                                    person.number,
                                    MultiSportCardViewState.PRECAST_PREPARE
                                )
                            }
                        }
                    }
                    //进入区域
                    if (currentState == GroupSportsBaseChannelStatus.none && sportState == MultiSportState.SPORTING && roseMultiResult != null) {
                        currentCardState[skipMan.positionNum] = MultiSportCardViewState.SPORTING
                        changeMultiIdentityCardState(
                            skipMan.positionNum,
                            MultiSportCardViewState.LEAVE_TO_SPORTING
                        )
                    }
                }

                else -> {}
            }
            currentPersonState[skipMan.positionNum] = nextState
        }
    }


    @Synchronized
    protected open fun handleIdentify(skipRopeManList: MutableList<GroupSportBaseMan>) {
        //目前只有常规模式才需要识别人脸，运动会模式不需要
        if (getSportModel() != SportModel.NORMAL) return

        //如果为识别状态
        if (sportState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val skipRopeMan =
                skipRopeManList.find { skipRopeMan -> skipRopeMan.positionNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (skipRopeMan == null || skipRopeMan.checkHandUp().not()) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                changeMultiIdentityCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFY_FAIL
                )
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = frameProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(
                        skipRopeMan,
                        currentYuvData.width,
                        currentYuvData.height,
                        currentIdentifyNumber
                    ),
                    currentYuvData.yuv,
                    currentYuvData.width,
                    currentYuvData.height,
                    currentIdentifyNumber
                )
            }
            return
        }
        //如果正在识别，则等待识别完再处理下一个人脸识别
        if (isIdentifying()) {
            LogUtil.d("处理人脸识别：人已离开位置，但正在识别人脸; 道数 $currentIdentifyNumber")
            return
        }

        skipRopeManList.forEach {
            if (it.channelStatus == GroupSportsBaseChannelStatus.checkDown
                && skipPersonArray.get(it.positionNum, null) == null
                && it.checkHandUp()
            ) {
                currentIdentifyNumber = it.positionNum
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = frameProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(
                        it,
                        currentYuvData.width,
                        currentYuvData.height,
                        currentIdentifyNumber
                    ),
                    currentYuvData.yuv,
                    currentYuvData.width,
                    currentYuvData.height,
                    currentIdentifyNumber
                )
                changeMultiIdentityCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFYING
                )
                LogUtil.d("处理人脸识别：人位置状态OK，开始识别该位置，道数 $currentIdentifyNumber")
                return
            }
        }
    }

    /**
     * 主屏触发准备开始（倒计时结束/满员），统一检查协同开始条件
     */
    open fun readyGo() {
        startSport()
    }

    /**
     * 协同开始条件变化时统一调用：
     * 满足条件则开始主屏3-2-1倒计时，并向已连接副屏发送开始运动指令
     */
    private fun checkCollaborationSportStart() {
        if (!canStartCollaborationSport()) return
        startSport()
    }

    protected open fun startSport() {
        tryStartSport()
        sendSportCommand()
    }

    /**
     * 协同运动开始条件（任一满足即可开始）：
     * 条件1：所有屏（主屏+副屏）都识别满员
     * 条件2：所有屏（主屏+副屏）都倒计时结束
     */
    private fun canStartCollaborationSport(): Boolean {
        val connectedSubScreens = connectedSubScreenTypes()
        //前提：主屏和所有已连接副屏都必须识别到至少1人，任一屏为0则不能开始
        if (skipPersonArray.size <= 0) return false
        if (getSubScreenAreas().any {
                it in connectedSubScreens &&
                        collaborationStateMap[it] == CollaborationAreaState.NORMAL &&
                        areaPersonArray(it).size <= 0
            }) return false
        //条件1：所有屏（主屏+已连接副屏）都识别满员才能直接开始，未连接的副屏不参与判定
        val allScreenFull = skipPersonArray.size >= manCount &&
                getSubScreenAreas().all {
                    it !in connectedSubScreens || areaPersonArray(it).size >= manCount
                }
        if (allScreenFull) return true
        //条件2：主屏与参与协同的副屏都倒计时结束；未连接或占用中（未加入协同）的副屏不参与判定
        if (!mainCountDownEnded) return false
        return connectedSubScreens.all {
            it in subScreenCountDownEnded ||
                    collaborationStateMap[it] != CollaborationAreaState.NORMAL
        }
    }

    /**
     * 是否需要等待其他区域：主屏识别倒计时已结束，但仍有已加入协同的副屏未倒计时结束
     */
    private fun needWaitOtherAreaJoin(): Boolean {
        if (!mainCountDownEnded) return false
        return connectedSubScreenTypes().any {
            collaborationStateMap[it] == CollaborationAreaState.NORMAL &&
                    it !in subScreenCountDownEnded
        }
    }

    /**
     * 检查并真正开始主屏3-2-1倒计时，只执行一次
     */
    protected open fun tryStartSport() {
        if (mainSportStarted) return
        mainSportStarted = true
        onBeforeSportStart()
        startCountDownTimer?.cancel()
        _countDownFinishLD.tryEmit(DisplayType.MAIN to true)
        //开始3-2-1倒计时就表示比赛已经开始
        sportState = MultiSportState.COUNTDOWN
        onCountDownStarted()
    }

    /**
     * 开始竞技前的运动特有处理（默认：支持运动处方时指定模型输出目录 + 停止倒计时音效）
     */
    protected open fun onBeforeSportStart() {
        if (factory.isSupportPrescription()) {
            config.checkExercisePrescription = true
            config.fileDir = getVideoPath(sportInfo?.sportSkuId).parentFile
        }
        //停止倒计时音效
        stopPlayCutDown()
    }

    /**
     * 3-2-1倒计时开始后的处理（默认：摄像头切换到运动分辨率）
     */
    protected open fun onCountDownStarted() {
        //处理开始后摄像头分辨率
        if (streamType == 1) {
            handleStartCameraResolution()
        }
    }

    /**
     * 处理开始后的摄像头分辨率
     */
    protected open fun handleStartCameraResolution() {
        viewModelScope.launch(Dispatchers.IO) {
            changeTo2K()
        }
    }

    protected open fun changeTo4K() {
        ipcHik.pauseProcessor()
        setResolution(ipcHik, getSportResolution())
        SystemClock.sleep(100)
        _stopPreviewLD.tryEmit(true)
        SystemClock.sleep(100)
        ipcHik.restartProcessor()
        _startPreviewLD.tryEmit(Pair(ipcHik, cameraDispatcher))
        SystemClock.sleep(200)
        /********************切换分辨率********************/
        multiSportProcessor.clearTransform()
    }

    protected open fun changeTo2K() {
        ipcHik.pauseProcessor()
        setResolution(ipcHik, getSportResolution())
        SystemClock.sleep(100)
        _stopPreviewLD.tryEmit(true)
        SystemClock.sleep(100)
        ipcHik.restartProcessor()
        _startPreviewLD.tryEmit(Pair(ipcHik, cameraDispatcher))
        SystemClock.sleep(200)
        /********************切换分辨率********************/
        multiSportProcessor.clearTransform()
    }

    protected open fun changeMultiIdentityCardState(index: Int, state: MultiSportCardViewState) {
        uiHandler.post {
            LogUtil.d("changeMultiIdentityCardState: index $index state ${state.name}")
            _changeSportViewStateLD.tryEmit(DisplayType.MAIN to Pair(index, state))
        }
    }

    protected open fun identified(type: DisplayType, index: Int, accountInfo: AccountInfo) {
        uiHandler.post {
            _identifiedLD.tryEmit(type to Pair(index, accountInfo))
        }
    }

    /**
     * 开始录像,在此回调中处理录像，解决切换分辨率，录像还是录的4K的问题
     */
    open fun onCameraOpen() {
        multiSportProcessor.clearTransform()
        if (sportState == MultiSportState.SPORTING || sportState == MultiSportState.COUNTDOWN) {
            LogUtil.d("${manCount}人运动，开始录像")
            startRecord()
        }
    }


    override fun onResume() {
        super.onResume()
        if (streamType == 1) {
            ipcHik.login(object : AdlCameraListener {

                override fun onOpenSucceed(p0: Boolean) {

                }

                override fun onLoginSucceed() {
                    setResolution(ipcHik, getIdentifyResolution(), getInitCameraFrameRate())
                    SystemClock.sleep(100)
                    _cameraLoginStateLD.tryEmit(true)
                    _startPreviewLD.tryEmit(Pair(ipcHik, cameraDispatcher))
                }

                override fun onLoginFail() {
                    _cameraLoginStateLD.tryEmit(false)
                }

                override fun onOpenStatus(p0: String?) {

                }

                override fun onError(p0: Exception?) {
                    _cameraLoginStateLD.tryEmit(false)
                }

                override fun onStreamLoss() {
                }

                override fun onOpenFail() {
                }
            })
            timeOutHelper.start()
        }
    }

    override fun toStatusSportStart() {
        super.toStatusSportStart()
        startCountDownTimer = null
        sportState = MultiSportState.SPORTING
        _sportStartedLD.value = true
        refreshCurrentBest()
        startTime = System.currentTimeMillis()
        //运动特有的开始后处理（如处理器切到运动态）
        onSportStarted()
        sportCountDownTime = sportInfo?.endValue?.toLong()?.div(1000) ?: 60
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

                _sportCountDownLD.tryEmit(DisplayType.MAIN to simpleFormat.format(Date((sportCountDownTime) * 1000)))
            }

            override fun onFinish() {
                //比赛结束
                _sportCountDownLD.tryEmit(DisplayType.MAIN to "0:00")
                sportFinish()
            }

        }
        sportingCountDownTimer?.start()
        multiSportParser.checkSkipScore = true
        //运动开始记录上传（默认无；需要上传开始记录的运动覆写）
        onSportStartRecordUpload()
        //比赛开始设置卡片状态为运动状态（运动特有表现由子类决定）
        onSportStartedCards()
    }

    /**
     * 运动开始记录上传（默认无；需要上传开始记录的运动覆写）
     */
    protected open fun onSportStartRecordUpload() {
    }

    /**
     * 运动开始后的运动特有处理（默认无）
     */
    protected open fun onSportStarted() {
    }

    /**
     * 运动开始后刷新各道次卡片（默认：仅记录内部状态，不主动刷新副屏卡片）
     */
    protected open fun onSportStartedCards() {
        for (index in 0 until skipPersonArray.size) {
            val multiResult = skipPersonArray.valueAt(index)
            currentCardState[multiResult.number] = MultiSportCardViewState.SPORTING
        }
    }

    protected open fun startRecord() {
        getVideoPath(sportInfo!!.sportSkuId)
        createTime = System.currentTimeMillis()
        if (videoRecorder.isStarting) {
            LogUtil.d("${manCount}人运动,录像已开始")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val size = getCameraSize()
            videoRecorder.onStart(videoFile, size.first, size.second)
            cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
                override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
                    videoRecorder.inputNv21(p0)
                }
            })
        }
    }

    /**
     * 结束比赛
     */
    open fun toDone() {
        toStatusSportEnd()
        //有崩溃，补货异常
        try {
            sportState = MultiSportState.IDLE
            multiSportProcessor.changeTo(SportProcessorStatus.Idle)
            multiSportParser.checkChannelStatus = false
            multiSportParser.checkSkipScore = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *  比赛结束，计算结果
     */
    protected open fun sportFinish() {
        endTime = System.currentTimeMillis()
        toDone()
        SoundPlayUtils.play(24)
        //停止录像，上传录像成绩
        videoRecorder.onStop()
        //主屏结束：需等所有已加入副屏也结束后，才统一计算并展示成绩结果页
        mainSportFinished = true
        checkAllSportFinished()
    }

    /**
     * 运动结束统一检查：主屏结束 且 所有已加入协同的副屏都结束，才计算成绩并展示结果页
     */
    protected open fun checkAllSportFinished() {
        if (!mainSportFinished) return
        val joinedSubScreens = getSubScreenAreas().filter { isSubScreenJoined(it) }
        if (joinedSubScreens.any { it !in subScreenSportFinished }) {
            LogUtil.d("等待副屏运动结束：已加入=$joinedSubScreens，已结束=$subScreenSportFinished")
            return
        }
        handleResult(skipPersonArray)
    }

    /**
     * 处理成绩：合并主屏与已连接副屏成绩，按高优排序，计算名次，并生成结果页统一数据
     */
    protected open fun handleResult(skipPersonArray: SparseArray<MultiSportResult>) {
        if (sportInfo == null || skipPersonArray.isEmpty()) {
            return
        }
        val resultList = mutableListOf<MultiSportResult>()
        skipPersonArray.forEach { _, value ->
            resultList.add(value)
        }
        resultList.sortWith(compareBy { -it.count })
        var preRank = 0
        var preScore = -1
        for (index in resultList.indices) {
            val skipRoseMultiResult = resultList[index]
            var rank = 0
            if (skipRoseMultiResult.count == preScore) {
                rank = preRank
            } else {
                rank = preRank + 1
            }
            preRank = rank
            preScore = skipRoseMultiResult.count
            skipRoseMultiResult.rank = rank
        }
        //合并主屏+副屏成绩，供结果页统一展示
        val mergedItems = mergeAllAreaResults(resultList)
        _toDoneLD.tryEmit(mergedItems)
        handleUploadResult(resultList)
        //运动特有收尾处理（如卡片切换到结束状态）
        onSportResultReady(resultList)
    }

    /**
     * 运动结果产出后的运动特有处理（默认无）
     */
    protected open fun onSportResultReady(resultList: MutableList<MultiSportResult>) {
    }

    /**
     * 成绩上传的满分语义（默认0；仰卧起坐等以完成次数作为成绩分数的运动覆写为 count）
     */
    protected open fun uploadPerfectScore(count: Int): Int = 0

    /**
     * 合并主屏与已连接副屏成绩，生成结果页统一数据项，按成绩高优排序并重新计算总排名
     */
    protected open fun mergeAllAreaResults(mainResults: MutableList<MultiSportResult>): List<ThirtySportResultItem> {
        val areaNameMap = mapOf(
            DisplayType.MAIN to getString(R.string.area_main),
            DisplayType.LEFT to getString(R.string.area_left),
            DisplayType.RIGHT to getString(R.string.area_right)
        )
        val merged = mutableListOf<ThirtySportResultItem>()
        //主屏成绩
        mainResults.forEach { result ->
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
        //副屏成绩（仅统计参与协同且已连接的副屏区域）
        getSubScreenAreas().forEach { area ->
            if (subScreenIp(area)?.let { GrpcManager.getClientIdByIp(it) } == null) return@forEach
            areaPersonArray(area).forEach { _, result ->
                merged.add(
                    ThirtySportResultItem(
                        result = result,
                        displayType = area,
                        areaName = areaNameMap[area] ?: "",
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
        var preScore = -1
        merged.forEachIndexed { index, item ->
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
    protected open fun handleUploadResult(resultList: MutableList<MultiSportResult>) {
        GlobalScope.launch(Dispatchers.IO) {
            val multiId = if (personIndex > 0) {
                "${resultList[0].meetGroup?.classId}${personIndex}"
            } else {
                System.currentTimeMillis().toString()
            }
            SystemClock.sleep(1500)
            for (index in resultList.indices) {
                var multiResult = resultList[index]
                var sportResult = SportResult()
                sportResult.sportDuration = sportInfo!!.endValue.toInt()
                sportResult.maxComboCount = 0
                sportResult.sportAchievement = multiResult.count
                sportResult.perfectCount = 0
                sportResult.goodCount = 0
                sportResult.comeOnCount = 0
                sportResult.perfectScore = uploadPerfectScore(multiResult.count)
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
                when (multiSportParser) {
                    is GroupSkippingLRemixParser -> {
                        val groupSkippingManRemixList =
                            (multiSportParser as GroupSkippingLRemixParser).sportResult.groupManList as MutableList<GroupSkippingManLRemix>
                        val pre =
                            groupSkippingManRemixList.firstOrNull { it.positionNum == multiResult.number }?.euRopeSkipExercisePrescription
                        if (pre == null) {
                            LogUtil.d("${multiResult.number}道无报告,运动人数:${skipPersonArray.size},报告总数: ${groupSkippingManRemixList.size}")
                        }
                        pre
                    }

                    is GroupSkippingRemixParser -> {
                        val groupSkippingManRemixList =
                            (multiSportParser as GroupSkippingRemixParser).sportResult.groupManList as MutableList<GroupSkippingManRemix>
                        val pre =
                            groupSkippingManRemixList.firstOrNull { it.positionNum == multiResult.number }?.euRopeSkipExercisePrescription
                        if (pre == null) {
                            LogUtil.d("${multiResult.number}道无报告,运动人数:${skipPersonArray.size},报告总数: ${groupSkippingManRemixList.size}")
                        }
                        pre
                    }

                    else -> null
                }?.let { prescription ->
                    indicatorsList =
                        IndicatorsHelper.getSkipIndicatorsList(
                            prescription,
                            startTime,
                            endTime,
                            null
                        )
                    val multiSportResult = skipPersonArray[multiResult.number]
                    IndicatorsHelper.createIndicatorsResultDto(
                        IndicatorsConstants.SKIP_CODE,
                        indicatorsList,
                        startTime,
                        endTime,
                        multiSportResult?.count ?: 0,
                        videoFile,
                        videoCover,
                        multiSportResult?.accountInfo?.studentEntity?.accountName,
                        multiSportResult?.accountInfo?.studentEntity?.userSex?.toInt(),
                        multiSportResult?.accountInfo?.studentEntity?.userFaceImgUrl
                    )?.let {
                        indicatorsMap.put(multiResult.number, it)
                    }
                }
                var recordPlan: SportRecordEntity.RecordPlan? = null
                if (sceneId == SceneConstants.ORGANIZE_TEST) {
                    recordPlan = SportRecordEntity.RecordPlan().apply {
                        ptPlanId = this@BaseCollabSportViewModel.ptPlanId
                        ptStandardId = this@BaseCollabSportViewModel.ptStandardId
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

    protected fun playCutDown() {
        asyncPlayer.play(getApplication(), getCountDownBgmUri(), false, AudioManager.STREAM_MUSIC)
    }

    protected fun stopPlayCutDown() {
        asyncPlayer.stop()
    }


    open fun getDefaultCountDownTime(): Long = 15

    open fun getCountDownBgmUri(): Uri =
        ("android.resource://" + ApplicationUtil.getContext().packageName + "/" + R.raw.bgm_15_prepare).toUri()

    open fun getBodyFilter(): TsTaskBodyFilter = RegionFilter()

    open fun getIdentifyResolution(): Byte =
        if (getSportModel() == SportModel.NORMAL) 64 else getSportResolution()

    open fun getSportResolution(): Byte = 70

    open fun getCameraSize(): Pair<Int, Int> = 2560 to 1440

    //获取运动模式（默认常规；由子类按各自配置返回）
    open fun getSportModel(): SportModel = SportModel.NORMAL

    open fun getInitCameraFrameRate(): Byte = if (getSportModel() == SportModel.NORMAL) 14 else 17

    open fun getIdentityRect(
        it: TsSportChannel?,
        width: Int,
        height: Int,
        currentIdentifyNumber: Int
    ): Rect {
        if (it?.body == null || it.skeleton == null) {
            LogUtil.d("it.bodyrect  is Null null null null ------", true)
            return Rect(0, 0, 50, 50)
        }
        var shoulderOffset = 0
        var rect = Rect(it.body.x, it.body.y, it.body.right(), it.body.bottom())
        if (it.skeleton != null && it.skeleton.LShoulder != null && it.skeleton.RShoulder != null) {
            val shoulderWidth = abs(it.skeleton.LShoulder.x - it.skeleton.RShoulder.x)
            rect.bottom = max(it.skeleton.LShoulder.y, it.skeleton.RShoulder.y).toInt()
            rect.left =
                max(
                    min(
                        it.skeleton.LShoulder.x,
                        it.skeleton.RShoulder.x
                    ).toInt() - shoulderOffset, 0
                )
            rect.right = min(rect.left + (shoulderWidth + 2 * shoulderOffset).toInt(), width)
            rect.top = if (it.skeleton.headTop != null) {
                it.skeleton.headTop.y.toInt()
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
    open fun getIndicatorsResultDto(number: Int): IndicatorsResultDto? =
        indicatorsMap.getOrDefault(number, null)

    override fun reset() {
        super.reset()
        //重置，重新开始运动流程
        //复位开始流程标记（下一组/重测需要重新走3-2-1倒计时与开始条件检查）
        mainSportStarted = false
        mainSportFinished = false
        mainCountDownEnded = false
        startCountDownTimer?.cancel()
        startCountDownTimer = null
        subScreenCountDownEnded.clear()
        //清除所有数据
        indicatorsMap.clear()
        currentPersonState.clear()
        currentCardState.clear()
        for (index in 1..manCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        skipPersonArray.clear()

        //重置人脸识别
        initFaceIdentify()

        multiSportProcessor.onReset()
        openTsProcess()
        //切换摄像头分辨率
        if (streamType == 1) {
            changeTo4K()
        }
//        viewModelScope.launch(Dispatchers.IO) {
//            /********************切换分辨率********************/
//            ipcHik.pauseProcessor()
//            setResolution(ipcHik, getIdentifyResolution())
//            SystemClock.sleep(100)
//            _stopPreviewLD.tryEmit(true)
//            SystemClock.sleep(100)
//            changeResolution()
//            ipcHik.restartProcessor()
//            _startPreviewLD.tryEmit(Pair(ipcHik, cameraDispatcher))
//            SystemClock.sleep(200)
//            /********************切换分辨率********************/
//            multiSportProcessor.clearTransform()
//            multiSportProcessor.onReset()
//            openTsProcess()
//        }

    }

    open fun onFrame(yuv: ByteArray, info: AdlCameraPreviewInfo) {
        cameraDispatcher.onFrame(yuv, info)
    }

    //是否支持运动处方报告（默认支持；由子类按各自配置返回）
    open fun isSupportReport(): Boolean = true

    fun registerGrpcCmd() {
        registerSportCardStatus()
        registerAccountInfo()
        registerResult()
        registerCollaborationStatus()
        registerIdentifiedCount()
        registerCountDownEnd()
        registerGrpcSportStatusHandler()
    }

    private fun registerCountDownEnd() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COUNTDOWN_END, grpcKey) {
            subScreenCountDownEnded.add(it.from.getDisplayType(leftSubScreenIp, rightSubScreenIp))
            checkCollaborationSportStart()
        }
    }

    /**
     * 进入项目：检查左右屏连接状态，已连接的发送协同运动开始指令
     */
    open fun initCollaboration() {
        //无副屏参与的纯单屏运动，协同层自动退化
        if (!hasSubScreens()) return
        getSubScreenAreas().forEach { checkSubScreen(it, subScreenIp(it)) }
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

    /**
     * 发送协同指令（不支持协同的运动不向客户端发送任何Grpc消息）
     */
    private fun sendCollaborationCommand(clientId: String, cmd: CollaborationCmdEnum) {
        if (!isSupportCollaboration()) return
        val payload = GrpcCollaborationSportInfo(
            cmd = cmd.ordinal,
            sportSkuId = sportInfo?.sportSkuId ?: "",
            testCount = sportInfo?.testCount?.toInt() ?: 1,
            sceneId = sceneId ?: SceneConstants.PK
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
    open fun sendCollaborationFinish() {
        if (!hasSubScreens()) return
        getSubScreenAreas().forEach { type ->
            subScreenIp(type)?.let { GrpcManager.getClientIdByIp(it) }?.let {
                sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
            }
        }
    }

    /**
     * 注册协同状态回复指令
     */
    private fun registerCollaborationStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COLLABORATION_STATUS, grpcKey) { data ->
            val status = data.payload.toObject<GrpcCollaborationStatus>()?.status
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val statusEnum = CollaborationStatusEnum.fromCode(status ?: -1)
            val state = when (statusEnum) {
                //运动中、已有已识别人员 → 不能加入协同，设备占用中
                CollaborationStatusEnum.SPORTING -> CollaborationAreaState.OCCUPIED
                //其余状态可加入协同
                else -> CollaborationAreaState.NORMAL
            }
            LogUtil.d("收到协同状态回复：$displayType, status=$status, state=$state")
            updateCollaborationState(displayType, state)
            //运动中记录为占用中并提示是否强制加入；恢复空闲则移除记录
            updateOccupiedSubScreens(displayType, state == CollaborationAreaState.OCCUPIED)
        }
    }

    /**
     * 注册运动结束指令
     */
    private fun registerGrpcSportStatusHandler() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_STATUS, grpcKey) { data ->
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
                    //与副屏成绩写入保持同一线程时序，避免结果未合并就出成绩页
                    uiHandler.post { checkAllSportFinished() }
                }
            }
        }
    }

    /**
     * 当前已连接副屏类型列表
     */
    private fun connectedSubScreenTypes(): List<DisplayType> =
        getSubScreenAreas().filter { type ->
            subScreenIp(type)?.let { GrpcManager.getClientIdByIp(it) } != null
        }

    /**
     * 指定副屏是否已加入协同（collaborationStateMap 中状态为 NORMAL 可加入）
     */
    open fun isSubScreenJoined(displayType: DisplayType): Boolean =
        displayType != DisplayType.MAIN &&
                _collaborationStateLD.value[displayType] == CollaborationAreaState.NORMAL

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
     * 注册已识别人数指令：客户端发起协同时，若已有识别成功的人员，上报已识别人数到服务端。
     * 服务端据此弹出"左区已识别X人，右区已识别X人，是否清空已识别结果"提示。
     */
    private fun registerIdentifiedCount() {
        GrpcManager.registerServerHandler(
            GrpcCmdCode.CMD_SPORT_IDENTIFIED_COUNT,
            grpcKey
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
     * "是否清空已识别结果"弹窗展示期间，客户端有新用户指令（CMD_ACCOUNT_INFO）到达时，
     * 同步累加该区域已识别人数，并通知界面更新弹窗内容。
     */
    protected fun syncIdentifiedCountWhileTip(displayType: DisplayType) {
        uiHandler.post {
            if (identifiedCountMap.containsKey(displayType)) {
                identifiedCountMap[displayType] =
                    if (displayType == DisplayType.LEFT) leftPersonArray.size else rightPersonArray.size()
                _identifiedCountLD.tryEmit(Unit)
            }
        }
    }

    /**
     * 用户确认清空：按需发送清除指令，只发给上报过已识别人数的区域（左区/右区），
     * 并同步清空主屏本地对应区域数据、重置卡片状态。
     */
    open fun confirmClearIdentifyInfo() {
        //不支持协同的运动不向客户端发送Grpc消息（单屏时本方法也不会有记录，直接返回）
        if (!isSupportCollaboration()) return
        identifiedCountMap.keys.forEach { displayType ->
            val ip = when (displayType) {
                DisplayType.LEFT -> leftSubScreenIp
                DisplayType.RIGHT -> rightSubScreenIp
                else -> null
            }
            val clientId = ip?.let { GrpcManager.getClientIdByIp(it) }
            if (clientId != null) {
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
     * 用户取消清空：关闭本次提示，清空计数记录（本次提示处理结束）
     */
    open fun cancelClearIdentifyInfo() {
        identifiedCountMap.clear()
    }

    /**
     * 记录/移除副屏占用中状态，并在占用集合变化时通知界面更新"是否强制加入协同"提示
     */
    private fun updateOccupiedSubScreens(displayType: DisplayType, occupied: Boolean) {
        if (displayType != DisplayType.LEFT && displayType != DisplayType.RIGHT) return
        val changed =
            if (occupied) occupiedSubScreens.add(displayType) else occupiedSubScreens.remove(
                displayType
            )
        if (!changed) return
        uiHandler.post {
            LogUtil.d("副屏占用中变化：${occupiedSubScreens.map { it.name }}")
            if (sportState != MultiSportState.COUNTDOWN
                && sportState != MultiSportState.SPORTING
            ) {
                _subScreenOccupiedLD.tryEmit(occupiedSubScreens.toSet())

            }
        }
    }

    /**
     * 用户确认强制加入：向占用中的副屏发送强制加入协同指令
     */
    open fun confirmForceJoin() {
        occupiedSubScreens.forEach { displayType ->
            val ip = when (displayType) {
                DisplayType.LEFT -> leftSubScreenIp
                DisplayType.RIGHT -> rightSubScreenIp
                else -> null
            }
            val clientId = ip?.let { GrpcManager.getClientIdByIp(it) }
            if (clientId == null) {
                LogUtil.d("${displayType.name}副屏未连接，无法发送强制加入指令：$ip")
                return@forEach
            }
            LogUtil.d("发送强制加入协同指令到${displayType.name}副屏：$ip")
            sendCollaborationCommand(clientId, CollaborationCmdEnum.FORCE_JOIN)
        }
    }

    /**
     * 用户取消强制加入：仅关闭本次提示，保留占用记录，等待副屏重新上报状态
     */
    open fun cancelForceJoin() {
        LogUtil.d("取消强制加入协同")
    }

    /**
     * 主屏本地同步清空副屏区域已识别数据，并重置对应道次卡片为未识别状态
     */
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
                _changeSportViewStateLD.tryEmit(
                    displayType to (index to MultiSportCardViewState.NOBODY)
                )
            }
        }
    }

    /**
     * 注册成绩指令接收
     */
    private fun registerResult() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_RESULT, grpcKey) { data ->
            val result = data.payload.toObject<GrpcSportResult>()
            result?.let {
                val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
                val score = result.sportResult.toIntOrNull() ?: 0
                uiHandler.post {
                    //参考主屏postResult，将成绩存储到对应区域人员数组的count中
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
                    refreshCurrentBest()
                    _singleResultLD.tryEmit(displayType to (result.index to score))
                }
            }
        }
    }

    /**
     * 注册用户消息
     */
    open fun registerAccountInfo() {
        //注册人员消息
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_ACCOUNT_INFO, grpcKey) { data ->
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
                    //存储到对应区域的人员数组，供成绩存储/底部导航栏统计使用
                    DisplayType.LEFT -> {
                        if (account.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                            for (index in 0 until leftPersonArray.size) {
                                val multiResult = leftPersonArray.valueAt(index)
                                //已识别过此人
                                if (multiResult.accountInfo.studentEntity!!.accountId == account.studentEntity?.accountId) {
                                    leftPersonArray.removeAt(index)
                                    changeMultiIdentityCardState(
                                        multiResult.number,
                                        MultiSportCardViewState.NOBODY
                                    )
                                    break
                                }
                            }
                        }
                        leftPersonArray.put(info.index, MultiSportResult(account, info.index))
                        identified(DisplayType.LEFT, info.index, account)
                        //弹窗展示期间，新用户到达需同步已识别人数
                        syncIdentifiedCountWhileTip(DisplayType.LEFT)
                        //副屏识别到人，统一检查协同开始条件
                        checkCollaborationSportStart()
                    }

                    DisplayType.RIGHT -> {
                        if (account.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                            for (index in 0 until rightPersonArray.size) {
                                val multiResult = rightPersonArray.valueAt(index)
                                //已识别过此人
                                if (multiResult.accountInfo.studentEntity!!.accountId == account.studentEntity?.accountId) {
                                    rightPersonArray.removeAt(index)
                                    changeMultiIdentityCardState(
                                        multiResult.number,
                                        MultiSportCardViewState.NOBODY
                                    )
                                    break
                                }
                            }
                        }
                        rightPersonArray.put(info.index, MultiSportResult(account, info.index))
                        identified(DisplayType.RIGHT, info.index, account)
                        //弹窗展示期间，新用户到达需同步已识别人数
                        syncIdentifiedCountWhileTip(DisplayType.RIGHT)
                        //副屏识别到人，统一检查协同开始条件
                        checkCollaborationSportStart()
                    }

                    else -> LogUtil.d("收到未知区域用户消息：clientId=${data.from}")
                }
            }
        }
    }

    /**
     *
     */
    open fun queryOnlineReport(recordId: String?) {
        if (recordId.isNullOrEmpty()) {
            ToastUtil.getInstance(getApplication(), "暂无报告").show()
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

    /**
     * 注册卡片状态消息
     */
    open fun registerSportCardStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_CARD_STATUS, grpcKey) {
            val sportCardStatus = it.payload.toObject<GrpcSportCardStatus>()
            LogUtil.d(
                "收到客户端（${GrpcManager.getClientIp(it.from)}）消息：${it.commandCode},payload:${
                    it.payload.toObject<GrpcSportCardStatus>().toString()
                }"
            )
            sportCardStatus?.let { card ->
                MultiSportCardViewState.fromValue(card.status)?.let { state ->
                    uiHandler.post {
                        _changeSportViewStateLD.tryEmit(
                            it.from.getDisplayType(
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
        sportingCountDownTimer?.cancel()
        startCountDownTimer?.cancel()
        toDone()
        frameProcessor.onRelease()
        sportState = MultiSportState.IDLE
        cameraDispatcher.quit()
        uiHandler.removeCallbacksAndMessages(null)
        focalHandler.removeCallbacksAndMessages(null)
        videoRecorder.onStop()
        videoRecorder.onRelease()
//        asyncPlayer.stop()
        config.unloadModel()
        multiSportProcessor.onRelease()
        GrpcManager.unregisterHandler(grpcKey)
    }
}
