package com.ty.bus.multi.core.ui.set.sitreacheighteenset

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlExecutor
import com.adl.base.common.AdlPreferences
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.element.ElementFpsCounter
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraListener
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.sport.ballBase.ResultCodeType
import com.adl.sport.controllers.sitandreach.SitAndReachCallBack
import com.adl.sport.controllers.sitandreach.SitAndReachController
import com.adl.sport.controllers.sitandreach.SitAndReachControllerConfig
import com.adl.sport.markerrecognizer.MarkerRecognizerConfig
import com.adl.sport.sitandreach.SitAndReachConfig
import com.adl.sport.sitandreach.SitAndReachObject
import com.adl.sport.sitandreach.SitAndReachType
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.databinding.ActivitySitReachEighteenSettingBinding
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.expand.toJson
import com.ty.tybusbase.expand.toObject
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.grpc.GrpcCmdCode
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil
import com.ty.tybusbase.view.DirectionPadView

/**
 * 18人坐位体前屈设置页面：主屏 + 左副屏 + 右副屏，每屏6人。
 *
 * 在三屏框体设置（ThirtySportSettingActivity）的基础上，
 * 叠加六人坐位体前屈（SitReachSixSettingActivity）的铺板与道次/测试次数设置：
 * - 顶部下拉框在"智慧屏多人运动 / 微操场左区 / 微操场右区"之间切换，仅显示已连接的副屏
 * - 切换区域时自动切换对应屏幕的摄像头画面，并加载该区域独立的框体配置
 * - 主屏保存即生效；副屏保存后通过 gRPC 将区域配置下发到对应副屏
 */
