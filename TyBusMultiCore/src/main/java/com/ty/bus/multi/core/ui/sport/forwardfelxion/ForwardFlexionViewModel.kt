package com.ty.bus.multi.core.ui.sport.forwardfelxion

import android.app.Application
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.MutableLiveData
import com.adl.sport.common.bean.Point
import com.adl.sport.forwardflexion.config.FFlexionSportResult
import com.adl.sport.forwardflexion.informCode.InformCode
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIdentityEngine
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import kotlin.math.abs
import kotlin.math.max

class ForwardFlexionViewModel(application: Application) : BaseMultiSportViewModel(application) {
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

    val identifyTimeOut = 3 * 1000L //识别超时时间3秒，最后一个识别完，此时间内无新识别，则开始运动

    //已识别人员
    val identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()

    val sportStartLD = MutableLiveData<Int>() //开始运动

    val resetLD = MutableLiveData<Boolean>()//成绩OK

    //识别卡片状态
    var changeIdentityCardStateLD = MutableLiveData<Pair<Int, MultiSportCardViewState>>()


    private var currentIdentifyNumber = -1 //记录当前正在识别的位置
    fun initData() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
        )

        sportTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_TIMES,
            MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
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
            MMKVUtil.decodeBoolean(MMKVBusiKey.MULTI_ROPE_SKIP_VISITOR, true) && matchId == null
        multiIdentityEngine.visitorAble = visitor
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
        uiHandler.post {
            sportState = MultiSportState.PREPARING
            //需要检测是否已经识别此人，如已识别，则清掉上次识别的位置,非游客
            if (accountInfo.studentEntity!!.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                var rmKey = -1
                sportPersonArray.forEach { key, value ->
                    //已识别过此人
                    if (value.accountInfo.studentEntity!!.accountId == accountInfo.studentEntity!!.accountId) {
                        LogUtil.d("已识别过${value.accountInfo.studentEntity!!.accountName}")
                        rmKey = key
                        changeMultiIdentityCardState(
                            value.number,
                            MultiSportCardViewState.IDENTIFY_CHANGE
                        )
                    }
                }
                if (rmKey != -1) {
                    sportPersonArray.remove(rmKey)
                }
            }
            //存已识别的人信息
            sportPersonArray.put(
                currentIdentifyNumber,
                MultiSportResult(accountInfo, currentIdentifyNumber)
            )
            identifiedLD.postValue(Pair(currentIdentifyNumber, accountInfo))
            checkMap[currentIdentifyNumber] = true
            changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFIED)

            LogUtil.d("人脸识别结果：姓名：${accountInfo.studentEntity!!.accountName}; 道数 $currentIdentifyNumber")
            //重置识别道次
            currentIdentifyNumber = -1
            //识别完成
            if (sportPersonArray.size == sportManCount) {
                toStatusPrepare()
            } else {
                mHandler.postDelayed({
                    toStatusPrepare()
                }, identifyTimeOut)
            }

        }

    }

    @Synchronized
    private fun handleIdentityTimeFail() {
        changeMultiIdentityCardState(currentIdentifyNumber, MultiSportCardViewState.IDENTIFY_FAIL)
        //处理下一道是否有人
        var tempNumber = currentIdentifyNumber + 1
        var findOk = false
        while (tempNumber < sportManCount) {
            //找到下一道，状态为checkDown且还没有识别的位置
            if (statusMap[tempNumber] == InformCode.PREPARECHECKINFINSIH && sportPersonArray.get(
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
            currentIdentifyNumber = -1
        }

    }

    private fun changeMultiIdentityCardState(index: Int, state: MultiSportCardViewState) {
        uiHandler.post {
            changeIdentityCardStateLD.value = Pair(index, state)
        }
    }

    fun onPostureStatus(map: MutableMap<Int, InformCode>) {
        statusMap = map
        handlePrepareStatus(map)
    }

    /**
     * 处理姿态状态
     */
    private fun handlePrepareStatus(map: MutableMap<Int, InformCode>) {
        if (sportState == MultiSportState.SPORTING) return
        var prepareCount = 0
        map.forEach {
            if (it.value == InformCode.PREPAREFINSIH) {
                changeMultiIdentityCardState(it.key, MultiSportCardViewState.PREPARED)
                prepareCount++
                prepareStatusList[it.key - 1] = true
                LogUtil.d(it.key.toString() + "号准备完成")
            } else if (it.value == InformCode.PREPAREERR) {
                LogUtil.d(it.key.toString() + "号准备异常")
                //准备异常，需要重新准备
                resetLD.postValue(true)
                SystemClock.sleep(100)
                toStatusPrepare()
            } else if (it.value == InformCode.NOTCHECKIN) {
                LogUtil.d(it.key.toString() + "号未检录")
            }
        }
        //全部准备完成，开始运动
        val prepareList = prepareStatusList.filter { it }
        //没有人准备，则返回
        if (prepareList.isEmpty()) return
        val checkPerson = checkMap.filter { it.value }

        if (prepareList.size == checkPerson.size && sportState != MultiSportState.SPORTING) {
            if (currentSportTimes == 1) {
                startSportTime = System.currentTimeMillis()
            }
            if (sportTimes > 1) {
                SpeechWrapper.speech(
                    getString(
                        R.string.forward_flexion_times_start_speech,
                        currentSportTimes
                    )
                )
            } else {
                SpeechWrapper.speech(getString(R.string.forward_flexion_start_speech))
            }
            sportStartLD.postValue(currentSportTimes)
            //切换卡片状态，方便重置
            sportPersonArray.forEach { key, value ->
                changeMultiIdentityCardState(key, MultiSportCardViewState.SPORTING)
            }
            sportState = MultiSportState.SPORTING
        }
    }

    fun onBonePoints(map: MutableMap<Int, MutableList<Point>>?) {
        if (map != null) {
            handleIdentify(map)
        }
    }

    @Synchronized
    private fun handleIdentify(map: MutableMap<Int, MutableList<Point>>) {
        if (sportState != MultiSportState.IDENTIFYING && sportState != MultiSportState.PREPARING) {
            //不是识别状态
            return
        }
        //如果为识别状态
        if (sportState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val skeletonPoint = map.getOrDefault(currentIdentifyNumber, null)
            //没找到人，说明，人已离开位置，则重置状态
            if (skeletonPoint == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
                    getIdentityRect(skeletonPoint),
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

        statusMap.forEach {
            if (it.value == InformCode.PREPARECHECKINFINSIH
                && sportPersonArray.get(it.key, null) == null
                && map.getOrDefault(it.key, null) != null
            ) {
                currentIdentifyNumber = it.key
                multiIdentityEngine.reset()
                sportState = MultiSportState.IDENTIFYING
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                multiIdentityEngine.identify(
                    getIdentityRect(map[it.key]),
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

    fun getIdentityRect(it: MutableList<Point>?): Rect {
        var rect = Rect(0, 0, 50, 50)
        if (it.isNullOrEmpty() || it.size < 4) return rect
        val header = it[0]
        val lShouder = it[2]
        val rShouder = it[3]
        val centerX: Double =
            if (header.x > 0f) header.x.toDouble() else (lShouder.x + rShouder.x) / 2.0
        val shoulderWidth = abs(lShouder.x - rShouder.x) * 1.5
        rect.bottom = max(lShouder.y, rShouder.y).toInt()
        rect.left = (centerX - shoulderWidth / 2.0).toInt()
        rect.right = (centerX + shoulderWidth / 2).toInt()
        rect.top =
            if (header.y != 0f) header.y.toInt() else (rect.bottom - 1.2 * shoulderWidth).toInt()

        if (rect.width() < 40 || rect.height() < 40) {
            rect.left = rect.left - 20
            rect.right = rect.left + 40
            rect.top = rect.top - 20
            rect.bottom = rect.top + 40
        }
        return rect
    }

    fun handleResult(result: FFlexionSportResult) {
        if (result.grade == null) {
            return
        }
        result.grade.forEach {
            val person = sportPersonArray.get(it.key)
            if (person == null) return@forEach
            val name = person.accountInfo.studentEntity?.accountName ?: ""
            person.count = max(person.count, it.value)
            SpeechWrapper.speech("${name}，${getResultSpeech(it.value)}")
            resultStatusList[it.key - 1] = true
        }
        val list = resultStatusList.filter { it }
        val checkSize = checkMap.filter { it.value }.size
        //还有人未出成绩
        if (list.size != checkSize) {
            return
        }
        sportState = MultiSportState.PREPARING
        currentSportTimes++
        if (currentSportTimes > sportTimes) {
            //测试次数超标，运动结束
            toStatusSportEnd()
        } else {
            resetLD.postValue(true)
            SystemClock.sleep(1000)
            toStatusPrepare()
        }

    }

    override fun toStatusPrepare() {
        super.toStatusPrepare()
        sportState = MultiSportState.COUNTDOWN
        for (i in 0 until sportManCount) {
            prepareStatusList[i] = false
            resultStatusList[i] = false
        }
    }

    //结束处理成绩
    override fun toStatusSportEnd() {
        super.toStatusSportEnd()
        sportState = MultiSportState.IDLE
        endSportTime = System.currentTimeMillis()
        //延迟200ms，等待录制视屏结束
        SystemClock.sleep(200)
        sportPersonArray.forEach { key, value ->
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
            sportResult.userId = value.accountInfo.studentEntity?.accountId
            sportResult.startTime = startSportTime
            sportResult.endTime = endSportTime
            if (value.count in -200..400) {
                sportResult.sportResult = value.count.toString()
            } else {
                sportResult.sportViolationStatus = 1
                sportResult.sportViolationNumber = "1"
            }
            sportResult.videoCover = videoCover
            sportResult.videoFile = videoFile
            uploadSportResult(
                sceneId ?: SceneConstants.INDEPENDENT_TEST,
                sportResult,
                sportResult.userId
            )

        }

    }

    private fun getResultSpeech(result: Int): String {
//        成绩${entry.value / 10.0}厘米
        return when {
            result == -300 -> getString(R.string.violations)
            result > -200 && result < 400 -> "${result / 10.0}厘米"
            else -> getString(R.string.violations)
        }
    }

    fun retest() {
        sportPersonArray.forEach { index, result ->
            result.count = 0
        }
        currentSportTimes = 1
        sportState = MultiSportState.PREPARING
        toStatusPrepare()
        videoFile = null
    }

    override fun reset() {
        super.reset()
        multiIdentityEngine.clearCountMap()
        sportPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.IDLE
        videoFile = null
    }

}