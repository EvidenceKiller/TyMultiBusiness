package com.ty.bus.multi.core.ui.set.sitreachsixset

import android.graphics.ImageFormat
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlExecutor
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.element.ElementFpsCounter
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
import com.adl.sport.ballBase.ResultCodeType
import com.adl.sport.controllers.sitandreach.SitAndReachCallBack
import com.adl.sport.controllers.sitandreach.SitAndReachController
import com.adl.sport.controllers.sitandreach.SitAndReachControllerConfig
import com.adl.sport.sitandreach.SitAndReachConfig
import com.adl.sport.sitandreach.SitAndReachObject
import com.adl.sport.sitandreach.SitAndReachType
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.ui.sport.sitreachsix.SitReachSixActivity
import com.ty.bus.multi.databinding.ActivitySitReachSixSettingBinding
import com.ty.forwardflexion.callback.FFlexionSportCb
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.forwardflexion.config.FFlexionSportResult
import com.ty.forwardflexion.informCode.InformCode
import com.ty.forwardflexion.processor.ForwardFlexionProcessor
import com.ty.sport.common.basic.bean.Point
import com.ty.sport.common.sport.processor.SportConfig
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil


class SitReachSixSettingActivity :
    BaseActivity<ActivitySitReachSixSettingBinding, BaseViewModel>() {
    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

    //    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/sitReach8point.mp4"
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/sit6人_1080p.mp4"
    val streamType = 1 //1:camera , 2: video

    /***************************视频播放调试*************************/
    private var testPeopleCount = 0
    private var testTimes = 0
    private var minPeopleCount = 1
    private var maxPeopleCount = 6

    private var minTestTimes = 1
    private var maxTestTimes = 3

    private var isModelLoadOk = false

    private lateinit var controller: SitAndReachController

    private val cameraDispatcher by lazy {
        DynamicSportFrameDispatcher(ElementFpsCounter("Camera").apply {
            this.setPosition(
                0f,
                0f
            )
        })
    }

    private lateinit var ipcHik: NsCameraHKSource
    override fun initObserver() {

    }

    override fun initData() {
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
            testPeopleCount = MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
            testTimes = MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES_DEFAULT
            mBinding.tvNumber.text = testPeopleCount.toString()
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            MMKVUtil.encode(MultiCoreConstant.SIT_REACH_SIX_TEST_TIMES, testTimes)
            mBinding.sitAndReachLayout.markerRecognizerOverlay.restoreSportSetting()
            controller.saveOverlay()
            updateOverlay()
        }

        mBinding.tvConfirm.setOnClickListener {
            controller.saveOverlay()
            finish()
        }
        initOverlay()
        initConfigAndController()
        if (streamType == 1) {
            initCamera()
        } else {
            initVideo()
            initVideoView()
        }
    }

    private fun updateOverlay() {
        controller.reset()
        controller.initPlayGround()
    }

    private fun initConfigAndController() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.SIT_REACH_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        showLoadingDialog(getString(R.string.model_loding))
        mBinding.sitAndReachLayout.setInputSource(SitAndReachControllerConfig.InputSource.CAMERA)
        val config = SitAndReachControllerConfig()
        config.drawSkeleton = true
        config.inputSource =
            if (streamType == 1) SitAndReachControllerConfig.InputSource.CAMERA else SitAndReachControllerConfig.InputSource.VIDEO
        config.checkMethod = SitAndReachConfig.CheckMethod.TWO_HANDS
        val sitAndReachType = SitAndReachType.SIT_AND_REACH_CUSTOM
        sitAndReachType.num = diskNum
        sitAndReachType.boardNum = 8
        config.sitAndReachType = sitAndReachType
        config.debug = true
        config.enableRecordVideo = false
        config.needSetUI = true
        controller = SitAndReachController(config, mBinding.sitAndReachLayout)
        controller.callBack = object : SitAndReachCallBack {
            override fun onInitPlayGround(isSuccess: Boolean, msg: String?) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachSixSettingActivity,
                        msg, Toast.LENGTH_SHORT
                    ).show()
                })
                controller.prepare()
            }

            override fun onModelState(isSuccess: Boolean) {
                AdlExecutor.postUI(Runnable {
                    Toast.makeText(
                        this@SitReachSixSettingActivity,
                        "模型加载: " + (if (isSuccess) "成功" else "失败"), Toast.LENGTH_LONG
                    ).show()
                })
                isModelLoadOk = true
                controller.initPlayGround()
                hideLoadingDialog()
            }

            override fun onResult(sitAndReachObjectList: MutableList<SitAndReachObject>?) {
            }

            override fun onMessage(resultCodeType: ResultCodeType?) {
            }
        }
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

    private fun initCamera() {
        cameraDispatcher.setMirror(true)
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
        ipcHik.login(object : AdlCameraListener {
            override fun onOpenSucceed(p0: Boolean) {

            }

            override fun onLoginSucceed() {
                changeResolution()
                SystemClock.sleep(100)
                runOnUiThread {
                    startCameraPreview()
                }
            }

            override fun onLoginFail() {
            }

            override fun onOpenStatus(p0: String?) {

            }

            override fun onError(p0: Exception?) {
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
        mBinding.sitAndReachLayout.cameraPreviewView.addFrameProcessor(cameraDispatcher)
        mBinding.sitAndReachLayout.cameraPreviewView.textureView.scaleX = -1f
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 27, 26, 14)
    }

    private fun startMat() {
        // 运动配置
//        controller.reset()
        updateOverlay()
    }

    private fun initOverlay() {
    }


    private fun releaseModel() {
        TsModelResPool.instance().markReleaseGroup(MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME)
    }


    override fun onResume() {
        super.onResume()
        if (streamType == 1) {
            cameraLogin()
        }
    }

    override fun onPause() {
        super.onPause()
        controller.reset()
        mBinding.sitAndReachLayout.cameraPreviewView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingDialog()
        controller.release()
        mBinding.sitAndReachLayout.cameraPreviewView.release()
        cameraDispatcher.quit()
        releaseModel()
    }

}
