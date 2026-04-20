package com.ty.tybusbase.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.adl.base.overlay.SportOverlay
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.mount.impl.TsSportLifecycleSingle
import com.adl.mount.sport.TsConfig
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigSingle
import com.adl.processor.SportModelType
import com.adl.processor.SportProcessorSingle
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.sport.general.EuSingleBase
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.databinding.BaseSingleIdentifyAcitvityBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.helper.GlobalTouchHelper
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.OnceCacheProcessor
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.MMKVUtil
import java.lang.reflect.ParameterizedType

abstract class BaseSingleIdentifyActivity<VB : ViewBinding, VM : BaseSportViewModel> :
    BaseExitActivity<VB, VM>(), ISportView {
    private lateinit var mBaseBinding: BaseSingleIdentifyAcitvityBinding

    protected val singleTracker by lazy { SingleTracker() }
    lateinit var overlayVessel: SportOverlay
    protected lateinit var tsSportLifecycle: TsSportLifecycle<*>

    protected var onceCacheProcessor = OnceCacheProcessor()

    private var identifyListener: IdentifyMachineListener? = null

    //賽事ID
    protected var matchId: String? = null
    protected var modelId: String? = null

    //賽事名字
    protected var matchName: String? = null

    protected var sceneId: String? = null

    //赛事运动详情ID
    protected var matchSportId: String? = null

    var accountInfo: AccountInfo? = null

    //运动详情
    protected var sportInfo: SceneSportInfoEntity? = null

    protected lateinit var processorSingle: SportProcessorSingle
    override fun initData() {
        matchId = intent.getStringExtra("match_id")
        modelId = intent.getStringExtra("model_id")
        matchName = intent.getStringExtra("match_name")
        sceneId = intent.getStringExtra("scene_id")
        matchSportId = intent.getStringExtra("match_sport_id")
        accountInfo = intent.getParcelableExtra("account_info")
        sportInfo = intent.getSerializableExtra("sport_info") as? SceneSportInfoEntity
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
                SportStatusEnum.IDENTIFY_MAT -> onStatusIdentifyMat()
                SportStatusEnum.IDENTIFY_FACE_SUCCESS -> onStatusIdentifySuccess()
                SportStatusEnum.CHECK_AREA -> {}
                SportStatusEnum.SPORT_START -> onStatusSport()
                SportStatusEnum.SPORT_END -> onStatusEnd()
                else -> {}
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBaseBinding.ivExit.setOnClickListener { showExitDialog() }
        initOverlay()
        initSportEngine()
        initCamera()
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
        mBaseBinding.cameraView.addProcessor(processorSingle)
        mBaseBinding.cameraView.addProcessor(viewModel.sportEngine)
    }

    fun getCamera() = mBaseBinding.cameraView

    private fun initOverlay() {
        val drawSkeleton = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, BusiBaseConstantsValue.DEFAULT_SKELETON)
        val isDebug: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        overlayVessel = getSportOverlay()
        overlayVessel.enableDrawSkeleton(drawSkeleton)
        overlayVessel.enableDrawBox(false)
        overlayVessel.enableDraw(true)
        overlayVessel.enableDebug(isDebug)
//        if (isDebug) overlayVessel.addElement(mBaseBinding.cameraView.cameraFpsCounter)
    }


    override fun initSportEngine() {
        val isDebug: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        // 运动配置
        val tsConfig = SportConfigSingle()
        tsConfig.debug = isDebug
        tsConfig.bodyModelId = ModelIdConstants.COMMON_BODY_MODEL_ID // 人框
        tsConfig.poseModelId = ModelIdConstants.COMMON_POSE_MODEL_ID // 骨骼
        tsConfig.drawSkeleton = true
        tsConfig.drawSingleBody = isDebug

        val tsSportLifecycleSingle = object : TsSportLifecycleSingle(createEuBase()) {
            override fun onIdAuth(frame: TsSportFrameSingle) {
                if (viewModel.statusLD.value == SportStatusEnum.IDENTIFY_FACE) {
                    val yuvData = onceCacheProcessor.getCurrentYuvData()
                    mBaseBinding.faceIdentify.identify(
                        frame.skeleton,
                        yuvData.yuv,
                        yuvData.width,
                        yuvData.height,
                        getIdentifyType()
                    )
                }
            }
        }.apply { tsSportLifecycle = this }

        processorSingle = SportProcessorSingle().apply {
            setSportLifecycle(tsSportLifecycleSingle)
            setConfig(tsConfig)
            setBodyTracker(singleTracker)
            setPoseType(if (DeviceUtil.isTsRk3588()) SportModelType.TS else SportModelType.KunL)
            setSmooth(false)
            setAutoDeleteFrame(false)
        }
        processorSingle.open(overlayVessel)
        processorSingle.onInit()
        overlayVessel.setListener {
            tsConfig.sportRegion = getRegion()
            sportInit()
        }
    }

    fun removeProcessorSingle() {
        mBaseBinding.cameraView.removeProcessor(processorSingle)
    }

    protected open fun initFaceRec() {
        mBaseBinding.faceIdentify.isSpeakResult = false //识别完不说话
        identifyListener = object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {}
            override fun onIdentityTimeOn() {}
            override fun onIdentityOk(accountInfo: AccountInfo) {
                viewModel.accountInfoLD.postValue(accountInfo)
            }

            override fun onIdentityTimeFail() {
                SpeechWrapper.speech2(getString(R.string.rec_face_fail_retry)) { //                        finish();//超时回退
                    //超时不退出，重置为识别状态
//                        viewModel.toStatusPrepare()
                }
            }
        }
        accountInfo?.let {
            mBaseBinding.faceIdentify.openMachineByMatchModel(identifyListener, it)
        } ?: run {
            mBaseBinding.faceIdentify.openMachine(identifyListener)
        }
    }

    override fun createEuBase(): TsSingleProcessor {
        return object : EuSingleBase(null){
            override fun onSkeleton(p0: AdlPoseSkeleton?) {

            }
        }
    }

    protected open fun getSportOverlay(): SportOverlay = mBaseBinding.overlay

    override fun getRegion(): TsSportRegion {
        return TsSportRegion.defCenterRegion(1920, 1080)
    }

    open fun sportInit() {
        processorSingle.changeTo(SportProcessorStatus.Sporting)
        viewModel.toStatusPrepare()
    }


    override fun onCheckArea() {

    }

    override fun onStatusSport() {
        GlobalTouchHelper.pause()
        tsSportLifecycle.toSporting()
        singleTracker.changeStatus(SportStatusEnum.SPORT_START)
    }

    override fun onStatusEnd() {
        GlobalTouchHelper.restart()
        tsSportLifecycle.toIdle()
        processorSingle.changeTo(SportProcessorStatus.Done)
        runOnUiThread {
            getSportOverlay().clearScreen()
        }
    }

    override fun onStatusIdentifySuccess() {
        mBaseBinding.faceIdentify.visibility = View.INVISIBLE
        mBaseBinding.faceIdentify.cancelTimeOutTask()
        mBaseBinding.faceIdentify.closeMachine()
    }

    override fun onStatusIdentify() {
        initFaceRec()
        tsSportLifecycle.toIdAuth()
    }

    open fun getIdentifyType() = 0

    override fun initViewDataBinding() {
        mBaseBinding = BaseSingleIdentifyAcitvityBinding.inflate(layoutInflater)
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
        tsSportLifecycle.toIdle()
        processorSingle.changeTo(SportProcessorStatus.Idle)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBaseBinding.cameraView.onRelease()
        mBaseBinding.faceIdentify.closeMachine()
        tsSportLifecycle.onRelease()
        processorSingle.onRelease()
        identifyListener = null
//        SpeechWrapper.stop()
    }
}