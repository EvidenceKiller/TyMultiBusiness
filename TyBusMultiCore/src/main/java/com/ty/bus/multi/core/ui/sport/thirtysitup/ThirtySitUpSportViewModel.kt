package com.ty.bus.multi.core.ui.sport.thirtysitup

import android.app.Application
import android.graphics.Rect
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import androidx.core.util.size
import androidx.lifecycle.viewModelScope
import com.adl.base.element.ElementFpsCounter
import com.adl.base.overlay.SportOverlayMultiRegion
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.sport.TsSportChannel
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.adl.sport.general.situp.SitUpBodyFilter
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiPipelineIdentifyViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.ThirtySitUpSportConfig
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.factory.AbstractMultiSportFactory
import com.ty.bus.multi.core.factory.MultiSportProduceFactory
import com.ty.bus.multi.core.ui.sport.thirtysport.CollaborationAreaState
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
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
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.max

/**
 * 30人仰卧起坐 ViewModel：主屏 + 左副屏 + 右副屏，每屏10人。
 *
 * 业务逻辑在十人仰卧起坐（TenSitUpSportViewModel/Activity）的基础上，
 * 按照三十人跳绳（ThirtySportViewModel/Activity）扩展十人跳绳的方式，
 * 增加三屏协同（gRPC）能力：
 * - 副屏识别/卡片状态/成绩通过 gRPC 上报，主屏统一渲染三个区域
 * - 协同开始条件：所有屏满员或所有屏倒计时结束
 * - 运动结束条件：主屏与所有已加入副屏均结束
 * - 结果页合并三屏成绩统一排名
 */
