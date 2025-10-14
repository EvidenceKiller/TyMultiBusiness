package com.ty.bus.multi.core.ui.sport.forwardfelxion

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup.LayoutParams
import com.adl.base.config.AdlUIConfig
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResGroup
import com.adl.mount.core.TsModelResPool
import com.adl.mount.core.TsModelResPool.OnLoadModelCallbackSample
import com.adl.processor.SportProcessorStatus
import com.adl.sport.common.bean.Point
import com.adl.sport.forwardflexion.callback.FFlexionSportCallBack
import com.adl.sport.forwardflexion.config.FFlexionRoleConfig
import com.adl.sport.forwardflexion.config.FFlexionSportConfig
import com.adl.sport.forwardflexion.config.FFlexionSportResult
import com.adl.sport.forwardflexion.informCode.InformCode
import com.adl.sport.forwardflexion.processor.ForwardFlexionProcessor
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.common.PointElement
import com.ty.bus.multi.core.view.forwardflexion.ForwardFlexionResultView
import com.ty.bus.multi.core.view.forwardflexion.ForwardFlexionResultViewListener
import com.ty.bus.multi.core.view.forwardflexion.ForwardFlexionSportView
import com.ty.bus.multi.databinding.ActivityForwardFlexionBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil


class ForwardFlexionActivity :
    BaseMultiSportActivity<ActivityForwardFlexionBinding, ForwardFlexionViewModel>() {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var forwardFlexionProcessor: ForwardFlexionProcessor = ForwardFlexionProcessor()
    private var sportConfig: FFlexionSportConfig? = null

    private val pointElement by lazy { PointElement() } // 调试用的点绘制

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
        viewModel.initData()
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
        mainHandler.postDelayed(tipRunnable, 1000)

        initOverlay()
        initProcessor()
        initCamera()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.changeIdentityCardStateLD.observe(this) {
            mBinding.identityView.changeState(
                it,
                getString(R.string.forward_flexion_identified_tip)
            )
//            val imageView = imgMap[it.first]
//            if (it.second == MultiSportCardViewState.NOBODY) {
//                imageView?.visibility = View.VISIBLE
//            } else {
//                imageView?.visibility = View.GONE
//            }

            //已识别，设置已识别的位置给parser
//            if (it.second == MultiSportCardViewState.IDENTIFIED) {
//                setTip(factory.getReadyTip())
//                for (i in checkStatusList.indices) {
//                    checkStatusList[i] = false
//                }
//                LogUtil.d("======================>>>>>>>>>>>>>>>>>>>>>> ${viewModel.sportPersonArray.size()}")
//                viewModel.sportPersonArray.forEach { _, value ->
//                    val number = value.number
//                    if (number in checkStatusList.indices) {
//                        checkStatusList[number] = true
//                    }
//                }
//                parser.setCheckStatusList(checkStatusList)
//            }
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.identityView.setUserInfo(it)
        }

        viewModel.sportStartLD.observe(this) {
            if (it == 1) {
                mBinding.sportView.visibility = View.VISIBLE
                mBinding.sportView.setAccountInfo(viewModel.sportPersonArray)
                startRecording()
            }
            mBinding.identityView.visibility = View.GONE

            forwardFlexionProcessor.changeTo(SportProcessorStatus.Sporting)
        }

    }

    private fun hideInTips() {
        startMatDetect()
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
            override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
                p1?.let {
                    p1.frameTimestamp = p1.frameTimestamp / 1000_000
                }
                forwardFlexionProcessor.onFrame(p0, p1)
            }
        })
        mBinding.cameraView.addProcessor(viewModel.onceCacheProcessor)
        mBinding.cameraView.addProcessor(viewModel.sportEngine)
        mBinding.cameraView.setFps(30)
    }

    private fun initOverlay() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, true)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, true)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig: FFlexionRoleConfig? = AdlUIConfig.instance()
            .readConfig(mBinding.overlay.configKey, FFlexionRoleConfig::class.java)
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.enableTouch(false)
        if (isDebug) {
            mBinding.overlay.addElement(pointElement)
        }
    }

    private fun initProcessor() {
        val diskNum = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
        )
        // 运动配置
        sportConfig = FFlexionSportConfig.builder()
            .debug(true)
            .isRecord(true)
            .videoFps(30)
            .screenWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .screenHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .value(intArrayOf(diskNum, 60, 20, 110, 125))
            .isCheckFootLocation(false)
            .isCheckLegUncurl(false)
            .isCheckHandAlign(false)
            .build()

        mBinding.overlay.setMarkerAreaNum(diskNum)
        forwardFlexionProcessor.setConfig(sportConfig)
        forwardFlexionProcessor.setSportCallBack(object : FFlexionSportCallBack {
            override fun onResult(result: FFlexionSportResult) {
                forwardFlexionProcessor.onReset()
                runOnUiThread {
                    //显示成绩
                    result.grade.forEach {
                        mBinding.sportView.addScore(it.key, it.value)
                    }
                }
                viewModel.handleResult(result)
            }

            override fun onInform(code: InformCode?, error: String?) {
                LogUtil.d("$code:$error")
                if (code == InformCode.LANDMARKFINSIH || code == InformCode.LANDMARKFINSIHERR) {
                    viewModel.toStatusIdentifyFace()
                }
            }

            override fun onPostureStatus(status: MutableMap<Int, InformCode>) {
                viewModel.onPostureStatus(status)
//                for (entry in status.entries) {
//                    if (entry.value == InformCode.PREPAREFINSIH || entry.value == InformCode.PREPAREERR || entry.value == InformCode.NOTCHECKIN) {
//                        if (entry.value == InformCode.PREPAREFINSIH) {
//                            LogUtil.d(entry.key.toString() + "号准备完成")
//                        } else if (entry.value == InformCode.PREPAREERR) {
//                            LogUtil.d(entry.key.toString() + "号准备异常")
//                        } else if (entry.value == InformCode.NOTCHECKIN) {
//                            LogUtil.d(entry.key.toString() + "号未检录")
//                        }
//
////                        prepareIndex++
////
////                        if (diskNum === prepareIndex) {
////                            begin()
////                        }
//                    } else if (entry.value == InformCode.LEGUNCURLERR) {
//                        LogUtil.d(entry.key.toString() + "号腿未伸直")
//                    } else if (entry.value == InformCode.FOOTLOCATIONERR) {
//                        LogUtil.d(entry.key.toString() + "号脚位置不对")
//                    } else if (entry.value == InformCode.HANDALIGNERR) {
//                        LogUtil.d(entry.key.toString() + "号双手未对齐")
//                    }
//                }
            }

            override fun onBonePoints(bonePoints: MutableMap<Int, MutableList<Point>>) {
                bonePoints.forEach {
                    pointElement.setPoints(it.value)
                    pointElement.setRect(viewModel.getIdentityRect(it.value))
                    mBinding.overlay.refreshNow()
                }
                viewModel.onBonePoints(bonePoints)
            }
        })
        forwardFlexionProcessor.onInit()
        loadModel()
    }

    override fun onStatusIdentify() {
        LogUtil.d("开始人脸识别")
        mBinding.identityView.visibility = View.VISIBLE
        forwardFlexionProcessor.open(mBinding.overlay)
        forwardFlexionProcessor.updateCheckIn(HashMap<Int, Boolean>().apply { put(1, true) })
        forwardFlexionProcessor.changeTo(SportProcessorStatus.IdAuth)
    }

    override fun onStatusPrepare() {
        processorPrepare()
    }

    protected fun loadModel() {
        val tsModelRes = forwardFlexionProcessor.getTsModels()
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
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Idle)
        forwardFlexionProcessor.stopRecord()
        mBinding.sportView.visibility = View.GONE
        addResultView()
    }

    private fun addResultView() {
        val resultView = ForwardFlexionResultView(this, null)
        mBinding.clViewContainer.addView(
            resultView,
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST) {
            resultView.showNextBtn()
        }
        resultView.setSportResult(viewModel.sportPersonArray)
        resultView.listener = object : ForwardFlexionResultViewListener {
            override fun onRetestClick() {
                retest()
                mBinding.clViewContainer.removeView(resultView)
            }

            override fun onNextClick() {
                next()
                mBinding.clViewContainer.removeView(resultView)
            }

            override fun onEndTestClick() {
                finish()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mBinding.cameraView.onResume()
    }

    /**
     * 开始检测垫子
     */
    private fun startMatDetect() {
        mBinding.groupInTip.visibility = View.GONE
        forwardFlexionProcessor.open(mBinding.overlay)
        forwardFlexionProcessor.changeTo(SportProcessorStatus.MatDetect)
    }


    /**
     * 下一组
     */
    private fun next() {
        viewModel.reset()
        mBinding.identityView.reset()
        mBinding.sportView.reset()
        viewModel.toStatusIdentifyFace()
    }

    /**
     * 重测试
     */
    private fun retest() {
        forwardFlexionProcessor.onReset()
        mBinding.sportView.reset()
        viewModel.retest()
    }

    protected fun processorPrepare() {
        forwardFlexionProcessor.updateCheckIn(viewModel.checkMap)
        forwardFlexionProcessor.changeTo(SportProcessorStatus.Prepare)
    }

    /**
     * 开始录像
     */
    private fun startRecording() {
        val file = viewModel.getVideoPath(viewModel.sportInfo?.sportSkuId)
        forwardFlexionProcessor.startRecording(file)
    }

    override fun onDestroy() {
        super.onDestroy()
        forwardFlexionProcessor.stopRecord()
        forwardFlexionProcessor.onRelease();
        mBinding.cameraView.onRelease()
        releaseModel()
    }

}