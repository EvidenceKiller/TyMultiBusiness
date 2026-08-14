package com.ty.bus.multi.core.ui.sport.thirtysport

import adl.ssp.common.report.model.IndicatorsResultDto
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import androidx.recyclerview.widget.RecyclerView
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlHandlerThread
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.config.MultiCoreConfig
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.ThirtySportConfig
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.view.thirtysport.ThirtySportResultView
import com.ty.bus.multi.databinding.AcThirtySportBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.ui.report.SportReportActivity
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class ThirtySportActivity : BaseMultiSportActivity<AcThirtySportBinding, ThirtySportViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/0201010016_177691236skip_ten.mp4"

    /***************************视频播放调试*************************/

    private var top1Count = 0
    private lateinit var srrvResult: ThirtySportResultView
    private val number = 10
    private var startDialog: CommonTipDialog? = null
    override fun initData() {
        super.initData()
//        viewModel.streamType = 2
//        timeOutHelper.cancel()
        val studentList =
            intent.getSerializableExtra("student_list") as? ArrayList<MeetGroupDetailsEntity>
        viewModel.personIndex = intent.getIntExtra("person_index", 0)
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.thirtySportConfig = intent.getSerializableExtra("sport_config") as? ThirtySportConfig
        if (viewModel.sportInfo == null) {
            viewModel.sportInfo = SceneSportInfoEntity().apply {
                sportSkuId = "0201010001"
                sportItemCode = SportConstant.SPORT_SKIP_ROPE
                endValue = "60000"
                testCount = "10"
            }
        }
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面打开")
        viewModel.initEngine(number)
        viewModel.getBestRankTop()
        viewModel.loadPerson(studentList)
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //如果是赛事，则隐藏今日最佳
        viewModel.matchId?.let {
            mBinding.groupTodayTop.visibility = View.GONE
        }
        //自主，组织，随堂测隐藏今日最佳
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST || viewModel.sceneId == SceneConstants.INDEPENDENT_TEST || viewModel.sceneId == SceneConstants.CLASS_TEST) {
            mBinding.groupTodayTop.visibility = View.GONE
        }

        val isDebug =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.MODEL_DEBUG,
                BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        mBinding.overlay.enableDraw(isDebug)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_ABLE,
                BusiBaseConstantsValue.DEFAULT_SKELETON
            )
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number",
            RoleConfigMulti::class.java
        )
        uiConfig.number = number
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        viewModel.combineOverlay(mBinding.overlay)
        mBinding.overlay.post {
            viewModel.openTsProcess()
        }
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName

        mBinding.sportView.startNowCallback = {
            startDialog = CommonTipDialog().setCallback {
                //如果是运动会模式，则切到准备状态
                if (viewModel.thirtySportConfig?.sportModel == SportModel.MEETING) {
                    viewModel.skipPersonArray.forEach { key, value ->
                        mBinding.sportView.changeState(
                            value.number,
                            MultiSportCardViewState.PREPARED
                        )
                    }
                }
                startDialog = null
                viewModel.readyGo()
            }
            startDialog?.show(supportFragmentManager, "tips")
        }

        mBinding.sportView.setSportStartedText(viewModel.thirtySportConfig?.sportStartText)

        if (viewModel.thirtySportConfig?.sportModel == SportModel.MEETING) {
            mBinding.sportView.visibilityStartBtn(View.VISIBLE)
        }
        if (viewModel.streamType == 2) {
            mBinding.groupVideo.visible()
            initVideo()
            initVideoView()
        }
    }

    private fun initVideoView() {
        mBinding.bntStart.setOnClickListener {
            videoReader?.onStart()
        }
        mBinding.btnPause.setOnClickListener {
            videoReader?.onPause()
        }
        mBinding.bntNext.setOnClickListener {
            videoReader?.nextFrame()
        }

        mBinding.bntChange.setOnClickListener {
            playMode = when (playMode) {
                AdlAvReader.PlayMode.Auto -> AdlAvReader.PlayMode.OneFrame
                AdlAvReader.PlayMode.OneFrame -> AdlAvReader.PlayMode.Auto
            }
            videoReader?.setPlayMode(playMode)
            videoReader?.onStart()
        }

        mBinding.bntReset.setOnClickListener {
            videoReader?.onReset()
        }
    }

    fun initVideo() {
        videoReader = AdlVideoReader()
        videoReader?.setPlayMode(playMode)
        videoReader?.setSurface(mBinding.surfaceView.holder.surface)
        videoReader?.enableRender(enableRender)
        videoReader?.onReset(videoFilePath)
        videoReader?.setCallback(object : VideoReaderCallback {
            override fun onError(e: Exception?) {
            }

            override fun onFrame(yuv: ByteArray, width: Int, height: Int, time: Long) {
                frameTime += 43
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
                viewModel.onFrame(yuv, info)
            }

            override fun onComplete() {

            }
        })
    }

    //模拟
    fun mockResult() {
        srrvResult = ThirtySportResultView(this).apply { measureUnit = viewModel.measureUnit }

        if (viewModel.sportInfo?.sportItemCode == SportConstant.SPORT_SKIP_ROPE) {
            srrvResult.setReportOnclickListener {
                viewReport(viewModel.getIndicatorsResultDto(it))
            }
        }

        val params =
            ConstraintLayout.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                mBinding.clSkipSportViewContainer.height
            )
        params.topToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        mBinding.clSkipSportViewContainer.addView(
            srrvResult,
            params
        )
        srrvResult.setConfirmOnclickListener {
            finish()
        }
        srrvResult.setNextOnclickListener {
            mBinding.clSkipSportViewContainer.removeView(srrvResult)
        }
        doResultViewTranslateAnimation(srrvResult)
        srrvResult.setResult(
            mutableListOf(
                MultiSportResult(AccountInfo().apply {
                    studentEntity = StudentEntity().apply {
                        accountName = "张三"
                        userFaceImgUrl = ""
                    }
                }, 1, 100, 1),
                MultiSportResult(AccountInfo().apply {
                    studentEntity = StudentEntity().apply {
                        accountName = "王五"
                        userFaceImgUrl = ""
                    }
                }, 3, 10, 2)
            ),
            if (viewModel.matchId != null) 100000 else top1Count,
            viewModel.thirtySportConfig?.isVoiceBroadcastSupported == true,
            MultiCoreConfig.isMultiSportThemeLargeScreenStyle()
        )
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.startPreviewLD.observe(this) {
            //开启摄像头
            mBinding.cameraView.startPreview(it.first, object :
                AdlCameraPreviewView.OnViewStatusListener {
                override fun onAvailable() {

                }

                override fun onCameraOpen() {
                    viewModel.onCameraOpen()
                    hideTips()
                }

                override fun onCameraReOpen() {

                }

                override fun onDestroyed() {

                }

                override fun onStreamLoss() {
                    setTips(getString(R.string.camera_loss_tips))
                }

                override fun onOpenFail() {
                    setTips(getString(R.string.ipc_camera_fail_tip))
                }
            })
            mBinding.cameraView.addFrameProcessor(it.second)
            mBinding.cameraView.textureView.scaleX = -1f
        }

        viewModel.cameraLoginStateLD.observe(this) {
            if (it) hideTips() else setTips(getString(R.string.ipc_camera_fail_tip))
        }

        viewModel.stopPreviewLD.observe(this) {
            mBinding.cameraView.stopPreview()
        }

        viewModel.resultLD.observe(this) {
            mBinding.sportView.updateScore(it)
        }

        viewModel.identifiedLD.observe(this) {
            AdlLogger.d("identifiedLD: 道次：${it.first}--人员：${it.second.studentEntity!!.accountName}")
            mBinding.sportView.setUserInfo(it)
            timeOutHelper.cancel()
        }

        viewModel.changeSportViewStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
        }

        viewModel.countDownNoticeLD.observe(this) {
            val configText = viewModel.thirtySportConfig?.countdownText
            val str = if (!TextUtils.isEmpty(configText)) "${configText}: $it" else "${
                com.ty.tybusbase.expand.getString(
                    R.string.race_start_countdown
                )
            }：$it"
            mBinding.sportView.setCountDownNotice(str)
        }

        viewModel.countDownFinishLD.observe(this) {
            startDialog?.dismiss()
            mBinding.sportView.readyGo {
                //比赛开始
                viewModel.toStatusSportStart()
            }
        }

        viewModel.sportCountDownLD.observe(this) {
            mBinding.sportView.updateTime(it)
        }

        viewModel.toDoneLD.observe(this) {
            //结束清除骨骼数据等
            mBinding.overlay.clearScreen()
            addResultView(it)
        }

        viewModel.changeTimeTextBgLD.observe(this) {
            mBinding.sportView.changeTimeTextBg(it)
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }

    }

    private fun addResultView(resultList: MutableList<MultiSportResult>) {
        //创建结果页，并添加到页面中
        srrvResult = ThirtySportResultView(this).apply {
            measureUnit = viewModel.measureUnit
            setEnergyPoint(viewModel.energyPoint)
        }
        srrvResult.setSupportReport(viewModel.isSupportReport())
        if (viewModel.sportInfo?.sportItemCode == SportConstant.SPORT_SKIP_ROPE) {
            srrvResult.setReportOnclickListener {
                viewReport(viewModel.getIndicatorsResultDto(it))
            }
        }

        val params =
            ConstraintLayout.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                mBinding.clSkipSportViewContainer.height
            )
        params.topToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        mBinding.clSkipSportViewContainer.addView(
            srrvResult,
            params
        )
        srrvResult.setConfirmOnclickListener {
            finish()
        }
        srrvResult.setResultTitleText(viewModel.thirtySportConfig?.resultPageText)
        if (viewModel.thirtySportConfig?.isSupportNextGroup == true) {
            srrvResult.setNextOnclickListener {
                mBinding.clSkipSportViewContainer.removeView(srrvResult)
                reset()
            }
        }

        doResultViewTranslateAnimation(srrvResult)
        srrvResult.setResult(
            resultList,
            if (viewModel.matchId != null) 100000 else top1Count,
            viewModel.thirtySportConfig?.isVoiceBroadcastSupported == true,
            MultiCoreConfig.isMultiSportThemeLargeScreenStyle()
        )
    }

    private fun setTips(text: String) {
        AdlLogger.d(text)
        mBinding.tvCameraTips.visibility = View.VISIBLE
        mBinding.tvCameraTips.text = text
    }

    private fun hideTips() {
        mBinding.tvCameraTips.visibility = View.GONE
    }

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        mBinding.cameraView.stopPreview()
        viewModel.thirtySportConfig?.autoFinishTime?.let {
            if (it > 0) {
                AdlHandlerThread.getUIHandler().postDelayed({ finish() }, it)
            }
        }
    }

    fun reset() {
        mBinding.sportView.reset()
        viewModel.reset()
    }

    /**
     * 初始化今日最佳
     */
    private fun initTopView(topEntity: BestRankTopEntity) {
        if (!TextUtils.isEmpty(topEntity.originalSportResult)) {
            mBinding.tvCount.visibility = View.VISIBLE
            mBinding.tvUnit.visibility = View.VISIBLE
            mBinding.tvUnit.text = viewModel.measureUnit
            mBinding.tvName.text = topEntity.accountName
            mBinding.tvCount.text = topEntity.originalSportResult
            top1Count = topEntity.originalSportResult.toInt()
            Glide.with(this).load(AdlService.getService().facePrefix + topEntity.faceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        }
    }

    private fun viewReport(indicatorDo: IndicatorsResultDto?) {
        indicatorDo?.apply {
            val intent = Intent(this@ThirtySportActivity, SportReportActivity::class.java)
            intent.putExtra("report_indicator", this@apply)
            intent.putExtra("type", 1)
            startActivity(intent)
        } ?: run {
            ToastUtil.getInstance(this, getString(R.string.no_report))
                .show()
        }
    }

    private fun doResultViewTranslateAnimation(view: View) {
        view.post {
            ObjectAnimator.ofFloat(
                view,
                "translationY",
                view.height.toFloat(),
                -view.height.toFloat()
            ).setDuration(500).start()
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面关闭")
        mBinding.cameraView.release()
        mBinding.sportView.release()
        mBinding.overlay.release()
        AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }
}