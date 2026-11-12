package com.ty.bus.multi.core.ui.sport.thirtysitup

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
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.core.base.BaseCollabSportActivity
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.ThirtySitUpSportConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.view.thirtysitup.ThirtySitUpSportView
import com.ty.bus.multi.databinding.AcThirtySitUpBinding
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.expand.visible

/**
 * 30人仰卧起坐页面：公共协同 UI 由 [BaseCollabSportActivity] 承载，
 * 这里只保留仰卧起坐特有部分：视频调试、配置钩子、倒计时结束后的启动顺序。
 */
class ThirtySitUpSportActivity :
    BaseCollabSportActivity<AcThirtySitUpBinding, ThirtySitUpSportViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_sit_up-2k.mp4"

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

    override fun overlayConfigKey(): String = MultiCoreConstant.TEN_SIT_UP_REGION_KEY

    override fun readSportConfig(intent: Intent) {
        viewModel.thirtySitUpSportConfig =
            intent.getSerializableExtra("sport_config") as? ThirtySitUpSportConfig
    }

    override fun provideDefaultSportInfo(): SceneSportInfoEntity = SceneSportInfoEntity().apply {
        sportSkuId = SportConstant.SPORT_SIT_UP + "0024"
        sportItemCode = SportConstant.SPORT_SIT_UP
        endValue = "60000"
        testCount = "30"
    }

    override fun sportStartText(): String? = viewModel.thirtySitUpSportConfig?.sportStartText

    override fun countdownText(): String? = viewModel.thirtySitUpSportConfig?.countdownText

    override fun isMeetingMode(): Boolean =
        viewModel.thirtySitUpSportConfig?.sportModel == SportModel.MEETING

    override fun resultTitleText(): String? = viewModel.thirtySitUpSportConfig?.resultPageText

    override fun isSupportNextGroup(): Boolean =
        viewModel.thirtySitUpSportConfig?.isSupportNextGroup == true

    override fun voiceBroadcastSupported(): Boolean =
        viewModel.thirtySitUpSportConfig?.isVoiceBroadcastSupported == true

    override fun autoFinishTime(): Long? = viewModel.thirtySitUpSportConfig?.autoFinishTime

    //仰卧起坐暂不支持本地报告
    override fun supportResultReport(): Boolean = false

    override fun onCountDownGoFinished() {
        timeBar().visibility = View.VISIBLE
        //通知ViewModel开始计时（处理器切到运动态、卡片切到运动态）
        viewModel.toStatusSportStart()
        //主屏：刷新已识别人员的卡片为运动状态
        (getAreaSportViewByDisplay(DisplayType.MAIN) as ThirtySitUpSportView)
            .onSportStarted(viewModel.skipPersonArray)
        //副屏：只有已加入协同的才进入运动状态
        if (viewModel.isSubScreenJoined(DisplayType.LEFT)) {
            (getAreaSportViewByDisplay(DisplayType.LEFT) as ThirtySitUpSportView)
                .onSportStarted(viewModel.leftPersonArray)
            getAreaSportViewByDisplay(DisplayType.LEFT).onStarted()
        }
        if (viewModel.isSubScreenJoined(DisplayType.RIGHT)) {
            (getAreaSportViewByDisplay(DisplayType.RIGHT) as ThirtySitUpSportView)
                .onSportStarted(viewModel.rightPersonArray)
            getAreaSportViewByDisplay(DisplayType.RIGHT).onStarted()
        }
    }
    /***************************仰卧起坐特有初始化***************************/

    override fun initViewExtra() {
        //倒计时、开始按钮、提示文案属于整个运动，统一由Activity管理，各区域面板共享引用
        listOf(mBinding.sportViewMain, mBinding.sportViewLeft, mBinding.sportViewRight)
            .forEach { it.bindCountDownViews(mBinding.tvCountDownNotice, mBinding.tvCountDownGo) }

        //如果是赛事，则隐藏本次实时最高
        viewModel.matchId?.let {
            mBinding.groupCurrentBest.visibility = View.GONE
        }
        //自主，组织，随堂测隐藏本次实时最高
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST || viewModel.sceneId == SceneConstants.INDEPENDENT_TEST || viewModel.sceneId == SceneConstants.CLASS_TEST) {
            mBinding.groupCurrentBest.visibility = View.GONE
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
}
