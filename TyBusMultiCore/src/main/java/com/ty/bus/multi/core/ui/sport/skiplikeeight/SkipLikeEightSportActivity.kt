package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.os.Bundle
import com.adl.base.bean.Quadrilateral
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.mount.impl.TsDoubleProcessor
import com.adl.mount.impl.TsSportFrameDouble
import com.adl.mount.impl.TsSportLifecycleDouble
import com.adl.mount.sport.TsSportLifecycle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportConfigDouble
import com.adl.processor.SportProcessorDouble
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.databinding.ActivitySkipLikeEightExerciseBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class SkipLikeEightSportActivity :
    BaseMultiSportActivity<ActivitySkipLikeEightExerciseBinding, SkipLikeEightViewModel>() {
    lateinit var sportLifecycleDouble: TsSportLifecycle<*>
    lateinit var processorDouble: SportProcessorDouble

    private var leftRegion: TsSportRegion? = null
    private var rightRegion: TsSportRegion? = null
    private var isLeftPrepared = false
    private var isRightPrepared = false

    protected val tsConfig = SportConfigDouble()

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
        mBinding.cameraView.addProcessor(processorDouble)
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
                processorDouble.changeTo(SportProcessorStatus.Sporting)
                sportLifecycleDouble.toSporting()
                viewModel.toStatusPrepare()
                multiRegion.setDrawing(isDebug)
            }
        }
    }

    open fun initRegion() {
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
            closeDoubleProcessor()
        }
    }

    private fun rightPrepared() {
        isRightPrepared = true
        ToastUtil.getInstance(this, "右边准备好了").show()
        if (isLeftPrepared) {
            closeDoubleProcessor()
        }
    }


    private fun closeDoubleProcessor() {
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
        mBinding.cameraView.removeProcessor(processorDouble)
    }


    override fun onResume() {
        super.onResume()
        mBinding.cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.onPause()
        processorDouble.changeTo(SportProcessorStatus.Idle)
        sportLifecycleDouble.toIdle()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.cameraView.onRelease()
        processorDouble.onRelease()
        sportLifecycleDouble.onRelease()
        SpeechWrapper.stop()
    }

}