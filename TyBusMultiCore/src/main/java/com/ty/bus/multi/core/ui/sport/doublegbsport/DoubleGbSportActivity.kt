package com.ty.bus.multi.core.ui.sport.doublegbsport

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.adl.base.bean.Quadrilateral
import com.adl.base.common.AdlHandlerThread
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.mount.impl.TsDoubleProcessor
import com.adl.mount.impl.TsSportFrameDouble
import com.adl.mount.sport.TsSportChannel
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportProcessorStatus
import com.adl.sport.standard.JumpConfig
import com.adl.sport.standard.JumpType
import com.adl.sport.standard.jumpcore.BaseSportState
import com.adl.sport.standard.jumpcore.JumpRopeProcessor
import com.adl.sport.standard.jumpcore.Jumper
import com.ty.bus.multi.core.callback.DoubleBaseCallback
import com.ty.bus.multi.core.config.DoubleSportConfig
import com.ty.bus.multi.databinding.ActivityDoubleGbSportBinding
import com.ty.tybusbase.base.BaseDoubleSportIdentifyActivity
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.SoundPlayUtils
import com.ty.tybusbase.view.SportComboAnimal
import com.ty.tybusbase.view.SportLevelAnimation
import java.io.File

class DoubleGbSportActivity :
    BaseDoubleSportIdentifyActivity<ActivityDoubleGbSportBinding, DoubleGbSportViewModel>() {

    private var createTime: Long = 0
    private lateinit var videoFile: File
    private var leftComboAnimal: SportComboAnimal? = null
    private var rightComboAnimal: SportComboAnimal? = null

    private var leftLevelAnimal: SportLevelAnimation? = null
    private var rightLevelAnimal: SportLevelAnimation? = null

    private var upLeftComboTime: Long = 0
    private var upRightComboTime: Long = 0

    private val config by lazy { JumpConfig() }
    private var jumpRopeProcessor: JumpRopeProcessor? = null
    private var modelIsLoaded = false

    private var preFrameTime = 0L //上一帧的时间，用于判断是纳秒还是毫秒时间戳
    private var isNus = false //是否是纳秒时间戳
    private var countFrame = 0

    private var isStop = false

    override fun initData() {
        super.initData()
        viewModel.doubleSportConfig =
            intent.getSerializableExtra("sport_config") as? DoubleSportConfig
    }

    override fun createTsDoubleProcessor(): TsDoubleProcessor {
        return object : TsDoubleProcessor {
            override fun onAnalyzer(frame: TsSportFrameDouble?) {
            }

            override fun onInit() {
            }

            override fun onReset() {
            }

            override fun onRelease() {
            }

            override fun version(): String = ""

        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.viewSportResult.setResultBtnListener { finish() }
        lifecycle.addObserver(mBinding.surfaceOverlay)
    }

    private fun initOverlay() {
        val uiConfig = AdlUIConfig.instance().readConfig(
            mBinding.sportOverlay.configKey,
            RoleConfigRopeSkip::class.java
        )
        if (uiConfig.targetRegion == null) {
            uiConfig.targetRegion =
                "[{\"x\":0,\"y\":0},{\"x\":10,\"y\":0},{\"x\":0,\"y\":10},{\"x\":10,\"y\":10}]"
        }
        val isDebug: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        val skeleton =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_ABLE,
                BusiBaseConstantsValue.DEFAULT_SKELETON
            )
        mBinding.sportOverlay.apply {
            enableDebug(isDebug)
            enableDrawBox(false)
            enableDrawSkeleton(skeleton)
        }
        mBinding.sportOverlay.isMultiPerson = true
        mBinding.sportOverlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.sportOverlay.refresh()
        mBinding.sportOverlay.enableTouch(false)
    }

    fun configSurfaceOverlay() {
        leftLevelAnimal = SportLevelAnimation(100, 450, 150)
        rightLevelAnimal = SportLevelAnimation(100, 1400, 150)
        leftComboAnimal = SportComboAnimal(60, 100, 50, 500, true)
        rightComboAnimal = SportComboAnimal(60, 100, 960 + 50, 500, false)
        mBinding.surfaceOverlay.subscribe(leftLevelAnimal)
        mBinding.surfaceOverlay.subscribe(rightLevelAnimal)
        mBinding.surfaceOverlay.subscribe(leftComboAnimal)
        mBinding.surfaceOverlay.subscribe(rightComboAnimal)
        mBinding.surfaceOverlay.start()
    }

    override fun onStatusIdentifySuccess() {
        super.onStatusIdentifySuccess()
        //识别成功
        mBinding.overLayout.setLeftPlayer(viewModel.leftAccountInfo.value)
        mBinding.overLayout.setRightPlayer(viewModel.rightAccountInfo.value)
        viewModel.leftSportResult.userId = viewModel.leftAccountInfo.value?.studentEntity?.accountId
        viewModel.rightSportResult.userId =
            viewModel.rightAccountInfo.value?.studentEntity?.accountId
        viewModel.leftSportResult.cardType =
            viewModel.leftAccountInfo.value?.studentEntity?.cardType ?: ""
        viewModel.rightSportResult.cardType =
            viewModel.rightAccountInfo.value?.studentEntity?.cardType ?: ""
        viewModel.leftSportResult.energyPoint = viewModel.energyPoint
        viewModel.rightSportResult.energyPoint = viewModel.energyPoint
    }

    override fun onStatusSport() {
        super.onStatusSport()
        startJumping()
        mBinding.overLayout.visibility = View.VISIBLE
        mBinding.overLayout.setSportName(viewModel.sportInfo!!.sportSkuName ?: "--")
        //todo 录像
        videoFile = viewModel.getVideoPath()
        createTime = System.currentTimeMillis()
        viewModel.sportEngine.startRecorder(videoFile)
        processorDouble.changeTo(SportProcessorStatus.Idle)
        overlayVessel.clearScreen()
        overlayVessel.gone()
        //配置overlay动画
        Thread {
            configSurfaceOverlay()
        }.start()
    }

    @SuppressLint("ImplicitSamInstance")
    override fun onStatusEnd() {
        super.onStatusEnd()
        stopJumping()
        SoundPlayUtils.play(2)
        //如果配置了自动结束
        viewModel.doubleSportConfig?.autoFinishTime?.let {
            if (it > 0) {
                AdlHandlerThread.getUIHandler().postDelayed({ finish() }, it)
            }
        }
        LogUtil.d("---运动结束了")
        mBinding.viewSportResult.setResult(
            viewModel.rightSportResult,
            viewModel.leftSportResult,
            viewModel.energyPoint
        )
        val multiId = System.currentTimeMillis().toString()
        viewModel.rightSportResult.multiPersonId = multiId
        viewModel.leftSportResult.multiPersonId = multiId


        mBinding.surfaceOverlay.stop()
        mBinding.surfaceOverlay.visibility = View.GONE
        mBinding.overLayout.visibility = View.GONE
        mBinding.groupResult.visibility = View.VISIBLE

        //todo 停止录像
        viewModel.sportEngine.stopRecorder {
            viewModel.uploadSportResult(
                SceneConstants.PK,
                viewModel.rightSportResult,
                viewModel.rightAccountInfo.value?.studentEntity?.accountId
            )
            viewModel.uploadSportResult(
                SceneConstants.PK,
                viewModel.leftSportResult,
                viewModel.leftAccountInfo.value?.studentEntity?.accountId
            )
        }
    }

    override fun refreshTime(time: Long) {
        mBinding.overLayout.refreshTime(time)
    }

    override fun warningTime() {
        mBinding.overLayout.warnTime()
    }

    private fun initGbJumpRopeProcessor() {
        showLoadingDialog("运动引擎加载中...")
        val drawSkeleton = BusiBaseConfig.getSkeletonEnable()
        val isDebug: Boolean = BusiBaseConfig.getDebugEnable()
        // 创建配置对象
        config.debug = isDebug // 开启调试模式
        config.checkRope = true // 检测绳索
        config.checkDownRope = true // 检测下方绳索
        config.drawSkeleton = drawSkeleton // 绘制骨骼
        config.drawRope = isDebug // 绘制绳索
        config.multiPersonType = JumpConfig.MultiPersonType.MULTI_PERSON_THREE // 单人模式
        config.enablePrescription = false
        config.experienceEnable = false
        config.drawAllBody = isDebug
        config.drawSingleBody = isDebug
        config.drawSelectBody = isDebug
        // 设置跳绳类型
        config.setJumpType(JumpType.jump, object : TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
            }

            override fun onSucceed(list: List<TsModelInfo?>?) {
                // todo 模型加载成功回调
                //模型加载成功后开始人脸识别
                if (isStop) return
                modelIsLoaded = true
                hideLoadingDialog()
                modelInitSuccessful()
            }

            override fun onFinish(
                list: List<TsModelInfo?>?,
                errrorList: List<TsModelInfo?>?
            ) {

            }

            override fun onEnd(str: String?) {
                // 模型加载完成
            }
        })
        jumpRopeProcessor = JumpRopeProcessor(config)

        jumpRopeProcessor?.setJumpRopeCallBack { singleJumper, multiJumper ->
            runOnUiThread {
                handleJumpRopeResult(multiJumper)
            }
        }
        getCameraView().addProcessor(object : AdlCameraFrameProcessor {
            override fun onFrame(yuv: ByteArray?, previewInfo: AdlCameraPreviewInfo?) {

                // TODO 针对国标跳绳，帧率控制在25帧， add by 南山, 2026/05/27
                countFrame++
                if (countFrame % 5 == 0) return@onFrame

                previewInfo?.let {
                    if (preFrameTime != 0L) {
                        if (it.frameTimestamp - preFrameTime > 1000_000) {
                            isNus = true
                        }
                    } else {
                        preFrameTime = it.frameTimestamp
                    }
                    if (isNus) {
                        previewInfo.frameTimestamp = previewInfo.frameTimestamp / 1000_000
                        previewInfo.imageId = (previewInfo.imageId.toLong() / 1000_000).toString()
                    }
                    jumpRopeProcessor?.onFrame(yuv, previewInfo)
                }
            }

        })
        jumpRopeProcessor?.combineJumpRopeOverlay(mBinding.sportOverlay)
    }

    @Synchronized
    private fun handleJumpRopeResult(jumpers: List<Jumper>) {
        val leftJumper = jumpers.firstOrNull { it.indexNum == 0 }
        val rightJumper = jumpers.firstOrNull { it.indexNum == 2 }
        if ((leftJumper?.score ?: 0) != viewModel.leftValue) {
            onLeftComplete(leftJumper?.score ?: 0)
        }
        if ((rightJumper?.score ?: 0) != viewModel.rightValue) {
            onRightComplete(rightJumper?.score ?: 0)
        }
        checkArea(leftJumper?.skeleton, rightJumper?.skeleton)
    }

    fun startJumping() {
        config.sportRegion = TsSportRegion(mBinding.sportOverlay.sportRegion.previewPoints)
        config.sportRegionLeft = TsSportRegion(mBinding.sportOverlay.sportRegionLeft.previewPoints)
        config.sportRegionRight =
            TsSportRegion(mBinding.sportOverlay.sportRegionRight.previewPoints)
        jumpRopeProcessor?.changeStatus(BaseSportState.Sporting)
        jumpRopeProcessor?.setStart(true)
    }

    fun stopJumping() {
        jumpRopeProcessor?.changeStatus(BaseSportState.Standby)
        jumpRopeProcessor?.setStart(false)
        jumpRopeProcessor?.onRelease()
    }


    //区域可配置
    override fun initRegion() {
        super.initRegion()
        initOverlay()
        initGbJumpRopeProcessor()
    }

    @Synchronized
    private fun modelInitSuccessful() {
        viewModel.toStatusIdentifyFace()
        runOnUiThread {
            initFaceRec()
        }
    }


    override fun onStatusPrepare() {

    }

    private fun onLeftComplete(score: Int) {
        viewModel.refreshLeftValue(score)
        leftLevelAnimal?.setLevel(SportConstant.SPORT_PERFECT)
        //播放计数音效
        SoundPlayUtils.play(1)
        if (System.currentTimeMillis() - upLeftComboTime < 2000) {
            leftComboAnimal?.combo()
        } else {
            val i: Int = leftComboAnimal?.comboInterrupt() ?: 0
            viewModel.setLeftComboCount(i)
        }
        upLeftComboTime = System.currentTimeMillis()
        if (viewModel.getEndType() == SportConstant.PATTERN_LIMIT_COUNT && viewModel.getEndValue() == viewModel.leftValue.toLong()) {
            if (viewModel.rightValue.toLong() == viewModel.getEndValue()) {
                viewModel.toStatusSportEnd()
            }
        }
        mBinding.overLayout.post { mBinding.overLayout.setLeftValue(viewModel.leftValue) }
    }

    private fun onRightComplete(score: Int) {
        viewModel.refreshRightValue(score)
        rightLevelAnimal?.setLevel(SportConstant.SPORT_PERFECT)
        //播放计数音效
        SoundPlayUtils.play(1)
        if (System.currentTimeMillis() - upRightComboTime < 2000) {
            rightComboAnimal?.combo()
        } else {
            val i: Int = rightComboAnimal?.comboInterrupt() ?: 0
            viewModel.setRightComboCount(i)
        }
        upRightComboTime = System.currentTimeMillis()
        if (viewModel.getEndType() == SportConstant.PATTERN_LIMIT_COUNT && viewModel.getEndValue() == viewModel.rightValue.toLong()) {
            if (viewModel.leftValue.toLong() == viewModel.getEndValue()) {
                viewModel.toStatusSportEnd()
            }
        }
        mBinding.overLayout.post { mBinding.overLayout.setRightValue(viewModel.rightValue) }
    }

    private fun checkArea(leftSkeleton: AdlPoseSkeleton?, rightSkeleton: AdlPoseSkeleton?) {
        mBinding.overLayout.checkLeftArea(leftSkeleton, viewModel.sportInfo!!.sportItemCode)
        mBinding.overLayout.checkRightArea(rightSkeleton, viewModel.sportInfo!!.sportItemCode)
    }

    override fun onResume() {
        super.onResume()
        isStop = false
    }

    override fun onPause() {
        super.onPause()
        isStop = true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopJumping()
        mBinding.surfaceOverlay.stop()
        AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }

}