package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.MeetGroupDetailsEntity
import com.ty.bus.multi.R
import com.ty.tybusbase.expand.toMs
import com.ty.tybusbase.expand.toRaceTiming
import com.ty.tybusbase.expand.visible

class SkipLikeEightSportActivity :
    BaseMultiSportActivity<ActivitySkipLikeEightExerciseBinding, SkipLikeEightViewModel>() {
    lateinit var sportLifecycleDouble: TsSportLifecycle<*>
    lateinit var processorDouble: SportProcessorDouble

    private var leftRegion: TsSportRegion? = null
    private var rightRegion: TsSportRegion? = null
    private var isLeftPrepared = false
    private var isRightPrepared = false

    // 区域动画相关
    private val animHandler = Handler(Looper.getMainLooper())
    private var leftAnimRunnable: Runnable? = null
    private var rightAnimRunnable: Runnable? = null
    private var leftAnimFrame = 0
    private var rightAnimFrame = 0
    private val animInterval = 1000L // 区域提示框动画时间

    private val config by lazy { JumpConfig() }
    private var jumpRopeProcessor: JumpRopeProcessor? = null
    private var modelIsLoaded = false

    protected val tsConfig = SportConfigDouble()

    private var count = 0

    private val comboTimeThreshold = 3000

    private var maxComboCount = 0
    private var upComboTime: Long = 0
    protected var comboCount = 1

    private var isStop = false

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun initData() {
        super.initData()
        viewModel.studentList =
            intent.getSerializableExtra("student_list") as? ArrayList<MeetGroupDetailsEntity>
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initSportEngine()
        initDoubleOverlay()
        initCamera()
        observeViewModel()
        viewModel.loadPerson()
        setupGradientText()
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

            // 初始化区域动画
            initRegionAnimation()
        }
    }

    fun readConfig(): RoleConfigMulti {
        return AdlUIConfig.instance().readConfig(
            MultiCoreConstant.SKIP_EIGHT_IDENTIFY_OVERLAY_UI_KEY,
            RoleConfigMulti::class.java
        ).apply {
            if (targetRegion == null) {
                targetRegion =
                    "[{\"x\":215,\"y\":587},{\"x\":514,\"y\":582},{\"x\":210,\"y\":782},{\"x\":533,\"y\":774},{\"x\":1345,\"y\":590},{\"x\":1665,\"y\":581},{\"x\":1332,\"y\":789},{\"x\":1676,\"y\":785}]"
            }
        }
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
        runOnUiThread {
            frame?.left?.skeleton?.let {
//                leftPrepared()
            }

            frame?.right?.skeleton?.let {
                rightPrepared()
            }
        }
    }

    private fun leftPrepared() {
        if (isLeftPrepared) return
        isLeftPrepared = true
        ToastUtil.getInstance(this, "左边准备好了").show()
        // 切换到准备状态图片
        showLeftRegionReady()
        if (isRightPrepared) {
            viewModel.toStatusPrepare()
        }
    }

    private fun rightPrepared() {
        if (isRightPrepared) return
        isRightPrepared = true
        ToastUtil.getInstance(this, "右边准备好了").show()
        // 切换到准备状态图片
        showRightRegionReady()
        if (isLeftPrepared) {
            viewModel.toStatusPrepare()
        }
    }

    /**
     * 初始化区域动画
     */
    private fun initRegionAnimation() {
        // 显示左右区域ImageView
        mBinding.ivLeftRegion.visibility = View.VISIBLE
        mBinding.ivRightRegion.visibility = View.VISIBLE

        // 设置图片位置到左右区域的中点
        setRegionImagePosition()

        // 启动左侧动画
        startLeftRegionAnimation()
        // 启动右侧动画
        startRightRegionAnimation()
    }

    /**
     * 设置区域图片位置到左右区域中点
     */
    private fun setRegionImagePosition() {
        // 获取左右区域的中点
        val leftCenterX = leftRegion?.centerX() ?: 0
        val leftCenterY = leftRegion?.centerY() ?: 0
        val rightCenterX = rightRegion?.centerX() ?: 0
        val rightCenterY = rightRegion?.centerY() ?: 0

        // 获取图片控件的宽高
        val imageWidth = mBinding.ivLeftRegion.measuredWidth.takeIf { it > 0 }
            ?: resources.getDimensionPixelSize(R.dimen.dp_303)
        val imageHeight = mBinding.ivLeftRegion.measuredHeight.takeIf { it > 0 }
            ?: resources.getDimensionPixelSize(R.dimen.dp_87)

        // 计算偏移量（图片宽高的一半）
        val offsetX = imageWidth / 2
        val offsetY = imageHeight / 2

        // 设置左侧图片位置（中心点对齐）
        val leftParams = mBinding.ivLeftRegion.layoutParams as ConstraintLayout.LayoutParams
        leftParams.leftMargin = leftCenterX - offsetX
        leftParams.topMargin = leftCenterY - offsetY
        mBinding.ivLeftRegion.layoutParams = leftParams

        // 设置右侧图片位置（中心点对齐，使用 leftMargin）
        val rightParams = mBinding.ivRightRegion.layoutParams as ConstraintLayout.LayoutParams
        rightParams.leftMargin = rightCenterX - offsetX
        rightParams.topMargin = rightCenterY - offsetY
        mBinding.ivRightRegion.layoutParams = rightParams
    }

    /**
     * 启动左侧区域动画
     */
    private fun startLeftRegionAnimation() {
        leftAnimFrame = 0
        leftAnimRunnable = object : Runnable {
            override fun run() {
                leftAnimFrame = (leftAnimFrame + 1) % 2
                val resId = if (leftAnimFrame == 0) {
                    R.mipmap.user_swingrope_01
                } else {
                    R.mipmap.user_swingrope_02
                }
                mBinding.ivLeftRegion.setImageResource(resId)
                animHandler.postDelayed(this, animInterval)
            }
        }
        animHandler.post(leftAnimRunnable!!)
    }

    /**
     * 启动右侧区域动画
     */
    private fun startRightRegionAnimation() {
        rightAnimFrame = 0
        rightAnimRunnable = object : Runnable {
            override fun run() {
                rightAnimFrame = (rightAnimFrame + 1) % 2
                val resId = if (rightAnimFrame == 0) {
                    R.mipmap.user_swingrope_01
                } else {
                    R.mipmap.user_swingrope_02
                }
                mBinding.ivRightRegion.setImageResource(resId)
                animHandler.postDelayed(this, animInterval)
            }
        }
        animHandler.post(rightAnimRunnable!!)
    }

    /**
     * 停止左侧区域动画
     */
    private fun stopLeftRegionAnimation() {
        leftAnimRunnable?.let {
            animHandler.removeCallbacks(it)
            leftAnimRunnable = null
        }
    }

    /**
     * 停止右侧区域动画
     */
    private fun stopRightRegionAnimation() {
        rightAnimRunnable?.let {
            animHandler.removeCallbacks(it)
            rightAnimRunnable = null
        }
    }

    /**
     * 显示左侧准备状态图片
     */
    private fun showLeftRegionReady() {
        stopLeftRegionAnimation()
        mBinding.ivLeftRegion.setImageResource(R.mipmap.user_swingrope_ready)
        // 添加淡入效果
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
            fillAfter = true
        }
        mBinding.ivLeftRegion.startAnimation(fadeIn)
    }

    /**
     * 显示右侧准备状态图片
     */
    private fun showRightRegionReady() {
        stopRightRegionAnimation()
        mBinding.ivRightRegion.setImageResource(R.mipmap.user_swingrope_ready)
        // 添加淡入效果
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
            fillAfter = true
        }
        mBinding.ivRightRegion.startAnimation(fadeIn)
    }

    /**
     * 停止所有区域动画
     */
    private fun stopRegionAnimation() {
        stopLeftRegionAnimation()
        stopRightRegionAnimation()
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
        mBinding.llCountdown.visible()
        mBinding.rlSpeed.visible()
        mBinding.llException.visible()
        mBinding.tvCount.visible()
        mBinding.tvUnit.visible()
        viewModel.startSportTime = System.currentTimeMillis()
        //开始录像
        mBinding.cameraView.addProcessor(object : AdlCameraFrameProcessor {
            override fun onFrame(p0: ByteArray?, p1: AdlCameraPreviewInfo?) {
                viewModel.videoRecorder.inputNv21(p0)
            }
        })
        viewModel.startRecord()

        startJumping()
        startCountDown()
    }

    private fun startCountDown() {
        val totalSeconds = viewModel.getEndValue() / 1000
        viewModel.startCountDown(totalSeconds)
    }

    private fun setCountTime(time: Long) {
        val min = time / 60
        val sec = time % 60
        mBinding.tvTime.text =
            "${viewModel.toTwoStr(min.toInt())}:${viewModel.toTwoStr(sec.toInt())}"
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        viewModel.endSportTime = System.currentTimeMillis()
        stopJumping()
        // 停止录像,上传成绩
        viewModel.stopRecord()
        uploadResult()
        showResultView()
    }

    private fun uploadResult() {
        //结束上传成绩
//        viewModel.recordMeet?.let {
//            it.meetNumberCode = meetGroupDetail!!.numberCode
//            it.meetRunwayCode = meetGroupDetail!!.runwayCode
//            it.serialNumber = meetGroupDetail!!.serialCode
//        }
//        viewModel.uploadSportResult(
//            sceneId ?: SceneConstants.FREE,
//            sportResult,
//            modelId,
//            matchId,
//            matchSportId,
//            matchName,
//            recordMeet
//        )
    }

    private fun showResultView() {
        mBinding.sportResultView.root.visible()
        mBinding.sportResultView.sportScore.text = count.toString()
        mBinding.sportResultView.tvCount.text = count.toString()
        mBinding.sportResultView.tvDuration.text =
            ((viewModel.endSportTime - viewModel.startSportTime) / 1000L).toRaceTiming()
        mBinding.sportResultView.btnSure.setOnClickListener { finish() }
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
                    modelInitSuccessful()
                }

                override fun onFinish(
                    list: List<TsModelInfo?>?,
                    errrorList: List<TsModelInfo?>?
                ) {

                }

                override fun onEnd(str: String?) {
                    // 模型加载完成
                    hideLoadingDialog()
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

    private fun handleJumpRopeResult(singleJumper: Jumper) {
        if (count == singleJumper.score) return
        //处理combo
        if (System.currentTimeMillis() - upComboTime < comboTimeThreshold) {
            mBinding.layoutCombo.visible()
            mBinding.tvCombo.text = "x$comboCount"
            comboCount++
        } else {
            maxComboCount = maxComboCount.coerceAtLeast(comboCount)
            mBinding.layoutCombo.gone()
            comboCount = 1
        }
        upComboTime = System.currentTimeMillis()

        val speedQ = singleJumper.ropeSkipOnce.speed
        mBinding.waveLine.insert(speedQ)
        val exceptionList = arrayListOf<String>().apply {
            addAll(singleJumper.ropeSkipOnce.exceptionList)
        }
        count = singleJumper.score
        mBinding.tvCount.text = count.toString()
        mBinding.tvSpeed.text =
            getString(
                R.string.trick_jump_speed,
                speedQ
            )
        if (exceptionList.isNotEmpty()) {
            mBinding.tvNoException.visibility = View.GONE
            mBinding.svException.visibility = View.VISIBLE
            mBinding.tvException.text = ""
            exceptionList.forEach {
                mBinding.tvException.append(
                    "${(System.currentTimeMillis() - viewModel.startSportTime).toMs()} $it\n"
                )
                mBinding.svException.fullScroll(View.FOCUS_DOWN)
            }

        }
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

    private fun setupGradientText() {
        applyTextGradient(mBinding.tvCount, "#FFFFFF", "#D1ECFD", 0.2962f, 0.7231f)
        applyTextGradient(mBinding.tvUnit, "#FFFFFF", "#D1ECFD", 0.2962f, 0.7231f)
        applyTextGradient(mBinding.tvCombo, "#FFEE00", "#FFCC00", 0.2931f, 0.7931f)
        applyTextGradient(mBinding.tvComboText, "#FFEE00", "#FFCC00", 0.2931f, 0.7931f)
    }

    private fun applyTextGradient(
        textView: android.widget.TextView,
        startColor: String,
        endColor: String,
        startPos: Float,
        endPos: Float
    ) {
        textView.post {
            textView.setTextColor(Color.WHITE)
            val height = textView.textSize
            val shader = LinearGradient(
                0f, 0f, 0f, height,
                intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)),
                floatArrayOf(startPos, endPos),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
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
        // 清理动画资源
        stopRegionAnimation()
    }

}