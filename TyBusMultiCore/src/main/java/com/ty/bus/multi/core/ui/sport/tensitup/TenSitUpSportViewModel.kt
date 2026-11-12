package com.ty.bus.multi.core.ui.sport.tensitup

import android.app.Application
import android.graphics.Rect
import androidx.core.util.forEach
import com.adl.mount.sport.TsSportChannel
import com.adl.processor.SportProcessorStatus
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.adl.sport.general.groupsportbase.GroupSportsBaseChannelStatus
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.adl.sport.general.multi.MultiSportProcessor
import com.adl.sport.general.situp.SitUpBodyFilter
import com.adl.service.entity.SportStartRecordEntity.SportStartAccountDetail
import com.ty.bus.multi.core.base.BaseCollabSportViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max

/**
 * 十人仰卧起坐：协同流程复用 [BaseCollabSportViewModel]（单屏退化，DisplayType 恒为 MAIN），
 * 这里保留仰卧起坐特有的识别框、人脸阈值、开始记录上传、重测与配置。
 */
class TenSitUpSportViewModel(application: Application) :
    BaseCollabSportViewModel(application) {

    var tsConfig: SixSportConfig? = null

    //十人页为纯单屏运动：不支持协同，不向客户端发送任何Grpc消息，协同层自动退化
    override fun isSupportCollaboration(): Boolean = false

    //是否正在播报违规语音
    private var isSpeeching = false

    //十人页保持原行为：全程2K@15fps，不做分辨率切换
    override fun getInitCameraFrameRate(): Byte = 15

    override fun getIdentifyResolution(): Byte = 70

    override fun getSportResolution(): Byte = 70

    /***************************流程钩子***************************/

    override fun initProcess() {
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
        frameProcessor.addFrameProcessor(multiSportProcessor)
    }

    override fun openTsProcess() {
        multiSportParser.checkChannelStatus = true
        sportState = MultiSportState.PREPARING
        multiSportProcessor.changeTo(SportProcessorStatus.Prepare)
    }

    //十人页保持2K不切换，覆写为空
    override fun onCountDownStarted() {
    }

    //主屏手动开始（点击「立即开始」）：直接开赛，不校验倒计时是否结束
    override fun readyGo() {
        mainCountDownEnded = true
        super.readyGo()
    }

    override fun onSportStarted() {
        //处理器切到运动态
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
    }

    override fun onSportStartedCards() {
        //比赛开始设置卡片状态为运动状态
        for (index in 0 until manCount) {
            val multiResult = skipPersonArray.get(index, null)
            if (multiResult != null) {
                //必须通知UI层切换卡片到运动状态，仅记录状态不会刷新界面
                changeMultiIdentityCardState(index, MultiSportCardViewState.SPORTING)
            } else {
                changeMultiIdentityCardState(index, MultiSportCardViewState.NOBODY_SPORT)
            }
        }
    }

    /**
     * 十人页保持原行为：运动开始时上传运动开始记录
     */
    override fun onSportStartRecordUpload() {
        val sportDetail = mutableListOf<SportStartAccountDetail>()
        skipPersonArray.forEach { _, multiResult ->
            currentCardState[multiResult.number] = MultiSportCardViewState.SPORTING
            sportDetail.add(SportStartAccountDetail().apply {
                this.accountId = multiResult.accountInfo.studentEntity?.accountId
                this.sportStartTime = startSportTime
            })
        }
        uploadSportStartRecord(sportDetail)
    }

    override fun identifiedCardState(): MultiSportCardViewState {
        //识别成功后同步已识别道次给解析器（原页面在卡片切IDENTIFIED时刷新checkStatusList）
        val checkStatusList = MutableList(manCount) { false }
        skipPersonArray.forEach { _, value ->
            if (value.number in 0 until manCount) {
                checkStatusList[value.number] = true
            }
        }
        multiSportParser.setCheckStatusList(checkStatusList)
        return MultiSportCardViewState.IDENTIFIED
    }

    override fun onFirstIdentified() {
        //重新复位倒计时时间
        countDownTime = getDefaultCountDownTime()
    }

    //仰卧起坐以完成次数作为成绩分数上传
    override fun uploadPerfectScore(count: Int): Int = count

    override fun onSportResultReady(resultList: MutableList<MultiSportResult>) {
        //切换卡片到结束状态
        resultList.forEach { result ->
            changeMultiIdentityCardState(result.number, MultiSportCardViewState.FINISH)
        }
    }

    //面板顶部提示展示开始提示文案
    fun getSportStartTip(): String = factory.getStartTip()

    /**
     * 页面恢复时重置处理器（保持原页面 onResume 行为）
     */
    fun resetProcessor() {
        multiSportProcessor.onReset()
    }

    /**
     * 重测：保留已识别人员与道次，成绩清零后重新开赛
     */
    fun retest() {
        val saved = skipPersonArray.clone()
        reset()
        skipPersonArray.clear()
        saved.forEach { _, result ->
            result.count = 0
            skipPersonArray.put(result.number, result)
            changeMultiIdentityCardState(result.number, MultiSportCardViewState.PREPARED)
        }
        videoFile = null
        readyGo()
    }

    /***************************仰卧起坐识别***************************/

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

    /**
     * 记录每个位置的状态，并通知UI层更新卡片状态
     * 只处理卡片状态，此函数回调，一定有人在框里
     */
    @Synchronized
    override fun handlePersonState(sitUpManList: MutableList<GroupSportBaseMan>) {
        //非运动状态下记录每个位置的人员进入状态
        for (sitUpMan in sitUpManList) {
            val nextState = sitUpMan.channelStatus
            currentPersonState[sitUpMan.positionNum] = nextState
            LogUtil.d("处理位置状态：道数 ${sitUpMan.positionNum} 状态 $nextState")
            //处理进入状态
            when (nextState) {
                //躺下就绪：运动开始后（仰卧起坐每次躺下都会再次触发）不再回退到"已准备"
                GroupSportsBaseChannelStatus.layDown -> {
                    if (sportState == MultiSportState.PREPARING || sportState == MultiSportState.IDENTIFYING || sportState == MultiSportState.COUNTDOWN) {
                        changeMultiIdentityCardState(
                            sitUpMan.positionNum,
                            MultiSportCardViewState.PREPARED
                        )
                    }
                }

                else -> {}
            }
        }
    }

    @Synchronized
    override fun handleIdentify(sitUpManList: MutableList<GroupSportBaseMan>) {
        //如果为识别状态
        if (sportState == MultiSportState.IDENTIFYING && currentIdentifyNumber != -1) {
            LogUtil.d("处理人脸识别：当前正在识别人脸 道数 $currentIdentifyNumber")
            val sitUpMan =
                sitUpManList.find { sitUpMan -> sitUpMan.positionNum == currentIdentifyNumber }
            //没找到人，说明，人已离开位置，则重置状态
            if (sitUpMan == null) {
                LogUtil.d("处理人脸识别：人已离开位置，重置状态; 道数 $currentIdentifyNumber")
                currentIdentifyNumber = -1
                sportState = MultiSportState.PREPARING
            } else {
                //送数据进行人脸识别
                LogUtil.d("处理人脸识别：送数据进行人脸识别： 道数 $currentIdentifyNumber")
                val currentYuvData = frameProcessor.getCurrentYuvData()
                identify(
                    getIdentityRect(
                        sitUpMan,
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

        sitUpManList.forEach {
            if (it.channelStatus == GroupSportsBaseChannelStatus.checkDown
                && skipPersonArray.get(it.positionNum, null) == null
                && checkHandUp(it)
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
     * 仰卧起坐识别条件：举手（手腕高于肩部）
     */
    private fun checkHandUp(it: GroupSportBaseMan): Boolean {
        if (it.skeleton == null || it.skeleton.lShoulder == null || it.skeleton.lWrist == null)
            return false
        return it.skeleton.lWrist.y < it.skeleton.lShoulder.y
    }

    /**
     * 仰卧起坐人脸识别区域：优先用头部关键点推测，无头部关键点时用双肩推测。
     * （基类钩子为4参数签名，仰卧起坐的定位逻辑只依赖骨骼关键点，其余参数忽略）
     */
    override fun getIdentityRect(
        it: TsSportChannel?,
        width: Int,
        height: Int,
        currentIdentifyNumber: Int
    ): Rect {
        var rect = Rect(0, 0, 50, 50)
        if (it?.body == null || it.skeleton == null) {
            LogUtil.d("it.bodyrect  is Null null null null ------", true)
            return rect
        }
        LogUtil.d("处理人脸识别：用骨骼")
        val rectWidth = 180
        val header = it.skeleton.headTop
        if (header != null) {
            rect.top = header.y.toInt()
            rect.left = header.x.toInt() - rectWidth / 2
            rect.right = header.x.toInt() + rectWidth / 2
            rect.bottom = header.y.toInt() + rectWidth
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
}
