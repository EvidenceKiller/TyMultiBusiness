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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.adl.sport.general.groupjumpingjack.GroupJumpingJackParser
import com.adl.sport.general.groupskippingremix.GroupSkippingLRemixParser
import com.adl.sport.general.groupskippingremix.GroupSkippingRemixParser
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.groupsquat.GroupSquatParser
import com.adl.sport.general.multi.MultiSportCallback
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.ty.tybusbase.expand.getString
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.factory.AbstractMultiSportFactory
import com.ty.bus.multi.core.factory.MultiSportProduceFactory
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIdentityEngine
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.ApplicationUtil.getContext
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
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.config.SportModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.IndicatorsConstants
import com.ty.tybusbase.face.FaceDetectorHelper
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

open class BaseIPCMultiSportViewModel(application: Application) :
    BaseMultiSportViewModel(application) {
    private var createTime: Long = 0
    private val bsSkipRoseMultiProcessor by lazy { OnceCacheProcessor() } //五人跳绳代理帧数据处理类
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

    protected val multiIdentityEngine by lazy { MultiIdentityEngine() } //人脸识别引擎类

    val skipPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息

    private val indicatorsMap by lazy {
        ConcurrentHashMap<Int, IndicatorsResultDto>()
    }

    //随堂赛的组号
    var personIndex = 0

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var skipManCount = 0 //几人跳绳

    //记录开始和结束时间
    var startTime: Long = 0
    var endTime: Long = 0

    val startPreviewLD = MutableLiveData<Pair<AdlCamera, AdlCameraFrameProcessor>>() //开启预览LD
    val stopPreviewLD = MutableLiveData<Boolean>() //停止预览LD
    val resultLD = MutableLiveData<MutableList<GroupSportBaseMan>>() //成绩结果LD
    val countDownFinishLD = MutableLiveData<Boolean>() //竞技开始倒计时完成LD
    val toDoneLD = MutableLiveData<MutableList<MultiSportResult>>() //比赛结束LD
    val changeSportViewStateLD = MutableLiveData<Pair<Int, MultiSportCardViewState>>() //切换运动卡片状态
//    val indicatorLD = MutableLiveData<IndicatorsResultDto?>()

    //已识别人员
    val identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()
    val changeTimeTextBgLD = MutableLiveData<Int>() //改变时间显示UI的背景色LD
    val cameraLoginStateLD = MutableLiveData<Boolean>() //相机登录状态LD


    var countDownTime: Long = 10 //竞技倒计时10S，单位秒
    var countDownNoticeLD = MutableLiveData<String>() //竞技倒计时过程中更新UI的LD

    //记录每个位置状态
    private val currentPersonState =
        mutableListOf<GroupSportsBaseChannelStatus>() //记录每个位置进入的时间，用来处理是否可以人脸识别
    private val currentCardState = mutableListOf<MultiSportCardViewState>()

    private var skipRoseState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    private val asyncPlayer by lazy { AsyncPlayer("CutDownAsyncPlayer") }

    lateinit var ipcHik: NsCameraHKSource
    var focalHandler = Handler(Looper.getMainLooper())

    //X秒竞技倒计时的Timer任务
    val startCountDownTimer = object : CountDownTimer(getDefaultCountDownTime() * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            countDownNoticeLD.postValue("${countDownTime--}")
        }

        override fun onFinish() {
            readyGo()
        }
    }

    //当前播放音效的索引
    var soundIndex = 0

    //总音效数
    val soundCount = 5

    val simpleFormat = SimpleDateFormat("m:ss")
    var sportCountDownTime = 60L // 比赛时间 S
    val sportCountDownLD = MutableLiveData<String>()//更新倒计时的LD

    private lateinit var factory: AbstractMultiSportFactory

    //运动倒计时任务
    private var sportingCountDownTimer: CountDownTimer? = null
    fun initEngine(number: Int) {
        factory =
            MultiSportProduceFactory.getFactory(sportInfo?.sportItemCode, sportInfo?.testCount)
        skipManCount = number
        for (index in 1..skipManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        countDownTime = getDefaultCountDownTime()
        initProcess()
        initCamera()
        initFaceIdentify()
        //如果是赛事，请求运动详情数据，用于上传数据
        matchId?.let {
//            getFreeModelSportDetail(MatchMultiListActivity.uploadSportDetail!!.sportsDetailUid)
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
            skipPersonArray.put(
                index,
                MultiSportResult(accountInfo, index, meetGroup = meetGroupDetailsEntity)
            )
            identified(index, accountInfo)
            changeMultiIdentityCardState(
                index,
                MultiSportCardViewState.PRECAST_PERSON
            )
        }
    }

    @Synchronized
    private fun processCallback(groupSkippingManList: MutableList<GroupSportBaseMan>) {
        //处理卡片和人的状态
        if (groupSkippingManList.isEmpty()) {
            handleNoPersonState()
        } else {
            handlePersonState(groupSkippingManList)
        }

        when (skipRoseState) {

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
        }
    }

    private fun initProcess() {
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
        bsSkipRoseMultiProcessor.addFrameProcessor(multiSportProcessor)
    }

    private fun initCamera() {
        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(bsSkipRoseMultiProcessor)
        val ip =
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_IP,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP
            )
        val account = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
        )
        val psw =
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_PSW,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
            )
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = false
        ipcHik = hikSubConfig.setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
            ip,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
            account,
            psw
        ).setStreamType(0).build() as NsCameraHKSource
    }

    /**
     * 切分辨率
     */
    private fun changeResolution() {
        val ip =
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_IP,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP
            )
        val account = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
        )
        val psw =
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_PSW,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
            )
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = false
        ipcHik = hikSubConfig.setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
            ip,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
            account,
            psw
        ).setStreamType(0).build() as NsCameraHKSource
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun setResolution(camera: AdlCamera, re: Byte, frameRate: Byte = 17) {
        val hkSource = camera as NsCameraHKSource
        val deviceItem = hkSource.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, re, 27, frameRate)
    }

    open fun initFaceIdentify() {
        //目前只有常规模式，才需要人脸识别，运动会模式不需要
        if (getSportModel() != SportModel.NORMAL) return
        /**
         * 人脸识别回调
         */
        var visitor: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.VISITOR_ABLE,
                BusiBaseConstantsValue.DEFAULT_VISITOR_MODULE
            ) && matchId == null
        multiIdentityEngine.visitorAble = visitor
        FaceDetectorHelper.getInstance().setFrameInterval(2)
        multiIdentityEngine.openMachine(object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityTimeOn() {

            }

            override fun onIdentityOk(accountInfo: AccountInfo) {
                handleIdentityOk(accountInfo)
            }

            override fun onIdentityTimeFail() {
                handleIdentityTimeFail()
            }
        })
    }

    fun openTsProcess() {
//        tsProcessor.combineRegions()
//        tsProcessor.toSporting()
//        tsProcessor.setCheckChannelStatus(true)
        multiSportParser.checkChannelStatus = true
        skipRoseState = MultiSportState.PREPARING
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
    }

    fun combineOverlay(overlay: SportOverlayMultiRegion) {
        multiSportProcessor.open(overlay)
        multiSportProcessor.onInit()
    }

    @Synchronized
    private fun handleIdentityTimeFail() {
        changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFY_FAIL)
        //处理下一道是否有人
        var tempNumber = currentIdentifyNumber + 1
        var findOk = false
        while (tempNumber < skipManCount) {
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
                multiIdentityEngine.reset()
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

    @Synchronized
    private fun handleIdentityOk(accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (skipRoseState != MultiSportState.IDENTIFYING) {
            AdlLogger.d("识别到人脸：但已不是识别状态 $currentIdentifyNumber")
            return
        }


        stopPlayCutDown()
        SpeechWrapper.speech(
            getString(
                R.string.identified_someone,
                accountInfo.studentEntity!!.accountName
            )
        )
        val identifyNumber = currentIdentifyNumber
        synchronized(this) {
            viewModelScope.launch(Dispatchers.Main) {
                //处理结果加锁，post到主线程执行时，子线程可能会改变currentIdentifyNumber的值，或其他状态
                skipRoseState = MultiSportState.PREPARING
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
                    startCountDownTimer.cancel()
                    skipPersonArray.put(
                        identifyNumber,
                        MultiSportResult(accountInfo, identifyNumber)
                    )
                    AdlLogger.d("人脸识别结果：姓名：${accountInfo.studentEntity?.accountName}; 道数 $identifyNumber")
                    identifiedLD.value = Pair(identifyNumber, accountInfo)
                    //给accountInfo赋值，用来获取存储视频的地址
                    if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID && accountInfoLD.value == null) {
                        accountInfoLD.postValue(accountInfo)
                    }
                    LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity?.accountName};道数 $identifyNumber 切换到准备状态")
                    changeMultiIdentityCardState(
                        identifyNumber,
                        MultiSportCardViewState.PREPARED
                    )

                    resetIdentifyNumber()
                    //处理竞技倒计时
                    //如果已识别五人，则直接开始
                    if (skipPersonArray.size == skipManCount) {
                        readyGo()
                        return@launch
                    }
                    //重新复位倒计时时间
                    countDownTime = getDefaultCountDownTime()
                    countDownNoticeLD.value =
                        "${getString(R.string.race_start_countdown)}：${countDownTime}"
                    startCountDownTimer.start()
                    playCutDown()
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
    private fun resetIdentifyNumber() {
        LogUtil.d("重置识别编号为-1")
        currentIdentifyNumber = -1
    }

    /**
     * 处理成绩，通知UI层更新成绩UI
     */
    private fun postResult(result: MutableList<GroupSportBaseMan>) {
        //运动中才更新成绩，否则结束成绩回调会是0
        if (skipRoseState == MultiSportState.SPORTING) {
            var str = ""
            result.forEach {
                str = "$str -- ${it.score} -- ${it.positionNum} /////"
                skipPersonArray[it.positionNum]?.count = it.score
            }
            resultLD.postValue(result)
        }
    }

    /**
     * 没有人了，处理一下状态复位
     */
    @Synchronized
    private fun handleNoPersonState() {
        when (skipRoseState) {
            //IDLE状态或者正在识别状态，清掉卡片状态，并复位状态到IDLE
            MultiSportState.IDENTIFYING -> {
                for (index in currentPersonState.indices) {
                    if (currentPersonState[index] != GroupSportsBaseChannelStatus.none) {
                        changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY)
                    }
                }
                LogUtil.d("处理无人时的状态复位")
                resetIdentifyNumber()
                skipRoseState = MultiSportState.PREPARING
            }

            //准备中，即将开始，不处理离开
            MultiSportState.PREPARING -> {

            }

            //运动中，最后一个人离开需要更新卡片到离开状态,可直接更新所有位置的卡片为离开状态
            MultiSportState.SPORTING -> {
                for (index in 0 until skipManCount) {
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
    private fun handlePersonState(skipRopeManList: MutableList<GroupSportBaseMan>) {

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
                        if (skipRoseState == MultiSportState.SPORTING && roseMultiResult != null) {
                            changeMultiIdentityCardState(
                                skipMan.positionNum,
                                MultiSportCardViewState.LEAVE
                            )
                            currentCardState[skipMan.positionNum] = MultiSportCardViewState.LEAVE
                        }
                        if (skipRoseState != MultiSportState.SPORTING && roseMultiResult == null) {
                            changeMultiIdentityCardState(
                                skipMan.positionNum,
                                MultiSportCardViewState.NOBODY
                            )
                        }
                    }
                }

                GroupSportsBaseChannelStatus.standBy -> {
                    if (getSportModel() == SportModel.NORMAL && skipRoseState != MultiSportState.SPORTING) {
                        changeMultiIdentityCardState(
                            skipMan.positionNum,
                            MultiSportCardViewState.STANDBY
                        )
                    }
                    //进入区域
                    if (currentState == GroupSportsBaseChannelStatus.none && skipRoseState == MultiSportState.SPORTING && roseMultiResult != null) {
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
                        if (skipRoseState != MultiSportState.SPORTING && skipRoseState != MultiSportState.COUNTDOWN && skipRoseState != MultiSportState.IDLE) {
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
                    if (currentState == GroupSportsBaseChannelStatus.none && skipRoseState == MultiSportState.SPORTING && roseMultiResult != null) {
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
    private fun handleIdentify(skipRopeManList: MutableList<GroupSportBaseMan>) {
        //目前只有常规模式才需要识别人脸，运动会模式不需要
        if (getSportModel() != SportModel.NORMAL) return

        //如果为识别状态
        if (skipRoseState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val skipRopeMan =
                skipRopeManList.find { skipRopeMan -> skipRopeMan.positionNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (skipRopeMan == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                changeMultiIdentityCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFY_FAIL
                )
                currentIdentifyNumber = -1
                skipRoseState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = bsSkipRoseMultiProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
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
        if (multiIdentityEngine.isIdentifying()) {
            LogUtil.d("处理人脸识别：人已离开位置，但正在识别人脸; 道数 $currentIdentifyNumber")
            return
        }

        skipRopeManList.forEach {
            if (it.channelStatus == GroupSportsBaseChannelStatus.checkDown && skipPersonArray.get(
                    it.positionNum,
                    null
                ) == null
            ) {
                currentIdentifyNumber = it.positionNum
                multiIdentityEngine.reset()
                skipRoseState = MultiSportState.IDENTIFYING
                val currentYuvData = bsSkipRoseMultiProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
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
     * 准备开始赛，321倒计时
     */
    fun readyGo() {
        if (factory.isSupportPrescription()) {
            config.checkExercisePrescription = true
            config.fileDir = getVideoPath(sportInfo?.sportSkuId).parentFile
        }
        //停止倒计时音效
        stopPlayCutDown()
        startCountDownTimer.cancel()
        countDownFinishLD.postValue(true)
        //开始3-2-1倒计时就表示比赛已经开始
        skipRoseState = MultiSportState.COUNTDOWN
        multiIdentityEngine.closeMachine()
        //处理开始后摄像头分辨率
        handleStartCameraResolution()
    }

    /**
     * 处理开始后的摄像头分辨率
     */
    private fun handleStartCameraResolution() {
        viewModelScope.launch(Dispatchers.IO) {
            /********************切换分辨率********************/
            ipcHik.pauseProcessor()
            setResolution(ipcHik, getSportResolution())
            SystemClock.sleep(100)
            stopPreviewLD.postValue(true)
            SystemClock.sleep(100)
            ipcHik.restartProcessor()
            startPreviewLD.postValue(Pair(ipcHik, cameraDispatcher))
            SystemClock.sleep(200)
            /********************切换分辨率********************/
            multiSportProcessor.clearTransform()
        }
    }

    private fun changeMultiIdentityCardState(index: Int, state: MultiSportCardViewState) {
        uiHandler.post {
            LogUtil.d("changeMultiIdentityCardState: index $index state ${state.name}")
            changeSportViewStateLD.value = Pair(index, state)
        }
    }

    private fun identified(index: Int, accountInfo: AccountInfo) {
        uiHandler.post {
            identifiedLD.value = Pair(index, accountInfo)
        }
    }

    /**
     * 开始录像,在此回调中处理录像，解决切换分辨率，录像还是录的4K的问题
     */
    fun onCameraOpen() {
        multiSportProcessor.clearTransform()
        if (skipRoseState == MultiSportState.SPORTING || skipRoseState == MultiSportState.COUNTDOWN) {
            LogUtil.d("${skipManCount}人运动，开始录像")
            startRecord()
        }
    }

    override fun onResume() {
        super.onResume()
        ipcHik.login(object : AdlCameraListener {
            override fun onOpenSucceed() {

            }

            override fun onLoginSucceed() {
                setResolution(ipcHik, getIdentifyResolution(), getInitCameraFrameRate())
                SystemClock.sleep(100)
                cameraLoginStateLD.postValue(true)
                startPreviewLD.postValue(Pair(ipcHik, cameraDispatcher))
            }

            override fun onLoginFail() {
                cameraLoginStateLD.postValue(false)
            }

            override fun onOpenStatus(p0: String?) {

            }

            override fun onError(p0: Exception?) {
                cameraLoginStateLD.postValue(false)
            }

            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
        timeOutHelper.start()
    }

    override fun toStatusSportStart() {
        super.toStatusSportStart()
        skipRoseState = MultiSportState.SPORTING
        startTime = System.currentTimeMillis()
        sportCountDownTime = sportInfo?.endValue?.toLong()?.div(1000) ?: 60
        sportingCountDownTimer = object : CountDownTimer(sportCountDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sportCountDownTime--
                //每隔10秒，且剩余时间大于等于5秒，播放音效,millisUntilFinished > 0.5过滤掉开始时间也能整除10的情况
                if (sportCountDownTime % 10 == 0L && sportCountDownTime >= 5) {
                    SoundPlayUtils.play(25 + soundIndex++ % soundCount)
                }
                if (sportCountDownTime == 10L) {
                    changeTimeTextBgLD.postValue(R.drawable.pk_skip_rose_time_warning_shape)
                }

                sportCountDownLD.postValue(simpleFormat.format(Date((sportCountDownTime) * 1000)))
            }

            override fun onFinish() {
                //比赛结束
                sportCountDownLD.postValue("0:00")
                sportFinish()
            }

        }
        sportingCountDownTimer?.start()
        multiSportParser.checkSkipScore = true
        //比赛开始设置卡片状态为运动状态
        for (index in 0 until skipPersonArray.size) {
            val multiResult = skipPersonArray.valueAt(index)
            currentCardState[multiResult.number] = MultiSportCardViewState.SPORTING
        }
    }

    private fun startRecord() {
        getVideoPath(sportInfo!!.sportSkuId)
        createTime = System.currentTimeMillis()
        if (videoRecorder.isStarting) {
            LogUtil.d("${skipManCount}人运动,录像已开始")
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
    fun toDone() {
        toStatusSportEnd()
        //java.lang.NullPointerException: Attempt to invoke virtual method 'void com.adl.mount.core.TsTaskNormalCenter.reset()' on a null object reference
        //	at com.adl.sport.general.multi.MultiSportImplTs.reset(MultiSportImplTs.java:117)
        //	at com.adl.sport.general.multi.MultiSportProcessor.changeTo(MultiSportProcessor.java:250)
        //	at com.ty.bus.multi.core.base.BaseIPCMultiSportViewModel.toDone(BaseIPCMultiSportViewModel.kt:846)
        //有崩溃，补货异常
        try {
            skipRoseState = MultiSportState.IDLE
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
        handleResult(skipPersonArray)

    }

    /**
     * 处理成绩，处理名次，成绩结果页显示，成绩上传
     */
    private fun handleResult(skipPersonArray: SparseArray<MultiSportResult>) {
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
        toDoneLD.postValue(resultList)
        handleUploadResult(resultList)
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
                var sportResult = SportResult()
                sportResult.sportDuration = sportInfo!!.endValue.toInt()
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
                        ptPlanId = this@BaseIPCMultiSportViewModel.ptPlanId
                        ptStandardId = this@BaseIPCMultiSportViewModel.ptStandardId
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

    override fun onDestroy() {
        super.onDestroy()
        sportingCountDownTimer?.cancel()
        startCountDownTimer.cancel()
        toDone()
        bsSkipRoseMultiProcessor.onRelease()
        skipRoseState = MultiSportState.IDLE
        multiIdentityEngine.closeMachine()
        cameraDispatcher.quit()
        uiHandler.removeCallbacksAndMessages(null)
        focalHandler.removeCallbacksAndMessages(null)
        videoRecorder.onStop()
        videoRecorder.onRelease()
        asyncPlayer.stop()
        config.unloadModel()
        multiSportProcessor.onRelease()
    }

    open fun getDefaultCountDownTime(): Long = 10L

    open fun getCountDownBgmUri() =
        Uri.parse("android.resource://" + getContext()?.packageName + "/" + R.raw.rope_catdown)

    open fun getBodyFilter(): TsTaskBodyFilter = RegionFilter()

    open fun getIdentifyResolution(): Byte = 64
    open fun getSportResolution(): Byte = 27

    open fun getCameraSize(): Pair<Int, Int> = 1920 to 1080

    //获取运动模式
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

    fun getIndicatorsResultDto(number: Int): IndicatorsResultDto? =
        indicatorsMap.getOrDefault(number, null)

    override fun reset() {
        super.reset()
        //重置，重新开始运动流程
        //清除所有数据
        indicatorsMap.clear()
        currentPersonState.clear()
        currentCardState.clear()
        for (index in 1..skipManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
        }
        skipPersonArray.clear()

        //重置人脸识别
        initFaceIdentify()

        //切换摄像头分辨率
        initCamera()
        multiSportProcessor.onReset()
        openTsProcess()
        onResume()
//        viewModelScope.launch(Dispatchers.IO) {
//            /********************切换分辨率********************/
//            ipcHik.pauseProcessor()
//            setResolution(ipcHik, getIdentifyResolution())
//            SystemClock.sleep(100)
//            stopPreviewLD.postValue(true)
//            SystemClock.sleep(100)
//            changeResolution()
//            ipcHik.restartProcessor()
//            startPreviewLD.postValue(Pair(ipcHik, cameraDispatcher))
//            SystemClock.sleep(200)
//            /********************切换分辨率********************/
//            multiSportProcessor.clearTransform()
//            multiSportProcessor.onReset()
//            openTsProcess()
//        }

    }

    open fun isSupportReport(): Boolean {
        return true
    }
}