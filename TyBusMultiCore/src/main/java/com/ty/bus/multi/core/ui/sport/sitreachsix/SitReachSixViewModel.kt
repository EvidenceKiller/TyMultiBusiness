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
import com.ty.bus.multi.core.base.BaseMultiPipelineIdentifyViewModel
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

class SitReachSixViewModel(application: Application) :
    BaseMultiPipelineIdentifyViewModel(application) {

    companion object {
        private const val TAG = "SitReachSix"

        /** loose≥该值(cm) 且与 max 相差过大时，改用 distanceMax，避免脏峰值 */
        private const val LOOSE_HIGH_CM = 57f
        private const val MAX_LOOSE_DIFF_CM = 15f
    }

    var ffSportConfig: ForwardFlexionConfig? = null

    private val previewSize = 2560 to 1440

    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private var statusMap = mutableMapOf<Int, Boolean>()

    private val param by lazy { MultiCoreConfig.getForwardFlexionParam() }

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

    val identifyTimeOut = 10 * 1000L //识别超时时间3秒，最后一个识别完，此时间内无新识别，则开始运动

    val sportTimeOut = 10 //运动超时提示时间

    var sportCountDownTimer: CountDownTimer? = null

    var sportCountDown = 0

    //已识别人员
    val identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()

    val sportStartLD = MutableLiveData<Int>() //开始运动

    val testOnceFinishLD = MutableLiveData<Boolean>()//成绩OK

    val sportTipsInfoLD = MutableLiveData<String>() //运动提示信息

    //成绩
    val sportResultLd = MutableLiveData<Pair<Int, MultiSportResult>>()

    //运动卡片的状态
    var sportCardStateLD = MutableLiveData<Pair<Int, ForwardFlexionCardState>>()

    private var currentIdentifyNumber = -1 //记录当前正在识别的位置

    private var isRetest = false //是否为重测

    /** 本轮收尾进行中，防止 handleResult 与超时 finish 重复结束 */
    @Volatile
    private var currentRoundFinishing = false

    /**
     * 成绩写回世代：下一组/重测时递增。
     * 用于丢弃上一轮已排队到 Main 的迟到 addScore，避免新一轮识别后刷出旧「最佳」。
     */
    @Volatile
    private var scoreEpoch = 0

    /** 本轮开测后该道是否出现过非 END，用于屏蔽算法残留 END */
    private val laneScoreArmed = BooleanArray(7) { false }

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
        LogUtil.d("$TAG::identifyOk: lane=$index, account=${accountInfo.studentEntity?.accountName}")
        //正在运动，不处理识别
        if (sportState != MultiSportState.IDENTIFYING) {
            LogUtil.d("识别到人脸：但已不是识别状态 $index")
            // 业务未接纳：清引擎该道 isOk，并放开独占道次
            multiIdentityEngine.clearIdentified(index)
            currentIdentifyNumber = -1
            return
        }

        if (accountInfo.studentEntity == null) {
            LogUtil.d("识别到人员为空")
            multiIdentityEngine.clearIdentified(index)
            currentIdentifyNumber = -1
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
            calcNextNumber(index + 1)
            if (!checkResult) {
                // 计划拒游客等：清引擎 isOk，放开该道后续 fail/再识别
                LogUtil.d("$TAG::identifyOk: checkStudent reject lane=$index, " + "name=${accountInfo.studentEntity!!.accountName}, clearIdentified")
                multiIdentityEngine.clearIdentified(index)
                currentIdentifyNumber = -1
                ToastUtil.getInstance(getApplication(), getString(R.string.busi_base_name_without_plan, accountInfo.studentEntity!!.accountName)).show()
                return@launch
            }
            //存已识别的人信息
            sportPersonArray.put(
                index,
                MultiSportResult(accountInfo, index, -1000)
            )
            identifiedLD.postValue(Pair(index, accountInfo))
            checkMap[index] = true
            changeSportCardState(index, ForwardFlexionCardState.IDENTIFIED)

            LogUtil.d(
                "$TAG::identifyOk: name=${accountInfo.studentEntity!!.accountName}, " +
                    "lane=$index, identified=${sportPersonArray.size}/$sportManCount"
            )
            // 满员直接开测；未满员则等「立即开始」倒计时/点击（不再用首人空闲超时抢跑）
            if (sportPersonArray.size == sportManCount) {
                LogUtil.d("$TAG::identifyOk: full, toStatusPrepare")
                toStatusPrepare()
            }

        }

    }

    override fun identifyFail(index: Int) {
        LogUtil.d("$TAG::identifyFail: lane=$index")
        changeSportCardState(index, ForwardFlexionCardState.IDENTIFY_FAIL)
        //处理下一道是否有人
        calcNextNumber(index + 1)
    }

    private fun calcNextNumber(index: Int) {
        LogUtil.d("当前识别失败道次：$index")
        var findOk = false
        var tempNumber = index % sportManCount
        while (tempNumber < sportManCount) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (statusMap[tempNumber] == true && sportPersonArray.get(
                    tempNumber,
                    null
                ) == null
            ) {
                currentIdentifyNumber = tempNumber
                changeSportCardState(
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
            LogUtil.d(
                "$TAG::handlePrepareStatus: start sport, round=$currentSportTimes/$sportTimes, " +
                    "people=${sportPersonArray.size}, isRetest=$isRetest, epoch=$scoreEpoch"
            )
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
            // 开测时解除残留 END 门闩，须先见到非 END 才接受成绩
            for (i in laneScoreArmed.indices) {
                laneScoreArmed[i] = false
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
            // 已识别道次不再被不可用/空闲覆盖，避免下一组铺板重建时冲掉识别回显
            if (sportPersonArray.get(it.indexNum) != null) return@forEach
            when {
                it.isCompliant.not() -> {
                    LogUtil.d("$TAG::updateStatusMap: lane=${it.indexNum} UNAVAILABLE")
                    changeSportCardState(it.indexNum, ForwardFlexionCardState.UNAVAILABLE)
                }

                it.skeleton != null && checkHandUp(it.skeleton) -> {
                    statusMap.put(it.indexNum, true)
                }

                it.skeleton == null -> changeSportCardState(
                    it.indexNum,
                    ForwardFlexionCardState.IDLE
                )
//                else -> changeSportCardState(it.indexNum, ForwardFlexionCardState.IDLE)
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
                identify(
                    getIdentityRect(flexionBody),
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

        data.forEach {
            if (sportPersonArray.get(it.indexNum, null) == null
                && it.skeleton != null
                && checkHandUp(it.skeleton)
            ) {
                currentIdentifyNumber = it.indexNum
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(it),
                    currentYuvData.yuv,
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
        val lWrist = skeleton?.lWrist
        val lShoulder = skeleton?.lShoulder
        val rWrist = skeleton?.rWrist
        val rShoulder = skeleton?.rShoulder
        if (lWrist == null || lShoulder == null || rWrist == null || rShoulder == null) return false
        return lWrist.y < lShoulder.y || rWrist.y < rShoulder.y
//        return true
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
        val epoch = scoreEpoch
        viewModelScope.launch(Dispatchers.IO) {
            // 本协程是否已在 Main 发出过成绩；未发出则禁止齐人收尾，避免抢在 UI 前把状态打成 IDLE
            var emittedUi = false
            result.forEach {
                val lane = it.indexNum
                if (lane !in 1..6) return@forEach
                if (it.state != SitAndReachObject.State.END) {
                    // 开测后先见到非 END，才允许本道出分（挡住上一轮残留 END）
                    laneScoreArmed[lane] = true
                    return@forEach
                }
                val person = sportPersonArray.get(lane)
                if (person == null) return@forEach
                if (!laneScoreArmed[lane]) {
                    LogUtil.d("$TAG::handleResult: drop residual END lane=$lane, epoch=$epoch")
                    return@forEach
                }
                val distanceMax = it.distanceMax
                val distanceLoose = it.distanceMaxLoose
                LogUtil.d("$TAG::handleResult: prefer distanceMax lane=$lane, loose=$distanceLoose, max=$distanceMax")
                val useDistance = if (distanceLoose >= LOOSE_HIGH_CM && abs(distanceLoose - distanceMax) >= MAX_LOOSE_DIFF_CM) {
                    distanceMax
                } else {
                    distanceLoose
                }
                // 同步占坑：并发双帧 END 时同一道只出一次分
                val accepted = synchronized(resultStatusList) {
                    if (resultStatusList[lane - 1]) {
                        false
                    } else {
                        person.count = max((useDistance * 10 - 200).toInt(), -200)
                        // -200（展示 -20cm）为有效伸距地板值，按未完成处理，与播报/上传注释对齐
                        person.status = if (person.count == -200) 2 else calcViolation(it)
                        resultStatusList[lane - 1] = true
                        true
                    }
                }
                if (!accepted) {
                    LogUtil.d("$TAG::handleResult: drop duplicate END lane=$lane")
                    return@forEach
                }
                LogUtil.d("$TAG::handleResult: lane=$lane END count=${person.count} status=${person.status} " +
                        "epoch=$epoch max=$distanceMax loose=$distanceLoose use=$useDistance")
                withContext(Dispatchers.Main) {
                    // 仅丢弃跨组（epoch 变化）的迟到写回；本轮占坑后必须先发出 UI，再允许 finish
                    if (epoch != scoreEpoch) {
                        LogUtil.d(
                            "$TAG::handleResult: drop stale score lane=$lane, " +
                                "epoch=$epoch/$scoreEpoch"
                        )
                        return@withContext
                    }
                    sportResultLd.value = lane to person
                    emittedUi = true
                }
            }
            if (epoch != scoreEpoch) return@launch
            val list = resultStatusList.filter { it }
            val checkSize = checkMap.filter { it.value }.size
            //还有人未出成绩
            if (list.size != checkSize) {
                LogUtil.d("$TAG::handleResult: waiting scores done=${list.size}/$checkSize")
                return@launch
            }
            // 齐人收尾只允许「本协程已发出 sportResultLd」的路径触发，避免旁路协程抢先 IDLE
            if (!emittedUi) {
                LogUtil.d("$TAG::handleResult: all ready but skip finish, waiting emitter UI")
                return@launch
            }
            //冗余数据，超过了测试次数
            if (currentSportTimes > sportTimes) {
                LogUtil.d("$TAG::handleResult: skip redundant, times=$currentSportTimes/$sportTimes")
                return@launch
            }
            LogUtil.d("$TAG::handleResult: all scores ready, finishCurrentRound")
            finishCurrentRound()
        }
    }

    private fun calcViolation(srj: SitAndReachObject): Int {
        if (srj.direction == SitAndReachObject.Direction.UNKNOWN) {
            return 2
        }
        LogUtil.d(
            "====================${srj.failMsgList.getOrNull(0)}--${
                srj.failMsgList.getOrNull(
                    1
                )
            }--${srj.failMsgList.getOrNull(2)}"
        )
        return if ((param.checkFoot && srj.failMsgList.getOrNull(2) == false)
            || (param.checkHand && srj.failMsgList.getOrNull(0) == false)
            || (param.checkLeg && srj.failMsgList.getOrNull(1) == false)
            || (param.checkInertia && srj.failMsgList.getOrNull(3) == true)
        ) 1 else 0
    }

    private fun startSportCountDown() {
        LogUtil.d("$TAG::startSportCountDown: timeoutSec=$sportTimeOut")
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
        LogUtil.d("$TAG::finishWithTimeout: mark unfinished lanes")
        synchronized(resultStatusList) {
            for (i in resultStatusList.indices) {
                val index = i + 1
                val person = sportPersonArray.get(index)
                if (person != null) {
                    if (resultStatusList[i].not()) {
                        //未完成
                        LogUtil.d("$TAG::finishWithTimeout: lane=${person.number} unfinished")
                        person.status = 2
                        resultStatusList[i] = true
                        sportResultLd.value = index to person
                    }
                }
            }
        }
        // 超时后无新结果帧时也必须收尾，否则顶部倒计时不消、无再测/下一组
        finishCurrentRound()
    }

    /**
     * 本轮全部出分（含超时补未完成）后的统一收尾，与 handleResult / finishWithTimeout 共用。
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

        stopSportCountDown()
        sportTipsInfoLD.postValue("")
        LogUtil.d(
            "$TAG::finishCurrentRound: round done, beforeInc times=$currentSportTimes/$sportTimes"
        )
        currentSportTimes++
        sportPersonArray.forEach { index, _ ->
            changeSportCardState(index, ForwardFlexionCardState.SPORTING)
        }
        testOnceFinishLD.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            if (currentSportTimes > sportTimes) {
                LogUtil.d("$TAG::finishCurrentRound: all rounds done -> sportEnd")
                SpeechWrapper.speech(
                    getString(R.string.end_test, currentSportTimes - 1)
                )
                sportState = MultiSportState.IDLE
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
                    currentRoundFinishing = false
                    toStatusPrepare()
                }
            }
        }
    }

    private fun stopSportCountDown() {
        sportCountDownTimer?.cancel()
    }

    override fun toStatusPrepare() {
        // 满员已开测时，「立即开始」倒计时结束可能再次回调，避免打回 COUNTDOWN
        if (sportState == MultiSportState.COUNTDOWN || sportState == MultiSportState.SPORTING) {
            LogUtil.d("$TAG::toStatusPrepare: skip, already state=$sportState")
            return
        }
        super.toStatusPrepare()
        LogUtil.d(
            "$TAG::toStatusPrepare: people=${sportPersonArray.size}, round=$currentSportTimes/$sportTimes"
        )
        mHandler.removeCallbacksAndMessages(null)
        currentRoundFinishing = false
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
            if (value.status == 2) return@forEach
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
        LogUtil.d("$TAG::toStatusSportEnd")
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

    /**
     * 重测，只测一次
     */
    fun retest() {
        LogUtil.d("$TAG::retest: people=${sportPersonArray.size}")
        isRetest = true
        scoreEpoch++
        for (i in laneScoreArmed.indices) {
            laneScoreArmed[i] = false
        }
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
        LogUtil.d("$TAG::reset: clear people=${sportPersonArray.size}, state=$sportState")
        super.reset()
        stopSportCountDown()
        mHandler.removeCallbacksAndMessages(null)
        currentRoundFinishing = false
        scoreEpoch++
        for (i in laneScoreArmed.indices) {
            laneScoreArmed[i] = false
        }
        isRetest = false
        prepareStatusList.clear()
        resultStatusList.clear()
        for (i in 1..sportManCount) {
            checkMap.put(i, false)
            prepareStatusList.add(false)
            resultStatusList.add(false)
        }
        multiIdentityEngine.reset()
        sportPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        videoFile = null
        LogUtil.d("$TAG::reset: scoreEpoch=$scoreEpoch")
    }

}
