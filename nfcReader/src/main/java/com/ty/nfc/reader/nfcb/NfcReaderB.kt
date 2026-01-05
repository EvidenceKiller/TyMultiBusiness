package com.ty.nfc.reader.nfcb

import android.content.Context
import android.util.Log
import com.ty.nfc.reader.base.AbstractNfcReader
import com.ty.nfc.reader.base.ReadCallback
import lc.comproCall
import java.util.concurrent.TimeUnit
import kotlin.experimental.and

class NfcReaderB : AbstractNfcReader() {
    private val TAG = "NFCB"
    private var devNum = -1
    private val pModVer: CharArray = CharArray(512)
    private val rvHexCard: CharArray = CharArray(128) //Card SN in HEX type(Reverse)
    private val rvStrCard: CharArray = CharArray(128) //Card SN in HEX string type (Reverse)
    private val pSnrM1: ByteArray = ByteArray(255)
    private val pByteHex: ByteArray = ByteArray(255)
    private val rv_hexCard: ByteArray = ByteArray(128) //Card SN in HEX type(Reverse)
    private val rv_hexCardStr: ByteArray = ByteArray(128)
    private val snrSize: ByteArray = ByteArray(2)
    private val tag: IntArray = IntArray(2)
    private val sak: ByteArray = ByteArray(2)
    private var pCharHex: CharArray = CharArray(255)
    private val pCharSingle: CharArray = CharArray(255)
    override fun init(context: Context) {
    }

    override fun startRead(callback: ReadCallback) {
        super.startRead(callback)
        readingTask = scheduler.scheduleWithFixedDelay({
            performReaderARead()
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    private fun performReaderARead() {
        if (devNum == -1) {
            devNum = comproCall.lc_init_ex(2, null, 0).toInt()
            if (devNum != -1) {
                val result = comproCall.lc_getver(devNum, pModVer)
                if (result == 0) {
                    Log.d(TAG, "设备初始化成功")
                } else {
                    Log.e(TAG, "设备初始化失败,设备类型不匹配")
                    comproCall.lc_exit(devNum)
                    stopRead()
                }
            } else {
                Log.d(TAG, "设备初始化失败")
                return
            }
        }
        comproCall.lc_rfReset(devNum, 10) //RF Reset
        //获取卡号 UID
        val result = comproCall.lc_card(devNum, 1.toByte(), pSnrM1, snrSize, tag, sak);
        if (0 == result) {
            comproCall.lc_beep(devNum, 10)
            for (i in 0 until snrSize[0]) {
                pCharSingle[i] = Char(pSnrM1[i].toUShort())
            }
            val cardNum =
                ((pCharSingle[3].code and 0xff).toLong() shl 24) + ((pCharSingle[2].code and 0xff).toLong() shl 16) + ((pCharSingle[1].code and 0xff).toLong() shl 8) + (pCharSingle[0].code and 0xff).toLong()
            Log.d(TAG, "cardNum: $cardNum")
            mReadCallback?.readCardBack(cardNum.toString())
        }
    }

    fun bytesToChars(pChars: ByteArray): CharArray {
        val length = pChars.size
        val out = CharArray(length)

        var i: Int = 0
        while (i < length) {
            out[i] = Char(pChars[i].toUShort())
            i++
        }

        return out
    }

    override fun stopRead() {
        super.stopRead()
        comproCall.lc_exit(devNum);
        devNum = -1
    }
}