package com.ty.bus.multi.core.ui.sport.sitreacheighteen

import android.app.Application
import android.graphics.Rect
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
 * 按照三十人跳绳（ThirtySportViewModel/Activity）扩展十人跳绳的方式，
 * 增加三屏协同（gRPC）能力：
 * - 副屏识别/卡片状态/成绩通过 gRPC 上报，主屏统一渲染三个区域
 * - 协同开始条件：主屏或任一副屏有人员识别成功即开始10秒识别倒计时，
 *   倒计时结束时主屏与副屏合计只要有1人识别成功，主屏与已连接副屏同时开始运动
 * - 运动结束条件：主屏与所有已加入副屏均结束，才展示合并后的18人成绩结果页
 */
class SitReachEighteenViewModel(application: Application) :
    BaseMultiPipelineIdentifyViewModel(application) {

    private val GRPC_KEY = "SitReachEighteenViewModel" //注册Grpc消息的ID
    private val leftSubScreenIp by lazy { BusiBaseConfig.readLeftSubScreenIp() }
    private val rightSubScreenIp by lazy { BusiBaseConfig.readRightSubScreenIp() }

    var ffSportConfig: ForwardFlexionConfig? = null

    private val previewSize = 2560 to 1440

    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

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

    var sportManCount = 0 //每屏测试人数（6人）

    var sportTimes = 0 //测试次数

    var currentSportTimes = 1 //当前测试次数

    val identifyTimeOut = 10 * 1000L //识别超时时间，最后一个识别完，此时间内无新识别，则进入准备状态

    val sportTimeOut = 10 //运动超时提示时间

    var sportCountDownTimer: CountDownTimer? = null

    var sportCountDown = 0

    /** 识别倒计时时长（秒）：与副屏一致，主屏或副屏有人员识别成功后开始倒计时 */
    val identifyCountDownSeconds = 10

    /** 识别倒计时（"立即开始"按钮倒计时） */
    private var identifyCountDownTimer: CountDownTimer? = null

    /** 识别倒计时是否正在进行 */
    private var identifyCountDownRunning = false

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var isRetest = false //是否为重测

    //---------- 三屏协同 ----------
    //主屏是否已就绪（识别完成，进入准备状态）
    private var mainReady = false

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

    //识别倒计时剩余秒数：主屏或任一副屏有人员识别成功即开始，倒计时结束主副屏同时开始运动
    private val _identifyCountDownLD = eventFlow<Int>()
    val identifyCountDownLD: SharedFlow<Int> = _identifyCountDownLD.asSharedFlow()

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
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $index")
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
            calcNextNumber(index + 1)
            if (!checkResult) {
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

            LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $index")
            //主屏已识别到人员：触发识别倒计时（倒计时结束即主副屏同时开始运动）
            checkCollaborationSportStart()
            //识别完成
            if (sitReachPersonArray.size() == sportManCount) {
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
        changeSportCardState(DisplayType.MAIN, index, ForwardFlexionCardState.IDENTIFY_FAIL)
        //处理下一道是否有人
        calcNextNumber(index + 1)
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
     * 处理姿态状态：统一检查是否可以开始识别倒计时
     */
    @Synchronized
    private fun handlePrepareStatus(map: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.COUNTDOWN) return
        checkCollaborationSportStart()
    }

    /**
     * 协同识别状态变化时统一调用：主屏或任一副屏有人员识别成功，即开始识别倒计时，
     * 倒计时结束后由 [startSport] 统一开始主屏运动并通知副屏同时开始
     */
    private fun checkCollaborationSportStart() {
        if (!canStartIdentifyCountDown()) return
        startIdentifyCountDown()
    }

    private fun startSport() {
        //主屏与副屏合计没有识别成功的人员，暂不开始
        if (!hasAnyIdentifiedPerson()) return
        tryStartSport()
        sendSportCommand()
    }

    /**
     * 运动开始条件：主屏与副屏合计只要有1人识别成功即可开始
     */
    private fun hasAnyIdentifiedPerson(): Boolean =
        sitReachPersonArray.size() > 0 || leftPersonArray.size() > 0 || rightPersonArray.size() > 0

    /**
     * 识别倒计时触发条件（任一满足即可，不再等待所有屏就绪/满员）：
     * 条件1：主屏已就绪（识别完成进入准备状态）或已有人员识别成功
     * 条件2：任一副屏已有人员识别成功，或副屏倒计时结束（就绪）
     */
    private fun canStartIdentifyCountDown(): Boolean {
        //主屏已就绪或已识别到人员
        if (mainReady || sitReachPersonArray.size() > 0) return true
        //副屏已识别到人员，或副屏倒计时结束（就绪）
        return leftPersonArray.size() > 0 || rightPersonArray.size() > 0 ||
                connectedSubScreenTypes().any { it in subScreenCountDownEnded }
    }

    /**
     * 开始识别倒计时：倒计时期间继续识别，"立即开始"按钮展示剩余秒数，
     * 倒计时结束即开始主屏运动并向已连接副屏发送开始运动指令（主副屏同时开始）
     */
    private fun startIdentifyCountDown() {
        if (identifyCountDownRunning) return
        if (mainSportStarted) return
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
                        //倒计时结束：主屏与副屏合计只要有1人识别成功，主屏与已连接副屏同时开始运动
                        startSport()
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
     * 点击"立即开始"：跳过识别倒计时，主屏与已连接副屏立即同时开始运动
     */
    fun startSportNow() {
        //没有人员识别成功，不允许开始
        if (!hasAnyIdentifiedPerson()) return
        stopIdentifyCountDown()
        startSport()
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
                it.isCompliant.not() -> {
                    changeSportCardState(
                        DisplayType.MAIN,
                        it.indexNum,
                        ForwardFlexionCardState.UNAVAILABLE
                    )
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
                val identifyData = if (streamType == 1) {
                    val nv21 = ByteArray(currentYuvData.width * currentYuvData.height * 3 / 2)
                    AdlYUVUtil.YV12toNV21(
                        currentYuvData.yuv,
                        nv21,
                        currentYuvData.width,
                        currentYuvData.height
                    )
                    nv21
                } else {
                    currentYuvData.yuv
                }
                identify(
                    getIdentityRect(flexionBody),
                    identifyData,
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

        data.forEach {
            if (sitReachPersonArray.get(it.indexNum, null) == null
                && it.skeleton != null
                && checkHandUp(it.skeleton)
            ) {
                currentIdentifyNumber = it.indexNum
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                val identifyData = if (streamType == 1) {
                    val nv21 = ByteArray(currentYuvData.width * currentYuvData.height * 3 / 2)
                    AdlYUVUtil.YV12toNV21(
                        currentYuvData.yuv,
                        nv21,
                        currentYuvData.width,
                        currentYuvData.height
                    )
                    nv21
                } else {
                    currentYuvData.yuv
                }
                identify(
                    getIdentityRect(it),
                    identifyData,
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

    private fun checkHandUp(skeleton: AdlPoseSkeleton?): Boolean {
        val lWrist = skeleton?.lWrist
        val lShoulder = skeleton?.lShoulder
        val rWrist = skeleton?.rWrist
        val rShoulder = skeleton?.rShoulder
        if (lWrist == null || lShoulder == null || rWrist == null || rShoulder == null) return false
        return lWrist.y < lShoulder.y || rWrist.y < rShoulder.y
    }

    fun getIdentityRect(it: SitAndReachObject?): Rect {
        var rect = Rect(0, 0, 50, 50)
        val skeleton = it?.skeleton
        if (it == null || skeleton == null) return rect
        LogUtil.d("处理人脸识别：用骨骼")
        val width = 180
        val header = skeleton.headTop
        if (header != null) {
            rect.top = header.y.toInt()
            rect.left = header.x.toInt() - width / 2
            rect.right = header.x.toInt() + width / 2
            rect.bottom = header.y.toInt() + width
        } else {
            val lShouder = skeleton.lShoulder
            val rShouder = skeleton.rShoulder
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

    /**
     * 处理主屏成绩
     */
    fun handleResult(result: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.SPORTING) return
        viewModelScope.launch(Dispatchers.IO) {
            result.forEach {
                val person = sitReachPersonArray.get(it.indexNum)
                if (person == null) return@forEach
                if (it.state == SitAndReachObject.State.END && resultStatusList[it.indexNum - 1].not()) {
                    person.count = max((it.distanceMaxLoose * 10 - 200).toInt(), -200)
                    person.status = calcViolation(it)
                    resultStatusList[it.indexNum - 1] = true
                    withContext(Dispatchers.Main) {
                        _sportResultLD.tryEmit(
                            DisplayType.MAIN to Pair(it.indexNum, person)
                        )
                    }
                }
            }
            val list = resultStatusList.filter { it }
            val checkSize = checkMap.filter { it.value }.size
            //还有人未出成绩
            if (list.size != checkSize) {
                LogUtil.d("总识别人数：${checkSize} -- 已出成绩:${list.size}")
                return@launch
            }
            //冗余数据，超过了测试次数
            if (currentSportTimes > sportTimes) {
                return@launch
            }
            stopSportCountDown()
            LogUtil.d("测试结束：总测试次数:${sportTimes}, 已测试次数:${currentSportTimes}")
            currentSportTimes++
            sitReachPersonArray.forEach { index, _ ->
                changeSportCardState(
                    DisplayType.MAIN,
                    index,
                    ForwardFlexionCardState.SPORTING
                )
            }
            _testOnceFinishLD.tryEmit(true)
            if (currentSportTimes > sportTimes) {
                //测试次数达标，运动结束
                SpeechWrapper.speech(
                    getString(R.string.end_test, currentSportTimes - 1)
                )
                toStatusSportEnd()
            } else {
                SpeechWrapper.speech(
                    getString(R.string.forward_flexion_sport_times_end, currentSportTimes - 1)
                )
                SystemClock.sleep(2000)
                sportState = MultiSportState.PREPARING
                toStatusPrepare()
            }
        }
    }

    private fun calcViolation(srj: SitAndReachObject): Int {
        if (srj.direction == SitAndReachObject.Direction.UNKNOWN) {
            return 2
        }
        LogUtil.d(
            "====================${srj.failMsgList.getOrNull(0)}--${
                srj.failMsgList.getOrNull(1)
            }--${srj.failMsgList.getOrNull(2)}"
        )
        return if ((param.checkFoot && srj.failMsgList.getOrNull(2) == false)
            || (param.checkHand && srj.failMsgList.getOrNull(0) == false)
            || (param.checkLeg && srj.failMsgList.getOrNull(1) == false)
        ) 1 else 0
    }

    private fun startSportCountDown() {
        LogUtil.d("开始倒计时  开始倒计时  开始倒计时")
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
                    //结束运动
                    finishWithTimeout()
                }
            }.start()
        }
    }

    private fun finishWithTimeout() {
        for (i in resultStatusList.indices) {
            val index = i + 1
            val person = sitReachPersonArray.get(index)
            if (person != null) {
                if (resultStatusList[i].not()) {
                    //未完成
                    LogUtil.d("${person.number}道未完成=============")
                    person.status = 2
                    _sportResultLD.tryEmit(DisplayType.MAIN to Pair(index, person))
                }
                resultStatusList[i] = true
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
     * 重测，只测一次
     */
    fun retest() {
        isRetest = true
        sitReachPersonArray.forEach { _, result ->
            result.count = -1000
        }
        for (ind in resultStatusList.indices) {
            resultStatusList[ind] = false
        }
        sportTimes = 1
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        toStatusPrepare()
        videoFile = null
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
        multiIdentityEngine.reset()
        sitReachPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        videoFile = null
        //重置协同状态
        stopIdentifyCountDown()
        mainReady = false
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
