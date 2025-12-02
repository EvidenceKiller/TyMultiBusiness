package com.ty.tybusbase.utils

import java.util.concurrent.Executors


class ThreadCountDown {
    private var running = false
    private val TIME_INTERVAL = 1000L
    private var totalTime = 0

    /**
     * type 1: countdown,2: countup
     */
    @Synchronized
    fun start(time: Int, callback: (Int) -> Unit, type: Int = 1, end: (() -> Unit)? = null) {
        if (running) {
            return
        }
        if (type == 1) {
            totalTime = time
        }
        running = true
        Executors.newSingleThreadExecutor().execute {
            while (running) {
                try {
                    Thread.sleep(TIME_INTERVAL)
                    if (running) {
                        if (type == 1) {
                            callback.invoke(--totalTime)
                            if (totalTime == 0) {
                                end?.invoke()
                            }
                        } else {
                            callback.invoke(++totalTime)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        running = false
    }

}