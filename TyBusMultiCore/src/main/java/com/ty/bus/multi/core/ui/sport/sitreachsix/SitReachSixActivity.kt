package com.ty.bus.multi.core.ui.sport.sitreachsix

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlExecutor
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.mount.core.TsModelResPool
import com.adl.sport.ballBase.ResultCodeType
import com.adl.sport.controllers.sitandreach.SitAndReachCallBack
import com.adl.sport.controllers.sitandreach.SitAndReachController
import com.adl.sport.controllers.sitandreach.SitAndReachControllerConfig
import com.adl.sport.sitandreach.SitAndReachConfig
import com.adl.sport.sitandreach.SitAndReachObject
import com.adl.sport.sitandreach.SitAndReachType
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.common.PointElement
import com.ty.bus.multi.databinding.ActivitySitReach6Binding
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil


class SitReachSixActivity :
    BaseMultiSportActivity<ActivitySitReach6Binding, SitReachSixViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

//    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/sitReach8point.mp4"
//    val videoFilePath = "/sdcard/com.adl.sport/app/20260826/1601010001/00000000000000000000000000000000/175148/1601010001_1787737908925.mp4"
    val videoFilePath = "/sdcard/com.adl.sport/app/20260826/1601010001/00000000000000000000000000000000/175121/1601010001_1787737881518.mp4"
