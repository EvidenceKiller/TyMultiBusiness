package com.ty.bus.multi.core.ui.sport.sixsport

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import androidx.core.util.size
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.processor.SportProcessorStatus
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.outer.AdlService
import com.adl.sport.general.multi.MultiSportCallbackImpl
import com.adl.sport.general.multi.MultiSportConfig
import com.adl.sport.general.multi.MultiSportParser
import com.adl.sport.general.multi.MultiSportProcessor
import com.adl.sport.general.multi.MultiSportRegionCheckFilter
import com.adl.sport.general.multi.MultiSportResult
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.ThreeSportConfig
import com.ty.bus.multi.core.factory.AbstractMultiSportFactory
import com.ty.bus.multi.core.factory.MultiSportProduceFactory
import com.ty.bus.multi.core.utils.ThreeSportStandFilter
import com.ty.bus.multi.core.view.threesport.ThreeSportResultView
import com.ty.bus.multi.databinding.ActivitySixSportBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil

class SixSportActivity : BaseMultiSportActivity<ActivitySixSportBinding, SixSportViewModel>() {
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val multiSportProcessor by lazy { MultiSportProcessor() }

    private val sportConfig by lazy { MultiSportConfig() }

    private lateinit var parser: MultiSportParser<*>

    private val imgMap = hashMapOf<Int, ImageView>()

    private lateinit var srrvResult: ThreeSportResultView

    private lateinit var factory: AbstractMultiSportFactory

    private var tipCountDownCount = 3  //提示的倒计时时间
    private val tipRunnable: Runnable = object : Runnable {
        override fun run() {
            tipCountDownCount--
            if (tipCountDownCount == 0) {
                viewModel.toStatusIdentifyFace()
                return
            }
            mBinding.btnInTipConfirm.text =
                getString(R.string.confirm_countdown, tipCountDownCount)
            mainHandler.postDelayed(this, 1000)
        }

    }
    private val checkStatusList = mutableListOf<Boolean>()

    override fun initData() {
        super.initData()
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.tsConfig =
            intent.getSerializableExtra("sport_config") as? ThreeSportConfig
        for (i in 0 until viewModel.sportManCount) {
            checkStatusList.add(false)
        }
        factory = MultiSportProduceFactory.getFactory(viewModel.sportInfo?.sportItemCode)
        viewModel.getBestRankTop()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName
        mBinding.tvInTip.text = factory.getStartTip()
        mBinding.btnInTipConfirm.setOnClickListener {
            mainHandler.removeCallbacks(tipRunnable)
            viewModel.toStatusIdentifyFace()
        }
        mainHandler.postDelayed(tipRunnable, 1000)
        initOverlay()
        initPersonStateImageView()
        initProcess()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.changeSportViewStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
        }

        viewModel.changeIdentityCardStateLD.observe(this) {
//            mBinding.sportView.changeState(it, factory.getReadyTip())
            val imageView = imgMap[it.first]
            if (it.second == MultiSportCardViewState.NOBODY) {
                imageView?.visibility = View.VISIBLE
            } else {
                imageView?.visibility = View.GONE
            }

            //已识别，设置已识别的位置给parser
            if (it.second == MultiSportCardViewState.IDENTIFIED) {
                setTip(factory.getReadyTip())
                for (i in checkStatusList.indices) {
                    checkStatusList[i] = false
                }
                LogUtil.d("======================>>>>>>>>>>>>>>>>>>>>>> ${viewModel.sportPersonArray.size}")
                viewModel.sportPersonArray.forEach { _, value ->
                    val number = value.number
                    if (number in checkStatusList.indices) {
                        checkStatusList[number] = true
                    }
                }
                parser.setCheckStatusList(checkStatusList)
            }
        }

        viewModel.identifiedLD.observe(this) {
            mBinding.sportView.setAccountInfo(it.first, it.second)
        }

        viewModel.countDownFinishLD.observe(this) {
            val params = mBinding.cameraView.layoutParams as ConstraintLayout.LayoutParams
            params.width = 342
            params.height = 192
            params.rightMargin = 20
            params.bottomMargin = 20
            mBinding.cameraView.layoutParams = params
            hideTip()
            //隐藏所有空闲图标
            imgMap.forEach { (_, u) ->
                u.visibility = View.GONE
            }
            if (mBinding.overlay.multiRegion != null) {
                mBinding.overlay.multiRegion.setDrawing(false)
            }

            //切换Pose模型ID
            sportConfig.poseModelId = factory.getPoseModelId()
            sportConfig.bodyModelId = factory.getBodyModelId()
            multiSportProcessor.setConfig(sportConfig)
            mBinding.readyView.startReady {
                viewModel.toStatusSportStart()
            }
        }

        viewModel.resultLD.observe(this) {
//            mBinding.threeSportView.updateScore(it)
        }

        viewModel.changeTimeTextBgLD.observe(this) {
//            mBinding.sportView.changeTimeTextBg(it)
        }

        viewModel.sportCountDownLD.observe(this) {
//            mBinding.sportView.updateTime(it)
        }

        viewModel.readySpeechLD.observe(this) {
            SpeechWrapper.speech(factory.getReadyTip())
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.toDoneLD.observe(this) {
        }

    }

