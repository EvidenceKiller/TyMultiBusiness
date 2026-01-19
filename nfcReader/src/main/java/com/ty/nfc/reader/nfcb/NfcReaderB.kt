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
    private val pSnrM1: ByteArray = ByteArray(255)
    private val snrSize: ByteArray = ByteArray(2)
    private val tag: IntArray = IntArray(2)
    private val sak: ByteArray = ByteArray(2)
    private val pCharSingle: CharArray = CharArray(255)
    override fun init(context: Context) {
    }

    override fun startRead(callback: ReadCallback) {
        super.startRead(callback)
        Log.d(TAG, "NFCB startRead")
        readingTask = scheduler.scheduleWithFixedDelay({
            performReaderARead()
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    @Synchronized
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
        Log.d(TAG, "NFCB 读卡")
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

    @Synchronized
    override fun stopRead() {
        super.stopRead()
        Log.d(TAG, "停止读卡")
        comproCall.lc_exit(devNum);
        devNum = -1
    }

    @Synchronized
    override fun release() {
        Log.d(TAG, "释放资源")
        super.release()
    }
}