//    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/sit6人_1080p.mp4"

    /***************************视频播放调试*************************/

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var controller: SitAndReachController

    val isDebug = BusiBaseConfig.getDebugEnable()
    private val pointElement by lazy { PointElement() } // 调试用的点绘制
    private var preFrameTime = 0L //上一帧的时间，用于判断是纳秒还是毫秒时间戳
    private var checkTimeOk = false //校验时间是否OK
    private var isNus = false //是否是纳秒时间戳
    private var uiConfig: FFlexionRoleConfig? = null
    private var isModelOk = false //模型是否加载成功
    private var isInit = false //是否开始流程

    //板子几个点
    private var boardNum = 8

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    private lateinit var ipcHik: NsCameraHKSource
    private var tipsHide = false //位置提示是否隐藏
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
//        viewModel.streamType = 2
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
        initConfigAndController()
        if (viewModel.streamType == 1) {
            mBinding.layout.visibility = View.GONE
            mBinding.llBtn.visibility = View.GONE
            initCamera()
        } else {
            initVideoView()
            initVideo()
        }

    }

    private fun initConfigAndController() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
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
        sitAndReachType.num = diskNum
        sitAndReachType.boardNum = boardNum
        config.sitAndReachType = sitAndReachType
        config.debug = debug
        config.enableRecordVideo = false
        config.needSetUI = false
        controller = SitAndReachController(config, mBinding.sitAndReachLayout)
        controller.callBack = object : SitAndReachCallBack {
            override fun onInitPlayGround(isSuccess: Boolean, msg: String?) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachSixActivity,
                        msg, Toast.LENGTH_SHORT
                    ).show()
                })
                controller.prepare()
                viewModel.toStatusIdentifyFace()

            }

            override fun onModelState(isSuccess: Boolean) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachSixActivity,
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
                val sb = StringBuilder()
                for (obj in sitAndReachObjectList) {
                    if (obj.skeleton != null) {
                        sb.append("道次: ").append(obj.getIndexNum())
                            .append("  方向: ").append(obj.getDirection())
                            .append("  成绩: ").append(obj.getDistanceMax())
                            .append("  成绩Live: ").append(obj.getDistanceLive())
                            .append("  手部: ").append(obj.getFingerDistance())
                            .append("  双手水平: ")
                            .append(if (obj.getHandParallel()) "达标" else "未水平")
                            .append("  腿部位置: ")
                            .append(if (obj.getFootPosition()) "达标" else "未在指定位置")
                            .append("  双腿伸直: ")
                            .append(if (obj.getLegParallel()) "达标" else "未伸直")
                            .append("\n")
                    }
                }

                AdlExecutor.postUI(Runnable {
                    LogUtil.d(sb.toString().trim { it <= ' ' })
                })
            }

            override fun onMessage(resultCodeType: ResultCodeType?) {
                LogUtil.d("消息: $resultCodeType")
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
        mBinding.btnSaveLayout.setOnClickListener {
            controller.saveOverlay()
        }
        mBinding.btnStartSport.setOnClickListener {
            controller.initPlayGround()

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
            }

            override fun onComplete() {

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
//                    mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportDoing)
                }
                mBinding.sportView.sportStarted()
            }
            controller.start()
            frameTime = System.currentTimeMillis()
            LogUtil.d("开始运动")
        }

        //一次测试完毕
        viewModel.testOnceFinishLD.observe(this) {
//            controller.reset()
            stopRecording()
            viewModel.uploadResult()
        }

        viewModel.sportTipsInfoLD.observe(this) {
            mBinding.sportView.sportingTips(it)
        }

        viewModel.sportResultLd.observe(this) {
            LogUtil.d("最终成绩：${it.first} -- ${it.second}")
            mBinding.sportView.addScore(it.first, it.second)
        }

    }


    private fun showDesc(msg: String) {
        if (isDebug.not()) return
        runOnUiThread {
            mBinding.tvDes.append(msg)
        }
    }

    private fun hideInTips() {
        tipsHide = true
        mBinding.groupInTip.visibility = View.GONE
        if (isModelOk) {
            startMatDetect()
        }
    }

    private fun initCamera() {
//        cameraDispatcher.setMirror(true)
        cameraDispatcher.addFrameProcessor(viewModel.onceCacheProcessor)
        cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
            override fun onFrame(yuv: ByteArray?, previewInfo: AdlCameraPreviewInfo) {
                controller.onFrame(yuv, previewInfo)
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
        mBinding.sitAndReachLayout.cameraPreviewView.startPreview(ipcHik, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {

            }

            override fun onCameraOpen() {
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
        mBinding.sitAndReachLayout.cameraPreviewView.addFrameProcessor(cameraDispatcher)
//        mBinding.sitAndReachLayout.cameraPreviewView.textureView.scaleX = -1f
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
     * 30帧
     */
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 70, 26, 18)
    }

    private fun initOverlay() {
        val isDebug = BusiBaseConfig.getDebugEnable()
        val skeletonAble = BusiBaseConfig.getSkeletonEnable()
        uiConfig = AdlUIConfig.instance()
            .readConfig(
                MultiCoreConstant.SIT_REACH_SIX_UI_KEY,
                FFlexionRoleConfig::class.java
            )
    }

    override fun onStatusIdentify() {
        LogUtil.d("开始人脸识别")
        mBinding.sportView.visibility = View.VISIBLE
        hideBackBtn()
    }

    override fun onStatusPrepare() {
        processorPrepare()
    }

    private fun releaseModel() {
        TsModelResPool.instance().markReleaseGroup(MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME)
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
        if (isInit || tipsHide.not()) return
        isInit = true
        hideLoadingDialog()
//        runOnUiThread {
//            mBinding.bntStart.performClick()
//        }
        controller.combineLayoutAndPrepare()
//        if (viewModel.streamType == 1) {
//            mBinding.root.postDelayed({
//                controller.initPlayGround()
//            }, 500)
        viewModel.toStatusIdentifyFace()
//        }
    }


    /**
     * 下一组
     */
    private fun next() {
        timeOutHelper.start()
        viewModel.reset()
        viewModel.toStatusIdentifyFace()
    }

    /**
     * 重测试
     */
    private fun retest() {
        controller.resetObject()
        mBinding.root.postDelayed({
            controller.prepare()
            controller.start()
            mBinding.sportView.visibility = View.VISIBLE
            viewModel.retest()
        }, 1000)
    }

    protected fun processorPrepare() {
        timeOutHelper.cancel()
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
