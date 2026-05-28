package com.ty.tybusbase.utils

import android.os.Handler
import android.os.Looper

/**
 * 运动计时器，支持倒计时与正计时
 * 采用配置驱动模式，实现高内聚、低耦合设计
 */
class SportTimer private constructor(private val config: Config) {

    enum class Mode {
        /** 倒计时 */
        COUNT_DOWN,
        /** 正计时 */
        COUNT_UP
    }

    /** 计时回调接口，与 Android 组件解耦 */
    interface Callback {
        /** 计时开始 */
        fun onStart(){}

        /** 计时 Tick
         * @param currentTimeSeconds 倒计时返回剩余秒数，正计时返回已过秒数
         */
        fun onTick(currentTimeSeconds: Long)

        /** 计时正常结束（倒计时到达 0 或正计时手动停止） */
        fun onFinish()

        /** 计时被取消（手动调用 cancel） */
        fun onCancel(){}
    }

    /** 配置类 */
    data class Config(
        val mode: Mode = Mode.COUNT_UP,
        /** 倒计时总时长（秒），正计时模式下忽略 */
        val totalTimeSeconds: Long = 0,
        /** Tick 间隔（毫秒），默认 1 秒 */
        val intervalMs: Long = 1000,
        /** 回调 */
        val callback: Callback? = null
    ) {
        class Builder {
            var mode: Mode = Mode.COUNT_UP
            var totalTimeSeconds: Long = 0
            var intervalMs: Long = 1000
            var callback: Callback? = null

            fun build() = Config(mode, totalTimeSeconds, intervalMs, callback)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    @Volatile private var isRunning = false
    @Volatile private var isPaused = false
    private var startTime = 0L
    private var pausedElapsed = 0L
    /** 已过去秒数，用于正计时/暂停恢复 */
    private var elapsedSeconds = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            if (isPaused) {
                handler.postDelayed(this, config.intervalMs)
                return
            }

            val now = System.currentTimeMillis()
            val elapsedMs = now - startTime
            elapsedSeconds = elapsedMs / 1000

            when (config.mode) {
                Mode.COUNT_DOWN -> {
                    val totalSeconds = config.totalTimeSeconds
                    val remainingSeconds = totalSeconds - elapsedSeconds
                    if (remainingSeconds <= 0) {
                        config.callback?.onTick(0)
                        stopInternal()
                        config.callback?.onFinish()
                    } else {
                        config.callback?.onTick(remainingSeconds)
                        handler.postDelayed(this, config.intervalMs)
                    }
                }
                Mode.COUNT_UP -> {
                    config.callback?.onTick(elapsedSeconds)
                    handler.postDelayed(this, config.intervalMs)
                }
            }
        }
    }

    /** 开始计时 */
    fun start(): SportTimer {
        if (isRunning) return this
        isRunning = true
        isPaused = false
        startTime = System.currentTimeMillis()
        pausedElapsed = 0
        elapsedSeconds = 0
        config.callback?.onStart()
        handler.post(tickRunnable)
        return this
    }

    /** 暂停计时 */
    fun pause() {
        if (!isRunning || isPaused) return
        isPaused = true
        pausedElapsed = System.currentTimeMillis() - startTime
    }

    /** 恢复计时 */
    fun resume() {
        if (!isRunning || !isPaused) return
        isPaused = false
        startTime = System.currentTimeMillis() - pausedElapsed
        pausedElapsed = 0
    }

    /** 取消计时 */
    fun cancel() {
        if (!isRunning) return
        stopInternal()
        config.callback?.onCancel()
    }

    fun isRunning() = isRunning
    fun isPaused() = isPaused

    private fun stopInternal() {
        isRunning = false
        isPaused = false
        handler.removeCallbacks(tickRunnable)
    }

    companion object {
        @JvmStatic
        fun build(block: Config.Builder.() -> Unit): SportTimer {
            return SportTimer(Config.Builder().apply(block).build())
        }
    }
}