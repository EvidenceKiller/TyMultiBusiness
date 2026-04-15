package com.ty.bus.multi.core.ui.sport.forwardfelxion

import android.graphics.Rect
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
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.common.PointElement
import com.ty.bus.multi.core.view.forwardflexion.ForwardFlexionResultView
import com.ty.bus.multi.databinding.ActivityForwardFlexionBinding
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
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.DisplayUtil
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil


open class ForwardFlexionActivity :
    BaseMultiSportActivity<ActivityForwardFlexionBinding, ForwardFlexionViewModel>() {

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
                getString(R.string.forward_flexion_prepare_tip)
            )
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.identityView.setUserInfo(it)
        }

        viewModel.sportStartLD.observe(this) {
            if (it == 1) {
                mBinding.sportView.visibility = View.VISIBLE
                mBinding.sportView.setAccountInfo(viewModel.sportPersonArray)
                hideTip()
                startRecording()
                if (isDebug) {
                    mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportDoing)
                }
            }
            mBinding.identityView.visibility = View.GONE

            forwardFlexionProcessor.changeTo(SportProcessorStatus.Sporting)
        }

        viewModel.resetLD.observe(this) {
            hideSportingTips()
            forwardFlexionProcessor.changeTo(SportProcessorStatus.Idle)
            forwardFlexionProcessor.onReset()
        }

        viewModel.sportTipsInfoLD.observe(this) {
            showSportingTips(it.first, it.second)
        }

    }

    private fun showSportingTips(tip: String, imgRes: Int) {
        mBinding.gSportingTips.visibility = View.VISIBLE
        mBinding.tvSportingTips.text = tip
        mBinding.ivSportingTips.setBackgroundResource(imgRes)
    }

    private fun hideSportingTips() {
        mBinding.gSportingTips.visibility = View.GONE
    }

    private fun showDesc(msg: String) {
        if (isDebug.not()) return
        runOnUiThread {
            mBinding.tvDes.append(msg)
        }
    }

    private fun setTip(tip: String) {
        mBinding.groupTvSportTip.visibility = View.VISIBLE
        mBinding.tvSportTip.text = tip
    }

    private fun hideTip() {
        mBinding.groupTvSportTip.visibility = View.GONE
    }

    private fun hideInTips() {
        mBinding.groupInTip.visibility = View.GONE
        if (isModelOk) {
            startMatDetect()
        }
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
                previewInfo.frameTimestamp = System.currentTimeMillis()
                previewInfo?.let {
                    if (preFrameTime != 0L && checkTimeOk.not()) {
                        if (it.frameTimestamp - preFrameTime > 1000_000) {
                            isNus = true
                        }
                        checkTimeOk = true
                    } else {
                        preFrameTime = it.frameTimestamp
                    }
                    if (isNus) {
                        previewInfo.frameTimestamp = previewInfo.frameTimestamp / 1000_000
                    }
                }
                if (checkTimeOk) {
                    forwardFlexionProcessor.onFrame(
                        1,
                        yuv,
                        previewInfo.width,
                        previewInfo.height,
                        previewInfo.frameTimestamp,
                        0
                    )
                }
            }
        })
        mBinding.cameraView.addProcessor(viewModel.onceCacheProcessor)
        mBinding.cameraView.addProcessor(viewModel.sportEngine)
        mBinding.cameraView.setFps(30)
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
        MultiCoreConfig
        val param = MultiCoreConfig.getForwardFlexionParam()
        // 运动配置
        sportConfig = SportConfig.builder()
            .debug(isDebug)
            .record(true)
            .videoFps(30)
            .videoDevice(1)
            .screenWidth(1920)
            .screenHeight(1080)
            .videoWidth(BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH)
            .videoHeight(BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT)
            .valueInt(intArrayOf(diskNum, 60, 20, param.getAngle(), param.getSsdqThVaule(), 1))
            .valueBool(booleanArrayOf(param.checkLeg, param.checkFoot, param.checkHand))
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
                //                if (code == InformCode.LANDMARKFINSIH || code == InformCode.LANDMARKFINSIHERR) {
                //                    viewModel.toStatusIdentifyFace()
                //                }
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

            override fun onBonePoints(body: Map<Int, FFlexionSportCb.FFlexionBody>?) {
                if (isDebug) {
                    val points = mutableListOf<Point>()
                    val rects = mutableListOf<Rect>()
                    body?.forEach {
                        points.addAll(it.value.body)
                        rects.add(viewModel.getIdentityRect(it.value.body))
                        mBinding.overlay.refreshNow()
                    }
                    pointElement.setPoints(points)
                    pointElement.setRects(rects)
                }
                viewModel.onBonePoints(body)
            }

        })
        forwardFlexionProcessor.onInit()
        loadModel()

    }

    override fun onStatusIdentify() {
        LogUtil.d("开始人脸识别")
        setTip(getString(R.string.forward_flexion_identified_tip))
        mBinding.identityView.visibility = View.VISIBLE
        mBinding.overlay.post {
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
                        if (DeviceUtil.isTsRk3588()) {
                            runOnUiThread {
                                showLoadingDialog(getString(R.string.model_loding))
                            }
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
                                        this@ForwardFlexionActivity,
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
        super.onStatusSport()
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        hideSportingTips()
        forwardFlexionProcessor.onReset()
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
        if (viewModel.ffSportConfig?.isSupportNextGroup == true){
            resultView.showNextBtn()
        }
        resultView.setSportResult(viewModel.sportPersonArray)
        resultView.listener = object : ResultViewBtnClickListener {
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
        if (isInit) return
        isInit = true
//        forwardFlexionProcessor.open(mBinding.overlay)
//        forwardFlexionProcessor.changeTo(SportProcessorStatus.MatDetect)
        viewModel.toStatusIdentifyFace()
    }


    /**
     * 下一组
     */
    private fun next() {
        timeOutHelper.start()
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
        timeOutHelper.cancel()
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
        mBinding.overlay.release()
        releaseModel()
    }

}