    override fun onStatusIdentify() {
        parser.checkChannelStatus = true
        multiSportProcessor.changeTo(SportProcessorStatus.Prepare)
        mBinding.groupInTip.visibility = View.GONE
        setTip(factory.getInTip())
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
        super.onStatusSport()
        multiSportProcessor.changeTo(SportProcessorStatus.Sporting)
        multiSportProcessor.setBodyFilter(MultiSportRegionCheckFilter())
        parser.checkSkipScore = true
    }

    override fun onStatusEnd() {
        hideTip()
        parser.checkChannelStatus = false
        parser.checkSkipScore = false
        multiSportProcessor.changeTo(SportProcessorStatus.Idle)
    }


    private fun initOverlay() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            BusiBaseConstantsValue.THREE_SPORT_CONFIG_KEY,
            RoleConfigMulti::class.java
        )
        uiConfig.number = viewModel.sportManCount
        if (uiConfig.targetRegion == null) {
            uiConfig.targetRegion = BusiBaseConstantsValue.THREE_SPORT_CONFIG_DEFAULT_REGION
        }
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.enableTouch(false)
    }

    /**
     * 添加各位置的空闲图片
     */
    private fun initPersonStateImageView() {
        mBinding.overlay.post {
            val displayRegion = mBinding.overlay.displayRegion
            val imgWidth = 118
            val imgHeight = 83
            displayRegion.forEachIndexed { index, quadrilateral ->
                val img = ImageView(this)
                img.setBackgroundResource(R.mipmap.pic_leisure)
                val params = ConstraintLayout.LayoutParams(imgWidth, imgHeight)
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                params.topMargin = quadrilateral.rect.centerY() - imgHeight.div(2)
                params.leftMargin = quadrilateral.rect.centerX() - imgWidth.div(2)
                img.layoutParams = params
                mBinding.root.addView(img)
                imgMap[index] = img
            }
        }
    }

    private fun initProcess() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        val skeleton = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)

        sportConfig.debug = isDebug
        sportConfig.bodyModelId = factory.getFaceBodyModelId()
        sportConfig.poseModelId = factory.getFacePoseModelId()
        sportConfig.drawAllBody = isDebug
        sportConfig.drawBody = isDebug
        sportConfig.drawSkeleton = skeleton
        sportConfig.sportType = factory.getMultiSportType()
        sportConfig.loadModel(object : TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
                if (DeviceUtil.isTsRk3588()) {
                    showLoadingDialog(getString(R.string.model_loding))
                }
            }

            override fun onSucceed(list: List<TsModelInfo?>?) {
            }

            override fun onFinish(
                list: List<TsModelInfo?>?,
                errrorList: List<TsModelInfo?>?
            ) {
                hideLoadingDialog()
            }

            override fun onEnd(str: String?) {
            }

        })

        parser = factory.getParser()

        multiSportProcessor.setConfig(sportConfig)
        multiSportProcessor.open(mBinding.overlay)
        multiSportProcessor.setBodyFilter(ThreeSportStandFilter())
        multiSportProcessor.setParser(parser)
        multiSportProcessor.setSmooth(factory.enableSmooth())
        multiSportProcessor.setCallback(object : MultiSportCallbackImpl() {
            override fun onResult(result: MultiSportResult) {
                /* var listMan: MutableList<GroupSportBaseMan>? = when {
                     o is GroupPushUpParser -> {
                         o.groupPushUpManList as? MutableList<GroupSportBaseMan>
                     }

                     o is GroupCrunchParser -> {
                         o.groupCrunchManList as? MutableList<GroupSportBaseMan>
                     }

                     else -> mutableListOf()
                 }*/
                viewModel.handleProcessorCallback(result.groupManList)

            }

            override fun onError(index: Int, code: Int, error: String) {}
        })
        multiSportProcessor.onInit()
    }

    private fun setTip(tip: String) {
    }

    private fun hideTip() {
    }

    /**
     * 下一组
     */
    private fun next() {
        //切换Pose模型ID
        mBinding.overlay.multiRegion.setDrawing(true)
        sportConfig.poseModelId = factory.getFacePoseModelId()
        sportConfig.bodyModelId = factory.getFaceBodyModelId()
        multiSportProcessor.setBodyFilter(ThreeSportStandFilter())
        multiSportProcessor.setConfig(sportConfig)
        timeOutHelper.start()
        //摄像头预览全屏
        val params = mBinding.cameraView.layoutParams as ConstraintLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.rightMargin = 0
        params.bottomMargin = 0
        mBinding.cameraView.layoutParams = params

        //显示所有空闲图片
        imgMap.forEach { (_, u) ->
            u.visibility = View.VISIBLE
        }
        viewModel.reset()
        multiSportProcessor.onReset()
        viewModel.toStatusIdentifyFace()
    }

    /**
     * 重测试
     */
    private fun retest() {
        multiSportProcessor.onReset()
        viewModel.retest()
    }

    override fun onResume() {
        super.onResume()
        multiSportProcessor.onReset()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        multiSportProcessor.onRelease()
        parser.resetScore()
        sportConfig.unloadModel()
    }
}