class SitReachEighteenSettingActivity :
    BaseActivity<ActivitySitReachEighteenSettingBinding, BaseViewModel>() {
    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/com.adl.sport/testDebugVideo/body_bend_ai_six/坐位体前屈6人2k.mp4"
    val streamType = 2 // todo 1:camera , 2: video

    /***************************视频播放调试*************************/

    private var testPeopleCount = 0
    private var testTimes = 0
    private var minPeopleCount = 1
    private var maxPeopleCount = 6

    private var minTestTimes = 1
    private var maxTestTimes = 3

    private var isModelLoadOk = false

    //板子几个点
    private var boardNum = 8

    private val controller: SitAndReachController by lazy {
        SitAndReachController(sportConfig, mBinding.sitAndReachLayout)
    }

    private val sportConfig by lazy {
        val modelGroupName = SitAndReachType.SIT_AND_REACH_CUSTOM.sportName
        val sourceType = if (streamType == 1)
            SitAndReachControllerConfig.InputSource.CAMERA
        else
            SitAndReachControllerConfig.InputSource.VIDEO

        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )

        val config = SitAndReachControllerConfig()
        config.drawSkeleton = BusiBaseConfig.getSkeletonEnable()
        config.inputSource = sourceType
        config.checkMethod = SitAndReachConfig.CheckMethod.TWO_HANDS
        val sitAndReachType = SitAndReachType.SIT_AND_REACH_CUSTOM.also {
            when (currentMode) {
                AreaMode.LEFT -> it.sportName = "${it.sportName}_left"
                AreaMode.RIGHT -> it.sportName = "${it.sportName}_right"
                AreaMode.MAIN -> {}
            }
        }
        sitAndReachType.num = diskNum
        sitAndReachType.boardNum = boardNum
        config.sitAndReachType = sitAndReachType
        config.debug = BusiBaseConfig.getDebugEnable()
        config.enableRecordVideo = false
        config.needSetUI = true
        config.modelGroupName = modelGroupName
        config
    }

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(0f, 0f)
        })
    }

    //是否已添加取流帧分发，避免切换区域重复添加
    private var frameProcessorAdded = false

    private val dxStep = 2

    //--------------------- 三屏区域 ---------------------
    private enum class AreaMode {
        MAIN, LEFT, RIGHT
    }

    private var currentMode = AreaMode.MAIN

    private var areaPopup: PopupWindow? = null
    private val areaOptions by lazy {
        resources.getStringArray(R.array.thirty_sport_area_options)
    }
    //--------------------- 三屏区域 ---------------------

    override fun initObserver() {

    }

    override fun initData() {
        currentMode = when (intent.getStringExtra("mode")) {
            "left" -> AreaMode.LEFT
            "right" -> AreaMode.RIGHT
            else -> AreaMode.MAIN
        }
        testPeopleCount = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        testTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES,
            MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES_DEFAULT
        )
    }

    override fun initView(savedInstanceState: Bundle?) {

        mBinding.tvNumber.text = testPeopleCount.toString()
        mBinding.tvTime.text = testTimes.toString()

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.tvShowMat.setOnClickListener {
            controller.combineLayoutAndPrepare()
        }

        mBinding.tvAutoMat.setOnClickListener {
            if (isModelLoadOk.not()) {
                ToastUtil.getInstance(this, getString(R.string.forward_flexion_model_loading))
                    .show()
                return@setOnClickListener
            }
            startMat()
        }

        mBinding.ivNumberMinus.setOnClickListener {
            if (testPeopleCount == minPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount--
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivNumberAdd.setOnClickListener {
            if (testPeopleCount == maxPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount++
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivTimeMinus.setOnClickListener {
            if (testTimes == minTestTimes) {
                return@setOnClickListener
            }
            testTimes--
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES, testTimes)
        }

        mBinding.ivTimeAdd.setOnClickListener {
            if (testTimes == maxTestTimes) {
                return@setOnClickListener
            }
            testTimes++
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES, testTimes)
        }

        mBinding.tvReset.setOnClickListener {
            resetCurrentArea()
        }

        mBinding.tvConfirm.setOnClickListener {
            saveConfig()
            finish()
        }

        initAreaSpinner()
        initMoveBtn()
        initConfigAndController()
        if (streamType == 1) {
            openCamera(currentMode)
        } else {
            initVideo()
            initVideoView()
        }
    }

    private fun initMoveBtn() {
        mBinding.directView.setOnDirectionClickListener(object :
            DirectionPadView.OnDirectionClickListener {
            override fun onMoveUp() {
                mBinding.sitAndReachLayout.markerRecognizerOverlay.moveSelectedPoint(0, -dxStep)
            }

            override fun onMoveDown() {
                mBinding.sitAndReachLayout.markerRecognizerOverlay.moveSelectedPoint(0, dxStep)
            }

            override fun onMoveLeft() {
                mBinding.sitAndReachLayout.markerRecognizerOverlay.moveSelectedPoint(-dxStep, 0)
            }

            override fun onMoveRight() {
                mBinding.sitAndReachLayout.markerRecognizerOverlay.moveSelectedPoint(dxStep, 0)
            }

        })
    }

    private fun updateOverlay() {
        mBinding.sitAndReachLayout.markerRecognizerOverlay.saveConfig()
        controller.reset()
    }

    /**
     * 重置当前区域的框体配置
     */
    private fun resetCurrentArea() {
        testPeopleCount = MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        testTimes = MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES_DEFAULT
        mBinding.tvNumber.text = testPeopleCount.toString()
        mBinding.tvTime.text = testTimes.toString()
        MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
        MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES, testTimes)
        mBinding.sitAndReachLayout.markerRecognizerOverlay.restoreSportSetting()
        updateOverlay()
        ToastUtil.getInstance(this, getString(R.string.reset_successful)).show()
    }

    /**
     * 保存当前区域配置：
     * 主屏直接保存；副屏保存后通过 gRPC 将区域配置下发到对应副屏
     */
    private fun saveConfig() {
        mBinding.sitAndReachLayout.markerRecognizerOverlay.saveConfig()
        if (currentMode == AreaMode.MAIN) {
            ToastUtil.getInstance(this, getString(R.string.save_successful)).show()
            return
        }
        val subScreenIp = when (currentMode) {
            AreaMode.LEFT -> BusiBaseConfig.readLeftSubScreenIp()
            AreaMode.RIGHT -> BusiBaseConfig.readRightSubScreenIp()
            else -> null
        }
        val clientId = subScreenIp?.let { GrpcManager.getClientIdByIp(it) }
        if (clientId.isNullOrEmpty()) {
            ToastUtil.getInstance(this, getString(R.string.save_failed)).show()
            return
        }

        val markerRecognizerConfig =
            AdlPreferences.initPreferences(this).getString(sportConfig.sitAndReachType.sportName)
        val markerRecognizerRectanglesConfig = AdlPreferences.initPreferences(this)
            .getString(sportConfig.sitAndReachType.sportName + "_recheckRectangles") ?: ""

        //  修改 markerRecognizerName
        val markerRecognizerConfigObj = markerRecognizerConfig.toObject<MarkerRecognizerConfig>()
        markerRecognizerConfigObj?.markerRecognizerName = getSitAndReachTypeSportNameDefault()
        val markerRecognizerConfigModify = markerRecognizerConfigObj?.toJson() ?: ""

        Log.e(
            "zjw",
            "markerRecognizerConfigModify:${markerRecognizerConfigModify}"
        )

        Log.e(
            "zjw",
            "markerRecognizerRectanglesConfig:${markerRecognizerRectanglesConfig}"
        )

        val payload = "${markerRecognizerConfigModify}&&${markerRecognizerRectanglesConfig}"
        GrpcManager.sendToClient(
            clientId,
            GrpcCmdCode.CMD_SIX_SIT_REACH_REGION_SETTING,
            payload,
            true,
            {
                ToastUtil.getInstance(
                    this@SitReachEighteenSettingActivity,
                    getString(R.string.save_successful)
                ).show()
            },
            { _, _ ->
                ToastUtil.getInstance(
                    this@SitReachEighteenSettingActivity,
                    getString(R.string.save_failed)
                ).show()
            }
        )
    }

    //--------------------- 三屏区域切换 ---------------------

    /**
     * 初始化顶部区域切换下拉框：仅展示已连接的副屏，未连接时保留主屏设置
     */
    private fun initAreaSpinner() {
        val modes = availableAreaModes()
        if (modes.size <= 1) {
            mBinding.llAreaSelector.visibility = View.GONE
            currentMode = AreaMode.MAIN
            return
        }
        mBinding.llAreaSelector.visibility = View.VISIBLE
        updateAreaText(currentMode)
        updateAreaArrow(false)
        mBinding.llAreaSelector.setOnClickListener {
            if (areaPopup?.isShowing == true) {
                areaPopup?.dismiss()
            } else {
                showAreaPopup(modes)
            }
        }
    }

    private fun availableAreaModes(): List<AreaMode> {
        val modes = mutableListOf(AreaMode.MAIN)
        if (isSubScreenConnected(BusiBaseConfig.readLeftSubScreenIp())) {
            modes.add(AreaMode.LEFT)
        }
        if (isSubScreenConnected(BusiBaseConfig.readRightSubScreenIp())) {
            modes.add(AreaMode.RIGHT)
        }
        return modes
    }

    private fun isSubScreenConnected(ip: String?): Boolean =
        !ip.isNullOrEmpty() && GrpcManager.getClientIdByIp(ip) != null

    private fun showAreaPopup(modes: List<AreaMode>) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_area_popup)
        }
        val checkedColor = Color.parseColor("#FDD401")
        modes.forEach { mode ->
            val item = LayoutInflater.from(this)
                .inflate(R.layout.item_area_option, content, false) as TextView
            item.text = areaOptions[mode.ordinal]
            item.setTextColor(if (mode == currentMode) checkedColor else Color.WHITE)
            item.setOnClickListener {
                areaPopup?.dismiss()
                onAreaModeSelected(mode)
            }
            content.addView(item)
        }
        areaPopup = PopupWindow(
            content,
            mBinding.llAreaSelector.width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(0))
            setOnDismissListener {
                updateAreaArrow(false)
                areaPopup = null
            }
        }
        updateAreaArrow(true)
        val yOffset = (8 * resources.displayMetrics.density).toInt()
        areaPopup?.showAsDropDown(mBinding.llAreaSelector, 0, yOffset)
    }

    private fun onAreaModeSelected(mode: AreaMode) {
        updateAreaText(mode)
        if (mode != currentMode) {
            switchArea(mode)
        }
    }

    private fun updateAreaText(mode: AreaMode) {
        mBinding.tvAreaSelected.text = areaOptions[mode.ordinal]
    }

    private fun updateAreaArrow(open: Boolean) {
        mBinding.ivAreaArrow.setImageResource(
            if (open) R.drawable.ic_area_arrow_up else R.drawable.ic_area_arrow_down
        )
    }

    /**
     * 切换区域：停止预览、保存原区域配置，再加载新区域的摄像头与框体配置
     */
    private fun switchArea(mode: AreaMode) {
        mBinding.sitAndReachLayout.markerRecognizerOverlay.saveConfig()
        currentMode = mode
        stopPreview()
        //  todo 直接重启页面设置
//        if (streamType == 1) {
//            openCamera(mode)
//        }
//        reloadOverlay()

        val mode = when (mode) {
            AreaMode.MAIN -> null
            AreaMode.LEFT -> "left"
            AreaMode.RIGHT -> "right"
        }
        intent.putExtra("mode", mode)
        finish()
        startActivity(intent)
    }

    private fun getCameraKey(mode: AreaMode): String = when (mode) {
        AreaMode.MAIN -> MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY
        AreaMode.LEFT -> MMKVBusiKey.LEFT_SUB_SCREEN_IPC_CAMERA_KEY
        AreaMode.RIGHT -> MMKVBusiKey.RIGHT_SUB_SCREEN_IPC_CAMERA_KEY
    }

    //--------------------- 三屏区域切换 ---------------------
    private fun initConfigAndController() {
        showLoadingDialog(getString(R.string.model_loding))

        mBinding.sitAndReachLayout.setInputSource(sportConfig.inputSource)

        controller.callBack = object : SitAndReachCallBack {
            override fun onInitPlayGround(isSuccess: Boolean, msg: String?) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachEighteenSettingActivity,
                        msg, Toast.LENGTH_SHORT
                    ).show()
                })
            }

            override fun onModelState(isSuccess: Boolean) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachEighteenSettingActivity,
                        "模型加载: " + (if (isSuccess) "成功" else "失败"), Toast.LENGTH_LONG
                    ).show()
                })
                isModelLoadOk = true
                hideLoadingDialog()
            }

            override fun onResult(sitAndReachObjectList: MutableList<SitAndReachObject>?) {
            }

            override fun onMessage(resultCodeType: ResultCodeType?) {
            }
        }
        controller.combineLayout()
    }

    private fun initVideoView() {
        mBinding.layout.visible()
        mBinding.llBtn.visible()
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
            mBinding.sitAndReachLayout.markerRecognizerOverlay.saveConfig()
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

    /**
     * 打开指定区域的摄像头：主屏/左副屏/右副屏各自独立的IPC摄像头配置
     */
    private fun openCamera(mode: AreaMode) {
        val cameraConfig = BusiBaseConfig.readIpcCameraConfig(getCameraKey(mode))
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = true
        val ipcHik = hikSubConfig
            .setCameraType(AdlCameraConfig.CameraType.HK)
            .setConnect(
                cameraConfig.ip,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
                cameraConfig.account,
                cameraConfig.psw
            )
            .setStreamType(0)
            .build() as NsCameraHKSource
        ipcHik.login(object : AdlCameraListener {
            override fun onOpenSucceed(p0: Boolean) {
            }

            override fun onLoginSucceed() {
                changeResolution(ipcHik)
                SystemClock.sleep(100)
                runOnUiThread {
                    startCameraPreview(ipcHik)
                }
            }

            override fun onLoginFail() {
                LogUtil.e("摄像头登录失败：${cameraConfig.ip}")
            }

            override fun onOpenStatus(p0: String?) {
            }

            override fun onError(p0: Exception?) {
                LogUtil.e("摄像头异常：${p0?.message}")
            }

            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
    }

    private fun startCameraPreview(ipcHik: NsCameraHKSource) {
        val previewView = mBinding.sitAndReachLayout.cameraPreviewView
        previewView.startPreview(ipcHik, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {
            }

            override fun onCameraOpen() {
            }

            override fun onCameraReOpen() {
            }

            override fun onDestroyed() {
            }

            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
        if (frameProcessorAdded.not()) {
            cameraDispatcher.addFrameProcessor(object : AdlCameraFrameProcessor {
                override fun onFrame(yuv: ByteArray?, previewInfo: AdlCameraPreviewInfo) {
                    controller.onFrame(yuv, previewInfo)
                }
            })
            cameraDispatcher.setMirror(false)
            previewView.addFrameProcessor(cameraDispatcher)
            frameProcessorAdded = true
        }
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun changeResolution(ipcHik: NsCameraHKSource) {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem == null || deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 70, 26, 16)
    }

    private fun startMat() {
        controller.initPlayGround()
    }

    private fun restSitAndReachTypeSportName() {
        SitAndReachType.SIT_AND_REACH_CUSTOM.sportName = getSitAndReachTypeSportNameDefault()
    }

    private fun getSitAndReachTypeSportNameDefault(): String {
        return SitAndReachType.SIT_AND_REACH_CUSTOM.sportName.replace("_left", "")
            .replace("_right", "")
    }

    private fun stopPreview() {
        mBinding.sitAndReachLayout.cameraPreviewView?.stopPreview()
        mBinding.sitAndReachLayout.cameraPreviewView?.release()
    }

    override fun onPause() {
        super.onPause()
        controller.reset()
        stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        restSitAndReachTypeSportName()
        hideLoadingDialog()
        controller.release()
        cameraDispatcher.quit()
        videoReader?.release()
    }
}
