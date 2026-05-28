package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.os.Bundle
import com.adl.base.bean.Quadrilateral
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.overlay.OnRenderScene
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.mount.impl.TsDoubleProcessor
import com.adl.mount.impl.TsSportFrameDouble
import com.adl.mount.impl.TsSportLifecycleDouble
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigDouble
import com.adl.processor.SportProcessorDouble
import com.adl.processor.SportProcessorStatus
import com.adl.sport.standard.JumpConfig
import com.adl.sport.standard.JumpType
import com.adl.sport.standard.jumpcore.BaseSportState
import com.adl.sport.standard.jumpcore.JumpRopeProcessor
import com.adl.sport.standard.jumpcore.Jumper
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.databinding.ActivitySkipLikeEightExerciseBinding
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil
import androidx.recyclerview.widget.LinearLayoutManager

class SkipLikeEightSportActivity :
    BaseMultiSportActivity<ActivitySkipLikeEightExerciseBinding, SkipLikeEightViewModel>() {
    lateinit var sportLifecycleDouble: TsSportLifecycle<*>
    lateinit var processorDouble: SportProcessorDouble

    private var leftRegion: TsSportRegion? = null
    private var rightRegion: TsSportRegion? = null
    private var isLeftPrepared = false
    private var isRightPrepared = false

    private val config by lazy { JumpConfig() }
    private var jumpRopeProcessor: JumpRopeProcessor? = null
    private var modelIsLoaded = false

    protected val tsConfig = SportConfigDouble()

    private var isStop = false

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun initData() {
        super.initData()

    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initSportEngine()
        initDoubleOverlay()
        initCamera()
        observeViewModel()
        viewModel.loadAvatars()
    }


    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBinding.cameraView.addProcessor(viewModel.sportEngine)
    }

    private fun initDoubleOverlay() {
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

        mBinding.overlay.apply {
            enableDebug(isDebug)
            enableDrawBox(false)
            enableDrawSkeleton(skeleton)
            // 加载页面配置文件
            val uiConfig = readConfig()
            setConfig(uiConfig, OnRenderScene.StatusSportSet)
            // 配置，绘制 绑定
            setListener {
                initRegion()
                initGbOverlay()
                initGbJumpRopeProcessor()
                processorDouble.changeTo(SportProcessorStatus.Sporting)
                sportLifecycleDouble.toSporting()
                multiRegion.setDrawing(isDebug)
            }
        }
    }

    fun initRegion() {
        val list: List<Quadrilateral> = mBinding.overlay.displayRegion
        if (list.size == 2) {
            val lq = list[0]
            val rq = list[1]
            val offset = 100
            leftRegion = TsSportRegion(
                lq.p1.apply { y -= offset },
                lq.p2.apply { y -= offset },
                lq.p3,
                lq.p4
            )
            rightRegion = TsSportRegion(
                rq.p1.apply { y -= offset },
                rq.p2.apply { y -= offset },
                rq.p3,
                rq.p4
            )
            tsConfig.leftRegion = leftRegion
            tsConfig.rightRegion = rightRegion
            tsConfig.leftTracker = SingleTracker().apply {
                setInitPointF(
                    leftRegion!!.centerX().toFloat(),
                    leftRegion!!.centerY().toFloat()
                )
            }
            tsConfig.leftTracker.setEnableSmooth(true)

            tsConfig.rightTracker = SingleTracker().apply {
                setInitPointF(
                    rightRegion!!.centerX().toFloat(),
                    rightRegion!!.centerY().toFloat()
                )
            }
            tsConfig.rightTracker.setEnableSmooth(true)

        }
    }

    fun readConfig(): RoleConfigMulti {

        return AdlUIConfig.instance().readConfig(
            "_double_person",
            RoleConfigMulti::class.java
        )
    }

    fun initSportEngine() {
        val isDebug: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        // 运动配置

        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = true
        tsConfig.drawSingleBody = isDebug
        tsConfig.drawAllBody = isDebug
        tsConfig.drawBody = isDebug

        val tsSportLifecycleDouble = object : TsSportLifecycleDouble(object : TsDoubleProcessor {
            override fun onAnalyzer(frame: TsSportFrameDouble?) {
                checkPersonState(frame)
            }

            override fun onInit() {
            }

            override fun onReset() {
            }

            override fun onRelease() {
            }

            override fun version(): String? = "v1.0"

        }) {
        }.apply { sportLifecycleDouble = this }
        processorDouble = SportProcessorDouble()
        processorDouble.setSportLifecycle(tsSportLifecycleDouble)
        processorDouble.setConfig(tsConfig)
        processorDouble.open(mBinding.overlay)
        //init方法耗时，放在线程中执行
        Thread {
            processorDouble.onInit()
        }.start()
    }

    private fun checkPersonState(frame: TsSportFrameDouble?) {
        if (frame == null || (isLeftPrepared && isRightPrepared)) return
        frame.left?.skeleton?.let {
            runOnUiThread {
                leftPrepared()
            }
        }

        frame.right?.skeleton?.let {
            runOnUiThread {
                rightPrepared()
            }
        }
    }

    private fun leftPrepared() {
        isLeftPrepared = true
        ToastUtil.getInstance(this, "左边准备好了").show()
        if (isRightPrepared) {
            viewModel.toStatusPrepare()
        }
    }

    private fun rightPrepared() {
        isRightPrepared = true
        ToastUtil.getInstance(this, "右边准备好了").show()
        if (isLeftPrepared) {
            viewModel.toStatusPrepare()
        }
    }

    override fun onStatusPrepare() {
        super.onStatusPrepare()
        closeDoubleProcessor()
        mBinding.readyView.startReady {
            viewModel.toStatusSportStart()
        }
    }

    override fun onStatusSport() {
        super.onStatusSport()
        startJumping()
        startCountDown()
    }

    private fun startCountDown() {
        val totalSeconds = viewModel.getEndValue() / 1000
        viewModel.startCountDown(totalSeconds)
    }

    private fun setCountTime(time: Long){
        val min = time / 60
        val sec = time % 60
        mBinding.tvTime.text = "${viewModel.toTwoStr(min.toInt())}:${viewModel.toTwoStr(sec.toInt())}"
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        stopJumping()
    }

    private fun closeDoubleProcessor() {
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
        mBinding.cameraView.removeProcessor(processorDouble)
        mBinding.overlay.clearScreen()
        mBinding.overlay.gone()
    }

    private fun initGbOverlay() {
        val uiConfig = AdlUIConfig.instance().readConfig(
            MultiCoreConstant.SKIP_EIGHT_OVERLAY_UI_KEY,
            RoleConfigRopeSkip::class.java
        )
        val list = mBinding.overlay.previewRegion
        if (uiConfig.targetRegion == null) {
            if (list.size == 2) {
                val lq = list[0]
                val rq = list[1]
                val leftX = lq.rect.centerX()
                val rightX = rq.rect.centerX()
                uiConfig.targetRegion =
                    "[{\"x\":$leftX,\"y\":0},{\"x\":$rightX,\"y\":0},{\"x\":$leftX,\"y\":1080},{\"x\":$rightX,\"y\":1080}]"
            } else {
                uiConfig.targetRegion =
                    "[{\"x\":$0,\"y\":0},{\"x\":$1920,\"y\":0},{\"x\":$0,\"y\":1080},{\"x\":$1920,\"y\":1080}]"
            }
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
        mBinding.gbOverlay.apply {
            enableDebug(isDebug)
            enableDrawBox(false)
            enableDrawSkeleton(skeleton)
        }
        mBinding.gbOverlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.gbOverlay.refresh()
        mBinding.gbOverlay.enableTouch(false)
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
        config.multiPersonType = JumpConfig.MultiPersonType.SINGLE_PERSON // 单人模式
        config.enablePrescription = false
        config.experienceEnable = false
        config.drawAllBody = isDebug
        config.drawSingleBody = isDebug
        config.drawSelectBody = isDebug
        // 设置跳绳类型
        config.setJumpType(
            JumpType.skippingLikeAEight,
            object : TsModelResPool.OnLoadModelCallback {
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
                handleJumpRopeResult(singleJumper)
            }
        }
        mBinding.cameraView.addProcessor(jumpRopeProcessor)
        jumpRopeProcessor?.combineJumpRopeOverlay(mBinding.gbOverlay)
    }

    private fun handleJumpRopeResult(jumper: Jumper) {

    }

    private fun modelInitSuccessful() {
        mBinding.cameraView.addProcessor(processorDouble)
    }

    fun startJumping() {
        jumpRopeProcessor?.changeStatus(BaseSportState.Sporting)
        jumpRopeProcessor?.setStart(true)
    }

    private fun observeViewModel() {
        viewModel.avatarListLD.observe(this) { items ->
            initAvatarList(items)
        }

        viewModel.countDownSecondsLD.observe(this) { seconds ->
            setCountTime(seconds)
        }

        viewModel.countDownFinishLD.observe(this) {
            viewModel.toStatusSportEnd()
        }
    }

    private fun initAvatarList(items: List<AvatarItem>) {
        val adapter = AvatarAdapter(items.toMutableList())
        mBinding.rvHeader.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            this.adapter = adapter
            addItemDecoration(OverlapDecoration(20))
            clipToPadding = false
        }
    }

    fun stopJumping() {
        jumpRopeProcessor?.changeStatus(BaseSportState.Standby)
        jumpRopeProcessor?.setStart(false)
        jumpRopeProcessor?.onRelease()
    }

    override fun onResume() {
        super.onResume()
        mBinding.cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopCountDown()
        mBinding.cameraView.onPause()
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
        stopJumping()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.cameraView.onRelease()
        processorDouble.onRelease()
        sportLifecycleDouble.onRelease()
        jumpRopeProcessor?.onRelease()
        viewModel.onRelease()
        SpeechWrapper.stop()
    }

}