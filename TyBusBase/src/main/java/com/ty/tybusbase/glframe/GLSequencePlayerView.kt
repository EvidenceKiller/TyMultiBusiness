package com.ty.tybusbase.glframe

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet

class GLSequencePlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var renderer: SequenceGLRenderer? = null
    private var decoder: SequenceFrameDecoder? = null
    private var isPlaying = false
    private var frameDurationMs = 33L   // 可配置
    private val playHandler = Handler(Looper.getMainLooper())

    private val frameUpdater = object : Runnable {
        override fun run() {
            if (!isPlaying) return
            val nextFrame = (renderer?.currentFrame ?: 0) + 1
            if (nextFrame < (decoder?.getTotalFrames() ?: 0)) {
                renderer?.currentFrame = nextFrame
                // 触发解码下一帧
                decoder?.preDecodeNext(nextFrame)
                // 将当前帧显示出来（实际上解码器会在回调中更新纹理）
                // 注意：解码器完成解码后会通过 onFrameReady 调用 renderer.updateFrame
                playHandler.postDelayed(this, frameDurationMs)
            } else {
                // 循环播放
                renderer?.currentFrame = 1
                decoder?.decodeFrame(1)
                playHandler.postDelayed(this, frameDurationMs)
            }
        }
    }

    init {
        setEGLContextClientVersion(2)  // 使用 OpenGL ES 2.0
        // 设置 EGL 配置：RGBA 各 8 位，深度 0，模板 0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
//        // 确保 SurfaceView 在顶层，以便透明叠加（如果不需要叠加其他视图，可省略）
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        // 保持 GL 上下文
        preserveEGLContextOnPause = true

        renderer = SequenceGLRenderer().also {
            setRenderer(it)
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    fun setDataSource(assetPrefix: String,assetSuffix:String, totalFrames: Int, width: Int, height: Int, hasAlpha: Boolean = false, fps: Int = 30) {
        frameDurationMs = (1000 / fps).toLong()
        decoder = SequenceFrameDecoder(
            assetManager = context.assets,
            assetPrefix = assetPrefix,
            assetSuffix = assetSuffix,
            totalFrames = totalFrames,
            targetWidth = width,
            targetHeight = height,
            hasAlpha = hasAlpha,
            onFrameReady = { frameIndex, bitmap ->
                // 当解码器解码完成时，将 bitmap 提交给渲染器
                renderer?.updateFrame(bitmap)
                // 如果是当前帧，同时更新播放位置（可选）
                if (frameIndex == renderer?.currentFrame) {
                    // 可在此做同步，但渲染器会在下一帧自动使用
                }
            }
        )
        // 加载第一帧
        decoder?.decodeFrame(1)
    }

    fun start() {
        isPlaying = true
        playHandler.removeCallbacks(frameUpdater)
        playHandler.post(frameUpdater)
    }

    fun pause() {
        isPlaying = false
        playHandler.removeCallbacks(frameUpdater)
    }

    fun release() {
        pause()
        decoder?.release()
        // GL 资源释放由 GLSurfaceView 处理
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }
}