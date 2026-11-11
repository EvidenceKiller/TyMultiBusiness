package com.ty.bus.multi.core.ui.sport.sitreachsix

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
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
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.common.PointElement
import com.ty.bus.multi.databinding.ActivitySitReach6Binding
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import java.util.TreeSet


class SitReachSixActivity :
    BaseMultiSportActivity<ActivitySitReach6Binding, SitReachSixViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

    //    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/sitReach8point.mp4"
//    val videoFilePath = "/sdcard/com.adl.sport/app/20260826/1601010001/00000000000000000000000000000000/175148/1601010001_1787737908925.mp4"
//    val videoFilePath =
//        "/sdcard/com.adl.sport/app/20260831/1601010001/00000000000000000000000000000000/112349/1601010001_1788146629491.mp4"
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/20260825-094549.mp4"

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

    /** 当前不可用道次（有序），用于右上角提示 */
    private val unavailableLanes = TreeSet<Int>()

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
        LogUtil.d("SitReachSix::Activity.initData: enter")
        super.initData()
//        viewModel.streamType = 2
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.ffSportConfig =
            intent.getSerializableExtra("sport_config") as? ForwardFlexionConfig
        viewModel.initData()
        viewModel.getBestRankTop()
        LogUtil.d(
            "SitReachSix::Activity.initData: done, ptPlanId=${viewModel.ptPlanId}, " +
                "sceneId=${viewModel.sceneId}, streamType=${viewModel.streamType}, " +
                "sportSku=${viewModel.sportInfo?.sportSkuName}"
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        LogUtil.d("SitReachSix::Activity.initView: enter")
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

        if (isDebug) {
            mBinding.ivBg.visibility = View.GONE
        } else {
            mBinding.ivBg.visibility = View.VISIBLE
        }
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
        LogUtil.d(
            "SitReachSix::Activity.initView: done, streamType=${viewModel.streamType}, " +
                "supportNext=${viewModel.ffSportConfig?.isSupportNextGroup}"
        )
    }

    private fun initConfigAndController() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
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
        sitAndReachType.num = diskNum
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
                    Toast.makeText(
                        this@SitReachSixActivity,
                        msg, Toast.LENGTH_SHORT
                    ).show()
                })
