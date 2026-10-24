package com.ty.bus.multi.core.ui.set.fourgbsportset

import android.graphics.SurfaceTexture
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlHandlerThread
import com.adl.base.common.AdlToast
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoParserCallback
import com.adl.base.common.VideoReaderCallback
import com.adl.base.element.ElementFpsCounter
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelResPool
import com.adl.sport.standard.JumpConfig
import com.adl.sport.standard.JumpType
import com.adl.sport.standard.jumpcore.JumpRopeProcessor
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ActivityFourGbSportSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.utils.ToastUtil
import java.io.File
import kotlin.math.min

class FourSportGbSettingActivity : BaseActivity<ActivityFourGbSportSettingBinding, BaseViewModel>() {

    companion object {
        const val INTENT_KEY_VIDEO_PATH: String = "FourSportGbSettingVideoPath"

        private const val TAG = "ZXN_TEST"
    }

    private val resolutionType by lazy { intent.getIntExtra("res_type", 1) }

    private val cameraPreviewSize: Size by lazy {
        when (resolutionType) {
            0 -> Size(1920, 1080)
            1 -> Size(2560, 1440)
            else -> Size(3840, 2160)
        }
    }

    private val jumpConfig: JumpConfig by lazy { JumpConfig() }
    private val jumpProcessor by lazy { JumpRopeProcessor(jumpConfig) }

    private var frameDispatcher: DynamicSportFrameDispatcher? = null

    private var cameraView: AdlCameraPreviewView? = null
    private val cameraConfig by lazy {
        BusiBaseConfig.readIpcCameraConfig(MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY)
    }

    private val isDebug: Boolean by lazy { BusiBaseConfig.getDebugEnable() }
    private val skeletonEnable: Boolean by lazy { BusiBaseConfig.getSkeletonEnable() }

    /***************************视频播放调试*************************/
    private var videoDebug: Boolean = false
    private var videoTextureView: TextureView? = null
    private var videoSurface: Surface? = null
    private var videoReader: AdlVideoReader? = null
    private var videoFilePath = ""
    private var frameTime: Long = 0
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
            Log.i(TAG, "FourSportGbSettingActivity::onSurfaceTextureAvailable : w: $width, h: $height")
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

    override fun initObserver() {}

    override fun initData() {
        videoFilePath = intent.getStringExtra(INTENT_KEY_VIDEO_PATH) ?: ""
        if (videoFilePath.isNotEmpty()) videoDebug = true
        Log.i(TAG, "FourSportGbSettingActivity::initData : videoFilePath = $videoFilePath")
    }

    override fun initView(savedInstanceState: Bundle?) {
        Log.i(TAG, "FourSportGbSettingActivity::initView")
        initListener()
        initJumpConfig()
        initJumpProcessor()
        initFrameDispatcher()
        initOverlay()
        if (videoDebug) {
            initVideoView()
        } else {
            initCameraView()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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

    private fun initListener() {
        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            mBinding.overlay.saveConfig()
            ToastUtil.getInstance(this, getString(R.string.save_successful)).show()
        }
        mBinding.btnReset.setOnClickListener {
            mBinding.overlay.restoreSportSetting()
            ToastUtil.getInstance(this, getString(R.string.reset_successful)).show()
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
                Log.i(TAG, "FourSportGbSettingActivity::initJumpConfig::onStart")
            }

            override fun onSucceed(p0: List<TsModelInfo?>?) {
                val sb = StringBuilder()
                p0?.let { list ->
                    list.forEach { info ->
                        sb.append(info?.modelId)
                        sb.append(',')
                    }
                }
                Log.i(TAG, "FourSportGbSettingActivity::initJumpConfig::onSuccess: $sb")
                jumpProcessor.initPlayGround()
            }

            override fun onFinish(p0: List<TsModelInfo?>?, p1: List<TsModelInfo?>?) {
                val sb1 = StringBuilder()
                p0?.let { list ->
                    list.forEach { info ->
                        sb1.append(info?.modelId)
                        sb1.append(',')
                    }
                }
                val sb2 = StringBuilder()
                p0?.let { list ->
                    list.forEach { info ->
                        sb2.append(info?.modelId)
                        sb2.append(',')
                    }
                }
                Log.i(TAG, "FourSportGbSettingActivity::initJumpConfig::onFinish: p0: $sb1, p1: $sb2")
            }

            override fun onEnd(p0: String?) {
                Log.i(TAG, "FourSportGbSettingActivity::initJumpConfig::onEnd: p0: $p0")
            }
        })
    }

    private fun initJumpProcessor() {
        jumpProcessor.setJumpRopeCallBack { _, _ ->
            Log.i(TAG, "FourSportGbSettingActivity::initJumpProcessor::onUpdateFrame")
        }
    }

    private fun initOverlay() {
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        mBinding.overlay.skeletonDrawer.enableMarker(skeletonEnable)
        mBinding.overlay.skeletonDrawer.enableLine(skeletonEnable)
        mBinding.overlay.enableDrawSkeleton(skeletonEnable)
        mBinding.overlay.setMarkerRecognizerConfig(jumpConfig.markerRecognizerConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.setListener {
            jumpProcessor.combineJumpRopeOverlay(mBinding.overlay)
        }
        mBinding.overlay.refresh()
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
            addFrameProcessor(jumpProcessor)
        }
    }

    private fun initCameraView() {
        Log.i(TAG, "FourSportGbSettingActivity::initCameraView")
        val size = calPreviewLayoutSize(cameraPreviewSize)
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

        val camera = AdlCameraConfig.builder()
            .setPreviewSize(cameraPreviewSize.width, cameraPreviewSize.height)
            .setCameraType(AdlCameraConfig.CameraType.HK)
            .setCameraId(AdlCameraConfig.CameraId.Back)
            .setConnect(cameraConfig.ip, cameraConfig.port, cameraConfig.account, cameraConfig.psw)
            .setPreviewFps(20)
            .setPreviewEnable(true)
            .setAutoFocus(true)
            .setStreamType(0)
            .build()
        cameraView?.startPreview(camera, object : AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {}

            override fun onCameraOpen() {
                Log.i(TAG, "FourSportGbSettingActivity::initCameraView::onCameraOpen")
            }

            override fun onCameraReOpen() {}

            override fun onDestroyed() {}

            override fun onStreamLoss() {}

            override fun onOpenFail() {
                Log.e(TAG, "FourSportGbSettingActivity::initCameraView : onOpenFail")
            }
        })
        cameraView?.scaleX = -1f
        cameraView?.addFrameProcessor(frameDispatcher)
    }

    private fun initVideoView() {
        Log.i(TAG, "FourSportGbSettingActivity::initVideoView")
        val size = calPreviewLayoutSize(cameraPreviewSize)
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
        mBinding.flContainer.layoutParams = containerParams
        mBinding.flContainer.requestLayout()
    }

    private fun initVideo() {
        Log.i(TAG, "FourSportGbSettingActivity::initVideo")
        val file = File(videoFilePath)
        if (!file.exists() || file.length() == 0L) {
            AdlToast.show("视频文件不存在或为空")
            return
        }
        if (videoSurface == null) {
            return
        }
        videoReader = AdlVideoReader().apply {
            setPlayMode(AdlAvReader.PlayMode.Auto)
            setSurface(videoSurface)
            enableRender(true)
            setCallback(object : VideoReaderCallback {
                override fun onError(e: Exception?) {
                    Log.e(TAG, "FourSportGbSettingActivity::initVideo::onError: ", e)
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
                    Log.i(TAG, "FourSportGbSettingActivity::initVideo::onComplete")
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

}
