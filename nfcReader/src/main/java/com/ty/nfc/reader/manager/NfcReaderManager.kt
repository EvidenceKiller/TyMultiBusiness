package com.ty.nfc.reader.manager

import android.content.Context
import com.ty.nfc.reader.base.AbstractNfcReader
import com.ty.nfc.reader.base.ReadCallback
import com.ty.nfc.reader.base.NfcType
import com.ty.nfc.reader.nfca.NfcReaderA
import com.ty.nfc.reader.nfcb.NfcReaderB

class NfcReaderManager {
    private var nfcReader: AbstractNfcReader? = null

    fun init(context: Context, readerType: NfcType) {
        nfcReader = when (readerType) {
            NfcType.NFC_A -> NfcReaderA()
            NfcType.NFC_B -> NfcReaderB()
        }
        nfcReader?.init(context)
    }

    /**
     * 设置间隔时间;单位毫秒
     */
    fun setInterval(interval: Long) {
        nfcReader?.interval = interval
    }

    fun startRead(callback: ReadCallback) {
        nfcReader?.startRead(callback)
    }

    fun stopRead() {
        nfcReader?.stopRead()
    }

    fun release() {
        nfcReader?.release()
    }


}