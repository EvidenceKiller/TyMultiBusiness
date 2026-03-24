package com.ty.tybusbase.glframe

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil
import androidx.core.graphics.createBitmap

/**
 * 管理序列帧的解码和缓存，使用双缓冲池（两个可复用的 Bitmap）
 */

class SequenceFrameDecoder(
    private val assetManager: AssetManager,
    private val assetPrefix: String,          // 例如 "frames/frame_"
    private val assetSuffix:String, //资源文件后缀
    private val totalFrames: Int,
    private val targetWidth: Int,             // 最终显示的纹理宽度（解码时会自动降采样）
    private val targetHeight: Int,            // 最终显示的纹理高度
    private val hasAlpha: Boolean = false,    // 是否有透明通道
    private val onFrameReady: (frameIndex: Int, bitmap: Bitmap) -> Unit
) {
    private val decoderThread = HandlerThread("FrameDecoder", Process.THREAD_PRIORITY_BACKGROUND)
    private var decoderHandler: Handler? = null
    private var isActive = AtomicBoolean(true)
    private val MAX_POOL_SIZE = 2

    // Bitmap 复用池：两个可复用的 Bitmap（一当前一预备）
    private val reusableBitmaps = mutableListOf<Bitmap>()

    // 待解码队列（可选，这里简单按顺序预加载）
    private var nextFrameToDecode = 0

    init {
        decoderThread.start()
        decoderHandler = Handler(decoderThread.looper)
//        // 预创建两个用于复用的 Bitmap
//        val config = if (hasAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
//        repeat(2) {
//            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, config)
//            reusableBitmaps.add(bitmap)
//        }
    }

    fun getTotalFrames(): Int {
        return totalFrames
    }

    /**
     * 开始解码指定帧
     */
    fun decodeFrame(frameIndex: Int) {
        if (!isActive.get()) return
        decoderHandler?.post {
            val path = "$assetPrefix${frameIndex}.${assetSuffix}"
            try {
                // 1. 获取原图尺寸并计算采样率
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                assetManager.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                val inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                // 2. 计算采样后的实际宽高
                val actualWidth = ceil(options.outWidth / inSampleSize.toDouble()).toInt()
                val actualHeight = ceil(options.outHeight / inSampleSize.toDouble()).toInt()

                // 3. 从复用池中寻找或创建与实际尺寸一致的 Bitmap
                val reusableBitmap = findOrCreateReusableBitmap(actualWidth, actualHeight, hasAlpha)

                // 4. 正式解码，使用 inBitmap 复用
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inMutable = true
                    inBitmap = reusableBitmap
                    inPreferredConfig =
                        if (hasAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
                    inJustDecodeBounds = false
                }
                assetManager.open(path).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
                    if (bitmap != reusableBitmap) {
                        // 理论上应该复用成功，但以防万一
                        reusableBitmap?.recycle()
                    }
                    onFrameReady(frameIndex, bitmap ?: reusableBitmap!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findOrCreateReusableBitmap(width: Int, height: Int, hasAlpha: Boolean): Bitmap? {
        val config = if (hasAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        // 在复用池中找尺寸和 config 匹配的
        val existing =
            reusableBitmaps.find { it.width == width && it.height == height && it.config == config }
        if (existing != null) return existing

        // 否则创建新的并加入池（最多保留两个，可调整池大小）
        val newBitmap = createBitmap(width, height, config)
        reusableBitmaps.add(newBitmap)
        // 如果池大小超过限制，移除最旧的
        while (reusableBitmaps.size > MAX_POOL_SIZE) {
            reusableBitmaps.removeAt(0).recycle()
        }
        return newBitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun findReusableBitmap(bitmaps: List<Bitmap>, width: Int, height: Int): Bitmap? {
        return bitmaps.find { it.width == width && it.height == height && it.isMutable }
    }

    /**
     * 预解码下一帧（可提前调用）
     */
    fun preDecodeNext(currentFrame: Int) {
        val next = currentFrame + 1
        if (next < totalFrames) {
            decodeFrame(next)
        }
    }

    fun release() {
        isActive.set(false)
        decoderThread.quitSafely()
        reusableBitmaps.forEach { it.recycle() }
        reusableBitmaps.clear()
    }
}