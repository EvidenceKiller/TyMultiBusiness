package com.ty.bus.multi.core.ui.set.forwardflexionset

import android.os.Bundle
import com.adl.base.config.AdlUIConfig
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResGroup
import com.adl.mount.core.TsModelResPool
import com.adl.mount.core.TsModelResPool.OnLoadModelCallbackSample
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.databinding.ActivityForwardFlexionSettingBinding
import com.ty.forwardflexion.callback.FFlexionSportCb
import com.ty.forwardflexion.config.FFlexionRoleConfig
import com.ty.forwardflexion.config.FFlexionSportResult
import com.ty.forwardflexion.informCode.InformCode
import com.ty.forwardflexion.processor.ForwardFlexionProcessor
import com.ty.forwardflexion.processor.ForwardFlexionSportOverlay
import com.ty.sport.common.basic.bean.Point
import com.ty.sport.common.sport.processor.SportConfig
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil


class ForwardFlexionSettingActivity :
    BaseActivity<ActivityForwardFlexionSettingBinding, BaseViewModel>() {

    private var testPeopleCount = 0
    private var testTimes = 0
    private var minPeopleCount = 1
    private var maxPeopleCount = 3

    private var minTestTimes = 1
    private var maxTestTimes = 3

    private var isModelLoadOk = false

    private var forwardFlexionProcessor: ForwardFlexionProcessor = ForwardFlexionProcessor()
    private var sportConfig: SportConfig? = null
    override fun initObserver() {

    }

    override fun initData() {
        testPeopleCount = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
        )
        testTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_TIMES,
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
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivNumberAdd.setOnClickListener {
            if (testPeopleCount == maxPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount++
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivTimeMinus.setOnClickListener {
            if (testTimes == minTestTimes) {
                return@setOnClickListener
            }
            testTimes--
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
        }

        mBinding.ivTimeAdd.setOnClickListener {
            if (testTimes == maxTestTimes) {
                return@setOnClickListener
            }
            testTimes++
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
        }

        mBinding.tvReset.setOnClickListener {
            testPeopleCount = MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
            testTimes = MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
            mBinding.tvNumber.text = testPeopleCount.toString()
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
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

    private fun startMat() {
        // 运动配置
        sportConfig = SportConfig.builder()
            .debug(true)
            .record(false)
            .videoFps(30)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .valueInt(intArrayOf(testPeopleCount, 60, 20, 110, 125,1))
            .valueBool(booleanArrayOf(false,false,false))
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
            .readConfig(mBinding.overlay.configKey, FFlexionRoleConfig::class.java)
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBinding.cameraView.addProcessor(object : AdlCameraFrameProcessor {
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
    }

    private fun initProcessor() {
        sportConfig = SportConfig.builder()
            .debug(true)
            .record(false)
            .videoFps(30)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .valueInt(intArrayOf(testPeopleCount, 60, 20, 110, 125,1))
            .valueBool(booleanArrayOf(false,false,false))
            .build()
        forwardFlexionProcessor.setConfig(sportConfig)
        forwardFlexionProcessor.setSportCallBack(object : FFlexionSportCb {
            override fun onResult(result: FFlexionSportResult) {
            }

            override fun onInform(code: InformCode?, error: String?) {
                LogUtil.d("$code:$error")
                if (code == InformCode.LANDMARKFINSIH || code == InformCode.LANDMARKFINSIHERR) {
                    ToastUtil.getInstance(
                        this@ForwardFlexionSettingActivity,
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

            override fun onBonePoints(bonePoints: MutableMap<Int, MutableList<Point>>) {
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
                            this@ForwardFlexionSettingActivity,
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
        mBinding.cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.cameraView.onRelease()
        forwardFlexionProcessor.onRelease()
        mBinding.overlay.release()
        releaseModel()
    }

}