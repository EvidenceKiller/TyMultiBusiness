package com.ty.bus.multi.core.ui.sport.tensitup

import android.content.Intent
import android.os.Handler
import android.os.Looper
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
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseCollabSportActivity
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.view.tensitup.TenSitUpSportView
import com.ty.bus.multi.databinding.ActivityTenSitUpBinding
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.SpeechWrapper

/**
 * 十人仰卧起坐页面：公共协同 UI 由 [BaseCollabSportActivity] 承载（单屏，DisplayType 恒为 MAIN），
 * 这里保留仰卧起坐特有部分：识别确认提示、独立3-2-1动画（readyView）、
 * 摄像头输入模式、面板内置结算（重测/下一组）、视频调试。
 */
class TenSitUpSportActivity :
    BaseCollabSportActivity<ActivityTenSitUpBinding, TenSitUpSportViewModel>() {

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_sit_up-2k.mp4"

    /***************************识别确认提示*************************/
    private var tipCountDownCount = 3  //提示的倒计时时间
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val tipRunnable: Runnable = object : Runnable {
        override fun run() {
            tipCountDownCount--
            if (tipCountDownCount == 0) {
                startIdentify()
                return
            }
            mBinding.btnInTipConfirm.text =
                getString(R.string.confirm_countdown, tipCountDownCount)
            mainHandler.postDelayed(this, 1000)
        }
    }

    /***************************控件 getter***************************/

    override fun cameraView(): AdlCameraPreviewView = mBinding.cameraView
    override fun overlayView(): SportOverlayMultiRegion = mBinding.overlay
    override fun titleView(): TextView = mBinding.tvTitle
    override fun cameraTipsView(): TextView = mBinding.tvCameraTips
    override fun resultContainer(): ViewGroup = mBinding.layout

    //十人页单面板：主/左/右区域共用同一个运动面板
    override fun sportViewByArea(area: com.ty.tybusbase.entity.DisplayType): BaseMultiSportViewNoAnimation =
        mBinding.sportView

    override fun sportViews(): List<BaseMultiSportViewNoAnimation> = listOf(mBinding.sportView)

    override fun startNowView(): View = mBinding.tvStartNow

    /***************************运动特有钩子***************************/

    //保持摄像头输入模式（VM默认streamType=1）
    override fun initDefaultStreamType() {
        viewModel.streamType = 2
    }

    override fun readSportConfig(intent: Intent) {
        viewModel.tsConfig = intent.getSerializableExtra("sport_config") as? SixSportConfig
    }

    override fun provideDefaultSportInfo(): SceneSportInfoEntity = SceneSportInfoEntity().apply {
        sportSkuId = SportConstant.SPORT_SIT_UP + "0024"
        sportItemCode = SportConstant.SPORT_SIT_UP
        endValue = "60000"
        testCount = "10"
    }

    override fun initOverlay() {
        overlayView().setSportType(2)
        super.initOverlay()
    }

    override fun overlayConfigKey(): String = MultiCoreConstant.TEN_SIT_UP_REGION_KEY

    override fun isSupportNextGroup(): Boolean =
        viewModel.tsConfig?.isSupportNextGroup == true

    //进入页面先展示"识别确认提示"，确认或3秒后才开始识别
    override fun autoStartIdentify(): Boolean = false

    //倒计时提示文案："X秒后自动开始"（原页面文案样式；"立即开始"按钮同在group内，随之显示）
    override fun onCountDownNotice(second: String) {
        val count = second.toLongOrNull() ?: return
        mBinding.groupTimeCountDown.visible()
        mBinding.tvTimeDown.text =
            com.ty.tybusbase.expand.getString(R.string.auto_start_second_after, count)
    }

    override fun hideCountDownNotice() {
        mBinding.groupTimeCountDown.gone()
    }

    //"立即开始"：与tv_time_down同属group_time_count_down统一控制显隐，点击直接开赛
    override fun bindStartNow() {
        startNowView()?.setOnClickListener { viewModel.readyGo() }
    }

    override fun showStartNowButton(visible: Boolean) {
        //显隐由group_time_count_down统一控制（保持原页面行为）
    }

    //3-2-1倒计时动画：页面独立的readyView
    override fun playCountDownGoAnimation(onFinished: () -> Unit) {
        mBinding.overlay.multiRegion?.setDrawing(false)
        SpeechWrapper.stop()
        mBinding.readyView.startReady { onFinished() }
    }

    //运动剩余时间内嵌在面板顶部提示
    override fun showSportTime(text: String) {
        getAreaSportViewByDisplay().updateTime(text)
    }

    //十人页单屏：无副屏进入运动态的处理
    override fun onCountDownGoFinished() {
        viewModel.toStatusSportStart()
        getAreaSportViewByDisplay().onStarted()
    }

    //十人页使用面板内置结算（含"重测"/"下一组"），不展示30人结果页
    override fun onSportFinished(items: List<ThirtySportResultItem>) {
        (getAreaSportViewByDisplay() as TenSitUpSportView).sportFinish()
    }


    /***************************十人页特有初始化***************************/

    override fun initViewExtra() {
        //初始化运动View按钮监听（面板内置结算）
        mBinding.sportView.listener = object : ResultViewBtnClickListener {
            override fun onStartClick() {
                viewModel.readyGo()
            }

            override fun onRetestClick() {
                retest()
            }

            override fun onNextClick() {
                next()
            }

            override fun onEndTestClick() {
                showExitDialog()
            }
        }
        if (viewModel.tsConfig?.isSupportNextGroup == true) {
            mBinding.sportView.showNextBtn()
        }

        //识别确认提示：3秒倒计时或手动确认后开始识别
        mBinding.tvInTip.text = viewModel.getSportStartTip()
        mBinding.btnInTipConfirm.setOnClickListener {
            mainHandler.removeCallbacks(tipRunnable)
            startIdentify()
        }
        mBinding.btnInTipConfirm.text =
            getString(R.string.confirm_countdown, tipCountDownCount)
        mainHandler.postDelayed(tipRunnable, 1000)

        if (viewModel.streamType == 2) {
            initVideoView()
            initVideo()
        } else {
            mBinding.layout.visibility = View.GONE
            mBinding.llBtn.visibility = View.GONE
        }
    }

    /**
     * 确认提示结束：开始识别（处理器切准备态、隐藏提示、显示运动面板）
     */
    private fun startIdentify() {
        viewModel.openTsProcess()
        mBinding.groupInTip.visibility = View.GONE
        mBinding.sportView.visibility = View.VISIBLE
    }

    /**
     * 下一组
     */
    private fun next() {
        mBinding.overlay.multiRegion?.setDrawing(true)
        timeOutHelper.start()
        mBinding.readyView.reset()
        viewModel.reset()
    }

    /**
     * 重测试
     */
    private fun retest() {
        mBinding.readyView.reset()
        viewModel.retest()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetProcessor()
    }

    override fun onPause() {
        super.onPause()
        mBinding.readyView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        videoReader?.release()
        SpeechWrapper.stop()
    }

    /***************************视频播放调试*************************/

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
                frameTime += 1000 / 24
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
