package com.ty.bus.multi.core.ui.sport.sitreacheighteen

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlExecutor
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.core.TsModelResPool
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.outer.AdlService
import com.adl.sport.ballBase.ResultCodeType
import com.adl.sport.controllers.sitandreach.SitAndReachCallBack
import com.adl.sport.controllers.sitandreach.SitAndReachController
import com.adl.sport.controllers.sitandreach.SitAndReachControllerConfig
import com.adl.sport.sitandreach.SitAndReachConfig
import com.adl.sport.sitandreach.SitAndReachObject
import com.adl.sport.sitandreach.SitAndReachType
import com.bumptech.glide.Glide
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.core.ui.sport.thirtysport.CollaborationAreaState
import com.ty.bus.multi.databinding.AcSitReachEighteenBinding
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 18人坐位体前屈：主屏 + 左副屏 + 右副屏，每屏6人，共18人。
 *
 * 按照三十人跳绳（ThirtySportActivity）扩展十人跳绳（TenSportActivity）的方式，
 * 在六人坐位体前屈（SitReachSixActivity）基础上扩展为三屏协同运动：
 * - 底部导航栏可在左区/中区/右区之间切换查看对应区域的道次卡片
 * - 副屏识别/卡片状态/成绩通过 gRPC 上报，主屏统一渲染
 * - 结果页合并三屏成绩统一排名展示（18人）
 */
