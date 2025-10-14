package com.ty.bus.multi.core.ui.sport.forwardfelxion

import android.app.Application
import android.graphics.Rect
import android.os.SystemClock
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.MutableLiveData
import com.adl.mount.sport.TsSportChannel
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
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.MultiIdentityEngine
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ForwardFlexionViewModel(application: Application) : BaseMultiSportViewModel(application) {
    val onceCacheProcessor by lazy { OnceCacheProcessor() } //人脸识别缓存

    private val multiIdentityEngine by lazy { MultiIdentityEngine() } //人脸识别引擎类

    private var statusMap = mutableMapOf<Int, InformCode>()

    val checkMap = HashMap<Int, Boolean>() //记录当前道是否已检录

    private var sportState: MultiSportState = MultiSportState.IDLE //记录当前比赛的状态

    val sportPersonArray by lazy { SparseArray<MultiSportResult>() } //存储已识别的位置和人员信息

    var sportManCount = 0 //测试人数

    var sportTimes = 0 //测试次数

    var currentSportTimes = 1 //当前测试次数

    //已识别人员
    val identifiedLD = MutableLiveData<Pair<Int, AccountInfo>>()

    val sportStartLD = MutableLiveData<Int>() //开始运动

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
        for (i in 0 until diskNum) {
            checkMap.put(i, false)
        }
        sportManCount = diskNum
        initFaceIdentify()
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
        handlePostureStatus(map)
    }

    /**
     * 处理姿态状态
     */
    private fun handlePostureStatus(map: MutableMap<Int, InformCode>) {
        var prepareCount = 0
        map.forEach {
            if (it.value == InformCode.PREPAREFINSIH) {
                prepareCount++
                LogUtil.d(it.key.toString() + "号准备完成")
            } else if (it.value == InformCode.PREPAREERR) {
                LogUtil.d(it.key.toString() + "号准备异常")
            } else if (it.value == InformCode.NOTCHECKIN) {
                LogUtil.d(it.key.toString() + "号未检录")
            }
        }
        //全部准备完成，开始运动
        if (prepareCount == sportManCount && sportState != MultiSportState.SPORTING) {
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

    private fun getIdentityRect(it: TsSportChannel?): Rect {

        if (it?.body == null || it.skeleton == null) {
            return Rect(0, 0, 50, 50)
        }
        var rect = Rect(it.body.x, it.body.y, it.body.right(), it.body.bottom())
        if (it.skeleton != null && it.skeleton.LShoulder != null && it.skeleton.RShoulder != null) {
            val shoulderWidth = abs(it.skeleton.LShoulder.x - it.skeleton.RShoulder.x)
            rect.bottom = max(it.skeleton.LShoulder.y, it.skeleton.RShoulder.y).toInt()
            rect.left = min(it.skeleton.LShoulder.x, it.skeleton.RShoulder.x).toInt()
            rect.right = rect.left + shoulderWidth.toInt()
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

    fun getIdentityRect(it: MutableList<Point>?): Rect {
        var rect = Rect(0, 0, 50, 50)
        if (it.isNullOrEmpty() || it.size < 4) return rect
        val header = it[0]
        val lShouder = it[2]
        val rShouder = it[3]
        val shoulderWidth = abs(lShouder.x - rShouder.x) * 1.5
        rect.bottom = max(lShouder.y, rShouder.y).toInt()
        rect.left = min(lShouder.x, rShouder.x).toInt()
        rect.right = rect.left + shoulderWidth.toInt()
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
        for (entry in result.grade.entries) {
            val person = sportPersonArray.get(entry.key)
            if (person == null) continue
            val name = person.accountInfo.studentEntity?.accountName ?: ""
            person.count = max(person.count, entry.value)
            SpeechWrapper.speech("${name}，${getResultSpeech(entry.value)}")
        }
        currentSportTimes++
        if (currentSportTimes > sportTimes) {
            //测试次数超标，运动结束
            toStatusSportEnd()
        } else {
            SystemClock.sleep(1000)
            toStatusPrepare()
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
    }

    override fun reset() {
        super.reset()
        multiIdentityEngine.clearCountMap()
        sportPersonArray.clear()
        currentSportTimes = 1
        sportState = MultiSportState.IDLE
    }

}