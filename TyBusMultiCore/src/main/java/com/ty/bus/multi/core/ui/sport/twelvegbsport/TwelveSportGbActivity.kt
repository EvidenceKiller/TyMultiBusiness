package com.ty.bus.multi.core.ui.sport.twelvegbsport

import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlHandlerThread
import com.adl.base.common.AdlToast
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.sport.standard.JumpConfig
import com.adl.sport.standard.JumpType
import com.adl.sport.standard.jumpcore.BaseSportState
import com.adl.sport.standard.jumpcore.JumpRopeProcessor
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.config.FourSportConfig
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.AcFourGbSportBinding
import com.ty.bus.multi.databinding.AcTwelveGbSportBinding
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.ui.webview.CommonWebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class TwelveSportGbActivity : BaseMultiSportActivity<AcTwelveGbSportBinding, TwelveSportGbViewModel>() {

    companion object {
        const val INTENT_KEY_VIDEO_PATH: String = "TwelveSportGbVideoPath"

        private const val TAG = "ZXN_TEST"
    }

    private val jumpConfig: JumpConfig by lazy { JumpConfig() }
    private val jumpProcessor by lazy { JumpRopeProcessor(jumpConfig) }

    private var frameDispatcher: DynamicSportFrameDispatcher? = null

    private var cameraView: AdlCameraPreviewView? = null

    private val isDebug: Boolean by lazy { BusiBaseConfig.getDebugEnable() }
    private val skeletonEnable: Boolean by lazy { BusiBaseConfig.getSkeletonEnable() }

    private val initFlag = AtomicInteger(0)

    private var startDialog: CommonTipDialog? = null
    /** "是否清空已识别结果"对话框（协同识别计数提示，展示期间内容可动态更新） */
    private var clearIdentifyDialog: CommonTipDialog? = null

    private val simpleFormat = SimpleDateFormat("m:ss")

    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
    private val recordProcessor: AdlCameraFrameProcessor by lazy {
        if (isDebug) {
            AdlCameraFrameProcessor { bytes, info -> viewModel.videoRecorder.inputNv21(bytes, simpleDateFormat.format(Date(info.frameTimestamp))) }
        } else {
            AdlCameraFrameProcessor { bytes, _ -> viewModel.videoRecorder.inputNv21(bytes, "") }
        }
    }

    /***************************视频播放调试*************************/
    private var videoDebug: Boolean = false
    private var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    private var videoTextureView: TextureView? = null
    private var videoSurface: Surface? = null
    private var videoReader: AdlVideoReader? = null
    private var videoFilePath = ""
    private var frameTime: Long = 0
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
            Log.i(TAG, "TwelveSportGbActivity::onSurfaceTextureAvailable : w: $width, h: $height")
            videoSurface?.release()
            videoSurface = Surface(st)
            initVideo()
        }

        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
            videoReader?.onPause()
            videoSurface?.release()
            videoSurface = null
            return true
        }

        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
    }
    /***************************视频播放调试*************************/

    override fun initData() {
        super.initData()
        val studentList = intent.getSerializableExtra("student_list") as? ArrayList<MeetGroupDetailsEntity>
        viewModel.personIndex = intent.getIntExtra("person_index", 0)
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.fourSportConfig = intent.getSerializableExtra("sport_config") as? FourSportConfig
        if (viewModel.sportInfo == null) {
            viewModel.sportInfo = SceneSportInfoEntity().apply {
                sportSkuId = "0201010001"
                sportItemCode = SportConstant.SPORT_SKIP_ROPE
                endValue = "60000"
                testCount = "10"
            }
        }
        videoFilePath = intent.getStringExtra(INTENT_KEY_VIDEO_PATH) ?: ""
        if (videoFilePath.isNotEmpty()) {
            videoDebug = true
            viewModel.streamType = 2
        } else {
            viewModel.streamType = 1
        }
        Log.i(TAG, "TwelveSportGbActivity::initData : videoFilePath = $videoFilePath")
        viewModel.loadPerson(studentList)
        timeOutHelper.cancel()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.modelLoadLD.collectOnLifecycle { result ->
            Log.i(TAG, "TwelveSportGbActivity::initObserver::modelLoadLD: $result")
            if (result) {
                if (videoDebug) {
                    if (initFlag.incrementAndGet() == 1) {
                        viewModel.toStatusPrepare()
                    }
                } else {
                    if (initFlag.incrementAndGet() == 2) {
                        viewModel.toStatusPrepare()
                    }
                }
            }
        }

        viewModel.startPreviewLD.collectOnLifecycle { pair ->
            Log.i(TAG, "TwelveSportGbActivity::initObserver::startPreviewLD")
            cameraView?.startPreview(pair.first, object : AdlCameraPreviewView.OnViewStatusListener {
                override fun onAvailable() {}

                override fun onCameraOpen() {
                    Log.i(TAG, "TwelveSportGbActivity::initCameraView::onCameraOpen")
                }

                override fun onCameraReOpen() {
                    Log.i(TAG, "TwelveSportGbActivity::initCameraView::onCameraReOpen")
                    hideTips()
                }

                override fun onDestroyed() {}

                override fun onStreamLoss() {
                    Log.i(TAG, "TwelveSportGbActivity::initCameraView::onStreamLoss")
                    setTips(getString(R.string.camera_loss_tips))
                }

                override fun onOpenFail() {
                    Log.e(TAG, "TwelveSportGbActivity::initCameraView : onOpenFail")
                    setTips(getString(R.string.ipc_camera_fail_tip))
                }
            })
            cameraView?.addFrameProcessor(frameDispatcher)
            if (initFlag.incrementAndGet() == 2) {
                viewModel.toStatusPrepare()
            }
        }

        viewModel.stopPreviewLD.collectOnLifecycle { processor ->
            Log.i(TAG, "TwelveSportGbActivity::initObserver::stopPreviewLD")
            cameraView?.removeFrameProcessor(frameDispatcher)
            cameraView?.stopPreview()
        }

        viewModel.cameraLoginStateLD.collectOnLifecycle {
            Log.i(TAG, "TwelveSportGbActivity::initObserver::cameraLoginStateLD: $it")
            if (it) hideTips() else setTips(getString(R.string.ipc_camera_fail_tip))
        }

        viewModel.resultLD.collectOnLifecycle {
            mBinding.sportView.updateScore(it.second)
        }

        viewModel.singleResultLD.collectOnLifecycle {
            mBinding.sportView.updateScore(it.second.first, it.second.second)
        }

        viewModel.identifiedLD.collectOnLifecycle {
            AdlLogger.d("identifiedLD: 道次：${it.first}--人员：${it.second.studentEntity!!.accountName}")
            mBinding.sportView.setUserInfo(it)
            timeOutHelper.cancel()
        }

        viewModel.sportStartedLD.collectOnLifecycle {
            Log.i(TAG, "TwelveSportGbActivity::initObserver::sportStartedLD")
            //运动开始，底部导航栏切换为"x人运动中"文案
            jumpProcessor.changeStatus(BaseSportState.Sporting)
            jumpProcessor.setStart(true)

            if (!videoDebug) {
                startRecord()
            }
        }

        viewModel.sportStopLD.collectOnLifecycle {
            Log.i(TAG, "TwelveSportGbActivity::initObserver::sportStopLD")
            if (!videoDebug) {
                stopRecord()
            }
            //运动开始，底部导航栏切换为"x人运动中"文案
            jumpProcessor.setStart(false)
            jumpProcessor.changeStatus(BaseSportState.Standby)
            jumpProcessor.reset()

            mBinding.llTime.visibility = View.GONE
        }

        viewModel.changeSportViewStateLD.collectOnLifecycle {
            mBinding.sportView.changeState(it.second.first, it.second.second)
        }

        viewModel.countDownNoticeLD.collectOnLifecycle {
            //倒计时提示文案与开始按钮属于整个运动，统一在Activity层展示
            val configText = viewModel.fourSportConfig?.countdownText
            val str = if (!TextUtils.isEmpty(configText)) "${configText}: ${it.second}" else "${
                com.ty.tybusbase.expand.getString(
                    R.string.race_start_countdown
                )
            }：${it.second}"
            mBinding.tvCountDownNotice.text = str
            mBinding.tvCountDownNotice.visibility = View.VISIBLE
            mBinding.tvStartNow.visibility = View.VISIBLE
        }

        viewModel.countDownFinishLD.collectOnLifecycle {
            startDialog?.dismiss()
            //竞技开始，隐藏开始按钮与等待文案，显示运动倒计时时间栏
            mBinding.tvStartNow.visibility = View.GONE
            mBinding.tvCountDownNotice.visibility = View.GONE
            mBinding.sportView.readyGo {
                mBinding.llTime.visibility = View.VISIBLE
                viewModel.toStatusSportStart()
            }
        }

        viewModel.sportCountDownLD.collectOnLifecycle {
            Log.i(TAG, "TwelveSportGbActivity::initObserver::sportCountDownLD: $it")
            //运动倒计时属于整个运动，统一在Activity层更新时间栏
            mBinding.tvTime.text = simpleFormat.format(Date((it.second) * 1000))
        }

        viewModel.toDoneLD.collectOnLifecycle { resultArray ->
            Log.i(TAG, "TwelveSportGbActivity::initObserver::toDoneLD")
            //结束清除骨骼数据等
            mBinding.overlay.clearScreen()
            setResultView(resultArray.toList())
        }

        viewModel.changeTimeTextBgLD.collectOnLifecycle {
            //时间栏背景属于整个运动，统一在Activity层设置
            mBinding.llTime.setBackgroundResource(R.mipmap.pic_time_warm_more)
        }

        viewModel.timeOutLD.observe(this) {
            Log.i(TAG, "TwelveSportGbActivity::initObserver::timeOutLD")
            finish()
        }

        viewModel.showReportLD.collectOnLifecycle {
            val intent = Intent(this, CommonWebViewActivity::class.java)
            intent.putExtra("url", it)
            intent.putExtra("show_white_board", true)
            startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheFont(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName

        mBinding.tvStartNow.setOnClickListener {
            startDialog = CommonTipDialog().setCallback {
                //如果是运动会模式，则切到准备状态
                if (viewModel.fourSportConfig?.sportModel == SportModel.MEETING) {
                    viewModel.sportResultMap.forEach { (index, _) ->
                        mBinding.sportView.changeState(index, MultiSportCardViewState.PREPARED)
                    }
                }
                startDialog = null
                viewModel.readyGo()
            }
            startDialog?.show(supportFragmentManager, "tips")
        }

        mBinding.sportView.setDebug(isDebug)
        mBinding.sportView.setSportStartedText(viewModel.fourSportConfig?.sportStartText)
        mBinding.sportView.bindCountDownViews(mBinding.tvCountDownNotice, mBinding.tvCountDownGo)

        if (viewModel.fourSportConfig?.sportModel == SportModel.MEETING) {
            mBinding.tvStartNow.visibility = View.VISIBLE
        }

        if (isDebug || videoDebug) {
            mBinding.ivBg.gone()
        }

        initJumpConfig()
        initJumpProcessor()
        initFrameDispatcher()
        initOverlay()
        if (videoDebug) {
            initVideoButton()
            initVideoView()
        } else {
            initCameraView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "TwelveSportGbActivity::onDestroy")
        mBinding.sportView.release()

        frameDispatcher?.removeFrameProcessor(jumpProcessor)
        frameDispatcher?.quit()
        frameDispatcher = null
        jumpProcessor?.onReset()
        jumpProcessor?.onRelease()
        if (videoDebug) {
            videoReader?.onPause()
            videoReader?.release()
            videoReader = null
            videoSurface?.release()
            videoSurface = null
        } else {
            cameraView?.removeFrameProcessor(frameDispatcher)
            cameraView?.stopPreview()
            cameraView?.release()
        }
        mBinding.overlay.release()
        AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }

    override fun onStatusPrepare() {
        Log.i(TAG, "TwelveSportGbActivity::onStatusPrepare")
        mBinding.sportView.reset()
        mBinding.llTime.visibility = View.GONE

        jumpProcessor.changeStatus(BaseSportState.Standby)
        jumpProcessor.setStart(false)

        viewModel.toStatusIdentifyFace()
    }

    override fun onStatusIdentify() {
        Log.i(TAG, "TwelveSportGbActivity::onStatusIdentify")
        jumpProcessor.changeStatus(BaseSportState.Sporting)
    }

    override fun onStatusIdentifySuccess() {
        Log.i(TAG, "TwelveSportGbActivity::onStatusIdentifySuccess")
    }

    override fun onStatusSport() {
        super.onStatusSport()
        Log.i(TAG, "TwelveSportGbActivity::onStatusSport")
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        Log.i(TAG, "TwelveSportGbActivity::onStatusEnd")
        viewModel.fourSportConfig?.autoFinishTime?.let {
            if (it > 0) {
                AdlHandlerThread.getUIHandler().postDelayed({ finish() }, it)
            }
        }
    }

    private fun initJumpConfig() {
        jumpConfig.debug = isDebug
        jumpConfig.checkRope = true
        jumpConfig.checkDownRope = true
        jumpConfig.drawSkeleton = true
        jumpConfig.drawRope = isDebug
        jumpConfig.multiPersonType = JumpConfig.MultiPersonType.MULTI_PERSON_FOUR
        jumpConfig.setJumpType(JumpType.doubleShakeJump, object: TsModelResPool.OnLoadModelCallback {
            override fun onStart() {
                showLoadingDialog("模型加载中")
            }

            override fun onSucceed(p0: List<TsModelInfo?>?) {
                hideLoadingDialog()
                jumpProcessor.initPlayGround()
                viewModel.modelLoaded()
            }

            override fun onFinish(p0: List<TsModelInfo?>?, p1: List<TsModelInfo?>?) {
            }

            override fun onEnd(p0: String?) {
                hideLoadingDialog()
            }
        })
    }

    private fun initJumpProcessor() {
        jumpProcessor.setJumpRopeCallBack { _, jumpers ->
            viewModel.processFrame(jumpers)
        }
        viewModel.addFrameProcessor(jumpProcessor)
    }

    private fun initOverlay() {
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(isDebug)
        mBinding.overlay.enableFace(true)
        mBinding.overlay.skeletonDrawer.enableMarker(skeletonEnable)
        mBinding.overlay.skeletonDrawer.enableLine(skeletonEnable)
        mBinding.overlay.enableDrawSkeleton(skeletonEnable)
        mBinding.overlay.setMarkerRecognizerConfig(jumpConfig.markerRecognizerConfig, OnRenderScene.StatusSportDoing)
        mBinding.overlay.setListener {
            jumpProcessor.combineJumpRopeOverlay(mBinding.overlay)
            mBinding.overlay.refresh()
        }
    }

    private fun initFrameDispatcher() {
        if (frameDispatcher != null) return
        val fpsCounter = ElementFpsCounter("Camera").apply {
            setPosition(20f, 270f)
        }
        frameDispatcher = DynamicSportFrameDispatcher(fpsCounter).apply {
            enableDebug(isDebug)
            setEnableDiscard(true)
            setFps(20)
            setRotate(0)
            setMirror(true)
            addFrameProcessor(viewModel.getFrameProcessor())
        }
    }

    private fun initCameraView() {
        Log.i(TAG, "TwelveSportGbActivity::initCameraView")
        val size = calPreviewLayoutSize(viewModel.getPreviewSize())
        val cameraParams = FrameLayout.LayoutParams(size.width, size.height)
            .apply {
                gravity = Gravity.CENTER
            }
        val layoutParams = ConstraintLayout.LayoutParams(size.width, size.height)
            .apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }

        cameraView = LayoutInflater.from(this)
            .inflate(R.layout.base_camera_view, null) as AdlCameraPreviewView
        mBinding.flContainer.addView(cameraView, 0, cameraParams)
        mBinding.flContainer.layoutParams = layoutParams
        mBinding.flContainer.requestLayout()
        cameraView?.scaleX = -1f

        viewModel.initCamera()
    }

    private fun initVideoButton() {
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

    private fun initVideoView() {
        Log.i(TAG, "TwelveSportGbActivity::initVideoView")
        val size = calPreviewLayoutSize(viewModel.getPreviewSize())
        val textureParams = FrameLayout.LayoutParams(size.width, size.height).apply {
            gravity = Gravity.CENTER
        }
        val containerParams = ConstraintLayout.LayoutParams(size.width, size.height).apply {
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }

        if (videoTextureView == null) {
            videoTextureView = TextureView(this).also {
                it.surfaceTextureListener = surfaceTextureListener
                mBinding.flContainer.addView(it, 0, textureParams)
            }
        } else {
            videoTextureView?.layoutParams = textureParams
        }
        videoTextureView?.scaleX = -1f
        mBinding.flContainer.layoutParams = containerParams
        mBinding.flContainer.requestLayout()
    }

    private fun initVideo() {
        Log.i(TAG, "TwelveSportGbActivity::initVideo")
        val file = File(videoFilePath)
        if (!file.exists() || file.length() == 0L) {
            AdlToast.show("视频文件不存在或为空")
            return
        }
        if (videoSurface == null) {
            return
        }
        videoReader = AdlVideoReader().apply {
            setPlayMode(playMode)
            setSurface(videoSurface)
            enableRender(true)
            setCallback(object : VideoReaderCallback {
                override fun onError(e: Exception?) {
                    Log.e(TAG, "TwelveSportGbActivity::initVideo::onError: ", e)
                    AdlToast.show("视频加载失败: ${e?.message}")
                }

                override fun onFrame(yuv: ByteArray?, width: Int, height: Int, time: Long) {
                    if (yuv == null) {
                        return
                    }
                    frameTime += 33
                    val info = AdlCameraPreviewInfo(width, height)
                    info.frameTimestamp = frameTime
                    info.imageId = frameTime.toString()
                    frameDispatcher?.onFrame(yuv, info)
                }

                override fun onComplete() {
                    Log.i(TAG, "TwelveSportGbActivity::initVideo::onComplete")
                }
            })
            openFile(videoFilePath)

            AdlHandlerThread.getUIHandler().postDelayed({ videoReader?.onStart() }, 500)
        }
    }

    private fun calPreviewLayoutSize(previewSize: Size): Size {
        val previewW = previewSize.width
        val previewH = previewSize.height
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val scale = min(screenW.toFloat() / previewW.toFloat(),
            screenH.toFloat() / previewH.toFloat())
        return Size((previewW * scale).toInt(), (previewH * scale).toInt())
    }






    /**
     * 收集 Flow。页面处于 STARTED 及以上状态才派发事件，退到后台暂停。
     * 注意：flowWithLifecycle 在每次回到 STARTED 时会重新订阅，因此 replay>0（粘性）的 Flow
     * 会把最近一次缓存事件重放；一次性事件请在 ViewModel 侧用 replay=0 的非粘性流。
     */
    private fun <T> Flow<T>.collectOnLifecycle(block: suspend (T) -> Unit) {
        lifecycleScope.launch {
            flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect(block)
        }
    }

    private fun setResultView(resultList: List<MultiSportResult>) {
        mBinding.resultView.visible()
        mBinding.resultView.setConfirmOnclickListener {
            finish()
        }
        mBinding.resultView.setResultTitleText(viewModel.fourSportConfig?.resultPageText)

        mBinding.resultView.setResult(resultList)
    }

    private fun setTips(text: String) {
        AdlLogger.d(text)
        mBinding.tvCameraTips.visibility = View.VISIBLE
        mBinding.tvCameraTips.text = text
    }

    private fun hideTips() {
        mBinding.tvCameraTips.visibility = View.GONE
    }

    private fun startRecord() {
        if (viewModel.videoRecorder.isStarting) {
            return
        }
        viewModel.resetVideoPath()
        val videFile = viewModel.getVideoPath()
        Log.i(TAG, "TwelveSportGbActivity::startRecord: ${videFile.absolutePath}")
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.videoRecorder.reset()
            val previewSize = viewModel.getPreviewSize()
            viewModel.videoRecorder.onStart(videFile, previewSize.width, previewSize.height)
            frameDispatcher?.addFrameProcessor(recordProcessor)
        }
    }

    private fun stopRecord() {
        Log.i(TAG, "TwelveSportGbActivity::stopRecord")
        lifecycleScope.launch(Dispatchers.IO) {
            frameDispatcher?.removeFrameProcessor(recordProcessor)
            viewModel.videoRecorder.onStop()
        }
    }

}