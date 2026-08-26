package com.ty.bus.multi.core.ui.sport.sitreachsix

import android.app.Application
import android.graphics.Rect
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlYUVUtil
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.service.entity.SportRecordEntity
import com.adl.sport.sitandreach.SitAndReachObject
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewModel
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.forwardflexion.informCode.InformCode
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.FaceIdentifyConfig
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIdentityEngine
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

class SitReachSixViewModel(application: Application) : BaseMultiSportViewModel(application) {

    var ffSportConfig: ForwardFlexionConfig? = null

    private val previewSize = 2560 to 1440

    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

    private val multiIdentityEngine by lazy { MultiIdentityEngine() } //人脸识别引擎类

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private var statusMap = mutableMapOf<Int, InformCode>()

    val checkMap = HashMap<Int, Boolean>() //记录当前道是否已检录

    //已准备的道次，所有已准备好，既开始
    val prepareStatusList = mutableListOf<Boolean>()

    //已出成绩的道次
    val resultStatusList = mutableListOf<Boolean>()

    private var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    val sportPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息

    var sportManCount = 0 //测试人数

    var sportTimes = 0 //测试次数

    var currentSportTimes = 1 //当前测试次数

    val identifyTimeOut = 5 * 1000L //识别超时时间3秒，最后一个识别完，此时间内无新识别，则开始运动

    val sportTimeOut = 10 //运动超时提示时间

    var sportCountDownTimer: CountDownTimer? = null

    var sportCountDown = 0

    //已识别人员
    val identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()

    val sportStartLD = MutableLiveData<Int>() //开始运动

    val testOnceFinishLD = MutableLiveData<Boolean>()//成绩OK

    val sportTipsInfoLD = MutableLiveData<String>() //运动提示信息

    //成绩
    val sportResultLd = MutableLiveData<Pair<Int, Int>>()

