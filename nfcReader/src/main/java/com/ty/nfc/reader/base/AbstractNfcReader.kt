package com.ty.nfc.reader.base

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

abstract class AbstractNfcReader : INfcReader {
    protected var mReadCallback: ReadCallback? = null
    protected val scheduler = Executors.newSingleThreadScheduledExecutor()
    protected var readingTask: ScheduledFuture<*>? = null
    protected var interval = 2L
    override fun startRead(callback: ReadCallback) {
        this.mReadCallback = callback
    }

    override fun stopRead() {
        readingTask?.cancel(true)
    }

    override fun release() {
        scheduler.shutdown()
    }
}