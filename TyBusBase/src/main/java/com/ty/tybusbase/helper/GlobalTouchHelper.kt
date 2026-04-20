package com.ty.tybusbase.helper

import android.os.Handler
import android.os.Looper
import com.ty.tybusbase.expand.Action

object GlobalTouchHelper {
    private var noTouchListener: Action? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var mTimeOut: Long = 5 * 60 * 1000L //分钟
    var enable = false //是否使能此功能
        set(value) {
            field = value
            //如果使能则开始超时回调
            if (value) {
                mHandler.postDelayed(mRunnable, mTimeOut)
            }
        }
    private var runningFlag = true //运行标识
    private val mRunnable = Runnable {
        noTouchListener?.invoke()
    }

    fun setNoTouchListener(listener: Action?) {
        noTouchListener = listener
    }

    fun setTimeOut(timeOut: Long) {
        mTimeOut = timeOut
    }

    @Synchronized
    fun onTouch() {
        if (enable.not() || runningFlag.not()) return
        mHandler.removeCallbacks(mRunnable)
        mHandler.postDelayed(mRunnable, mTimeOut)
    }

    @Synchronized
    fun pause() {
        if (enable.not()) return
        runningFlag = false
        mHandler.removeCallbacks(mRunnable)
    }

    @Synchronized
    fun restart() {
        if (enable.not()) return
        runningFlag = true
        mHandler.postDelayed(mRunnable, mTimeOut)
    }

    fun release() {
        noTouchListener = null
        mHandler.removeCallbacksAndMessages(null)
    }

}