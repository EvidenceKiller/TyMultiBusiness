package com.ty.tybusbase.base

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.adl.base.bean.Quadrilateral
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.element.ElementMultiRegion
import com.adl.base.overlay.OnRenderScene
import com.adl.base.overlay.SportOverlay
import com.adl.mount.core.TsTaskBodyMultiFilter
import com.adl.mount.impl.TsSportFrameDouble
import com.adl.mount.impl.TsSportLifecycleDouble
import com.adl.mount.sport.TsConfigDouble
import com.adl.mount.sport.TsSportEngine
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigDouble
import com.adl.processor.SportProcessorDouble
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.StudentEntity
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.databinding.BaseDoubleIdentifySportLayoutBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.DoubleIdentifyMachineListener
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.Timer
import com.ty.tybusbase.utils.ToastUtil
import java.lang.reflect.ParameterizedType
import java.util.concurrent.Executors

abstract class BaseDoubleSportIdentifyActivity<VB : ViewBinding, VM : BaseDoubleSportViewModel> :
    BaseExitActivity<VB, VM>(), IDoubleSportView {

    private lateinit var mBaseBinding: BaseDoubleIdentifySportLayoutBinding

    protected lateinit var tsSportEngine: TsSportEngine
    private lateinit var config: TsConfigDouble
    private var leftRegion: TsSportRegion? = null
    private var rightRegion: TsSportRegion? = null

    lateinit var overlayVessel: SportOverlay

    protected val tsConfig = SportConfigDouble()

    protected var onceCacheProcessor = OnceCacheProcessor()

    private var identifyListener: DoubleIdentifyMachineListener? = null

    private lateinit var sportLifecycleDouble: TsSportLifecycle<*>
    private lateinit var processorDouble: SportProcessorDouble
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var isPause = false

    private val mTimer: Timer = Timer()

    override fun initData() {
        val intent = intent
        val sportInfo = intent.getSerializableExtra("sport_info") as? SceneSportInfoEntity
        if (sportInfo == null) {
            ToastUtil.getInstance(this, "没有获取到运动信息").show()
            finish()
            return
        }
        if (TextUtils.isEmpty(sportInfo.sportItemCode)) {
            ToastUtil.showErrorHint(this, "没有获取到运动编码")
            finish()
            return
        }
        viewModel.sportInfo = sportInfo
    }

    override fun initObserver() {
        // 人脸识别成功
        viewModel.accountInfoLD.observe(this) { v ->
            if (v != null) {
                viewModel.toStatusIdentifySuccess()
            }
        }
        // 运动状态监听
        viewModel.statusLD.observe(this) { newStatus ->
            when (newStatus) {
                SportStatusEnum.PREPARE -> onStatusPrepare()
                SportStatusEnum.IDENTIFY_FACE -> onStatusIdentify()
                SportStatusEnum.IDENTIFY_FACE_SUCCESS -> onStatusIdentifySuccess()
                SportStatusEnum.SPORT_START -> onStatusSport()
                SportStatusEnum.SPORT_END -> onStatusEnd()
                else -> {}
            }
        }
        mTimer.timeTick.observe(this) { time: Long ->
            if (viewModel.sportInfo?.endType == SportConstant.PATTERN_LIMIT_TIME) {
                if (viewModel.sportInfo!!.endValue.toInt() - time == 5000L) {
                    warningTime()
                }
                refreshTime(viewModel.sportInfo!!.endValue.toInt() - time)
            } else refreshTime(time)
        }
        mTimer.timeDown.observe(this) { time: Long ->
            //只有倒计时主动结束与个数计数完成才展示结果页
            val duration = time.toInt()
            viewModel.leftSportResult.sportDuration = duration
            viewModel.rightSportResult.sportDuration = duration
            if (viewModel.sportInfo!!.endType == SportConstant.PATTERN_LIMIT_TIME && time == viewModel.sportInfo!!.endValue.toLong()) {
                LogUtil.d("---运动结束了11")
                viewModel.toStatusSportEnd()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBaseBinding.ivExit.setOnClickListener { showExitDialog() }
        initLifecycle()
        initSportEngine()
        initOverlay()
        initCamera()
    }

    private fun initLifecycle() {
        val lifecycle = this.lifecycle
        lifecycle.addObserver(mTimer)
    }

    private fun initCamera() {
        mBaseBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBaseBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBaseBinding.cameraView.addProcessor(onceCacheProcessor)
        mBaseBinding.cameraView.addProcessor(viewModel.sportEngine)
        mBaseBinding.cameraView.addProcessor(processorDouble)
    }

    private fun initOverlay() {
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
        mBaseBinding.overlay.apply {
            enableDebug(isDebug)
            enableDrawBox(false)
            enableDrawSkeleton(skeleton)
            // 加载页面配置文件
            val uiConfig = readConfig()
            setConfig(uiConfig, OnRenderScene.StatusSportSet)
            reloadRegion(multiRegion, tsConfig)
            // 配置，绘制 绑定
            setListener {
                initRegion()
                processorDouble.changeTo(SportProcessorStatus.Sporting)
                sportLifecycleDouble.toIdAuth()
                viewModel.toStatusPrepare()
                multiRegion.setDrawing(isDebug)
            }
        }
    }

    open fun reloadRegion(multiRegion: ElementMultiRegion?, tsConfig: SportConfigDouble) {
    }

    open fun readConfig(): RoleConfigMulti {

        return AdlUIConfig.instance().readConfig(
            "_double_person",
            RoleConfigMulti::class.java
        )
    }

    override fun initSportEngine() {
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

        val tsSportLifecycleDouble = object : TsSportLifecycleDouble(createTsDoubleProcessor()) {
            override fun onIdAuth(frame: TsSportFrameDouble) {
                onAuthFrame(frame)
                if (isPause) return
                if (viewModel.statusLD.value != SportStatusEnum.IDENTIFY_FACE) return
                //人脸识别
                //左边
                val currentYuvData = onceCacheProcessor.getCurrentYuvData()
                if (frame.left != null && frame.left.skeleton != null) {
                    mBaseBinding.faceIdentify.identify(
                        frame.left.skeleton,
                        currentYuvData.yuv,
                        currentYuvData.width,
                        currentYuvData.height,
                        true
                    )
                }
                //右边
                if (frame.right != null && frame.right.skeleton != null) {
                    mBaseBinding.faceIdentify.identify(
                        frame.right.skeleton,
                        currentYuvData.yuv,
                        currentYuvData.width,
                        currentYuvData.height,
                        false
                    )
                }
            }
        }.apply { sportLifecycleDouble = this }
        processorDouble = SportProcessorDouble()
        processorDouble.setSportLifecycle(tsSportLifecycleDouble)
        processorDouble.setConfig(tsConfig)
        processorDouble.open(mBaseBinding.overlay)
        //init方法耗时，放在线程中执行
        Thread {
            processorDouble.onInit()
        }.start()
    }

    private fun initRegion() {
        val list: List<Quadrilateral> = mBaseBinding.overlay.displayRegion
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
            tsConfig.leftTracker.setBodyFilter(createLeftBodyFilter())
            tsConfig.leftTracker.setEnableSmooth(openSmooth())

            tsConfig.rightTracker = SingleTracker().apply {
                setInitPointF(
                    rightRegion!!.centerX().toFloat(),
                    rightRegion!!.centerY().toFloat()
                )
            }
            tsConfig.rightTracker.setBodyFilter(createRightBodyFilter())
            tsConfig.rightTracker.setEnableSmooth(openSmooth())

        }
    }

    fun initFaceRec() {
        //初始化人脸识别
        mBaseBinding.faceIdentify.setNeedAreaCheck(false)
        mBaseBinding.faceIdentify.openMachine(object : DoubleIdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityOk(leftAccountInfo: AccountInfo, rightAccountInfo: AccountInfo) {
                viewModel.leftAccountInfo.value = leftAccountInfo
                viewModel.leftSportResult.userName = leftAccountInfo.studentEntity?.accountName
                viewModel.leftSportResult.headUrl = leftAccountInfo.studentEntity?.userFaceImgUrl

                viewModel.rightAccountInfo.value = rightAccountInfo
                viewModel.rightSportResult.userName = rightAccountInfo.studentEntity?.accountName
                viewModel.rightSportResult.headUrl = rightAccountInfo.studentEntity?.userFaceImgUrl
                //去重，如果左边已识别此人，则提示已识别，以游客身份加入
                if (viewModel.leftAccountInfo.value?.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID
                    && viewModel.leftAccountInfo.value?.studentEntity?.accountId == viewModel.rightAccountInfo.value?.studentEntity?.accountId
                ) {
                    ToastUtil.getInstance(
                        this@BaseDoubleSportIdentifyActivity,
                        getString(R.string.player_exist)
                    ).show()
                    val aInfo = AccountInfo().apply {
                        studentEntity = StudentEntity().apply {
                            accountId = BusiBaseConstantsValue.VISITOR_USER_ID
                            accountName = getString(R.string.visitor)
                        }
                    }
                    viewModel.rightAccountInfo.value = aInfo
                    viewModel.rightSportResult.userName = aInfo.studentEntity?.accountName
                    viewModel.rightSportResult.headUrl = null
                }
                viewModel.toStatusIdentifySuccess()
            }

            override fun onIdentityTimeFail() {
                mBaseBinding.faceIdentify.openMachine()
                viewModel.toStatusPrepare()
            }
        })
    }


    override fun onStatusPrepare() {
        super.onStatusPrepare()
        viewModel.toStatusIdentifyFace()
    }

    override fun onStatusSport() {
        sportLifecycleDouble.toSporting()
        viewModel.rightSportResult.startTime = System.currentTimeMillis()
        viewModel.leftSportResult.startTime = System.currentTimeMillis()
        if (viewModel.getEndType() == SportConstant.PATTERN_LIMIT_TIME) {
            mTimer.start(
                Timer.TimeModel.countdownTime,
                viewModel.getEndValue() / 1000
            )
            refreshTime(viewModel.getEndValue() * 1000)
        } else {
            mTimer.start(Timer.TimeModel.time, 0)
        }
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动开始！！！")
    }

    override fun onStatusIdentifySuccess() {
        mBaseBinding.cameraView.removeProcessor(onceCacheProcessor)
        reloadRegion(mBaseBinding.overlay.multiRegion, tsConfig)
        mBaseBinding.faceIdentify.visibility = View.INVISIBLE
        mBaseBinding.faceIdentify.closeMachine()
        mBaseBinding.readyView.visibility = View.VISIBLE
        mBaseBinding.readyView.startReady {
            viewModel.toStatusSportStart()
        }
        if (tsConfig.leftTracker != null && tsConfig.leftTracker is SingleTracker) {
            (tsConfig.leftTracker as SingleTracker).changeStatus(SportStatusEnum.SPORT_START)
        }

        if (tsConfig.rightTracker != null && tsConfig.rightTracker is SingleTracker) {
            (tsConfig.rightTracker as SingleTracker).changeStatus(SportStatusEnum.SPORT_START)
        }

    }

    override fun onStatusIdentify() {
        initFaceRec()
    }

    override fun onStatusEnd() {
        mTimer.stop()
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
        mBaseBinding.overlay.clearScreen()
        mBaseBinding.overlay.visibility = View.GONE
        viewModel.rightSportResult.endTime = System.currentTimeMillis()
        viewModel.leftSportResult.endTime = System.currentTimeMillis()
        //赋值视频和封面
        viewModel.rightSportResult.videoFile = viewModel.videoFile
        viewModel.rightSportResult.videoCover = viewModel.videoCover
        viewModel.leftSportResult.videoFile = viewModel.videoFile
        viewModel.leftSportResult.videoCover = viewModel.videoCover
        LogUtil.d("${viewModel.sportInfo?.sportSkuName} 运动结束！！")
    }

    /**
     * 授权的骨骼数据，用于运动前需要骨骼的情况
     */
    open fun onAuthFrame(frame: TsSportFrameDouble?) {

    }

    abstract fun refreshTime(time: Long)

    abstract fun warningTime()

    open fun openSmooth() = false

    open fun createLeftBodyFilter(): TsTaskBodyMultiFilter? = null
    open fun createRightBodyFilter(): TsTaskBodyMultiFilter? = null
    override fun initViewDataBinding() {
        mBaseBinding = BaseDoubleIdentifySportLayoutBinding.inflate(layoutInflater)
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            var mdPosition = -1
            var vbPosition = -1
            for (index in type.actualTypeArguments.indices) {
                val cls = type.actualTypeArguments[index] as Class<*>
                if (ViewDataBinding::class.java.isAssignableFrom(cls) || ViewBinding::class.java.isAssignableFrom(
                        cls
                    )
                ) {
                    vbPosition = index
                } else if (BaseViewModel::class.java.isAssignableFrom(cls)) {
                    mdPosition = index
                }
            }

            if (vbPosition >= 0) {
                val cls = type.actualTypeArguments[vbPosition] as Class<*>
                when {
                    ViewDataBinding::class.java.isAssignableFrom(cls) && cls != ViewDataBinding::class.java -> {
                        if (initLayoutId() == 0) throw IllegalArgumentException("Using DataBinding requires overriding method initLayoutId")
                        mBinding = DataBindingUtil.inflate(
                            layoutInflater, initLayoutId(), null, false
                        )
                        setContentView(mBaseBinding.root)
                        mBaseBinding.flContentLayout.addView(
                            mBinding.root, FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        (mBinding as ViewDataBinding).lifecycleOwner = this

                    }

                    ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                        cls.getDeclaredMethod("inflate", LayoutInflater::class.java).let {
                            @Suppress("UNCHECKED_CAST") mBinding =
                                it.invoke(null, layoutInflater) as VB
                            setContentView(mBaseBinding.root)
                            mBaseBinding.flContentLayout.addView(
                                mBinding.root, FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }
                    }

                    else -> {
                        if (initLayoutId() == 0) throw IllegalArgumentException("If you don't use ViewBinding, you need to override method initLayoutId")
                        layoutInflater.inflate(initLayoutId(), null, false)
                    }
                }
            }
            // val cls = type.actualTypeArguments[mdPosition] as Class<*>
            if (mdPosition >= 0) {
                createViewModel(type.actualTypeArguments[mdPosition])
            }

        } else throw IllegalArgumentException("Generic error")
    }

    override fun onResume() {
        super.onResume()
        mBaseBinding.cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBaseBinding.cameraView.onPause()
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBaseBinding.cameraView.onRelease()
        mBaseBinding.faceIdentify.closeMachine()
        identifyListener = null
        processorDouble.onRelease()
        sportLifecycleDouble.onRelease()
        SpeechWrapper.stop()
        mTimer.stop()
    }

}