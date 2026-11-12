package com.ty.bus.multi.core.ui.sport.thirtysport

import adl.ssp.common.report.model.IndicatorsResultDto
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.overlay.SportOverlayMultiRegion
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseCollabSportActivity
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.ThirtySportConfig
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.databinding.AcThirtySportBinding
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.ui.report.SportReportActivity
import com.ty.tybusbase.utils.ToastUtil

/**
 * 30人跳绳页面：公共协同 UI 由 [BaseCollabSportActivity] 承载，
 * 这里只保留跳绳特有部分：今日最佳/本地报告、视频调试、配置钩子。
 */
class ThirtySportActivity :
    BaseCollabSportActivity<AcThirtySportBinding, ThirtySportViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/0201010016_177691236skip_ten.mp4"

    /***************************视频播放调试*************************/

    /***************************控件 getter***************************/

    override fun cameraView(): AdlCameraPreviewView = mBinding.cameraView
    override fun overlayView(): SportOverlayMultiRegion = mBinding.overlay
    override fun titleView(): TextView = mBinding.tvTitle
    override fun cameraTipsView(): TextView = mBinding.tvCameraTips
    override fun sportViewByArea(area: DisplayType): BaseMultiSportViewNoAnimation = when (area) {
        DisplayType.LEFT -> mBinding.sportViewLeft
        DisplayType.RIGHT -> mBinding.sportViewRight
        else -> mBinding.sportViewMain
    }

    override fun tabLeft(): TextView = mBinding.tvTabLeft
    override fun tabMain(): TextView = mBinding.tvTabMain
    override fun tabRight(): TextView = mBinding.tvTabRight
    override fun areaNameView(): TextView = mBinding.tvAreaName
    override fun timeBar(): View = mBinding.llTime
    override fun timeText(): TextView = mBinding.tvTime
    override fun countDownNoticeView(): TextView = mBinding.tvCountDownNotice
    override fun countDownGoView(): View = mBinding.tvCountDownGo
    override fun startNowView(): View = mBinding.tvStartNow
    override fun resultContainer(): ViewGroup = mBinding.flResultViewContainer

    override fun currentBestViews(): CurrentBestViews = CurrentBestViews(
        group = mBinding.groupCurrentBest,
        name = mBinding.tvCurrentBestName,
        count = mBinding.tvCurrentBestCount,
        unit = mBinding.tvCurrentBestUnit
    )

    /***************************运动特有钩子***************************/

    override fun readSportConfig(intent: Intent) {
        viewModel.thirtySportConfig =
            intent.getSerializableExtra("sport_config") as? ThirtySportConfig
    }

    override fun provideDefaultSportInfo(): SceneSportInfoEntity = SceneSportInfoEntity().apply {
        sportSkuId = "0201010001"
        sportItemCode = SportConstant.SPORT_SKIP_ROPE
        endValue = "60000"
        testCount = "10"
    }

    override fun sportStartText(): String? = viewModel.thirtySportConfig?.sportStartText

    override fun countdownText(): String? = viewModel.thirtySportConfig?.countdownText

    override fun isMeetingMode(): Boolean =
        viewModel.thirtySportConfig?.sportModel == SportModel.MEETING

    override fun resultTitleText(): String? = viewModel.thirtySportConfig?.resultPageText

    override fun isSupportNextGroup(): Boolean =
        viewModel.thirtySportConfig?.isSupportNextGroup == true

    override fun voiceBroadcastSupported(): Boolean =
        viewModel.thirtySportConfig?.isVoiceBroadcastSupported == true

    override fun autoFinishTime(): Long? = viewModel.thirtySportConfig?.autoFinishTime

    override fun onResultReportClick(item: ThirtySportResultItem) {
        when (item.displayType) {
            DisplayType.MAIN -> viewReport(viewModel.getIndicatorsResultDto(item.trackNumber))
            else -> super.onResultReportClick(item)
        }
    }

    /***************************跳绳特有初始化***************************/

    override fun initViewExtra() {
        //倒计时、开始按钮、提示文案属于整个运动，统一由Activity管理，各区域面板共享引用
        listOf(mBinding.sportViewMain, mBinding.sportViewLeft, mBinding.sportViewRight)
            .forEach { it.bindCountDownViews(mBinding.tvCountDownNotice, mBinding.tvCountDownGo) }

        //如果是赛事，则隐藏今日最佳
        viewModel.matchId?.let {
            mBinding.groupTodayTop.visibility = View.GONE
        }
        //自主，组织，随堂测隐藏今日最佳
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST || viewModel.sceneId == SceneConstants.INDEPENDENT_TEST || viewModel.sceneId == SceneConstants.CLASS_TEST) {
            mBinding.groupTodayTop.visibility = View.GONE
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

    private fun initVideo() {
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

    /***************************今日最佳***************************/

    override fun initObserver() {
        super.initObserver()
        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }
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
            setTop1Count(topEntity.originalSportResult.toInt())
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
}
