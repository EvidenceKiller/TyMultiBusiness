package com.ty.bus.multi.core.ui.sport.doublegbsport

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.adl.base.bean.Quadrilateral
import com.adl.base.common.AdlHandlerThread
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.overlay.OnRenderScene
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
import com.ty.bus.multi.core.callback.DoubleBaseCallback
import com.ty.bus.multi.core.config.DoubleSportConfig
import com.ty.bus.multi.core.factory.DoubleSportDetector
import com.ty.bus.multi.databinding.ActivityDoubleGbSportBinding
import com.ty.tybusbase.base.BaseDoubleSportIdentifyActivity
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.utils.LogUtil
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

    private val rightConfig by lazy { JumpConfig() }
    private var rightJumpRopeProcessor: JumpRopeProcessor? = null
    private var rightModelIsLoaded = false

    private var doubleBaseCallback: DoubleBaseCallback = object : DoubleBaseCallback() {
        override fun onLeftCompleteOnce(result: SportDetectResult) {
            runOnUiThread {
                onLeftComplete(result)
            }
        }

        override fun onRightCompleteOnce(result: SportDetectResult) {
            runOnUiThread {
                onRightComplete(result)
            }
        }

        override fun onChannel(left: TsSportChannel?, right: TsSportChannel?) {
            runOnUiThread {
                checkArea(left, right)
            }
        }

    }

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
        initOverlay()
    }

    private fun initOverlay(){
        val uiConfig = AdlUIConfig.instance()
            .readConfig(
                mBinding.sportOverlay.configKey,
                RoleConfigRopeSkip::class.java
            )
        if (uiConfig.targetRegion == null) {
            uiConfig.targetRegion =
                "[{\"x\":777,\"y\":848},{\"x\":1131,\"y\":845},{\"x\":708,\"y\":1063},{\"x\":1215,\"y\":1065}]"
        }
        mBinding.sportOverlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.sportOverlay.enableTouch(false)
        mBinding.sportOverlay.sportRegion.setDrawing(false)
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
        config.checkDownRope = isDebug // 检测下方绳索
        config.drawSkeleton = drawSkeleton // 绘制骨骼
        config.drawRope = isDebug // 绘制绳索
        config.multiPersonType = JumpConfig.MultiPersonType.SINGLE_PERSON // 单人模式
        config.enablePrescription = true
        // 设置跳绳类型
        config.setJumpType(JumpType.jump, object : TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
            }

            override fun onSucceed(list: List<TsModelInfo?>?) {
                // todo 模型加载成功回调
                //模型加载成功后开始人脸识别
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
        config.sportRegion = getLeftRegion()
        jumpRopeProcessor = JumpRopeProcessor(config)

        jumpRopeProcessor?.setJumpRopeCallBack { singleJumper, _ ->
            runOnUiThread {
//                updateResult(singleJumper)
                doubleBaseCallback.onLeftCompleteOnce(SportDetectResult().apply {
                    sportAchievement = singleJumper.score
                    singleSportLevel = 5
                })
            }
        }
        getCameraView().addProcessor(jumpRopeProcessor)
        jumpRopeProcessor?.combineJumpRopeOverlay(mBinding.sportOverlay)
    }

    private fun initRightGbJumpRopeProcessor() {
        showLoadingDialog("运动引擎加载中...")
        val drawSkeleton = BusiBaseConfig.getSkeletonEnable()
        val isDebug: Boolean = BusiBaseConfig.getDebugEnable()
        // 创建配置对象
        rightConfig.debug = isDebug // 开启调试模式
        rightConfig.checkRope = true // 检测绳索
        rightConfig.checkDownRope = isDebug // 检测下方绳索
        rightConfig.drawSkeleton = drawSkeleton // 绘制骨骼
        rightConfig.drawRope = isDebug // 绘制绳索
        rightConfig.multiPersonType = JumpConfig.MultiPersonType.SINGLE_PERSON // 单人模式
        rightConfig.enablePrescription = true
        // 设置跳绳类型
        rightConfig.setJumpType(JumpType.jump, object : TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
            }

            override fun onSucceed(list: List<TsModelInfo?>?) {
                // todo 模型加载成功回调
                //模型加载成功后开始人脸识别
                rightModelIsLoaded = true
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
        rightConfig.sportRegion = getRightRegion()
        rightJumpRopeProcessor = JumpRopeProcessor(rightConfig)

        rightJumpRopeProcessor?.setJumpRopeCallBack { singleJumper, _ ->
            runOnUiThread {
//                updateResult(singleJumper)
            }
        }
        getCameraView().addProcessor(rightJumpRopeProcessor)
        rightJumpRopeProcessor?.combineJumpRopeOverlay(mBinding.sportOverlay)
    }

    fun startJumping() {
        jumpRopeProcessor?.changeStatus(BaseSportState.Sporting)
        jumpRopeProcessor?.setStart(true)

        rightJumpRopeProcessor?.changeStatus(BaseSportState.Sporting)
        rightJumpRopeProcessor?.setStart(true)
    }

    fun stopJumping() {
        jumpRopeProcessor?.changeStatus(BaseSportState.Standby)
        jumpRopeProcessor?.setStart(false)
        jumpRopeProcessor?.onRelease()

        rightJumpRopeProcessor?.changeStatus(BaseSportState.Standby)
        rightJumpRopeProcessor?.setStart(false)
        rightJumpRopeProcessor?.onRelease()
    }


    //区域可配置
    override fun initRegion() {
        super.initRegion()
        initGbJumpRopeProcessor()
        initRightGbJumpRopeProcessor()
    }

    private fun getLeftRegion(): TsSportRegion {
        val list: List<Quadrilateral> = getOverlay().displayRegion
        if (list.size == 2) {
            val lq = list[0]
            val offset = 100
            return TsSportRegion(
                lq.p1.apply { y -= offset },
                lq.p2.apply { y -= offset },
                lq.p3,
                lq.p4
            )
        }
        return TsSportRegion.defRegion(1920, 1080, 960, 1080)
    }

    private fun getRightRegion(): TsSportRegion {
        val list: List<Quadrilateral> = getOverlay().displayRegion
        if (list.size == 2) {
            val rq = list[1]
            val offset = 100
            return TsSportRegion(
                rq.p1.apply { y -= offset },
                rq.p2.apply { y -= offset },
                rq.p3,
                rq.p4
            )
        }
        return TsSportRegion.defRegion(1920, 1080, 960, 1080)
    }


    @Synchronized
    private fun modelInitSuccessful() {
        if (modelIsLoaded.not() || rightModelIsLoaded.not()) {
            return
        }
        viewModel.toStatusIdentifyFace()
        runOnUiThread {
            initFaceRec()
        }
    }


    override fun onStatusPrepare() {

    }

    private fun onLeftComplete(result: SportDetectResult) {
        viewModel.refreshLeftValue(result.sportAchievement, result.singleSportLevel)
        leftLevelAnimal?.setLevel(result.singleSportLevel)
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

    private fun onRightComplete(result: SportDetectResult) {
        viewModel.refreshRightValue(result.sportAchievement, result.singleSportLevel)
        rightLevelAnimal?.setLevel(result.singleSportLevel)
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

    private fun checkArea(left: TsSportChannel?, right: TsSportChannel?) {
        mBinding.overLayout.checkLeftArea(left?.skeleton, viewModel.sportInfo!!.sportItemCode)
        mBinding.overLayout.checkRightArea(right?.skeleton, viewModel.sportInfo!!.sportItemCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopJumping()
        mBinding.surfaceOverlay.stop()
        AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }

}