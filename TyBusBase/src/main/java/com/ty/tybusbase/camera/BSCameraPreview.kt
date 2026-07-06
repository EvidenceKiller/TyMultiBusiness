package com.ty.tybusbase.camera

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLogger
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.AdlCameraPreviewView.OnViewStatusListener
import com.adl.lib.camera.uvc.NsUVCCameraSource
import com.adl.lib.camera.uvc.NsUVCCameraSource.UVCModel
import com.ty.tybusbase.R
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.dispatcher.DynamicSportFrameDispatcher
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.MMKVUtil.decodeInt

/**
 * 摄像头统一配置
 */
class BSCameraPreview(context: Context, attrs: AttributeSet?) : FrameLayout(
    context!!, attrs
) {
    private var zyCamera: AdlCamera? = null
    var cameraView: AdlCameraPreviewView? = null
    var cameraFpsCounter: ElementFpsCounter? = null
    private var cameraDispatcher: DynamicSportFrameDispatcher? = null
    private var onCameraOpenListener: Action? = null
    private var previewFps = 30
    private var callbackFps = 30

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BSCameraPreview)
            .apply {
                previewFps = getInteger(R.styleable.BSCameraPreview_preview_fps, 30)
                callbackFps = getInteger(R.styleable.BSCameraPreview_callback_fps, 30)
                recycle()
            }
        loadView(context)
    }

    fun loadView(context: Context?) {
        cameraView = LayoutInflater.from(context)
            .inflate(R.layout.bs_camera_view, null) as AdlCameraPreviewView
        val params = LayoutParams(-1, -1)
        addView(cameraView, params)
        var cameraId = AdlCameraConfig.CameraId.Front
        val anInt =
            decodeInt(MMKVBusiKey.CAMERA_CHOOSE, BusiBaseConstantsValue.CAMERA_FRONT)
        if (anInt == BusiBaseConstantsValue.CAMERA_BACK) {
            cameraId = AdlCameraConfig.CameraId.Back
        }
        var cameraType = AdlCameraConfig.CameraType.UVC
        val tempType =
            decodeInt(
                MMKVBusiKey.CAMERA_TYPE_CHOOSE,
                BusiBaseConstantsValue.CAMERA_TYPE_DEFAULT
            )
        if (tempType == BusiBaseConstantsValue.CAMERA_TYPE_CAMERA) {
            cameraType = AdlCameraConfig.CameraType.Old
        } else if (tempType == BusiBaseConstantsValue.CAMERA_TYPE_CAMERAX) {
            cameraType = AdlCameraConfig.CameraType.CameraX
        }
        // 摄像头配置
        zyCamera = AdlCameraConfig.builder()
            .setCameraType(cameraType)
            .setCameraId(cameraId)
            .setPreviewSize(
                BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH,
                BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT
            )
            .setPreviewFps(previewFps)
            .setCallbackFps(callbackFps)
            .setDisplayOrientation(0)
            .build()
//         摄像头FPS 统计
        cameraFpsCounter = ElementFpsCounter("Camera")
        cameraFpsCounter!!.setPosition(20f, 270f) //设置camera fps，最大时间间隔，yuv预处理的显示位置

        // 摄像头帧率控制
        cameraDispatcher = DynamicSportFrameDispatcher(cameraFpsCounter)
        cameraDispatcher!!.enableDebug(false)
        cameraDispatcher!!.setEnableDiscard(true)
        cameraDispatcher!!.setFps(callbackFps)
        setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        setRotate(0)
    }

    fun addProcessor(processor: AdlCameraFrameProcessor?) {
        cameraDispatcher?.addFrameProcessor(processor)
    }

    fun removeProcessor(p: AdlCameraFrameProcessor?) {
        cameraDispatcher?.removeFrameProcessor(p)
    }

    fun setOnCameraOpenListener(listener: Action) {
        onCameraOpenListener = listener
    }

    fun setMirror(isMirror: Boolean) {
        (zyCamera as? NsUVCCameraSource)?.setMirror(isMirror) ?: cameraDispatcher?.setMirror(
            isMirror
        )
    }

    fun toggleMirror() {
        val skeleton = BusiBaseConfig.getSkeletonMirror().not()
        (zyCamera as? NsUVCCameraSource)?.setMirror(skeleton) ?: cameraDispatcher?.setMirror(
            skeleton
        )
    }

    fun setRotate(rotate: Int) {
        cameraDispatcher?.setRotate(rotate)
    }

    fun setFps(fps: Int) {
        cameraDispatcher?.setFps(fps)
    }

    fun onResume() {
        val start = System.currentTimeMillis()
        AdlLogger.d("开始打开相机")
        val cameraType = zyCamera!!.cameraType
        cameraView!!.startPreview(zyCamera, object : OnViewStatusListener {
            override fun onAvailable() {

            }

            override fun onCameraOpen() {
                if (cameraType == AdlCameraConfig.CameraType.UVC) {
                    configUVCModel()
                }
                onCameraOpenListener?.invoke()
            }

            override fun onCameraReOpen() {

            }

            override fun onDestroyed() {

            }

            override fun onStreamLoss() {

            }

            override fun onOpenFail() {

            }

        })
        val cameraMirror =
            MMKVUtil.decodeBoolean(MMKVBusiKey.CAMERA_MIRROR)

        if (cameraMirror && cameraType == AdlCameraConfig.CameraType.CameraX && cameraView?.previewView != null) {
            cameraView!!.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            cameraView!!.previewView.scaleX = -1f
        }
        if (cameraMirror && cameraType == AdlCameraConfig.CameraType.Old && cameraView!!.textureView != null) {
            cameraView!!.textureView.post { cameraView!!.textureView.scaleX = -1f }
        }
        cameraView!!.addFrameProcessor(cameraDispatcher)
        AdlLogger.d("打开相机耗时:" + (System.currentTimeMillis() - start))

    }

    fun configUVCModel() {
        val defa = decodeInt(
            MMKVBusiKey.UVC_CAMERA_DISPLAY_PARAM,
            BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_DEFAULT
        )
        val model = when (defa) {
            BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_GENERAL -> {
                UVCModel.Normal
            }

            BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_QG -> {
                UVCModel.StrongLight
            }

            BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_RG -> {
                UVCModel.WeakLight
            }

            BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_CUSTOM -> {
                UVCModel.Custom
            }

            else -> {
                UVCModel.Normal
            }
        }
        //UVC打开回调，是在调用了startPreview后直接调用的，不靠谱，需要延时
        postDelayed({
            if (zyCamera is NsUVCCameraSource) {
                (zyCamera as? NsUVCCameraSource)?.configUvcModel(model)
                if (defa == BusiBaseConstantsValue.UVC_CAMERA_DISPLAY_PARAM_CUSTOM) {
                    //设置进度值
                    val light =
                        decodeInt(
                            MMKVBusiKey.UVC_CAMERA_LIGHT,
                            BusiBaseConstantsValue.UVC_CAMERA_DEFAULT_LIGHT
                        )
                    val contrast = decodeInt(
                        MMKVBusiKey.UVC_CAMERA_CONTRAST,
                        BusiBaseConstantsValue.UVC_CAMERA_DEFAULT_CONTRAST
                    )
                    val exValue = decodeInt(MMKVBusiKey.UVC_CAMERA_EXPOSURE, 0)
                    zyCamera!!.asUVCCamera().configExposureAbsoluteTime(exValue)
                    zyCamera!!.asUVCCamera().configBrightnessPercent(light)
                    zyCamera!!.asUVCCamera().configContrastPercent(contrast)
                }
            }
        }, 1000)
    }

    fun onPause() {
        cameraView!!.stopPreview()
    }

    fun onRelease() {
        onPause() //先pause 否则会崩溃
        cameraView!!.release()
        removeView(cameraView)
        cameraDispatcher!!.quit()
    }

    fun setCamera(zyCamera: AdlCamera?) {
        this.zyCamera = zyCamera
    }

    fun getCamera(): AdlCamera? {
        return zyCamera
    }
}