//                controller.prepare()
//                viewModel.toStatusIdentifyFace()

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
                        sb.append("道次: ").append(obj.indexNum)
                            .append("  方向: ").append(obj.direction)
                            .append("  成绩(max): ").append(obj.distanceMax)
                            .append("  loose: ").append(obj.distanceMaxLoose)
                            .append("  live: ").append(obj.distanceLive)
                            .append("  手部: ").append(obj.fingerDistance)
                            .append("  双手水平: ")
                            .append(if (obj.handParallel) "达标" else "未水平")
                            .append("  腿部位置: ")
                            .append(if (obj.footPosition) "达标" else "未在指定位置")
                            .append("  双腿伸直: ")
                            .append(if (obj.legParallel) "达标" else "未伸直")
                            .append(" 运动是否结束：${obj.state.name}")
                            .append("  错误信息: ").append(obj.information)
                            .append("  是否可用：").append(obj.isCompliant)
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
                yuv?.let {
                    viewModel.onceCacheProcessor.onFrame(yuv, info)
                }
            }

            override fun onComplete() {

            }
        })
    }

    override fun initObserver() {
        LogUtil.d("SitReachSix::Activity.initObserver: enter")
        super.initObserver()
        viewModel.sportCardStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
//            updateUnavailableLanes(it.first, it.second)
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.sportView.setAccountInfo(it.first, it.second)
        }

        viewModel.sportStartLD.observe(this) {
            LogUtil.d("SitReachSix::Activity.sportStartLD: round=$it")
            startRecording()
            if (it == 1) {
                if (isDebug) {
//                    mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportDoing)
                }
                mBinding.sportView.sportStarted()
            }
            controller.start()
            frameTime = System.currentTimeMillis()
        }

        //一次测试完毕
        viewModel.testOnceFinishLD.observe(this) {
            LogUtil.d("SitReachSix::Activity.testOnceFinishLD: uploadResult")
//            controller.reset()
            stopRecording()
            viewModel.uploadResult()
        }

        viewModel.sportTipsInfoLD.observe(this) {
            mBinding.sportView.sportingTips(it)
        }

        viewModel.sportResultLd.observe(this) {
            LogUtil.d(
                "SitReachSix::Activity.sportResultLd: lane=${it.first}, " +
                    "count=${it.second.count}, status=${it.second.status}"
            )
            mBinding.sportView.addScore(it.first, it.second)
        }

        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }
        LogUtil.d("SitReachSix::Activity.initObserver: done")
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
        val cameraConfig =
            BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = true
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
     * 22帧
     */
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 70, 26, 16)
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
        LogUtil.d("SitReachSix::Activity.onStatusIdentifyFace")
        mBinding.sportView.visibility = View.VISIBLE
        hideBackBtn()
        SpeechWrapper.speech(getString(R.string.forward_flexion_identified_tip))
    }

    override fun onStatusPrepare() {
        processorPrepare()
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
        super.onStatusSport()
        controller.start()
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        LogUtil.d("SitReachSix::Activity.onStatusEnd -> sportFinish")
        mBinding.sportView.sportFinish()
    }

    override fun onResume() {
        LogUtil.d("SitReachSix::Activity.onResume")
        super.onResume()
        cameraLogin()
    }


    /**
     * 开始检测垫子
     */
    private fun startMatDetect() {
        if (isInit || tipsHide.not() || isFinishing) return
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
     * 根据卡片状态维护不可用道次，并刷新右上角提示。
     */
    private fun updateUnavailableLanes(lane: Int, state: ForwardFlexionCardState) {
        val changed = if (state == ForwardFlexionCardState.UNAVAILABLE) {
            unavailableLanes.add(lane)
        } else {
            unavailableLanes.remove(lane)
        }
        if (changed) {
            refreshUnavailableTip()
        }
    }

    private fun refreshUnavailableTip() {
        if (unavailableLanes.isEmpty()) {
            mBinding.tvUnavailableTip.visibility = View.GONE
            return
        }
        val lanesText = unavailableLanes.joinToString("、")
        mBinding.tvUnavailableTip.text = getString(R.string.position_unavailable_tip, lanesText)
        mBinding.tvUnavailableTip.visibility = View.VISIBLE
    }

    /**
     * 下一组
     */
    private fun next() {
        LogUtil.d("SitReachSix::Activity.next: reset controller + schedule identify in 1s")
        timeOutHelper.start()
        // 重置算法侧对象，避免残留 END 导致开测后立刻出上轮成绩并结束
        controller.resetObject()
        viewModel.reset()
        unavailableLanes.clear()
//        refreshUnavailableTip()
        // 对齐重测：铺板稳定后再进入识别，减少 isCompliant 短暂为 false 刷成不可用
        mBinding.root.postDelayed({
            LogUtil.d("SitReachSix::Activity.next: combineLayoutAndPrepare + toStatusIdentifyFace")
            controller.combineLayoutAndPrepare()
            viewModel.toStatusIdentifyFace()
        }, 1000)
    }

    /**
     * 重测试
     */
    private fun retest() {
        LogUtil.d("SitReachSix::Activity.retest")
        controller.resetObject()
        mBinding.root.postDelayed({
            controller.combineLayoutAndPrepare()
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
        LogUtil.d("SitReachSix::Activity.onPause")
        super.onPause()
        controller.reset()
        if (mBinding.sitAndReachLayout.cameraPreviewView != null) {
            mBinding.sitAndReachLayout.cameraPreviewView.stopPreview()
        }
    }

    override fun onDestroy() {
        LogUtil.d("SitReachSix::Activity.onDestroy")
        super.onDestroy()
        SpeechWrapper.stop()
        hideLoadingDialog()
        controller.release()
        if (mBinding.sitAndReachLayout.cameraPreviewView != null) {
            mBinding.sitAndReachLayout.cameraPreviewView.release()
        }
        cameraDispatcher.quit()
        videoReader?.release()
    }

}