class SitReachEighteenActivity :
    BaseMultiSportActivity<AcSitReachEighteenBinding, SitReachEighteenViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/com.adl.sport/testDebugVideo/body_bend_ai_six/坐位体前屈6人2k.mp4"

    /***************************视频播放调试*************************/

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var controller: SitAndReachController

    /** 每屏人数（6人），三屏共18人 */
    private val number = 6

    private var isModelOk = false //模型是否加载成功
    private var isInit = false //是否开始流程

    //板子几个点
    private var boardNum = 8

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(0f, 0f)
        })
    }

    private lateinit var ipcHik: NsCameraHKSource
    private var tipsHide = false //位置提示是否隐藏
    private var tipCountDownCount = 3  //提示的倒计时时间

    private var currentArea = DisplayType.MAIN
    private var leftIdentifiedCount = 0
    private var mainIdentifiedCount = 0
    private var rightIdentifiedCount = 0

    /** "是否清空已识别结果"对话框（协同识别计数提示，展示期间内容可动态更新） */
    private var clearIdentifyDialog: CommonTipDialog? = null

    /** 导航区协同异常状态字体颜色 */
    private val AREA_TAB_RED = "#F44134"

    private val tipRunnable: Runnable = object : Runnable {
        override fun run() {
            tipCountDownCount--
            if (tipCountDownCount == 0) {
                hideInTips()
                return
            }
            mBinding.btnInTipConfirm.text =
                getString(R.string.confirm_countdown, tipCountDownCount)
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun initData() {
        super.initData()
//        viewModel.streamType = 2
        timeOutHelper.cancel()
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.ffSportConfig =
            intent.getSerializableExtra("sport_config") as? ForwardFlexionConfig
        viewModel.initData()
        viewModel.getBestRankTop()
        //注册Grpc指令
        viewModel.registerGrpcCmd()
        //检查左右屏连接并发送协同运动开始指令
        viewModel.initCollaboration()
        SpeechWrapper.speech(getString(R.string.forward_flexion_identified_tip))
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //如果是赛事，则隐藏今日最佳
        viewModel.matchId?.let {
            mBinding.groupTodayTop.visibility = View.GONE
        }
        //自主，组织，随堂测隐藏今日最佳
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST || viewModel.sceneId == SceneConstants.INDEPENDENT_TEST || viewModel.sceneId == SceneConstants.CLASS_TEST) {
            mBinding.groupTodayTop.visibility = View.GONE
        }
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName
        mBinding.tvInTip.text = getString(R.string.forward_flexion_in_tip)
        mBinding.btnInTipConfirm.text =
            getString(R.string.confirm_countdown, tipCountDownCount)

        mBinding.btnInTipConfirm.setOnClickListener {
            mainHandler.removeCallbacks(tipRunnable)
            hideInTips()
        }

        initAreaViews()

        //开始/结束测试/重测/下一组属于整个运动
        mBinding.tvBegin.setOnClickListener {
            //点击"立即开始"：跳过识别倒计时，主副屏立即同时开始运动（开始后由sportStartLD收起按钮）
            viewModel.startSportNow()
        }
        mBinding.tvFinish.setOnClickListener {
            showExitDialog()
        }

        mainHandler.postDelayed(tipRunnable, 1000)

        initOverlay()
        initConfigAndController()
        if (viewModel.streamType == 1) {
            mBinding.layout.visibility = View.GONE
            mBinding.llBtn.visibility = View.GONE
            initCamera()
        } else {
            mBinding.layout.visibility = View.VISIBLE
            mBinding.llBtn.visibility = View.VISIBLE
            initVideoView()
            initVideo()
        }
    }

    private fun initConfigAndController() {
        val param = MultiCoreConfig.getForwardFlexionParam()
        val debug = BusiBaseConfig.getDebugEnable()
        showLoadingDialog(getString(R.string.model_loding))
        val sourceType =
            if (viewModel.streamType == 1) SitAndReachControllerConfig.InputSource.CAMERA else SitAndReachControllerConfig.InputSource.VIDEO
        mBinding.sitAndReachLayout.setInputSource(sourceType)
        val config = SitAndReachControllerConfig()
        config.drawSkeleton = BusiBaseConfig.getSkeletonEnable()
        config.inputSource = sourceType
        config.checkMethod = SitAndReachConfig.CheckMethod.TWO_HANDS
        val sitAndReachType = SitAndReachType.SIT_AND_REACH_CUSTOM
        sitAndReachType.num = number
        sitAndReachType.boardNum = boardNum
        config.sitAndReachType = sitAndReachType
        config.debug = debug
        config.enableRecordVideo = false
        config.needSetUI = false
        config.footPosition = param.checkFoot
        config.handParallel = param.checkHand
        config.legParallel = param.checkLeg
        controller = SitAndReachController(config, mBinding.sitAndReachLayout)
        controller.callBack = object : SitAndReachCallBack {
            override fun onInitPlayGround(isSuccess: Boolean, msg: String?) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(this@SitReachEighteenActivity, msg, Toast.LENGTH_SHORT).show()
                })
            }

            override fun onModelState(isSuccess: Boolean) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachEighteenActivity,
                        "模型加载: " + (if (isSuccess) "成功" else "失败"), Toast.LENGTH_LONG
                    ).show()
                })
                isModelOk = isSuccess
                startMatDetect()
                hideLoadingDialog()
            }

            override fun onResult(sitAndReachObjectList: MutableList<SitAndReachObject>?) {
                if (sitAndReachObjectList.isNullOrEmpty()) {
                    LogUtil.d("sitAndReachObjectList 为空")
                    return
                }
                viewModel.onHandleIdentity(sitAndReachObjectList)
                viewModel.onPostureStatus(sitAndReachObjectList)
                viewModel.handleResult(sitAndReachObjectList)
            }

            override fun onMessage(resultCodeType: ResultCodeType?) {
                LogUtil.d("消息: $resultCodeType")
            }
        }
    }

    private fun hideInTips() {
        tipsHide = true
        mBinding.groupInTip.visibility = View.GONE
        if (isModelOk) {
            startMatDetect()
        }
    }

    /**
     * 开始检测垫子
     */
    private fun startMatDetect() {
        if (isInit || tipsHide.not()) return
        isInit = true
        hideLoadingDialog()
        controller.combineLayoutAndPrepare()
        viewModel.toStatusIdentifyFace()
    }

    private fun initAreaViews() {
        updateAreaTabs()
        mBinding.tvTabLeft.setOnClickListener { switchArea(DisplayType.LEFT) }
        mBinding.tvTabMain.setOnClickListener { switchArea(DisplayType.MAIN) }
        mBinding.tvTabRight.setOnClickListener { switchArea(DisplayType.RIGHT) }
        switchArea(DisplayType.MAIN)
    }

    private fun switchArea(area: DisplayType) {
        currentArea = area
        mBinding.sportViewMain.visibility =
            if (area == DisplayType.MAIN) View.VISIBLE else View.GONE
        mBinding.sportViewLeft.visibility =
            if (area == DisplayType.LEFT) View.VISIBLE else View.GONE
        mBinding.sportViewRight.visibility =
            if (area == DisplayType.RIGHT) View.VISIBLE else View.GONE

        mBinding.tvAreaName.text = when (area) {
            DisplayType.LEFT -> getString(R.string.area_left)
            DisplayType.MAIN -> getString(R.string.area_main)
            DisplayType.RIGHT -> getString(R.string.area_right)
            DisplayType.OTHER -> "Other"
        }
        updateAreaTabs()
    }

    private fun updateAreaTabs() {
        leftIdentifiedCount = viewModel.leftPersonArray.size()
        mainIdentifiedCount = viewModel.sitReachPersonArray.size()
        rightIdentifiedCount = viewModel.rightPersonArray.size()

        val sporting = viewModel.sportStartedLD.value
        updateAreaTab(
            mBinding.tvTabLeft,
            DisplayType.LEFT,
            leftIdentifiedCount,
            sporting,
            R.string.area_left_identified,
            R.string.area_left_sporting,
            R.string.area_left_unconnected,
            R.string.area_left_occupied
        )
        mBinding.tvTabMain.text = getString(
            if (sporting) R.string.area_main_sporting else R.string.area_main_identified,
            mainIdentifiedCount
        )
        updateAreaTab(
            mBinding.tvTabRight,
            DisplayType.RIGHT,
            rightIdentifiedCount,
            sporting,
            R.string.area_right_identified,
            R.string.area_right_sporting,
            R.string.area_right_unconnected,
            R.string.area_right_occupied
        )

        mBinding.tvTabLeft.isSelected = currentArea == DisplayType.LEFT
        mBinding.tvTabMain.isSelected = currentArea == DisplayType.MAIN
        mBinding.tvTabRight.isSelected = currentArea == DisplayType.RIGHT
    }

    /**
     * 更新单个区域Tab：协同异常状态（未连接/设备占用中）显示红色且不可点击
     */
    private fun updateAreaTab(
        tvTab: TextView,
        displayType: DisplayType,
        count: Int,
        sporting: Boolean,
        identifiedRes: Int,
        sportingRes: Int,
        unconnectedRes: Int,
        occupiedRes: Int
    ) {
        val state = viewModel.collaborationStateLD.value[displayType]
            ?: CollaborationAreaState.UNCONNECTED
        when (state) {
            CollaborationAreaState.UNCONNECTED -> {
                tvTab.text = getString(unconnectedRes)
                tvTab.setTextColor(Color.parseColor(AREA_TAB_RED))
                tvTab.isEnabled = false
            }

            CollaborationAreaState.OCCUPIED -> {
                tvTab.text = getString(occupiedRes)
                tvTab.setTextColor(Color.parseColor(AREA_TAB_RED))
                tvTab.isEnabled = false
            }

            else -> {
                tvTab.text = getString(
                    if (sporting) sportingRes else identifiedRes,
                    count
                )
                tvTab.setTextColor(
                    ContextCompat.getColorStateList(this, R.color.selector_area_tab_text)
                )
                tvTab.isEnabled = true
            }
        }
    }

    private fun initVideoView() {
        mBinding.bntStart.setOnClickListener {
            videoReader?.onStart()
        }
        mBinding.btnPause.setOnClickListener {
            videoReader?.onPause()
        }
        mBinding.bntNext.setOnClickListener {
            videoReader?.nextFrame()
        }
        mBinding.bntChange.setOnClickListener {
            playMode = when (playMode) {
                AdlAvReader.PlayMode.Auto -> AdlAvReader.PlayMode.OneFrame
                AdlAvReader.PlayMode.OneFrame -> AdlAvReader.PlayMode.Auto
            }
            videoReader?.setPlayMode(playMode)
            videoReader?.onStart()
        }
        mBinding.bntReset.setOnClickListener {
            videoReader?.onReset()
        }
    }

    fun initVideo() {
        videoReader = AdlVideoReader()
        videoReader?.setPlayMode(playMode)
        videoReader?.setSurface(mBinding.surfaceView.holder.surface)
        videoReader?.enableRender(enableRender)
        videoReader?.onReset(videoFilePath)
        videoReader?.setCallback(object : VideoReaderCallback {
            override fun onError(e: Exception?) {
            }

            override fun onFrame(yuv: ByteArray?, width: Int, height: Int, time: Long) {
                frameTime += 66
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
                controller.onFrame(yuv, info)
                yuv?.let {
                    viewModel.onceCacheProcessor.onFrame(yuv, info)
                }
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * 收集 Flow。页面处于 STARTED 及以上状态才派发事件，退到后台暂停。
     */
    private fun <T> Flow<T>.collectOnLifecycle(block: suspend (T) -> Unit) {
        lifecycleScope.launch {
            flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect(block)
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.sportCardStateLD.collectOnLifecycle {
            getAreaSportViewByDisplay(it.first).changeState(it.second.first, it.second.second)
            //切换不可用状态时，页面右上角提示"场地构建失败，部分位置暂不可用"
            if (mBinding.tvUnavailableTip.isGone) {
                mBinding.tvUnavailableTip.visibility =
                    if (it.second.second == ForwardFlexionCardState.UNAVAILABLE) View.VISIBLE else View.GONE
            }
        }

        viewModel.identifiedLD.collectOnLifecycle {
            getAreaSportViewByDisplay(it.first).setAccountInfo(it.second.first, it.second.second)
            timeOutHelper.cancel()
            //识别到人员，刷新底部导航栏"已识别x人"文案
            updateAreaTabs()
        }

        viewModel.sportStartLD.collectOnLifecycle {
            mBinding.tvBegin.visibility = View.GONE
            startRecording()
            //三个区域同时进入运动状态（仅已加入协同的副屏）
            getAreaSportViewByDisplay(DisplayType.MAIN).sportStarted()
            if (viewModel.isSubScreenJoined(DisplayType.LEFT)) {
                getAreaSportViewByDisplay(DisplayType.LEFT).sportStarted()
            }
            if (viewModel.isSubScreenJoined(DisplayType.RIGHT)) {
                getAreaSportViewByDisplay(DisplayType.RIGHT).sportStarted()
            }
            controller.start()
            frameTime = System.currentTimeMillis()
            LogUtil.d("开始运动")
        }

        //一次测试完毕
        viewModel.testOnceFinishLD.collectOnLifecycle {
            mBinding.tvSportingTips.visibility = View.GONE
            stopRecording()
            viewModel.uploadResult()
        }

        viewModel.sportTipsInfoLD.collectOnLifecycle {
            mBinding.tvSportingTips.visibility = View.VISIBLE
            mBinding.tvSportingTips.text = it
        }

        viewModel.sportResultLD.collectOnLifecycle {
            LogUtil.d("最终成绩：${it.first} -- ${it.second.second.count}")
            getAreaSportViewByDisplay(it.first).addScore(it.second.first, it.second.second)
        }

        viewModel.collaborationStateLD.collectOnLifecycle {
            //协同状态变化，刷新导航区（未连接/设备占用中红字不可点击）
            updateAreaTabs()
        }

        //协同已识别人数变化：弹窗展示或更新"是否清空已识别结果"提示
        viewModel.identifiedCountLD.collectOnLifecycle {
            showOrUpdateClearIdentifyDialog()
        }

        viewModel.identifyCountDownLD.collectOnLifecycle {
            //主屏或副屏有人员识别成功：开始识别倒计时，"立即开始"按钮展示剩余秒数
            if (viewModel.sportStartedLD.value) return@collectOnLifecycle
            mBinding.tvBegin.visibility = View.VISIBLE
            mBinding.tvBegin.text = if (it > 0) {
                getString(R.string.start_now_num, it)
            } else {
                getString(R.string.start_now)
            }
        }

        viewModel.toDoneLD.collectOnLifecycle {

        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }
    }

    private fun addResultView(resultList: List<ThirtySportResultItem>) {
    }

    /**
     * 初始化今日最佳
     */
    private fun initTopView(topEntity: BestRankTopEntity) {
        if (!TextUtils.isEmpty(topEntity.originalSportResult)) {
            mBinding.tvCount.visibility = View.VISIBLE
            mBinding.tvUnit.visibility = View.VISIBLE
            mBinding.tvUnit.text = viewModel.measureUnit
            mBinding.tvName.text = topEntity.accountName
            mBinding.tvCount.text = topEntity.originalSportResult
            Glide.with(this).load(AdlService.getService().facePrefix + topEntity.faceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        }
    }

    /**
     * 展示或更新"是否清空已识别结果"对话框
     */
    @Synchronized
    private fun showOrUpdateClearIdentifyDialog() {
        val text = buildClearIdentifyText()
        val dialog = clearIdentifyDialog
        if (dialog != null) {
            LogUtil.d("更新内容：$text")
            dialog.updateContent(text)
            return
        }
        LogUtil.d("首次显示内容：$text")
        clearIdentifyDialog = CommonTipDialog()
            .setContent(text)
            .setCallback {
                //用户选择"是"：按需向已上报识别人数的区域发送清除指令
                viewModel.confirmClearIdentifyInfo()
                clearIdentifyDialog = null
                updateAreaTabs()
            }
            .setCancelCallback {
                //用户选择"否"：关闭本次提示
                viewModel.cancelClearIdentifyInfo()
                clearIdentifyDialog = null
            }
        clearIdentifyDialog?.show(supportFragmentManager, "clear_identify")
    }

    private fun buildClearIdentifyText(): String {
        val sb = StringBuilder()
        viewModel.identifiedCountMap[DisplayType.LEFT]?.let {
            sb.append("左区已识别${it}人，")
        }
        viewModel.identifiedCountMap[DisplayType.RIGHT]?.let {
            sb.append("右区已识别${it}人，")
        }
        sb.append("是否清空已识别结果")
        return sb.toString()
    }

    private fun initCamera() {
        cameraDispatcher.addFrameProcessor(viewModel.onceCacheProcessor)
        cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
            override fun onFrame(yuv: ByteArray?, previewInfo: AdlCameraPreviewInfo) {
                controller.onFrame(yuv, previewInfo)
            }
        })
        val ip = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_IP,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP
        )
        val account = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
        )
        val psw = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_PSW,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
        )
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = true
        ipcHik = hikSubConfig
            .setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
                ip,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
                account,
                psw
            ).setStreamType(0).build() as NsCameraHKSource
    }

    private fun cameraLogin() {
        if (viewModel.streamType != 1) return
        ipcHik.login(object : AdlCameraListener {
            override fun onOpenSucceed(p0: Boolean) {
            }

            override fun onLoginSucceed() {
                changeResolution()
                SystemClock.sleep(100)
                runOnUiThread {
                    hideCameraTips()
                    startCameraPreview()
                }
            }

            override fun onLoginFail() {
                runOnUiThread { setCameraTips(getString(R.string.ipc_camera_fail_tip)) }
            }

            override fun onOpenStatus(p0: String?) {
            }

            override fun onError(p0: Exception?) {
                runOnUiThread { setCameraTips(getString(R.string.ipc_camera_fail_tip)) }
            }

            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
    }

    private fun startCameraPreview() {
        mBinding.sitAndReachLayout.cameraPreviewView.startPreview(ipcHik, object :
            com.adl.lib.AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {
            }

            override fun onCameraOpen() {
                runOnUiThread { hideCameraTips() }
            }

            override fun onCameraReOpen() {
            }

            override fun onDestroyed() {
            }

            override fun onStreamLoss() {
                runOnUiThread { setCameraTips(getString(R.string.camera_loss_tips)) }
            }

            override fun onOpenFail() {
                runOnUiThread { setCameraTips(getString(R.string.ipc_camera_fail_tip)) }
            }
        })
        mBinding.sitAndReachLayout.cameraPreviewView.addFrameProcessor(cameraDispatcher)
    }

    private fun setCameraTips(text: String) {
        AdlLogger.d(text)
        mBinding.tvCameraTips.visibility = View.VISIBLE
        mBinding.tvCameraTips.text = text
    }

    private fun hideCameraTips() {
        mBinding.tvCameraTips.visibility = View.GONE
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 70, 26, 16)
    }

    private fun initOverlay() {
    }

    override fun onStatusIdentify() {
        LogUtil.d("开始人脸识别")
        hideBackBtn()
    }

    override fun onStatusPrepare() {
        timeOutHelper.cancel()
        //主屏已就绪，触发识别倒计时；"立即开始"按钮由识别倒计时驱动展示剩余秒数
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
        super.onStatusSport()
        controller.start()
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        LogUtil.d("onStatusEnd  onStatusEnd  onStatusEnd")
        mBinding.tvBegin.visibility = View.GONE
        mBinding.tvSportingTips.visibility = View.GONE
    }

    private fun releaseModel() {
        TsModelResPool.instance().markReleaseGroup(MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME)
    }

    /**
     * 开始录像
     */
    private fun startRecording() {
        if (viewModel.streamType == 1) {
            viewModel.videoFile = null
            viewModel.startRecord(cameraDispatcher)
        }
    }

    private fun stopRecording() {
        if (viewModel.streamType == 1) {
            viewModel.stopRecord(cameraDispatcher)
        }
    }

    private fun getAreaSportViewByDisplay(displayType: DisplayType = DisplayType.MAIN) =
        when (displayType) {
            DisplayType.LEFT -> mBinding.sportViewLeft
            DisplayType.MAIN -> mBinding.sportViewMain
            DisplayType.RIGHT -> mBinding.sportViewRight
            DisplayType.OTHER -> mBinding.sportViewMain
        }

    override fun onResume() {
        super.onResume()
        cameraLogin()
    }

    override fun onPause() {
        super.onPause()
        controller.reset()
        if (mBinding.sitAndReachLayout.cameraPreviewView != null) {
            mBinding.sitAndReachLayout.cameraPreviewView.stopPreview()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechWrapper.stop()
        hideLoadingDialog()
        controller.release()
        if (mBinding.sitAndReachLayout.cameraPreviewView != null) {
            mBinding.sitAndReachLayout.cameraPreviewView.release()
        }
        cameraDispatcher.quit()
        videoReader?.release()
        releaseModel()
    }
}