class ThirtySitUpSportViewModel(application: Application) :
    BaseMultiPipelineIdentifyViewModel(application) {

    private val GRPC_KEY = "ThirtySitUpSportViewModel" //注册Grpc消息的ID
    private val leftSubScreenIp by lazy { BusiBaseConfig.readLeftSubScreenIp() }
    private val rightSubScreenIp by lazy { BusiBaseConfig.readRightSubScreenIp() }

    var thirtySitUpSportConfig: ThirtySitUpSportConfig? = null
    private var createTime: Long = 0

    //人脸识别帧数据缓存
    private val onceCacheProcessor by lazy { OnceCacheProcessor() }

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    private val config = MultiSportConfig()

    private lateinit var multiSportProcessor: MultiSportProcessor
    private lateinit var multiSportParser: MultiSportParser<*>

    //主屏（中区）已识别的位置和人员信息
    val sitUpPersonArray by lazy { SparseArray<MultiSportResult>() }
    val leftPersonArray by lazy { SparseArray<MultiSportResult>() } //左副屏
    val rightPersonArray by lazy { SparseArray<MultiSportResult>() } //右副屏

    //随堂赛的组号
    var personIndex = 0

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var sitUpManCount = 0 //每屏运动人数（10人）

    //记录开始和结束时间
    var startTime: Long = 0
    var endTime: Long = 0

    //是否正在播报违规语音
    private var isSpeeching = false

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

    //主屏识别倒计时已结束、等待其他区域倒计时结束
    private val _waitOtherAreaLD = eventFlow<Unit>()
    val waitOtherAreaLD: SharedFlow<Unit> = _waitOtherAreaLD.asSharedFlow()

    //比赛结束事件（一次性消息：非粘性，避免返回页面重复展示结果页）
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
        checkArea(DisplayType.MAIN, sitUpPersonArray)
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

    /**
     * 更新单个区域协同状态，保留其他区域已有状态，避免互相覆盖
     */
    private fun updateCollaborationState(displayType: DisplayType, state: CollaborationAreaState) {
        collaborationStateMap[displayType] = state
        uiHandler.post { _collaborationStateLD.value = collaborationStateMap.toMap() }
    }

    //协同开始条件跟踪：主屏倒计时是否结束
    private var mainCountDownEnded = false

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

    var countDownTime: Long = 15 //识别倒计时，单位秒

    //识别倒计时过程中更新UI（每秒一次，高频事件流）
    private val _countDownNoticeLD = eventFlow<Pair<DisplayType, String>>()
    val countDownNoticeLD: SharedFlow<Pair<DisplayType, String>> = _countDownNoticeLD.asSharedFlow()

    //记录每个位置状态
    private val currentPersonState =
        mutableListOf<GroupSportsBaseChannelStatus>() //记录每个位置进入的状态，用来处理是否可以人脸识别
    private val currentCardState = mutableListOf<MultiSportCardViewState>()

    private var sitUpState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    lateinit var ipcHik: NsCameraHKSource

    //X秒竞技倒计时的Timer任务
    private var startCountDownTimer: CountDownTimer? = null

    //当前播放音效的索引
    var soundIndex = 0

    //总音效数
    val soundCount = 5
    val simpleFormat = SimpleDateFormat("m:ss")
    var sportCountDownTime = 60L // 比赛时间 S

    //运动倒计时文本（区域 → 文案，每秒一次，高频事件流）
    private val _sportCountDownLD = eventFlow<Pair<DisplayType, String>>()
    val sportCountDownLD: SharedFlow<Pair<DisplayType, String>> = _sportCountDownLD.asSharedFlow()

    private lateinit var factory: AbstractMultiSportFactory

    //运动倒计时任务
    private var sportingCountDownTimer: CountDownTimer? = null

    fun initEngine(number: Int) {
        factory =
            MultiSportProduceFactory.getFactory(sportInfo?.sportItemCode, sportInfo?.testCount)
        sitUpManCount = number
        for (index in 1..sitUpManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        countDownTime = getDefaultCountDownTime()
        initProcess()
        if (streamType == 1) {
            initCamera()
        } else {
            cameraDispatcher.addFrameProcessor(onceCacheProcessor)
        }
        //目前只有常规模式，才需要人脸识别，运动会模式不需要
        if (getSportModel() == SportModel.NORMAL) {
            initFaceIdentify()
        }
    }

    fun loadPerson(data: ArrayList<MeetGroupDetailsEntity>?) {
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
            sitUpPersonArray.put(
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
    private fun processCallback(sitUpManList: MutableList<GroupSportBaseMan>) {
        //处理卡片和人的状态
        if (sitUpManList.isEmpty()) {
            handleNoPersonState()
        } else {
            handlePersonState(sitUpManList)
        }
        when (sitUpState) {
            MultiSportState.PREPARING, MultiSportState.IDENTIFYING -> {
                handleIdentify(sitUpManList)
            }

            MultiSportState.COUNTDOWN -> {
            }

            MultiSportState.SPORTING -> {
                postResult(sitUpManList)
            }

            MultiSportState.IDLE -> {
            }

            MultiSportState.WAITING_START -> {
            }
        }
    }

    private fun initProcess() {
        multiSportProcessor = MultiSportProcessor()
        config.debug = true
        config.bodyModelId = factory.getBodyModelId()
        config.poseModelId = factory.getPoseModelId()
        config.faceStatusBodyId = factory.getFaceBodyModelId()
        config.faceStatusPoseId = factory.getFacePoseModelId()
        config.drawAllBody = true
        config.drawBody = true
        config.drawSkeleton = true
        config.sportType = factory.getMultiSportType()
        config.loadModel(null)
        config.liteBodyDetect = factory.liteBodyDetect()
        multiSportProcessor.setConfig(config)
        //仰卧起坐身体框过滤器
        multiSportProcessor.setBodyFilter(SitUpBodyFilter(0.3f))
        multiSportProcessor.setSmooth(factory.enableSmooth())
        multiSportProcessor.setHistoryBody(factory.enableHistoryBody())
        multiSportProcessor.setEnableDropFrame(false) // 整体丢帧: 全局的丢帧
        multiSportProcessor.setBodyDropFrame(false) // 默认是打开
        multiSportProcessor.setBodyInterval(3) // 丢帧间隔
        multiSportParser = factory.getParser()
        multiSportProcessor.setParser(multiSportParser)
        multiSportProcessor.setCallback(object : MultiSportCallbackImpl() {
            override fun onResult(result: com.adl.sport.general.multi.MultiSportResult) {
                processCallback(result.groupManList)
            }

            override fun onBreakRules(index: Int, code: Int, role: String?) {
                super.onBreakRules(index, code, role)
                LogUtil.d("onBreakRules: $index -- $code -- $role")
                //违规语音播报，一次只播报一条
                if (!isSpeeching) {
                    isSpeeching = true
                    SpeechWrapper.speech2("${index + 1}号${role}") {
                        isSpeeching = false
                    }
                }
            }

            override fun onError(index: Int, code: Int, error: String?) {}
        })
        onceCacheProcessor.addFrameProcessor(multiSportProcessor)
    }

    private fun createCountDownTimer() =
        object : CountDownTimer(getDefaultCountDownTime() * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _countDownNoticeLD.tryEmit(DisplayType.MAIN to "${countDownTime--}")
            }

            override fun onFinish() {
                //主屏倒计时结束，统一检查协同开始条件
                mainCountDownEnded = true
                sitUpState = MultiSportState.WAITING_START
                checkCollaborationSportStart()
                //主屏识别倒计时已结束，但仍有已加入副屏未倒计时结束：提示等待其他区域
                if (needWaitOtherAreaJoin()) {
                    _waitOtherAreaLD.tryEmit(Unit)
                }
            }
        }

    private fun initCamera() {
        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(onceCacheProcessor)
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
    private fun setResolution(camera: AdlCamera, re: Byte, frameRate: Byte = 15) {
        val hkSource = camera as NsCameraHKSource
        val deviceItem = hkSource.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, re, 27, frameRate)
    }

    fun openTsProcess() {
        multiSportParser.checkChannelStatus = true
        sitUpState = MultiSportState.PREPARING
        multiSportProcessor.changeTo(SportProcessorStatus.Prepare)
    }

    fun combineOverlay(overlay: SportOverlayMultiRegion) {
        multiSportProcessor.open(overlay)
        multiSportProcessor.onInit()
    }

    override fun initFaceIdentify() {
        super.initFaceIdentify()
        //0~9索引对应1~10道
        multiIdentityEngine.addIdentityConfig(0, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 2
            minScoreThreshold = 0.7f
        })
        multiIdentityEngine.addIdentityConfig(1, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 2
            minScoreThreshold = 0.7f
        })
        multiIdentityEngine.addIdentityConfig(2, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 2
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(3, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 2
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(4, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(5, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(6, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.4f
        })
        multiIdentityEngine.addIdentityConfig(7, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.5f
        })
        multiIdentityEngine.addIdentityConfig(8, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.5f
        })
        multiIdentityEngine.addIdentityConfig(9, PipelineFaceIdentifyConfig().apply {
            visitorRoundCount = 3
            minScoreThreshold = 0.4f
        })
    }

    override fun identifyFail(index: Int) {
        //处理下一道是否有人
        var tempNumber = currentIdentifyNumber + 1
        tempNumber = tempNumber % sitUpManCount
        var findOk = false
        while (tempNumber < sitUpManCount) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (currentPersonState[tempNumber] == GroupSportsBaseChannelStatus.checkDown && sitUpPersonArray.get(
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
            currentIdentifyNumber = -1
        }
    }

    override fun identifyOk(accountIndex: Int, accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (sitUpState != MultiSportState.IDENTIFYING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $currentIdentifyNumber")
            return
        }
        SpeechWrapper.speech(
            getString(
                R.string.identified_someone,
                accountInfo.studentEntity!!.accountName
            )
        )
        viewModelScope.launch(Dispatchers.Main) {
            sitUpState = MultiSportState.PREPARING
            //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
            if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                var rmKey = -1
                sitUpPersonArray.forEach { key, value ->
                    //已识别过此人
                    if (value.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity!!.accountId) {
                        LogUtil.d("已识别过${value.accountInfo.studentEntity!!.accountName}")
                        rmKey = key
                        changeMultiIdentityCardState(
                            value.number,
                            MultiSportCardViewState.IDLE
                        )
                    }
                }
                if (rmKey != -1) {
                    sitUpPersonArray.remove(rmKey)
                }
            }
            //如果是组织测试，需判断该人是否在组织计划中
            val checkResult = withContext(Dispatchers.IO) {
                checkStudent(accountInfo.studentEntity!!.accountId, ptPlanId)
            }
            if (checkResult) {
                //存已识别的人信息
                sitUpPersonArray.put(
                    accountIndex,
                    MultiSportResult(accountInfo, accountIndex)
                )
                _identifiedLD.tryEmit(DisplayType.MAIN to Pair(accountIndex, accountInfo))
                //给accountInfo赋值，用来获取存储视频的地址
                if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                    accountInfoLD.postValue(accountInfo)
                }
                changeMultiIdentityCardState(
                    accountIndex,
                    MultiSportCardViewState.IDENTIFIED
                )
                //重置识别道次
                currentIdentifyNumber = -1
                //主屏识别满员，统一检查协同开始条件
                if (sitUpPersonArray.size == sitUpManCount) {
                    checkCollaborationSportStart()
                    return@launch
                }
                //第一个人识别成功，则开始倒计时
                if (sitUpPersonArray.size == 1 && startCountDownTimer == null) {
                    countDownTime = getDefaultCountDownTime()
                    startCountDownTimer = createCountDownTimer()
                    startCountDownTimer?.start()
                }
                //识别到人，取消超时机制
                timeOutHelper.cancel()
                LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $accountIndex")
            } else {
                currentIdentifyNumber = -1
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

    /**
     * 处理成绩，通知UI层更新成绩UI
     */
    private fun postResult(result: MutableList<GroupSportBaseMan>) {
        //运动中才更新成绩，否则结束成绩回调会是0
        if (sitUpState == MultiSportState.SPORTING) {
            result.forEach {
                sitUpPersonArray[it.positionNum]?.count = it.score
            }
            refreshCurrentBest()
            _resultLD.tryEmit(DisplayType.MAIN to result)
        }
    }

    /**
     * 没有人了，处理一下状态复位
     */
    @Synchronized
    private fun handleNoPersonState() {
        when (sitUpState) {
            //正在识别状态，清掉卡片状态，并复位状态到准备中
            MultiSportState.IDENTIFYING -> {
                for (index in currentPersonState.indices) {
                    if (currentPersonState[index] != GroupSportsBaseChannelStatus.none) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
                    }
                }
                currentIdentifyNumber = -1
                sitUpState = MultiSportState.PREPARING
            }

            //准备中，即将开始，不处理离开
            MultiSportState.PREPARING -> {

            }

            //运动中，最后一个人离开需要更新卡片到离开状态,可直接更新所有位置的卡片为离开状态
            MultiSportState.SPORTING -> {
                for (index in 0 until sitUpManCount) {
                    val multiResult = sitUpPersonArray.get(index, null)
                    if (multiResult != null) {
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
    private fun handlePersonState(sitUpManList: MutableList<GroupSportBaseMan>) {

        //非运动状态下记录每个位置的人员进入状态
        for (sitUpMan in sitUpManList) {
            val nextState = sitUpMan.channelStatus
            currentPersonState[sitUpMan.positionNum] = nextState
            LogUtil.d("处理位置状态：道数 ${sitUpMan.positionNum} 状态 $nextState")
            //处理进入状态
            when (nextState) {
                //躺下就绪
                GroupSportsBaseChannelStatus.layDown -> {
                    changeMultiIdentityCardState(
                        sitUpMan.positionNum,
                        MultiSportCardViewState.PREPARED
                    )
                }

                else -> {}
            }
        }
    }

    @Synchronized
    private fun handleIdentify(sitUpManList: MutableList<GroupSportBaseMan>) {
        //目前只有常规模式才需要识别人脸，运动会模式不需要
        if (getSportModel() != SportModel.NORMAL) return

        //如果为识别状态
        if (sitUpState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val sitUpMan =
                sitUpManList.find { sitUpMan -> sitUpMan.positionNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (sitUpMan == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                currentIdentifyNumber = -1
                sitUpState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(sitUpMan),
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

        sitUpManList.forEach {
            if (it.channelStatus == GroupSportsBaseChannelStatus.checkDown
                && sitUpPersonArray.get(it.positionNum, null) == null
                && checkHandUp(it)
            ) {
                currentIdentifyNumber = it.positionNum
                sitUpState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(it),
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
     * 仰卧起坐识别条件：举手（手腕高于肩部）
     */
    private fun checkHandUp(it: GroupSportBaseMan): Boolean {
        if (it.skeleton == null || it.skeleton.lShoulder == null || it.skeleton.rWrist == null)
            return false
        return it.skeleton.lWrist.y < it.skeleton.lShoulder.y
    }

    /**
     * 主屏触发准备开始（倒计时结束/满员/点击开始按钮），统一检查协同开始条件
     */
    fun readyGo() {
        checkCollaborationSportStart()
    }

    /**
     * 协同开始条件变化时统一调用：
     * 满足条件则开始主屏3-2-1倒计时，并向已连接副屏发送开始运动指令
     */
    private fun checkCollaborationSportStart() {
        if (!canStartCollaborationSport()) return
        startSport()
    }

    private fun startSport() {
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
        if (sitUpPersonArray.size <= 0) return false
        if (DisplayType.LEFT in connectedSubScreens && collaborationStateMap[DisplayType.LEFT] == CollaborationAreaState.NORMAL && leftPersonArray.size <= 0) return false
        if (DisplayType.RIGHT in connectedSubScreens && collaborationStateMap[DisplayType.RIGHT] == CollaborationAreaState.NORMAL && rightPersonArray.size <= 0) return false
        //条件1：所有屏（主屏+已连接副屏）都识别满员才能直接开始，未连接的副屏不参与判定
        val allScreenFull = sitUpPersonArray.size >= sitUpManCount &&
                (DisplayType.LEFT !in connectedSubScreens || leftPersonArray.size >= sitUpManCount) &&
                (DisplayType.RIGHT !in connectedSubScreens || rightPersonArray.size >= sitUpManCount)
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
    private fun tryStartSport() {
        if (mainSportStarted) return
        mainSportStarted = true
        startCountDownTimer?.cancel()
        _countDownFinishLD.tryEmit(DisplayType.MAIN to true)
        //开始3-2-1倒计时就表示比赛已经开始
        sitUpState = MultiSportState.COUNTDOWN
    }

    private fun changeMultiIdentityCardState(index: Int, state: MultiSportCardViewState) {
        uiHandler.post {
            LogUtil.d("changeMultiIdentityCardState: index $index state ${state.name}")
            _changeSportViewStateLD.tryEmit(DisplayType.MAIN to Pair(index, state))
        }
    }

    private fun identified(type: DisplayType, index: Int, accountInfo: AccountInfo) {
        uiHandler.post {
            _identifiedLD.tryEmit(type to Pair(index, accountInfo))
        }
    }

    /**
     * 开始录像,在此回调中处理录像
     */
    fun onCameraOpen() {
        multiSportProcessor.clearTransform()
        if (sitUpState == MultiSportState.SPORTING || sitUpState == MultiSportState.COUNTDOWN) {
            LogUtil.d("${sitUpManCount}人运动，开始录像")
            startRecord()
        }
    }

    private fun startRecord() {
        getVideoPath(sportInfo!!.sportSkuId)
        createTime = System.currentTimeMillis()
        if (videoRecorder.isStarting) {
            LogUtil.d("${sitUpManCount}人运动,录像已开始")
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
        sitUpState = MultiSportState.SPORTING
        _sportStartedLD.value = true
        refreshCurrentBest()
        startTime = System.currentTimeMillis()
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
        multiSportParser.checkSkipScore = true
        sportCountDownTime = sportInfo?.endValue?.toLong()?.div(1000) ?: 60
        sportingCountDownTimer = object : CountDownTimer(sportCountDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sportCountDownTime--
                //每隔10秒，且剩余时间大于等于5秒，播放音效
                if (sportCountDownTime % 10 == 0L && sportCountDownTime >= 5) {
                    SoundPlayUtils.play(25 + soundIndex++ % soundCount)
                }
                if (sportCountDownTime == 10L) {
                    _changeTimeTextBgLD.tryEmit(DisplayType.MAIN to R.drawable.pk_skip_rose_time_warning_shape)
                }

                _sportCountDownLD.tryEmit(
                    DisplayType.MAIN to simpleFormat.format(Date((sportCountDownTime) * 1000))
                )
            }

            override fun onFinish() {
                //比赛结束
                _sportCountDownLD.tryEmit(DisplayType.MAIN to "0:00")
                sportFinish()
            }

        }
        sportingCountDownTimer?.start()
        //比赛开始设置卡片状态为运动状态
        for (index in 0 until sitUpManCount) {
            val multiResult = sitUpPersonArray.get(index, null)
            if (multiResult != null) {
                currentCardState[index] = MultiSportCardViewState.SPORTING
            } else {
                changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY_SPORT)
            }
        }
    }

    /**
     * 结束比赛
     */
    fun toDone() {
        toStatusSportEnd()
        //有崩溃，捕获异常
        try {
            sitUpState = MultiSportState.IDLE
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
    private fun sportFinish() {
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
    private fun checkAllSportFinished() {
        if (!mainSportFinished) return
        val joinedSubScreens =
            listOf(DisplayType.LEFT, DisplayType.RIGHT).filter { isSubScreenJoined(it) }
        if (joinedSubScreens.any { it !in subScreenSportFinished }) {
            LogUtil.d("等待副屏运动结束：已加入=$joinedSubScreens，已结束=$subScreenSportFinished")
            return
        }
        handleResult(sitUpPersonArray)
    }

    /**
     * 处理成绩：合并主屏与已连接副屏成绩，按高优排序，计算名次，并生成结果页统一数据
     */
    private fun handleResult(sitUpPersonArray: SparseArray<MultiSportResult>) {
        if (sportInfo == null || sitUpPersonArray.isEmpty()) {
            return
        }
        val resultList = mutableListOf<MultiSportResult>()
        sitUpPersonArray.forEach { _, value ->
            resultList.add(value)
        }
        resultList.sortWith(compareBy { -it.count })
        var preRank = 0
        var preScore = -1
        for (index in resultList.indices) {
            val multiResult = resultList[index]
            var rank = 0
            if (multiResult.count == preScore) {
                rank = preRank
            } else {
                rank = preRank + 1
            }
            preRank = rank
            preScore = multiResult.count
            multiResult.rank = rank
        }
        //合并主屏+副屏成绩，供结果页统一展示
        val mergedItems = mergeAllAreaResults(resultList)
        _toDoneLD.tryEmit(mergedItems)
        handleUploadResult(resultList)
    }

    /**
     * 合并主屏与已连接副屏成绩，生成结果页统一数据项，按成绩高优排序并重新计算总排名
     */
    private fun mergeAllAreaResults(mainResults: MutableList<MultiSportResult>): List<ThirtySportResultItem> {
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
    private fun handleUploadResult(resultList: MutableList<MultiSportResult>) {
        GlobalScope.launch(Dispatchers.IO) {
            val multiId = if (personIndex > 0) {
                "${resultList[0].meetGroup?.classId}${personIndex}"
            } else {
                System.currentTimeMillis().toString()
            }
            SystemClock.sleep(1500)
            for (index in resultList.indices) {
                val multiResult = resultList[index]
                val sportResult = SportResult()
                sportResult.sportDuration = sportInfo!!.endValue.toInt()
                sportResult.maxComboCount = 0
                sportResult.sportAchievement = multiResult.count
                sportResult.perfectCount = 0
                sportResult.goodCount = 0
                sportResult.comeOnCount = 0
                sportResult.perfectScore = multiResult.count
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
                var recordPlan: SportRecordEntity.RecordPlan? = null
                if (sceneId == SceneConstants.ORGANIZE_TEST) {
                    recordPlan = SportRecordEntity.RecordPlan().apply {
                        ptPlanId = this@ThirtySitUpSportViewModel.ptPlanId
                        ptStandardId = this@ThirtySitUpSportViewModel.ptStandardId
                    }
                }
                var sportMeet: SportRecordEntity.RecordMeeting? = null
                recordMeet?.let {
                    sportMeet = SportRecordEntity.RecordMeeting().apply {
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
                    sceneId = sceneId ?: SceneConstants.PK,
                    sportResult,
                    sportResult.userId,
                    recordPlan = recordPlan,
                    recordMeeting = sportMeet,
                )
            }
        }
    }

    fun getDefaultCountDownTime(): Long = 15

    fun getSportResolution(): Byte = 70

    fun getCameraSize(): Pair<Int, Int> = 2560 to 1440

    //获取运动模式
    fun getSportModel(): SportModel {
        return thirtySitUpSportConfig?.sportModel ?: SportModel.NORMAL
    }

    fun getInitCameraFrameRate(): Byte = if (getSportModel() == SportModel.NORMAL) 15 else 17

    fun getIdentifyResolution(): Byte =
        if (getSportModel() == SportModel.NORMAL) 70 else getSportResolution()

    /**
     * 仰卧起坐人脸识别区域：优先用头部关键点推测，无头部关键点时用双肩推测
     */
    private fun getIdentityRect(it: TsSportChannel?): Rect {
        var rect = Rect(0, 0, 50, 50)
        if (it?.body == null || it.skeleton == null) {
            LogUtil.d("it.bodyrect  is Null null null null ------", true)
            return rect
        }
        LogUtil.d("处理人脸识别：用骨骼")
        val width = 180
        val header = it.skeleton.headTop
        if (header != null) {
            rect.top = header.y.toInt()
            rect.left = header.x.toInt() - width / 2
            rect.right = header.x.toInt() + width / 2
            rect.bottom = header.y.toInt() + width
        } else {
            val lShouder = it.skeleton.lShoulder
            val rShouder = it.skeleton.rShoulder
            if (lShouder == null || rShouder == null) return rect
            val centerX: Double = (lShouder.x + rShouder.x) / 2.0
            val shoulderWidth = abs(lShouder.x - rShouder.x) * 2.7
            rect.bottom = max(lShouder.y, rShouder.y).toInt()
            rect.left = (centerX - shoulderWidth / 2.0).toInt()
            rect.right = (centerX + shoulderWidth / 2).toInt()
            rect.top = (rect.bottom - 1.2 * shoulderWidth).toInt()
        }

        if (rect.width() < 40 || rect.height() < 40) {
            rect.left = rect.left - 20
            rect.right = rect.left + 40
            rect.top = rect.top - 20
            rect.bottom = rect.top + 40
        }
        return rect
    }

    override fun reset() {
        super.reset()
        //重置，重新开始运动流程
        //清除所有数据
        currentPersonState.clear()
        currentCardState.clear()
        for (index in 1..sitUpManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        sitUpPersonArray.clear()

        //重置人脸识别
        initFaceIdentify()

        multiSportProcessor.onReset()
        openTsProcess()
    }

    fun onFrame(yuv: ByteArray, info: AdlCameraPreviewInfo) {
        cameraDispatcher.onFrame(yuv, info)
    }

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
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COUNTDOWN_END, GRPC_KEY) {
            subScreenCountDownEnded.add(it.from.getDisplayType(leftSubScreenIp, rightSubScreenIp))
            checkCollaborationSportStart()
        }
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

    /**
     * 发送协同指令
     */
    private fun sendCollaborationCommand(clientId: String, cmd: CollaborationCmdEnum) {
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
    fun sendCollaborationFinish() {
        leftSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
        }
        rightSubScreenIp?.let { GrpcManager.getClientIdByIp(it) }?.let {
            sendCollaborationCommand(it, CollaborationCmdEnum.FINISH)
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
                //其余状态可加入协同
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
                    //与副屏成绩写入保持同一线程时序，避免结果未合并就出成绩页
                    uiHandler.post { checkAllSportFinished() }
                }
            }
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
     * 指定副屏是否已加入协同（collaborationStateMap 中状态为 NORMAL 可加入）
     */
    fun isSubScreenJoined(displayType: DisplayType): Boolean =
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
     * "是否清空已识别结果"弹窗展示期间，客户端有新用户指令（CMD_ACCOUNT_INFO）到达时，
     * 同步累加该区域已识别人数，并通知界面更新弹窗内容。
     */
    private fun syncIdentifiedCountWhileTip(displayType: DisplayType) {
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
    fun confirmClearIdentifyInfo() {
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
    fun cancelClearIdentifyInfo() {
        identifiedCountMap.clear()
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
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_RESULT, GRPC_KEY) { data ->
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
    private fun registerAccountInfo() {
        //注册人员消息
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
                    //存储到对应区域的人员数组，供成绩存储/底部导航栏统计使用
                    DisplayType.LEFT -> {
                        if (account.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                            for (index in 0 until leftPersonArray.size) {
                                val multiResult = leftPersonArray.valueAt(index)
                                //已识别过此人
                                if (multiResult.accountInfo.studentEntity!!.accountId == account.studentEntity?.accountId) {
                                    leftPersonArray.removeAt(index)
                                    _changeSportViewStateLD.tryEmit(
                                        DisplayType.LEFT to (multiResult.number to MultiSportCardViewState.NOBODY)
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
                                    _changeSportViewStateLD.tryEmit(
                                        DisplayType.RIGHT to (multiResult.number to MultiSportCardViewState.NOBODY)
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
     * 注册卡片状态消息
     */
    private fun registerSportCardStatus() {
        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_SPORT_CARD_STATUS, GRPC_KEY) { data ->
            val sportCardStatus = data.payload.toObject<GrpcSportCardStatus>()
            LogUtil.d("收到客户端（${GrpcManager.getClientIp(data.from)}）消息：${data.commandCode}")
            sportCardStatus?.let { card ->
                MultiSportCardViewState.fromValue(card.status)?.let { state ->
                    uiHandler.post {
                        _changeSportViewStateLD.tryEmit(
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
        sportingCountDownTimer?.cancel()
        startCountDownTimer?.cancel()
        toDone()
        onceCacheProcessor.onRelease()
        sitUpState = MultiSportState.IDLE
        cameraDispatcher.quit()
        uiHandler.removeCallbacksAndMessages(null)
        videoRecorder.onStop()
        videoRecorder.onRelease()
        config.unloadModel()
        multiSportProcessor.onRelease()
        GrpcManager.unregisterHandler(GRPC_KEY)
    }
}
