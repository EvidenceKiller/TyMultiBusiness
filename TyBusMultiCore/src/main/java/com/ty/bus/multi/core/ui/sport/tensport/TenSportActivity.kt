package com.ty.bus.multi.core.ui.sport.tensport

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import adl.ssp.common.report.model.IndicatorsResultDto
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
import com.ty.bus.multi.core.base.BaseCollabSportActivity
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.view.tensport.TenSportSportView
import com.ty.bus.multi.databinding.AcTenSportBinding
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.ui.report.SportReportActivity
import com.ty.tybusbase.utils.ToastUtil

/**
 * 10人跳绳页面：公共流程由 [BaseCollabSportActivity] 承载（单屏、无区域导航），
 * 这里只保留十人页特有部分：摄像头模式、面板内嵌的开始按钮/倒计时/时间栏、今日最佳、视频调试。
 */
class TenSportActivity :
    BaseCollabSportActivity<AcTenSportBinding, TenSportViewModel>() {

    //视频播放调试
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/0201010016_177691236skip_ten.mp4"
    /***************************控件 getter***************************/

    override fun cameraView(): AdlCameraPreviewView = mBinding.cameraView
    override fun overlayView(): SportOverlayMultiRegion = mBinding.overlay
    override fun titleView(): TextView = mBinding.tvTitle
    override fun cameraTipsView(): TextView = mBinding.tvCameraTips
    override fun resultContainer(): ViewGroup = mBinding.clSkipSportViewContainer

    //十人页单面板：主/左/右区域共用同一个运动面板
    override fun sportViewByArea(area: DisplayType): BaseMultiSportViewNoAnimation =
        mBinding.sportView

    override fun sportViews(): List<BaseMultiSportViewNoAnimation> = listOf(mBinding.sportView)

    /***************************十人页特有钩子***************************/

    //保持摄像头输入模式（VM默认streamType=1）
    override fun initDefaultStreamType() {
    }

    override fun readSportConfig(intent: Intent) {
        viewModel.tenSportConfig = intent.getSerializableExtra("sport_config") as? TenSportConfig
    }

    override fun provideDefaultSportInfo(): SceneSportInfoEntity = SceneSportInfoEntity().apply {
        sportSkuId = "0201010001"
        sportItemCode = SportConstant.SPORT_SKIP_ROPE
        endValue = "60000"
        testCount = "10"
    }

    override fun sportStartText(): String? = viewModel.tenSportConfig?.sportStartText

    override fun countdownText(): String? = viewModel.tenSportConfig?.countdownText

    override fun isMeetingMode(): Boolean =
        viewModel.tenSportConfig?.sportModel == SportModel.MEETING

    override fun resultTitleText(): String? = viewModel.tenSportConfig?.resultPageText

    override fun isSupportNextGroup(): Boolean =
        viewModel.tenSportConfig?.isSupportNextGroup == true

    override fun voiceBroadcastSupported(): Boolean =
        viewModel.tenSportConfig?.isVoiceBroadcastSupported == true

    override fun autoFinishTime(): Long? = viewModel.tenSportConfig?.autoFinishTime

    //结果页报告：十人跳绳走本地指标报告
    override fun onResultReportClick(item: com.ty.bus.multi.core.entity.ThirtySportResultItem) {
        viewReport(viewModel.getIndicatorsResultDto(item.trackNumber))
    }

    /***************************面板内嵌控件适配***************************/

    //开始按钮内嵌在运动面板中
    override fun bindStartNow() {
        (getAreaSportViewByDisplay(DisplayType.MAIN) as TenSportSportView).startNowCallback = {
            onStartNowClicked()
        }
    }

    override fun showStartNowButton(visible: Boolean) {
        (getAreaSportViewByDisplay(DisplayType.MAIN) as TenSportSportView)
            .visibilityStartBtn(if (visible) View.VISIBLE else View.GONE)
    }

    //倒计时提示文案内嵌在运动面板中
    override fun showCountDownNotice(text: String) {
        getAreaSportViewByDisplay(DisplayType.MAIN).setCountDownNotice(text)
    }

    //运动倒计时时间内嵌在运动面板中
    override fun showSportTime(text: String) {
        getAreaSportViewByDisplay(DisplayType.MAIN).updateTime(text)
    }

    //时间警示样式内嵌在运动面板中
    override fun onSportTimeWarning(res: Int) {
        getAreaSportViewByDisplay(DisplayType.MAIN).changeTimeTextBg(res)
    }

    /***************************十人页特有初始化***************************/

    override fun initData() {
        super.initData()
        //如果是赛事，请求今日最佳数据
        viewModel.getBestRankTop()
    }

    override fun initViewExtra() {
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

    override fun initObserver() {
        super.initObserver()
        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }
    }

    override fun reset() {
        super.reset()
        //十人页保持原行为：重置时重建相机并重新登录预览
        if (viewModel.streamType == 1) {
            viewModel.reinitCameraAndResume()
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
                .placeholder(com.ty.bus.multi.R.mipmap.ic_head_default)
                .error(com.ty.bus.multi.R.mipmap.ic_head_default).into(mBinding.civHeader)
        }
    }

    private fun viewReport(indicatorDo: IndicatorsResultDto?) {
        indicatorDo?.apply {
            val intent = Intent(this@TenSportActivity, SportReportActivity::class.java)
            intent.putExtra("report_indicator", this@apply)
            intent.putExtra("type", 1)
            startActivity(intent)
        } ?: run {
            ToastUtil.getInstance(this, getString(com.ty.bus.multi.R.string.no_report))
                .show()
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
}
