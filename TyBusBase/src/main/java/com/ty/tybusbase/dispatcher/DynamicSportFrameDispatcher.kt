package com.ty.tybusbase.dispatcher

import com.adl.base.element.ElementFpsCounter
import com.adl.base.log.AdlLoggerImpl
import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlYUVUtil
import com.adl.mount.sport.TsYuvFrame
import com.adl.mount.sport.TsYuvFrameRecCache
import java.util.concurrent.ArrayBlockingQueue

class DynamicSportFrameDispatcher @JvmOverloads constructor(private var fpsCounter: ElementFpsCounter? = null as ElementFpsCounter?) :
    AdlCameraFrameProcessor {
    private val processors: MutableList<AdlCameraFrameProcessor?>

    @Volatile
    private var mActive = true
    private var mRotateBuffer: ByteArray? = null
    private var debug = false
    private var debugInfo: String? = null
    private var mCaller: NextCaller? = null
    private val mLogger: AdlLoggerImpl
    private var isMirror = false
    private var rotate = 0
    private var enableDiscard = false
    private var lastFrameTime: Long = 0
    private var cameraFpsTimer = 0
    private var mLastTimestamp: Long = 0
    private var fpsCurrent = 0 //记录相机每秒的FPS
    private var checkFpsStartTime: Long = 0 //开始检测时间
    private var isOverRang = true //预览是否超过指定帧
    private val floatFps = 5 //浮动帧数；比如30帧，浮动帧数5，就可能输出35帧，计算的时候，需要兼容浮动帧数
    private var fps = 30 // 默认赋值30帧

    init {
        processors = ArrayList()
        mLogger = AdlLoggerImpl.cameraLogger()
    }

    fun setMirror(isMirror: Boolean): DynamicSportFrameDispatcher {
        this.isMirror = isMirror
        return this
    }

    fun setRotate(rotate: Int): DynamicSportFrameDispatcher {
        this.rotate = rotate
        return this
    }

    fun setEnableDiscard(enable: Boolean): DynamicSportFrameDispatcher {
        enableDiscard = enable
        return this
    }

    fun setFps(fps: Int): DynamicSportFrameDispatcher {
        this.fps = fps
        cameraFpsTimer = 1000 / Math.max(fps, 15)
        return this
    }

    fun enableDebug(enable: Boolean): DynamicSportFrameDispatcher {
        debug = enable
        return this
    }

    fun addFrameProcessor(processor: AdlCameraFrameProcessor?) {
        if (processor != null && !processors.contains(processor)) {
            processors.add(processor)
        }
    }

    fun removeFrameProcessor(processor: AdlCameraFrameProcessor?) {
        if (processor != null && processors.contains(processor)) {
            processors.remove(processor)
        }
    }

    private fun discardCameraFrame(): Boolean {
        return if (System.currentTimeMillis() - lastFrameTime < cameraFpsTimer.toLong()) {
            true
        } else {
            lastFrameTime = System.currentTimeMillis()
            false
        }
    }

    override fun onFrame(nv21: ByteArray, info: AdlCameraPreviewInfo) {
        if (mActive) {
            checkFpsIsOverRang(fps)
            if (!enableDiscard || !isOverRang || !discardCameraFrame()) {
                if (fpsCounter != null) {
                    fpsCounter!!.updateFps()
                }
                if (mCaller == null) {
                    mCaller = NextCaller()
                    mCaller!!.start()
                }
                val width = info.width
                val height = info.height
                val enterTime = System.currentTimeMillis()
                val timestamp = info.frameTimestamp / 1000000L
                if (debug) {
                    mLogger.d("摄像头输出 --> 间隔=" + (timestamp - mLastTimestamp) + ", timestamp=" + timestamp + ", imageId=" + info.imageId)
                }
                if (isMirror) {
                    AdlYUVUtil.nv21Mirror(nv21, width, height)
                }
                val outNv21New: ByteArray?
                if (rotate > 0) {
                    if (mRotateBuffer == null) {
                        mRotateBuffer = ByteArray(nv21.size)
                    }
                    AdlYUVUtil.yuvRotate180ByJava(nv21, mRotateBuffer, width, height)
                    outNv21New = mRotateBuffer
                } else {
                    outNv21New = nv21
                }
                if (debug) {
                    debugInfo =
                        "Yuv预处理耗时=" + (System.currentTimeMillis() - enterTime) + ", 线程=" + Thread.currentThread().name
                    if (fpsCounter != null) {
                        fpsCounter!!.setOtherStr(debugInfo)
                    }
                }
                if (mCaller != null) {
                    mCaller!!.offer(outNv21New, info)
                }
                mLastTimestamp = timestamp
            }
        }
    }

    fun checkFpsIsOverRang(fps: Int) {
        if (checkFpsStartTime == 0L) {
            checkFpsStartTime = System.currentTimeMillis()
        }
        fpsCurrent++
        //收集帧超过1秒，判断是否大于设定的FPS
        if (System.currentTimeMillis() - checkFpsStartTime >= 1000) {
            isOverRang = fpsCurrent > fps + floatFps
            checkFpsStartTime = System.currentTimeMillis()
            fpsCurrent = 0
        }
    }

    fun quit() {
        mActive = false
        processors.clear()
        if (mCaller != null) {
            mCaller!!.clear()
            mCaller = null
        }
        fpsCounter = null
    }

    private inner class NextCaller : Runnable {
        private var mThread: Thread? = null
        private val mSize = 4
        private val mQueue: ArrayBlockingQueue<TsYuvFrame?> = ArrayBlockingQueue(4)
        private val mFixCache = TsYuvFrameRecCache.pool()
        fun start() {
            mFixCache.prepare(5)
            mThread = Thread(this)
            mThread!!.name = "#Caller"
            mThread!!.priority = 9
            mThread!!.start()
        }

        fun clear() {
            mQueue.clear()
            mFixCache.forceRecycle()
        }

        fun offer(yuv: ByteArray?, info: AdlCameraPreviewInfo) {
            if (mActive) {
                val queue = mQueue
                if (queue.size < 4) {
                    val frame = mFixCache.acquire(yuv, info)
                    if (frame != null) {
                        if (!queue.offer(frame)) {
                            frame.release()
                        } else if (debug) {
                            mLogger.d("警告: 分发队列已满, imageId=" + info.imageId + ", timestamp=" + info.frameTimestamp)
                        }
                    } else if (debug) {
                        mLogger.d("警告: 摄像头缓存无效, imageId=" + info.imageId + ", timestamp=" + info.frameTimestamp)
                    }
                } else if (debug) {
                    mLogger.d("警告： 摄像头消费繁忙，主动丢帧, imageId=" + info.imageId + ", timestamp=" + info.frameTimestamp + ", 线程=" + Thread.currentThread().name)
                }
            }
        }

        override fun run() {
            val queue = mQueue
            val fixCache = mFixCache
            val next: List<AdlCameraFrameProcessor?> = processors
            var info: AdlCameraPreviewInfo? = null
            while (mActive) {
                val frame = queue.poll()
                try {
                    if (frame == null) {
                        Thread.sleep(10L)
                    } else {
                        val yuv = frame.yuv
                        val key = frame.imageId
                        val width = frame.width
                        val height = frame.height
                        val timestamp = frame.frameTime
                        if (info == null) {
                            info = AdlCameraPreviewInfo(width, height)
                        }
                        info.imageId = key
                        info.width = width
                        info.height = height
                        info.frameTimestamp = timestamp
                        val enterTime = System.currentTimeMillis()
                        val var14: Iterator<*> = next.iterator()
                        while (var14.hasNext()) {
                            val processor = var14.next() as AdlCameraFrameProcessor
                            processor.onFrame(yuv, info)
                        }
                        val t = System.currentTimeMillis() - enterTime
                        if (t > 50L) {
                            mLogger.d("警告: 摄像头消费超时=$t")
                        }
                    }
                } catch (var19: Exception) {
                    var19.printStackTrace()
                } finally {
                    frame?.release()
                }
            }
            queue.clear()
            fixCache.forceRecycle()
        }
    }
}