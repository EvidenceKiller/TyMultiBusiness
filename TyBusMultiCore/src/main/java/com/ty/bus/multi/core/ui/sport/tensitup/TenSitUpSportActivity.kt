package com.ty.bus.multi.core.ui.sport.tensitup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.processor.SportProcessorStatus
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.adl.sport.general.multi.MultiSportResult
import com.adl.sport.general.situp.SitUpBodyFilter
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.factory.AbstractMultiSportFactory
import com.ty.bus.multi.core.factory.MultiSportProduceFactory
import com.ty.bus.multi.databinding.ActivityTenSitUpBinding
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TenSitUpSportActivity :
    BaseMultiSportActivity<ActivityTenSitUpBinding, TenSitUpSportViewModel>() {
    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

    //是否正在播报语音
    private var isSpeeching = false

    //    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/FM0640097_01_20260325145319339.mp4"
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_sit_up-2k.mp4"

    /***************************视频播放调试*************************/

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val multiSportProcessor by lazy { MultiSportProcessor() }

    private val sportConfig by lazy { MultiSportConfig() }

    private var needChangeResolution = false //是否需要切换分辨率

    private lateinit var parser: MultiSportParser<*>

    private lateinit var factory: AbstractMultiSportFactory

    private var tipCountDownCount = 3  //提示的倒计时时间
    private val tipRunnable: Runnable = object : Runnable {
        override fun run() {
            tipCountDownCount--
            if (tipCountDownCount == 0) {
                viewModel.toStatusIdentifyFace()
                return
            }
            mBinding.btnInTipConfirm.text =
                getString(R.string.confirm_countdown, tipCountDownCount)
            mainHandler.postDelayed(this, 1000)
        }

    }
    private val checkStatusList = mutableListOf<Boolean>()

    /**********************摄像头相关******************************/
    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    private lateinit var ipcHik: NsCameraHKSource

    /**********************摄像头相关******************************/

    override fun initData() {
        super.initData()
//        viewModel.streamType = 2
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.tsConfig =
            intent.getSerializableExtra("sport_config") as? SixSportConfig
        for (i in 0 until viewModel.sportManCount) {
            checkStatusList.add(false)
        }
        factory = MultiSportProduceFactory.getFactory(
            viewModel.sportInfo?.sportItemCode,
            viewModel.sportInfo?.testCount
        )
        viewModel.getBestRankTop()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.changeSportViewStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
            if (it.second == MultiSportCardViewState.IDENTIFIED) {
                for (i in checkStatusList.indices) {
                    checkStatusList[i] = false
                }
                LogUtil.d("======================>>>>>>>>>>>>>>>>>>>>>> ${viewModel.sportPersonArray.size}")
                viewModel.sportPersonArray.forEach { _, value ->
                    val number = value.number
                    if (number in checkStatusList.indices) {
                        checkStatusList[number] = true
                    }
                }
                parser.setCheckStatusList(checkStatusList)
            }
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.sportView.setAccountInfo(it.first, it.second)
        }

        viewModel.countDownFinishLD.observe(this) {
            hideTip()
            if (mBinding.overlay.multiRegion != null) {
                mBinding.overlay.multiRegion.setDrawing(false)
            }
            if (needChangeResolution) {
                changeResolutionTo2K()
            }
            //切换Pose模型ID
            mBinding.readyView.startReady {
                mBinding.sportView.sportStarted()
                viewModel.toStatusSportStart()
            }
        }

        viewModel.resultLD.observe(this) {
            mBinding.sportView.updateResult(it)
        }

        viewModel.changeTimeTextBgLD.observe(this) {
//            mBinding.sportView.changeTimeTextBg(it)
        }

        viewModel.sportCountDownLD.observe(this) {
            mBinding.sportView.updateTime(it)
        }

        viewModel.readySpeechLD.observe(this) {
            SpeechWrapper.speech(factory.getReadyTip())
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.toDoneLD.observe(this) {
        }

        lifecycleScope.launch {
            viewModel.countDownNoticeLD.collect {
                identifyCountDown(it)
            }
        }
    }

    private fun identifyCountDown(time: Long) {
        mBinding.groupTimeCountDown.visible()
        mBinding.tvTimeDown.text =
            com.ty.tybusbase.expand.getString(R.string.auto_start_second_after, time)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName
        mBinding.tvInTip.text = factory.getStartTip()
        mBinding.btnInTipConfirm.setOnClickListener {
            mainHandler.removeCallbacks(tipRunnable)
            viewModel.toStatusIdentifyFace()
        }
        mBinding.tvStartNow.setOnClickListener {
            viewModel.readyGo()
        }
        mainHandler.postDelayed(tipRunnable, 1000)
        mBinding.btnInTipConfirm.text =
            getString(R.string.confirm_countdown, tipCountDownCount)

        initOverlay()
        initProcess()
        if (viewModel.streamType == 1) {
            mBinding.layout.visibility = View.GONE
            mBinding.llBtn.visibility = View.GONE
            initCamera()
        } else {
            initVideoView()
            initVideo()
            cameraDispatcher.addFrameProcessor(viewModel.onceCacheProcessor)
            cameraDispatcher.addFrameProcessor(multiSportProcessor)
        }
        initSportView()
    }

    private fun initSportView() {
        if (viewModel.tsConfig?.isSupportNextGroup == true) {
            mBinding.sportView.showNextBtn()
        }
        //初始化运动View按钮监听
        mBinding.sportView.listener = object : ResultViewBtnClickListener {
            override fun onStartClick() {
                viewModel.readyGo()
            }

            override fun onRetestClick() {
                retest()
            }

            override fun onNextClick() {
                next()
            }

            override fun onEndTestClick() {
                showExitDialog()
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
                frameTime += 1000 / 24
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
//                multiSportProcessor.onFrame(yuv, info)
                yuv?.let {
                    cameraDispatcher.onFrame(yuv, info)
                }
            }

            override fun onComplete() {

            }
        })
    }


    private fun initCamera() {
        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(viewModel.onceCacheProcessor)
        cameraDispatcher.addFrameProcessor(multiSportProcessor)
        val cameraConfig =
            BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = false
        ipcHik = hikSubConfig
            .setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
                cameraConfig.ip,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
                cameraConfig.account,
                cameraConfig.psw
            ).setStreamType(0).build() as NsCameraHKSource
    }

    private fun cameraLogin() {
        if (viewModel.streamType != 1) return
        ipcHik.login(object : AdlCameraListener {

            override fun onOpenSucceed(p0: Boolean) {

            }

            override fun onLoginSucceed() {
                changeResolution(70,15)
                SystemClock.sleep(100)
                runOnUiThread {
                    hideCameraTips()
                    startCameraPreview()
                }
            }

            override fun onLoginFail() {
                runOnUiThread {
                    setCameraTips(getString(R.string.ipc_camera_fail_tip))
                }
            }

            override fun onOpenStatus(p0: String?) {

            }

            override fun onError(p0: Exception?) {
                runOnUiThread {
                    setCameraTips(getString(R.string.ipc_camera_fail_tip))
                }
            }

            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
    }

    private fun changeResolutionTo2K() {
        if (viewModel.streamType == 2) return
        needChangeResolution = false
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            /********************切换分辨率********************/
            ipcHik.pauseProcessor()
            changeResolution(70, 15)
            SystemClock.sleep(100)
            mBinding.cameraView.stopPreview()
            SystemClock.sleep(100)
            ipcHik.restartProcessor()
            startCameraPreview()
            SystemClock.sleep(200)
            multiSportProcessor.clearTransform()
            /********************切换分辨率********************/
        }
    }

    private fun changeResolutionTo4K() {
        if (viewModel.streamType == 2) return
        needChangeResolution = true
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            /********************切换分辨率********************/
            ipcHik.pauseProcessor()
            changeResolution(64, 14)
            SystemClock.sleep(100)
            mBinding.cameraView.stopPreview()
            SystemClock.sleep(100)
            ipcHik.restartProcessor()
            startCameraPreview()
            SystemClock.sleep(200)
            multiSportProcessor.clearTransform()
            /********************切换分辨率********************/
        }
    }

    private fun startCameraPreview() {
        //开启摄像头
        mBinding.cameraView.startPreview(ipcHik, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {

            }

            override fun onCameraOpen() {
                viewModel.onCameraOpen(multiSportProcessor, cameraDispatcher)
                runOnUiThread {
                    hideCameraTips()
                }
            }

            override fun onCameraReOpen() {

            }

            override fun onDestroyed() {

            }

            override fun onStreamLoss() {
                runOnUiThread {
                    setCameraTips(getString(R.string.camera_loss_tips))
                }
            }

            override fun onOpenFail() {
                runOnUiThread {
                    setCameraTips(getString(R.string.ipc_camera_fail_tip))
                }
            }
        })
        mBinding.cameraView.addFrameProcessor(cameraDispatcher)
        mBinding.cameraView.textureView.scaleX = -1f
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
    private fun changeResolution(re: Byte, frameRate: Byte = 14) {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, re, 27, frameRate)
    }


    override fun onStatusIdentify() {
        parser.checkChannelStatus = true
        multiSportProcessor.changeTo(SportProcessorStatus.Prepare)
        mBinding.groupInTip.visibility = View.GONE
        setTip(factory.getInTip())
        mBinding.overlay.post {
            mBinding.sportView.visibility = View.VISIBLE
            hideBackBtn()
        }
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
        super.onStatusSport()
        mBinding.groupTimeCountDown.gone()
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
        parser.checkSkipScore = true
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        hideTip()
        parser.checkChannelStatus = false
        parser.checkSkipScore = false
        mBinding.sportView.sportFinish()
        multiSportProcessor.changeTo(SportProcessorStatus.Idle)
    }


    private fun initOverlay() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        mBinding.overlay.enableDraw(isDebug)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            MultiCoreConstant.TEN_SIT_UP_REGION_KEY,
            RoleConfigMulti::class.java
        )
        uiConfig.number = viewModel.sportManCount
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.enableTouch(false)
    }

    private fun initProcess() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        val skeleton = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)

        sportConfig.debug = isDebug
        sportConfig.bodyModelId = factory.getBodyModelId()
        sportConfig.poseModelId = factory.getPoseModelId()
        sportConfig.drawAllBody = isDebug
        sportConfig.drawBody = isDebug
        sportConfig.drawSkeleton = skeleton
        sportConfig.sportType = factory.getMultiSportType()
        sportConfig.faceStatusBodyId = factory.getFaceBodyModelId()
        sportConfig.faceStatusPoseId = factory.getFacePoseModelId()
        sportConfig.loadModel(object : TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
                if (DeviceUtil.isTsRk3588()) {
                    showLoadingDialog(getString(R.string.model_loding))
                }
            }

            override fun onSucceed(list: List<TsModelInfo?>?) {
            }

            override fun onFinish(
                list: List<TsModelInfo?>?,
                errrorList: List<TsModelInfo?>?
            ) {
                hideLoadingDialog()
            }

            override fun onEnd(str: String?) {
            }

        })

        parser = factory.getParser()

        multiSportProcessor.setConfig(sportConfig)
        multiSportProcessor.open(mBinding.overlay)
        multiSportProcessor.setBodyFilter(SitUpBodyFilter(0.3f))
        multiSportProcessor.setEnableDropFrame(false); // 整体丢帧: 全局的丢帧
        multiSportProcessor.setBodyDropFrame(false); // 默认是打开
        multiSportProcessor.setBodyInterval(3); // 丢帧间隔
        multiSportProcessor.setParser(parser)
        multiSportProcessor.setSmooth(factory.enableSmooth())
        multiSportProcessor.setHistoryBody(factory.enableHistoryBody())
        multiSportProcessor.setCallback(object : MultiSportCallbackImpl() {
            override fun onResult(result: MultiSportResult) {
                viewModel.handleProcessorCallback(result.groupManList)

            }

            override fun onBreakRules(index: Int, code: Int, role: String?) {
                super.onBreakRules(index, code, role)
                LogUtil.d("onBreakRules: $index -- $code -- $role")
                if (!isSpeeching) {
                    isSpeeching = true
                    SpeechWrapper.speech2("${index + 1}号${role}") {
                        isSpeeching = false
                    }
                }
                runOnUiThread {
                    mBinding.sportView.addErrorMsg(index, code, role)
                }
            }

            override fun onError(index: Int, code: Int, error: String) {
                LogUtil.d("onError: $code $error")
            }
        })
        multiSportProcessor.onInit()
    }

    private fun setTip(tip: String) {
    }

    private fun hideTip() {
    }

    /**
     * 下一组
     */
    private fun next() {
        //切换Pose模型ID
        mBinding.overlay.multiRegion.setDrawing(true)
        timeOutHelper.start()
//        changeResolutionTo4K()
        mBinding.readyView.reset()
        viewModel.reset()
        multiSportProcessor.onReset()
        viewModel.toStatusIdentifyFace()
    }

    /**
     * 重测试
     */
    private fun retest() {
        mBinding.readyView.reset()
        multiSportProcessor.onReset()
        viewModel.retest()
    }

    override fun onResume() {
        super.onResume()
        multiSportProcessor.onReset()
        cameraLogin()
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        multiSportProcessor.onRelease()
        parser.resetScore()
        sportConfig.unloadModel()
        mBinding.cameraView.release()
        mBinding.overlay.release()
        cameraDispatcher.quit()
        videoReader?.release()
        SpeechWrapper.stop()
    }
}