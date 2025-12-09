package com.ty.bus.multi.core.ui.sport.forwardflexionsix

import android.graphics.ImageFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup.LayoutParams
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResGroup
import com.adl.mount.core.TsModelResPool
import com.adl.mount.core.TsModelResPool.OnLoadModelCallbackSample
import com.adl.processor.SportProcessorStatus
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.common.PointElement
import com.ty.bus.multi.core.view.sixforwardflexion.SixForwardFlexionResultView
import com.ty.bus.multi.databinding.ActivityForwardFlexion6Binding
import com.ty.forwardflexion.callback.FFlexionSportCb
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.forwardflexion.config.FFlexionSportResult
import com.ty.forwardflexion.informCode.InformCode
import com.ty.forwardflexion.processor.ForwardFlexionProcessor
import com.ty.sport.common.basic.bean.Point
import com.ty.sport.common.sport.processor.SportConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil


class ForwardFlexionSixActivity :
    BaseMultiSportActivity<ActivityForwardFlexion6Binding, ForwardFlexionSixViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/1.mp4"

    /***************************视频播放调试*************************/

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var forwardFlexionProcessor: ForwardFlexionProcessor = ForwardFlexionProcessor()
    private var sportConfig: SportConfig? = null
    val isDebug =
        MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE)
    private val pointElement by lazy { PointElement() } // 调试用的点绘制
    private var preFrameTime = 0L //上一帧的时间，用于判断是纳秒还是毫秒时间戳
    private var checkTimeOk = false //校验时间是否OK
    private var isNus = false //是否是纳秒时间戳
    private var uiConfig: FFlexionRoleConfig? = null
    private var isModelOk = false //模型是否加载成功
    private var isInit = false //是否开始流程

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    private lateinit var ipcHik: NsCameraHKSource

    private var tipCountDownCount = 3  //提示的倒计时时间
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
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.ffSportConfig =
            intent.getSerializableExtra("sport_config") as? ForwardFlexionConfig
        viewModel.initData()
        SpeechWrapper.speech(getString(R.string.forward_flexion_identified_tip))
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName
        mBinding.tvInTip.text = getString(R.string.forward_flexion_in_tip)
        mBinding.btnInTipConfirm.text =
            getString(R.string.confirm_countdown, tipCountDownCount)

        mBinding.btnInTipConfirm.setOnClickListener {
            mainHandler.removeCallbacks(tipRunnable)
            hideInTips()
        }
        mBinding.sportView.setStartListener {
            viewModel.toStatusPrepare()
        }

        if (viewModel.ffSportConfig?.isSupportNextGroup == true) {
            mBinding.sportView.showNextBtn()
        }

        mBinding.sportView.listener = object : ResultViewBtnClickListener {
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

        mainHandler.postDelayed(tipRunnable, 1000)

        initOverlay()
        initProcessor()
        if (viewModel.streamType == 1) {
            mBinding.layout.visibility = View.GONE
            mBinding.llBtn.visibility = View.GONE
            initCamera()
        } else {
            initVideoView()
            initVideo()
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
                forwardFlexionProcessor.onFrame(
                    1,
                    yuv,
                    info.width,
                    info.height,
                    info.frameTimestamp,
                    0
                )
            }
        })
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.sportCardStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.sportView.setAccountInfo(it.first, it.second)
        }

        viewModel.sportStartLD.observe(this) {
            startRecording()
            if (it == 1) {
                if (isDebug) {
                    mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportDoing)
                }
                mBinding.sportView.sportStarted()
            }
            forwardFlexionProcessor.changeTo(SportProcessorStatus.Sporting)
            frameTime = System.currentTimeMillis()
            LogUtil.d("开始运动")
        }

        viewModel.testOnceFinishLD.observe(this) {
            forwardFlexionProcessor.changeTo(SportProcessorStatus.Idle)
            forwardFlexionProcessor.onReset()
            forwardFlexionProcessor.stopRecord()
            viewModel.uploadResult()
        }

        viewModel.sportTipsInfoLD.observe(this) {
            mBinding.sportView.sportingTips(it)
        }

    }


    private fun showDesc(msg: String) {
        if (isDebug.not()) return
        runOnUiThread {
            mBinding.tvDes.append(msg)
        }
    }

    private fun hideInTips() {
        mBinding.groupInTip.visibility = View.GONE
        if (isModelOk) {
            startMatDetect()
        }
    }

    private fun initCamera() {
        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(viewModel.onceCacheProcessor)
        cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
            override fun onFrame(yuv: ByteArray?, previewInfo: AdlCameraPreviewInfo) {
                forwardFlexionProcessor.onFrame(
                    1,
                    yuv,
                    previewInfo.width,
                    previewInfo.height,
                    previewInfo.frameTimestamp,
                    1
                )
            }
        })
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
        ipcHik = hikSubConfig
            .setPreviewFormat(ImageFormat.YV12)
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
            override fun onOpenSucceed() {

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

    private fun startCameraPreview() {
        //开启摄像头
        mBinding.cameraView.startPreview(ipcHik, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {

            }

            override fun onCameraOpen() {
                runOnUiThread {
                    hideCameraTips()
                }
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
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 64, 26, 14)
    }

    private fun initOverlay() {
        val isDebug = MMKVUtil.decodeBoolean(
            MMKVBusiKey.MODEL_DEBUG,
            BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
        )
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(
            MMKVBusiKey.SKELETON_ABLE,
            BusiBaseConstantsValue.DEFAULT_SKELETON
        )
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        uiConfig = AdlUIConfig.instance()
            .readConfig(
                MultiCoreConstant.FORWARD_FLEXION_SIX_UI_KEY,
                FFlexionRoleConfig::class.java
            )
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.enableTouch(false)
        if (isDebug) {
            pointElement.setPreviewSize(
                BusiBaseConstantsValue.CAMERA_PREVIEW_4K_WIDTH,
                BusiBaseConstantsValue.CAMERA_PREVIEW_4K_HEIGHT
            )
            mBinding.overlay.addElement(pointElement)
        }
    }

    private fun initProcessor() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        MultiCoreConfig
        val param = MultiCoreConfig.getForwardFlexionParam()
        // 运动配置
        sportConfig = SportConfig.builder()
            .debug(isDebug)
            .record(true)
            .videoDevice(2)
            .videoFps(15)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_HEIGHT)
            .valueInt(intArrayOf(diskNum, 60, 20, param.getAngle(), param.getSsdqThVaule()))
            .valueBool(booleanArrayOf(param.checkLeg, param.checkFoot, param.checkHand))
//            .valueBool(booleanArrayOf(false, false, false))
            .build()
        mBinding.overlay.setMarkerAreaNum(diskNum)
        forwardFlexionProcessor.setConfig(sportConfig)
        forwardFlexionProcessor.setSportCallBack(object : FFlexionSportCb {
            override fun onResult(result: FFlexionSportResult) {
                runOnUiThread {
                    //显示成绩
                    result.grade.forEach {
                        LogUtil.d("${it.key}号成绩为${it.value}")
                        showDesc("${it.key}号成绩为${it.value}\n")
                        mBinding.sportView.addScore(it.key, it.value)
                    }
                }
                viewModel.handleResult(result)
            }

            override fun onInform(code: InformCode?, error: String?) {
                LogUtil.d("$code:$error")
                if (code == InformCode.LANDMARKFINSIH || code == InformCode.LANDMARKFINSIHERR) {
                    ToastUtil.getInstance(
                        this@ForwardFlexionSixActivity,
                        if (code == InformCode.LANDMARKFINSIH) getString(R.string.forward_flexion_auto_mat_success) else getString(
                            R.string.forward_flexion_auto_mat_fail
                        )
                    ).show()
                    viewModel.toStatusIdentifyFace()
                    mBinding.overlay.saveConfig()
                }
            }

            override fun onPostureStatus(status: MutableMap<Int, InformCode>) {
                viewModel.onPostureStatus(status)
                for (entry in status.entries) {
                    if (entry.value == InformCode.PREPAREFINSIH || entry.value == InformCode.PREPAREERR || entry.value == InformCode.NOTCHECKIN) {
                        if (entry.value == InformCode.PREPAREFINSIH) {
                            LogUtil.d(entry.key.toString() + "号准备完成")
                            showDesc("${entry.key}号准备完成\n")
                        } else if (entry.value == InformCode.PREPAREERR) {
                            LogUtil.d(entry.key.toString() + "号准备异常")
                            showDesc("${entry.key}号准备异常\n")
                        } else if (entry.value == InformCode.NOTCHECKIN) {
                            LogUtil.d(entry.key.toString() + "号未检录")
                            showDesc("${entry.key}号未检录\n")
                        }
                    } else if (entry.value == InformCode.LEGUNCURLERR) {
                        showDesc("${entry.key}号腿未伸直\n")
                        LogUtil.d(entry.key.toString() + "号腿未伸直")
                    } else if (entry.value == InformCode.FOOTLOCATIONERR) {
                        LogUtil.d(entry.key.toString() + "号脚位置不对")
                        showDesc("${entry.key}号脚位置不对\n")
                    } else if (entry.value == InformCode.HANDALIGNERR) {
                        LogUtil.d(entry.key.toString() + "号双手未对齐")
                        showDesc("${entry.key}号双手未对齐\n")
                    }
                }
            }

            override fun onBonePoints(bonePoints: MutableMap<Int, MutableList<Point>>) {
                if (isDebug) {
                    val points = mutableListOf<Point>()
                    val rects = mutableListOf<Rect>()
                    bonePoints.forEach {
                        it.value.forEach { po ->
                            points.add(Point(po.x, po.y))
                        }
                        rects.add(viewModel.getIdentityRect(it.value))
                        mBinding.overlay.refreshNow()
                    }
                    pointElement.setPoints(points)
                    pointElement.setRects(rects)
                }
                viewModel.onBonePoints(bonePoints)
            }
        })
        forwardFlexionProcessor.onInit()
        loadModel()
    }

    override fun onStatusIdentify() {
        LogUtil.d("开始人脸识别")
        mBinding.overlay.post {
            mBinding.sportView.visibility = View.VISIBLE
            hideBackBtn()
            forwardFlexionProcessor.open(mBinding.overlay)
            forwardFlexionProcessor.changeTo(SportProcessorStatus.IdAuth)
        }
    }

    override fun onStatusPrepare() {
        processorPrepare()
    }

    private fun loadModel() {
        val tsModelRes = forwardFlexionProcessor.tsModels
        TsModelResPool.instance()
            .loadModelGroup(
                listOf(
                    TsModelResGroup(
                        false,
                        0L,
                        MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME
                    ).addModelRes(tsModelRes)
                ), object :
                    OnLoadModelCallbackSample() {

                    override fun onStart() {
                        runOnUiThread {
                            showLoadingDialog(getString(R.string.model_loding))
                        }
                    }

                    override fun onSucceed(list: MutableList<TsModelInfo>?) {
                        startMatDetect()
                    }

                    override fun onFinish(
                        list: List<TsModelInfo?>?,
                        errrorList: List<TsModelInfo?>?
                    ) {
                        runOnUiThread {
                            errrorList?.size?.let {
                                if (it > 0) {
                                    ToastUtil.getInstance(
                                        this@ForwardFlexionSixActivity,
                                        getString(R.string.model_load_error)
                                    ).show()
                                }
                            }
                            hideLoadingDialog()
                        }
                    }

                })

    }

    private fun releaseModel() {
        TsModelResPool.instance().markReleaseGroup(MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME)
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {

    }

    override fun onStatusEnd() {
        LogUtil.d("onStatusEnd  onStatusEnd  onStatusEnd")
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Idle)
        forwardFlexionProcessor.onReset()
        mBinding.sportView.sportFinish()
    }

    override fun onResume() {
        super.onResume()
        cameraLogin()
    }


    /**
     * 开始检测垫子
     */
    private fun startMatDetect() {
        if (isInit) return
        isInit = true
        hideLoadingDialog()
        runOnUiThread {
            mBinding.bntStart.performClick()
        }
//        forwardFlexionProcessor.open(mBinding.overlay)
//        forwardFlexionProcessor.changeTo(SportProcessorStatus.MatDetect)
        viewModel.toStatusIdentifyFace()
    }


    /**
     * 下一组
     */
    private fun next() {
        viewModel.reset()
        viewModel.toStatusIdentifyFace()
    }

    /**
     * 重测试
     */
    private fun retest() {
        LogUtil.d("retest  retest  restets")
//        forwardFlexionProcessor.onReset()
        mBinding.sportView.visibility = View.VISIBLE
        viewModel.retest()
    }

    protected fun processorPrepare() {
        LogUtil.d("processorPrepare processorPrepare processorPrepare processorPrepare")
        forwardFlexionProcessor.updateCheckIn(viewModel.checkMap)
        forwardFlexionProcessor.onReset()
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Prepare)
    }

    /**
     * 开始录像
     */
    private fun startRecording() {
        if (viewModel.streamType == 1) {
            viewModel.videoFile = null
            val file = viewModel.getVideoPath(viewModel.sportInfo?.sportSkuId)
            forwardFlexionProcessor.startRecording(file)
        }
    }

    override fun onPause() {
        super.onPause()
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Done)
        mBinding.cameraView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread {
            forwardFlexionProcessor.stopRecord()
            forwardFlexionProcessor.onRelease();
        }.start()
        mBinding.cameraView.release()
        mBinding.overlay.release()
        cameraDispatcher.quit()
        videoReader?.release()
        releaseModel()
    }

}