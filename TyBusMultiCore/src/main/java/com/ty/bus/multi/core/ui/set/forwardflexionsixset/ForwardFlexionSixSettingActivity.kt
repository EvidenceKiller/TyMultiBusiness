package com.ty.bus.multi.core.ui.set.forwardflexionsixset

import android.graphics.ImageFormat
import android.os.Bundle
import android.os.SystemClock
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
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.databinding.ActivityForwardFlexionSixSettingBinding
import com.ty.forwardflexion.callback.FFlexionSportCb
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.forwardflexion.config.FFlexionSportResult
import com.ty.forwardflexion.informCode.InformCode
import com.ty.forwardflexion.processor.ForwardFlexionProcessor
import com.ty.sport.common.basic.bean.Point
import com.ty.sport.common.sport.processor.SportConfig
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil


class ForwardFlexionSixSettingActivity :
    BaseActivity<ActivityForwardFlexionSixSettingBinding, BaseViewModel>() {

    private var testPeopleCount = 0
    private var testTimes = 0
    private var minPeopleCount = 1
    private var maxPeopleCount = 6

    private var minTestTimes = 1
    private var maxTestTimes = 3

    private var isModelLoadOk = false

    private var forwardFlexionProcessor: ForwardFlexionProcessor = ForwardFlexionProcessor()
    private var sportConfig: SportConfig? = null

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
            MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER_DEFAULT
        )
        testTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_SIX_TEST_TIMES,
            MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
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
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivNumberAdd.setOnClickListener {
            if (testPeopleCount == maxPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount++
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivTimeMinus.setOnClickListener {
            if (testTimes == minTestTimes) {
                return@setOnClickListener
            }
            testTimes--
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_TIMES, testTimes)
        }

        mBinding.ivTimeAdd.setOnClickListener {
            if (testTimes == maxTestTimes) {
                return@setOnClickListener
            }
            testTimes++
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_TIMES, testTimes)
        }

        mBinding.tvReset.setOnClickListener {
            testPeopleCount = MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER_DEFAULT
            testTimes = MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
            mBinding.tvNumber.text = testPeopleCount.toString()
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_PEOPLE_NUMBER, testPeopleCount)
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_SIX_TEST_TIMES, testTimes)
            mBinding.overlay.setConfig(FFlexionRoleConfig(), OnRenderScene.StatusSportSet)
            updateOverlay()
            mBinding.overlay.saveConfig()
        }

        mBinding.tvConfirm.setOnClickListener {
            mBinding.overlay.saveConfig()
            finish()
        }
        initOverlay()
        initProcessor()
        initCamera()
    }

    private fun initCamera() {
        cameraDispatcher.setMirror(true)
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
        val cameraConfig = BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
        val hikSubConfig = AdlCameraConfig.builder()
        hikSubConfig.isPreviewEnable = true
        ipcHik = hikSubConfig
            .setPreviewFormat(ImageFormat.YV12)
            .setCameraType(AdlCameraConfig.CameraType.HK).setConnect(
                cameraConfig.ip,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
                cameraConfig.account,
                cameraConfig.psw
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
        mBinding.cameraView.startPreview(ipcHik, object :
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
        mBinding.cameraView.addFrameProcessor(cameraDispatcher)
        mBinding.cameraView.textureView.scaleX = -1f
    }

    /**
     * 设置摄像头分辨率；re:64(3840X2440), 27(1920x1080),70(2560X1440)
     */
    private fun changeResolution() {
        val deviceItem = ipcHik.deviceItem
        if (deviceItem.m_lUserID == -1) return
        DevConfigGuider.setCompressionResolutionCfg(deviceItem.m_lUserID, 64, 26, 14)
    }

    private fun startMat() {
        // 运动配置
        sportConfig = SportConfig.builder()
            .debug(true)
            .record(false)
            .videoDevice(2)
            .videoFps(15)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_HEIGHT)
            .valueInt(intArrayOf(testPeopleCount, 60, 20, 110, 125, 1))
            .valueBool(booleanArrayOf(false, false, false))
            .build()
        forwardFlexionProcessor.setConfig(sportConfig)
        forwardFlexionProcessor.open(mBinding.overlay)
        forwardFlexionProcessor.changeTo(
            SportProcessorStatus.MatDetect
        )
    }

    private fun initOverlay() {
        mBinding.overlay.setMarkerAreaNum(testPeopleCount)
        val uiConfig: FFlexionRoleConfig? = AdlUIConfig.instance()
            .readConfig(
                MultiCoreConstant.FORWARD_FLEXION_SIX_UI_KEY,
                FFlexionRoleConfig::class.java
            )
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
    }

    private fun initProcessor() {
        sportConfig = SportConfig.builder()
            .debug(true)
            .record(false)
            .videoDevice(2)
            .videoFps(15)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_4K_HEIGHT)
            .valueInt(intArrayOf(testPeopleCount, 60, 20, 110, 125))
            .valueBool(booleanArrayOf(false, false, false))
            .build()
        forwardFlexionProcessor.setConfig(sportConfig)
        forwardFlexionProcessor.setSportCallBack(object : FFlexionSportCb {
            override fun onResult(result: FFlexionSportResult) {
            }

            override fun onInform(code: InformCode?, error: String?) {
                LogUtil.d("$code:$error")
                if (code == InformCode.LANDMARKFINSIH || code == InformCode.LANDMARKFINSIHERR) {
                    ToastUtil.getInstance(
                        this@ForwardFlexionSixSettingActivity,
                        if (code == InformCode.LANDMARKFINSIH) getString(R.string.forward_flexion_auto_mat_success) else getString(
                            R.string.forward_flexion_auto_mat_fail
                        )
                    ).show()
                    forwardFlexionProcessor.onReset()
                    forwardFlexionProcessor.changeTo(SportProcessorStatus.Idle)
                }
            }

            override fun onPostureStatus(status: MutableMap<Int, InformCode>) {
            }

            override fun onBonePoints(p0: Map<Int?, FFlexionSportCb.FFlexionBody?>?) {
            }

        })
        forwardFlexionProcessor.onInit()
        loadModel()
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
                    }

                    override fun onSucceed(list: MutableList<TsModelInfo>?) {
                        isModelLoadOk = true
                        ToastUtil.getInstance(
                            this@ForwardFlexionSixSettingActivity,
                            getString(R.string.forward_flexion_model_load_complete)
                        ).show()
                    }

                })

    }

    private fun releaseModel() {
        TsModelResPool.instance().markReleaseGroup(MultiCoreConstant.FORWARD_FLEXION_MODEL_NAME)
    }

    private fun updateOverlay() {
        mBinding.overlay.setMarkerAreaNum(testPeopleCount)
        mBinding.overlay.updateConfig()
        mBinding.overlay.refresh()
    }

    override fun onResume() {
        super.onResume()
        cameraLogin()
    }

    override fun onPause() {
        super.onPause()
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Done)
        mBinding.cameraView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingDialog()
        mBinding.cameraView.release()
        Thread {
            forwardFlexionProcessor.onRelease()
        }.start()
        mBinding.overlay.release()
        cameraDispatcher.quit()
        releaseModel()
    }

}