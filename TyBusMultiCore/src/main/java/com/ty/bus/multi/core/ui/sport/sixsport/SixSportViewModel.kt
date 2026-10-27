package com.ty.bus.multi.core.ui.sport.sixsport

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Rect
import android.os.CountDownTimer
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.sport.TsSportChannel
import com.adl.service.entity.SportRecordEntity
import com.adl.service.entity.SportStartRecordEntity.SportStartAccountDetail
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.multi.MultiSportProcessor
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.config.ThreeSportConfig
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.FaceIdentifyConfig
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.expand.checkHandUp
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIdentityEngine
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.SoundPlayUtils
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.forEach
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SixSportViewModel(application: Application) : BaseMultiSportViewModel(application) {
    //记录开始和结束时间
    var tsConfig: SixSportConfig? = null

    var startTime: Long = 0
    var endTime: Long = 0
    private var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态
    private val currentPersonState =
        mutableListOf<GroupSportsBaseChannelStatus>() //记录每个位置进入的时间，用来处理是否可以人脸识别
    private val currentCardState = mutableListOf<MultiSportCardViewState>()
    private var currentIdentifyNumber = -1 //记录当前正在识别的位置
    var sportManCount = 6 //运动人数
    val sportPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息
    private val multiIdentityEngine by lazy { MultiIdentityEngine() } //人脸识别引擎类
    val simpleFormat = SimpleDateFormat("m:ss")

    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

    //视频创建时间
    private var createTime: Long = 0

    //每个位置的已准备状态；只要准备了，就不能再切换为未准备，除非去其他位置识别
    val layDownStatusList = mutableListOf<Boolean>()

    //成绩结果
    val resultLD = MutableLiveData<MutableList<GroupSportBaseMan>>() //成绩结果LD

    //运动卡片状态
    var changeSportViewStateLD = MutableLiveData<Pair<Int, MultiSportCardViewState>>()

    //识别卡片状态
    var changeIdentityCardStateLD = MutableLiveData<Pair<Int, MultiSportCardViewState>>()

    //已识别人员
    var identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()

    //开始倒计时
    var countDownFinishLD = MutableLiveData<SparseArray<MultiSportResult>>()

    //改变时间显示UI的背景色LD
    val changeTimeTextBgLD = MutableLiveData<Int>() //改变时间显示UI的背景色LD

    val readySpeechLD = MutableLiveData<Boolean>()

    var toDoneLD = MutableLiveData<MutableList<MultiSportResult>>() //比赛结束LD

    var sportCountDownTime = 60L // 比赛时间 S
    val sportCountDownLD = MutableLiveData<String>()//更新倒计时的LD

    //运动倒计时任务
    private var sportingCountDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        initData()
    }

    private fun initData() {
        for (index in 1..sportManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
            layDownStatusList.add(false)
        }
        sportState = MultiSportState.PREPARING
        initFaceIdentify()
        timeOutHelper.start()
    }


    private fun initFaceIdentify() {
        /**
         * 人脸识别回调
         */
        var visitor: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.VISITOR_ABLE,
                BusiBaseConstantsValue.VISITOR_OPEN
            ) && matchId == null
        multiIdentityEngine.visitorAble = visitor
        FaceDetectorHelper.getInstance().setFrameInterval(2)
        multiIdentityEngine.openMachine(object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityTimeOn() {

            }

            override fun onIdentityOk(identifyResult: AccountInfo) {
                handleIdentityOk(identifyResult)
            }

            override fun onIdentityTimeFail() {
                handleIdentityTimeFail()
            }
        })
        multiIdentityEngine.addIdentityConfig(0, FaceIdentifyConfig().apply {
            visitorRetryNum = 2
            minScoreThreshold = 0.7f
        })
        multiIdentityEngine.addIdentityConfig(1, FaceIdentifyConfig().apply {
            visitorRetryNum = 2
            minScoreThreshold = 0.7f
        })
        multiIdentityEngine.addIdentityConfig(2, FaceIdentifyConfig().apply {
            visitorRetryNum = 2
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(3, FaceIdentifyConfig().apply {
            visitorRetryNum = 2
            minScoreThreshold = 0.6f
        })
        multiIdentityEngine.addIdentityConfig(4, FaceIdentifyConfig().apply {
            visitorRetryNum = 3
        })
        multiIdentityEngine.addIdentityConfig(5, FaceIdentifyConfig().apply {
            visitorRetryNum = 3
        })

    }

    @Synchronized
    private fun handleIdentityTimeFail() {
        changeMultiSportViewCardState(currentIdentifyNumber, MultiSportCardViewState.IDLE)
        //处理下一道是否有人
        var tempNumber = currentIdentifyNumber + 1
        var findOk = false
        while (tempNumber < sportManCount) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (currentPersonState[tempNumber] == GroupSportsBaseChannelStatus.checkDown && sportPersonArray.get(
                    tempNumber,
                    null
                ) == null
            ) {
                currentIdentifyNumber = tempNumber
                changeMultiSportViewCardState(
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
            currentIdentifyNumber = -1
        }

    }

    @Synchronized
    private fun handleIdentityOk(accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $currentIdentifyNumber")
            return
        }
        SpeechWrapper.speech(
            getString(
                R.string.identified_someone,
                accountInfo.studentEntity!!.accountName
            )
        )

        //取消超时任务
        timeOutHelper.cancel()
        viewModelScope.launch(Dispatchers.Main) {
            sportState = MultiSportState.PREPARING
            //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
            if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                var rmKey = -1
                sportPersonArray.forEach { key, value ->
                    //已识别过此人
                    if (value.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity!!.accountId) {
                        LogUtil.d("已识别过${value.accountInfo.studentEntity!!.accountName}")
                        rmKey = key
                        layDownStatusList[value.number] = false
                        changeMultiSportViewCardState(
                            value.number,
                            MultiSportCardViewState.IDLE
                        )
                    }
                }
                if (rmKey != -1) {
                    sportPersonArray.remove(rmKey)
                }
            }
            //如果是组织测试，需判断该人是否在组织计划中
            val checkResult = withContext(Dispatchers.IO) {
                checkStudent(accountInfo.studentEntity!!.accountId, ptPlanId)
            }
            if (checkResult) {
                //存已识别的人信息
                sportPersonArray.put(
                    currentIdentifyNumber,
                    MultiSportResult(accountInfo, currentIdentifyNumber)
                )
                identifiedLD.postValue(Pair(currentIdentifyNumber, accountInfo))
                changeMultiSportViewCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFIED
                )
                LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $currentIdentifyNumber")
            } else {
                ToastUtil.getInstance(
                    getApplication(),
                    getString(
                        R.string.busi_base_name_without_plan,
                        accountInfo.studentEntity!!.accountName
                    )
                )
                    .show()
            }
            //重置识别道次
            currentIdentifyNumber = -1
        }

    }

    @Synchronized
    fun handleProcessorCallback(groupSkippingManList: MutableList<GroupSportBaseMan>?) {
        //处理卡片和人的状态
        if (groupSkippingManList.isNullOrEmpty()) {
            return
        }



        when (sportState) {

            MultiSportState.PREPARING, MultiSportState.IDENTIFYING -> {
                handlePersonState(groupSkippingManList)
                handleIdentify(groupSkippingManList)
            }

            MultiSportState.COUNTDOWN -> {

            }

            MultiSportState.SPORTING -> {
                handleResult(groupSkippingManList)
            }

            MultiSportState.IDLE -> {

            }

            MultiSportState.WAITING_START -> {}
        }
    }

    private fun handleNoPersonState() {
        when (sportState) {
            //IDLE状态或者正在识别状态，清掉卡片状态，并复位状态到IDLE
            MultiSportState.IDLE, MultiSportState.IDENTIFYING -> {
                for (index in currentPersonState.indices) {
                    if (currentPersonState[index] != GroupSportsBaseChannelStatus.none) {
                        changeMultiSportViewCardState(index, MultiSportCardViewState.NOBODY)
                    }
                }
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            }

            //准备中，即将开始，不处理离开
            MultiSportState.PREPARING -> {

            }

            //运动中，最后一个人离开需要更新卡片到离开状态,可直接更新所有位置的卡片为离开状态
            MultiSportState.SPORTING -> {
                for (index in 0 until sportManCount) {
                    val roseMultiResult = sportPersonArray.get(index, null)
                    if (roseMultiResult != null) {
                        changeMultiSportViewCardState(index, MultiSportCardViewState.LEAVE)
                    }
                    currentPersonState[index] = GroupSportsBaseChannelStatus.none
                }
            }

            else -> {}
        }

    }

    private fun handleIdentify(skipRopeManList: MutableList<GroupSportBaseMan>) {
        //如果为识别状态
        if (sportState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val skipRopeMan =
                skipRopeManList.find { skipRopeMan -> skipRopeMan.positionNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (skipRopeMan == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
                    getIdentityRect(skipRopeMan),
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
            if (it.channelStatus == GroupSportsBaseChannelStatus.checkDown
                && sportPersonArray.get(it.positionNum, null) == null
                && it.checkHandUp()
            ) {
                currentIdentifyNumber = it.positionNum
                multiIdentityEngine.reset()
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
                    getIdentityRect(it),
                    currentYuvData.yuv,
                    currentYuvData.width,
                    currentYuvData.height,
                    currentIdentifyNumber
                )
                changeMultiSportViewCardState(
                    currentIdentifyNumber,
                    MultiSportCardViewState.IDENTIFYING
                )
                LogUtil.d("处理人脸识别：人位置状态OK，开始识别该位置，道数 $currentIdentifyNumber")
                return
            }
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
            val roseMultiResult = sportPersonArray.get(skipMan.positionNum, null)
            val nextState = skipMan.channelStatus
            currentPersonState[skipMan.positionNum] = nextState
            LogUtil.d("处理位置状态：道数 ${skipMan.positionNum} 状态 $currentState -> ${nextState.name}")
            //处理进入时间
            when (nextState) {
//                GroupSportsBaseChannelStatus.none -> {
//                    //如果当前为standby,下一个状态为 none
//                    if (currentState == GroupSportsBaseChannelStatus.standBy) {
//                        //当前比赛状态为运动中... 切换人员卡片到离开状态
//                        if (sportState == MultiSportState.SPORTING && roseMultiResult != null) {
//                            changeMultiSportViewCardState(
//                                skipMan.positionNum,
//                                MultiSportCardViewState.LEAVE
//                            )
//                            currentCardState[skipMan.positionNum] = MultiSportCardViewState.LEAVE
//                        }
//                        if (sportState != MultiSportState.SPORTING && sportState != MultiSportState.COUNTDOWN && roseMultiResult == null) {
//                            changeMultiSportViewCardState(
//                                skipMan.positionNum,
//                                MultiSportCardViewState.NOBODY
//                            )
//                        }
//                    }
//                }
//
//                GroupSportsBaseChannelStatus.standBy -> {
//                    if (sportState != MultiSportState.SPORTING) {
//                        changeMultiSportViewCardState(
//                            skipMan.positionNum,
//                            MultiSportCardViewState.STANDBY
//                        )
//                    }
//                    //进入区域
//                    if (currentState == GroupSportsBaseChannelStatus.none && sportState == MultiSportState.SPORTING && roseMultiResult != null) {
//                        currentCardState[skipMan.positionNum] = MultiSportCardViewState.SPORTING
//                        changeMultiSportViewCardState(
//                            skipMan.positionNum,
//                            MultiSportCardViewState.LEAVE_TO_SPORTING
//                        )
//                    }
//                }
//
//                GroupSportsBaseChannelStatus.checkDown -> {
//                    //进入区域
//                    if (currentState == GroupSportsBaseChannelStatus.none && sportState == MultiSportState.SPORTING && roseMultiResult != null) {
//                        currentCardState[skipMan.positionNum] = MultiSportCardViewState.SPORTING
//                        changeMultiSportViewCardState(
//                            skipMan.positionNum,
//                            MultiSportCardViewState.LEAVE_TO_SPORTING
//                        )
//                    }
//                }

                GroupSportsBaseChannelStatus.layDown -> {
                    changeMultiSportViewCardState(
                        skipMan.positionNum,
                        MultiSportCardViewState.PREPARED
                    )
                    val status = layDownStatusList[skipMan.positionNum]
                    if (!status) {
                        layDownStatusList[skipMan.positionNum] = true
                        uiHandler.removeCallbacks(checkLayDownRunnable)
                        val layDownList =
                            layDownStatusList.filter { it }
                        if (layDownList.size == sportManCount) {
                            //全部OK，可开始比赛
                            readyGo()
                        } else {
                            //如果准备好的人数，不等于总运动人数，则等三秒检测已准备的是否等于已识别的
                            uiHandler.postDelayed(checkLayDownRunnable, 10_000)
                        }
                    }
                }

                else -> {}
            }

        }
    }

    private val checkLayDownRunnable = Runnable {
        val layDownList = layDownStatusList.filter { it }
        LogUtil.d("checkLayDownRunnable  ${layDownList.size}")
        if (layDownList.size == sportPersonArray.size()) {
            //全部OK，可开始比赛
            readyGo()
        } else {
            readySpeechLD.value = true
        }
    }

    /**
     * 处理成绩，通知UI层更新成绩UI
     */
    private fun handleResult(result: MutableList<GroupSportBaseMan>) {
        //运动中财更新成绩，否则结束成绩回调会是0
        if (sportState == MultiSportState.SPORTING) {
            var str = ""
            result.forEach {
                str = "$str -- ${it.score} -- ${it.positionNum} /////"
                sportPersonArray[it.positionNum]?.count = it.score
            }

            resultLD.postValue(result)
        }
    }

    private fun getIdentityRect(it: TsSportChannel?): Rect {
        var rect = Rect(0, 0, 50, 50)
        if (it?.body == null || it.skeleton == null) {
            LogUtil.d("it.bodyrect  is Null null null null ------", true)
            return rect
        }
        //无头框，用骨骼推测
        if (it.body == null) return rect
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

    private fun changeMultiSportViewCardState(index: Int, state: MultiSportCardViewState) {
        uiHandler.post {
            changeSportViewStateLD.value = Pair(index, state)
        }
    }

    fun readyGo() {
        sportState = MultiSportState.COUNTDOWN
        countDownFinishLD.postValue(sportPersonArray)
    }

    /**
     * 开始录像
     */
    private fun startRecord(cameraDispatcher: DynamicSportFrameDispatcher) {
        getVideoPath(sportInfo!!.sportSkuId)
        createTime = System.currentTimeMillis()
        if (videoRecorder.isStarting) {
            LogUtil.d("${sportManCount}人运动,录像已开始")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val size = 2560 to 1440
            videoRecorder.onStart(videoFile, size.first, size.second)
            cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
                override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
                    videoRecorder.inputNv21(p0)
                }
            })
        }
    }

    private fun stopRecord() {
        videoRecorder.onStop()
    }

    fun onCameraOpen(
        processor: MultiSportProcessor,
        cameraDispatcher: DynamicSportFrameDispatcher
    ) {
        processor.clearTransform()
        if (sportState == MultiSportState.SPORTING || sportState == MultiSportState.COUNTDOWN) {
            LogUtil.d("${sportManCount}人运动，开始录像")
            startRecord(cameraDispatcher)
        }
    }

    /**
     * 运动开始
     */
    override fun toStatusSportStart() {
        super.toStatusSportStart()
        sportCountDownTime = sportInfo?.endValue?.toLong()?.div(1000) ?: 60
        sportingCountDownTimer = object : CountDownTimer(sportCountDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sportCountDownTime--
                if (sportCountDownTime == 10L) {
                    changeTimeTextBgLD.postValue(R.drawable.pk_skip_rose_time_warning_shape)
                }
                sportCountDownLD.postValue(simpleFormat.format(Date((sportCountDownTime) * 1000)))
            }

            override fun onFinish() {
                //比赛结束
                sportCountDownLD.postValue("0:00")
                toStatusSportEnd()
            }

        }
        sportingCountDownTimer?.start()
        sportState = MultiSportState.SPORTING
        startTime = System.currentTimeMillis()
        //比赛开始设置卡片状态为运动状态
        val sportDetail = mutableListOf<SportStartAccountDetail>()
        for (index in 0 until sportManCount) {
            val multiResult = sportPersonArray.get(index)
            if (multiResult != null) {
                changeMultiSportViewCardState(multiResult.number, MultiSportCardViewState.SPORTING)
                sportDetail.add(SportStartAccountDetail().apply {
                    this.accountId = multiResult.accountInfo.studentEntity?.accountId
                    this.sportStartTime = startSportTime
                })
            } else {
                changeMultiSportViewCardState(index, MultiSportCardViewState.NOBODY_SPORT)
            }
//            currentCardState[multiResult.number] = MultiSportCardViewState.SPORTING
        }
        uploadSportStartRecord(sportDetail)

    }

    @SuppressLint("ImplicitSamInstance")
    override fun toStatusSportEnd() {
        endTime = System.currentTimeMillis()
        sportState = MultiSportState.IDLE
        SoundPlayUtils.play(24)
        stopRecord()
        handleUploadResult(sportPersonArray)
        //切换卡片到结束状态
        for (index in 0 until sportManCount) {
            val multiResult = sportPersonArray.get(index)
            if (multiResult != null) {
                changeMultiSportViewCardState(multiResult.number, MultiSportCardViewState.FINISH)
            }
        }
        super.toStatusSportEnd()
    }


    private fun handleUploadResult(skipPersonArray: SparseArray<MultiSportResult>) {
        if (sportInfo == null || skipPersonArray.size() == 0) {
            return
        }
        val skipList = mutableListOf<MultiSportResult>()
        skipPersonArray.forEach { _, value ->
            skipList.add(value)
        }
        skipList.sortWith(compareBy { -it.count })

        viewModelScope.launch(Dispatchers.IO) {
            val multiId = System.currentTimeMillis().toString()
            var preRank = 0
            var preScore = -1
            for (index in skipList.indices) {
                var multiResult = skipList[index]
                var sportResult = SportResult()
                sportResult.sportDuration = sportInfo!!.endValue.toInt()
                sportResult.maxComboCount = 0
                sportResult.sportAchievement = multiResult.count
                sportResult.perfectCount = 0
                sportResult.goodCount = 0
                sportResult.comeOnCount = 0
                sportResult.perfectScore = multiResult.count
                sportResult.goodScore = 0
                sportResult.comeOnScore = 0
                sportResult.sportNo = (multiResult.number + 1).toString()
                sportResult.userId = multiResult.accountInfo.studentEntity?.accountId
                sportResult.startTime = startTime
                sportResult.endTime = endTime
                //处理并列问题
                if (multiResult.count == preScore) {
                    sportResult.rank = preRank
                } else {
                    sportResult.rank = preRank + 1
                }
                preRank = sportResult.rank
                preScore = multiResult.count
                sportResult.multiPersonId = multiId
                sportResult.sportResult = multiResult.count.toString()
                sportResult.videoCover = videoCover
                sportResult.videoFile = videoFile
                var recordPlan: SportRecordEntity.RecordPlan? = null
                if (sceneId == SceneConstants.ORGANIZE_TEST) {
                    recordPlan = SportRecordEntity.RecordPlan().apply {
                        ptPlanId = this@SixSportViewModel.ptPlanId
                        ptStandardId = this@SixSportViewModel.ptStandardId
                    }
                }
                uploadSportResult(
                    sceneId = sceneId ?: SceneConstants.PK,
                    sportResult,
                    sportResult.userId,
                    recordPlan = recordPlan
                )
            }
        }

        toDoneLD.postValue(skipList)
    }

    override fun reset() {
        super.reset()
        multiIdentityEngine.clearCountMap()
        currentPersonState.clear()
        currentCardState.clear()
        sportPersonArray.clear()
        layDownStatusList.clear()
        for (index in 1..sportManCount) {
            currentPersonState.add(GroupSportsBaseChannelStatus.none)
            currentCardState.add(MultiSportCardViewState.NOBODY)
            layDownStatusList.add(false)
        }

        sportState = MultiSportState.PREPARING
    }

    fun retest() {
        super.reset()
        sportPersonArray.forEach { index, result ->
            changeMultiSportViewCardState(index, MultiSportCardViewState.PREPARED)
            result.count = 0
            changeTimeTextBgLD.postValue(R.drawable.pk_skip_rose_time_shape)
        }
        layDownStatusList.clear()
        readyGo()
        videoFile = null
    }

    override fun onDestroy() {
        super.onDestroy()
        sportEngine.stopRecorder(null)
        multiIdentityEngine.closeMachine()
        sportingCountDownTimer?.cancel()
        videoRecorder.onRelease()
        uiHandler.removeCallbacksAndMessages(null)
    }
}