package com.ty.nfc.reader.nfcb

import Lib.LCReader.comPro.Function_comPro
import android.content.Context
import android.util.Log
import com.ty.nfc.reader.base.AbstractNfcReader
import com.ty.nfc.reader.base.ReadCallback
import java.util.concurrent.TimeUnit

class NfcReaderB : AbstractNfcReader() {
    private val TAG = "NFCB"
    private var callComPro: Function_comPro? = null
    private var devNum = -1
    private val pModVer: CharArray = CharArray(512)
    private val rvHexCard: CharArray = CharArray(128) //Card SN in HEX type(Reverse)
    private val rvStrCard: CharArray = CharArray(128) //Card SN in HEX string type (Reverse)
    private val pSnrM1: ByteArray = ByteArray(255)
    private val snrSize: ByteArray = ByteArray(2)
    private val tag: IntArray = IntArray(2)
    private val sak: ByteArray = ByteArray(2)
    private val pCharHex: CharArray = CharArray(255)
    private val pCharSingle: CharArray = CharArray(255)
    override fun init(context: Context) {
        callComPro = Function_comPro(context, null)
    }

    override fun startRead(callback: ReadCallback) {
        super.startRead(callback)
        if (callComPro == null) {
            Log.d(TAG, "请先初始化")
            return
        }
        readingTask = scheduler.scheduleWithFixedDelay({
            performReaderARead()
        }, 0, interval, TimeUnit.SECONDS)
    }

    private fun performReaderARead() {
        if (devNum == -1) {
            devNum = callComPro?.lc_init_ex(2, null, 0)?.toInt() ?: -1
            if (devNum != -1) {
                val result = callComPro?.lc_getver(devNum, pModVer)
                if (result == 0) {
                    Log.d(TAG, "设备初始化成功")
                } else {
                    Log.e(TAG, "设备初始化失败,设备类型不匹配")
                    callComPro?.lc_exit(devNum)
                    stopRead()
                }
            } else {
                Log.d(TAG, "设备初始化失败")
                return
            }
        }
        callComPro!!.lc_rfReset(devNum, 10) //RF Reset
        //获取卡号 UID
        val result = callComPro!!.lc_card(devNum, 1.toByte(), pSnrM1, snrSize, tag, sak);
        if (0 == result) {
            for (i in 0 until snrSize[0]) {
                pCharSingle[i] = Char(pSnrM1[i].toUShort())
            }
            callComPro!!.hex_asc(pCharSingle, pCharHex, snrSize[0].toInt())
            for (i in 0 until 4) {
                rvHexCard[i] = pCharSingle[3 - i]
            }
            val cardNum =
                ((rvHexCard[0].code and 0xff).toLong() shl 24) + ((rvHexCard[1].code and 0xff).toLong() shl 16) + ((rvHexCard[2].code and 0xff).toLong() shl 8) + (rvHexCard[3].code and 0xff).toLong()
            callComPro!!.hex_asc(rvHexCard, rvStrCard, 4)
            mReadCallback?.readCardBack(cardNum.toString())
        }
    }

    override fun stopRead() {
        super.stopRead()
        callComPro?.lc_exit(devNum);
        devNum = -1
    }
}