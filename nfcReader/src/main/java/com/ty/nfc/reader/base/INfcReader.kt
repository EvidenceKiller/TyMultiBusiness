package com.ty.nfc.reader.base

import android.content.Context

interface INfcReader {
    fun init(context: Context)

    fun startRead(callback: ReadCallback)

    fun stopRead()

    fun release()
}