    //运动卡片的状态
    var sportCardStateLD = MutableLiveData<Pair<Int, ForwardFlexionCardState>>()

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var isRetest = false //是否为重测

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
            checkMap.put(i, false)
            prepareStatusList.add(false)
            resultStatusList.add(false)
        }
        sportManCount = diskNum
        initFaceIdentify()
        sportState = MultiSportState.IDENTIFYING
    }

    private fun initFaceIdentify() {
        var visitor: Boolean =
            MMKVUtil.decodeBoolean(MMKVBusiKey.VISITOR_ABLE, true) && matchId == null
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
    private fun handleIdentityOk(accountInfo: AccountInfo) {
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $currentIdentifyNumber")
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
        mHandler.removeCallbacksAndMessages(null)
        viewModelScope.launch {
            sportState = MultiSportState.PREPARING
            //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
            if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                var rmKey = -1
                sportPersonArray.forEach { key, value ->
                    //已识别过此人
                    if (value.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity!!.accountId) {
                        LogUtil.d("已识别过${value.accountInfo.studentEntity!!.accountName}")
                        rmKey = key
                        changeSportCardState(
                            value.number,
                            ForwardFlexionCardState.IDLE
                        )
                        checkMap[value.number] = false
                    }
                }
                if (rmKey != -1) {
                    sportPersonArray.remove(rmKey)
                }
            }

            val checkResult = withContext(Dispatchers.IO) {
                checkStudent(accountInfo.studentEntity!!.accountId, ptPlanId)
            }
            if (!checkResult) {
                //重置识别道次
                currentIdentifyNumber = -1
                ToastUtil.getInstance(
                    getApplication(),
                    getString(
                        R.string.busi_base_name_without_plan,
                        accountInfo.studentEntity!!.accountName
                    )
                )
                    .show()
                return@launch
            }
            //存已识别的人信息
            sportPersonArray.put(
                currentIdentifyNumber,
                MultiSportResult(accountInfo, currentIdentifyNumber, -1000)
            )
            identifiedLD.postValue(Pair(currentIdentifyNumber, accountInfo))
            checkMap[currentIdentifyNumber] = true
            changeSportCardState(currentIdentifyNumber, ForwardFlexionCardState.IDENTIFIED)

            LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $currentIdentifyNumber")
            //重置识别道次
            currentIdentifyNumber = -1
            //识别完成
            if (sportPersonArray.size == sportManCount) {
                LogUtil.d("识别到全部人数。。。。。。。。。。。。。。。。。。")
                toStatusPrepare()
            } else {
                mHandler.postDelayed({
                    LogUtil.d("识别超时。。。。。。。。。。。。。。。。。。。")
                    toStatusPrepare()
                }, identifyTimeOut)
            }

        }

    }

    @Synchronized
    private fun handleIdentityTimeFail() {
        changeSportCardState(currentIdentifyNumber, ForwardFlexionCardState.IDLE)
        //处理下一道是否有人
        var tempNumber = currentIdentifyNumber + 1
        var findOk = false
//        while (tempNumber < sportManCount) {
//            //找到下一道，状态为checkDown且还没有识别的位置
//            if (statusMap[tempNumber] == InformCode.PREPARECHECKINFINSIH && sportPersonArray.get(
//                    tempNumber,
//                    null
//                ) == null
//            ) {
//                currentIdentifyNumber = tempNumber
//                changeSportCardState(
//                    currentIdentifyNumber,
//                    ForwardFlexionCardState.IDENTIFYING
//                )
//                multiIdentityEngine.reset()
//                findOk = true
//                break
//            }
//            tempNumber++
//        }
        //没有找到下一道，置为-1，再从头开始识别
        if (!findOk) {
            currentIdentifyNumber = -1
        }

    }

    private fun changeSportCardState(index: Int, state: ForwardFlexionCardState) {
        uiHandler.post {
            sportCardStateLD.value = Pair(index, state)
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
     * 处理姿态状态
     */
    @Synchronized
    private fun handlePrepareStatus(map: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.COUNTDOWN) return
        var prepareCount = 0
//        map.forEach {
//            if (it.footPosition) {
//                changeSportCardState(it.indexNum, ForwardFlexionCardState.PREPARED)
//                prepareCount++
//                prepareStatusList[it.indexNum] = true
//            }
//        }
        //全部准备完成，开始运动
//        val prepareList = prepareStatusList.filter { it }
        //没有人准备，则返回
//        if (prepareList.isEmpty()) return
//        val checkPerson = checkMap.filter { it.value }
//        LogUtil.d("准备完成人数：${prepareList.size} 开始运动 ${sportState.name}")
//        if (prepareList.size == checkPerson.size && sportState != MultiSportState.SPORTING) {
        if (sportState != MultiSportState.SPORTING) {
            if (currentSportTimes == 1) {
                startSportTime = System.currentTimeMillis()
            }
            if (sportTimes > 1) {
                SpeechWrapper.speakAfterStop(
                    getString(
                        R.string.forward_flexion_times_start_speech,
                        currentSportTimes
                    )
                )
            } else {
                SpeechWrapper.speakAfterStop(getString(R.string.forward_flexion_start_speech))
            }
            sportStartLD.postValue(currentSportTimes)
            startSportCountDown()
            //切换卡片状态，方便重置
            //第一次测试进入第一次测试状态
            if (currentSportTimes == 1 && isRetest.not()) {
                for (index in 1..6) {
                    val value = sportPersonArray.get(index)
                    if (value != null) {
                        changeSportCardState(index, ForwardFlexionCardState.FIRST_SPORTING)
                    } else {
                        changeSportCardState(index, ForwardFlexionCardState.SPORTING_NO_USER)
                    }
                }
            } else {
                sportPersonArray.forEach { index, value ->
                    changeSportCardState(index, ForwardFlexionCardState.SPORTING)
                }
            }

            sportState = MultiSportState.SPORTING
//            toStatusSportStart()
        }
    }

    fun onHandleIdentity(data: MutableList<SitAndReachObject>?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (data != null) {
                handleIdentify(data)
            }
        }
    }

    @Synchronized
    private fun handleIdentify(data: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.IDENTIFYING && sportState != MultiSportState.PREPARING) {
            //不是识别状态
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
                val data = if (streamType == 1) {
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
                multiIdentityEngine.identify(
                    getIdentityRect(flexionBody),
                    data,
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

        data.forEach {
            if (sportPersonArray.get(it.indexNum, null) == null
                && it.skeleton != null
                && checkHandUp(it.skeleton)
            ) {
                currentIdentifyNumber = it.indexNum
                multiIdentityEngine.reset()
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                val data = if (streamType == 1) {
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
                multiIdentityEngine.identify(
                    getIdentityRect(it),
                    data,
                    currentYuvData.width,
                    currentYuvData.height,
                    currentIdentifyNumber
                )
                changeSportCardState(
                    currentIdentifyNumber,
                    ForwardFlexionCardState.IDENTIFYING
                )
                LogUtil.d("处理人脸识别：人位置状态OK，开始识别该位置，道数 $currentIdentifyNumber")
                return
            }
        }
    }

    private fun checkHandUp(skeleton: AdlPoseSkeleton?): Boolean {
//        if (it.isNullOrEmpty() || it.size < 8) return false
//        val header = it[0]
//        val lShouder = it[2]
//        val rShouder = it[3]
//        val lWrist = it[6]
//        val rWrist = it[7]
//        return lWrist.y < header.y
        return true
    }

    fun getIdentityRect(it: SitAndReachObject?): Rect {
        var rect = Rect(0, 0, 50, 50)
        val skeleton = it?.skeleton
        if (it == null || skeleton == null) return rect
        //用头框
//        if (it.headBox != null) {
//            LogUtil.d("处理人脸识别：用头框")
//            return Rect(
//                it.headBox.x,
//                it.headBox.y,
//                it.headBox.right.toInt(),
//                it.headBox.bottom.toInt()
//            )
//        } else {
        //无头框，用骨骼推测
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
//        }

    }

    fun handleResult(result: MutableList<SitAndReachObject>) {
        if (sportState != MultiSportState.SPORTING) return
        viewModelScope.launch(Dispatchers.IO) {
            result.forEach {
                val person = sportPersonArray.get(it.indexNum)
                if (person == null) return@forEach
                if (it.state == SitAndReachObject.State.END && resultStatusList[it.indexNum - 1].not()) {
                    person.count = max((it.distanceMaxLoose * 10 - 200).toInt(), -200)
                    resultStatusList[it.indexNum - 1] = true
                    withContext(Dispatchers.Main) {
                        sportResultLd.value = it.indexNum to person.count
                    }
                }
            }
            val list = resultStatusList.filter { it }
            val checkSize = checkMap.filter { it.value }.size
            //还有人未出成绩
            if (list.size != checkSize) {
                LogUtil.d("总是被人数：${checkSize} -- 已出成绩:${list.size}")
                return@launch
            }
            //冗余数据，超过了测试次数
            if (currentSportTimes > sportTimes) {
                return@launch
            }
            stopSportCountDown()
            LogUtil.d("测试结束：总测试次数:${sportTimes}, 已测试次数:${currentSportTimes}")
            currentSportTimes++
            sportPersonArray.forEach { index, value ->
                changeSportCardState(index, ForwardFlexionCardState.SPORTING)
            }
            withContext(Dispatchers.Main) {
                testOnceFinishLD.postValue(true)
            }
            if (currentSportTimes > sportTimes) {
                //测试次数超标，运动结束,等2秒结束，方便查看最后一次成绩
                SpeechWrapper.speech(
                    getString(
                        R.string.end_test,
                        currentSportTimes - 1
                    )
                )
                sportState = MultiSportState.IDLE
                toStatusSportEnd()
            } else {
                SpeechWrapper.speech(
                    getString(
                        R.string.forward_flexion_sport_times_end,
                        currentSportTimes - 1
                    )
                )
                SystemClock.sleep(2000)
                sportState = MultiSportState.PREPARING
                toStatusPrepare()
            }
        }
    }

    private fun startSportCountDown() {
        LogUtil.d("开始倒计时  开始倒计时  开始倒计时")
        Handler(Looper.getMainLooper()).post {
            sportCountDownTimer?.cancel()
            sportCountDown = sportTimeOut
            sportCountDownTimer = object : CountDownTimer(sportTimeOut * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    sportCountDown--
                    sportTipsInfoLD.postValue(
                        getString(
                            R.string.sport_count_down,
                            sportCountDown
                        )
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
            val person = sportPersonArray.get(index)
            if (person != null) {
                if (resultStatusList[i].not()) {
                    //未完成
                    LogUtil.d("${person.number}道未完成=============")
                    sportResultLd.value = index to -200
                }
                resultStatusList[i] = true
            }
        }
    }

    private fun stopSportCountDown() {
        sportCountDownTimer?.cancel()
    }

    override fun toStatusPrepare() {
        super.toStatusPrepare()
        mHandler.removeCallbacksAndMessages(null)
        sportState = MultiSportState.COUNTDOWN
        for (i in 0 until sportManCount) {
            prepareStatusList[i] = false
            resultStatusList[i] = false
        }
    }

    fun uploadResult() {
        endSportTime = System.currentTimeMillis()
        //延迟200ms，等待录制视屏结束
        SystemClock.sleep(100)
        sportPersonArray.forEach { key, value ->
            //如果是-200,表示未完成，不上传成绩
            if (value.count == -200) return@forEach
            var sportResult = SportResult()
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
            if (value.count > -200 && value.count <= 400) {
                sportResult.sportResult = value.count.toString()
            } else {
                sportResult.sportViolationStatus = 1
                sportResult.sportViolationNumber = "1"
            }
            sportResult.videoCover = videoCover
            sportResult.videoFile = videoFile
            var recordPlan: SportRecordEntity.RecordPlan? = null
            if (sceneId == SceneConstants.ORGANIZE_TEST) {
                recordPlan = SportRecordEntity.RecordPlan().apply {
                    ptPlanId = this@SitReachSixViewModel.ptPlanId
                    ptStandardId = this@SitReachSixViewModel.ptStandardId
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

    //结束处理成绩
    override fun toStatusSportEnd() {
        super.toStatusSportEnd()
        sportState = MultiSportState.IDLE
    }

    private fun getResultSpeech(result: Int): String {
//        成绩${entry.value / 10.0}厘米
        return when {
            result == -300 -> getString(R.string.violations)
            result > -200 && result <= 400 -> "${result / 10.0}厘米"
            else -> getString(R.string.un_do)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiIdentityEngine.closeMachine()
    }

    /**
     * 重测，只测一次
     */
    fun retest() {
        isRetest = true
        sportPersonArray.forEach { index, result ->
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
            checkMap.put(i, false)
            prepareStatusList.add(false)
            resultStatusList.add(false)
        }
        multiIdentityEngine.clearCountMap()
        sportPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        videoFile = null
    }

}
