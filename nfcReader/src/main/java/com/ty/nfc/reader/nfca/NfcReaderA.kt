package com.ty.nfc.reader.nfca

import Lib.LCReader.T6.function_T6
import android.content.Context
import android.util.Log
import com.ty.nfc.reader.base.AbstractNfcReader
import com.ty.nfc.reader.base.ReadCallback
import java.util.concurrent.TimeUnit


class NfcReaderA : AbstractNfcReader() {
    private val TAG = "NFCA"
    private var callContactLess: function_T6? = null
    private var devNum = -1
    private val pModVer: CharArray = CharArray(512)
    private val rvHexCard: CharArray = CharArray(128) //Card SN in HEX type(Reverse)
    private val rvStrCard: CharArray = CharArray(128) //Card SN in HEX string type (Reverse)
    private val strCard: CharArray = CharArray(128) //Card SN in HEX string type
    private val hexCard: CharArray = CharArray(128) //Card SN in HEX type
    override fun init(context: Context) {
        callContactLess = function_T6(context)
        callContactLess?.SetTransPara(0x20, 1137, 41234);
    }

    override fun startRead(callback: ReadCallback) {
        super.startRead(callback)
        if (callContactLess == null) {
            Log.d(TAG, "请先初始化设备")
            return
        }
        readingTask = scheduler.scheduleWithFixedDelay({
            performReaderARead()
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    @Synchronized
    private fun performReaderARead() {
        if (devNum == -1) {
            devNum = callContactLess!!.fw_init_ex(2, null, 0)
            if (devNum != -1) {
                val result = callContactLess!!.fw_getver(devNum, pModVer).toInt()
                if (result == 0) {
                    callContactLess!!.fw_config_card(devNum, 'A'.code.toByte())
                    Log.d(TAG, "设备初始化成功")
                } else {
                    Log.e(TAG, "设备初始化失败,设备类型不匹配")
                    callContactLess?.fw_exit(devNum)
                    stopRead()
                }
            } else {
                Log.d(TAG, "设备初始化失败")
                return
            }
        }
        //获取卡号 UID
        callContactLess!!.fw_reset(devNum, 10)
        val result = callContactLess!!.fw_card_str(devNum, 1.toShort(), strCard).toInt()
        if (0 == result) {
            callContactLess!!.fw_beep(devNum, 10)
            callContactLess!!.fw_halt(devNum)
            callContactLess!!.a_hex(hexCard, strCard, 4)
            val cardNum =
                ((hexCard[3]).code.toLong() shl 24) + ((hexCard[2]).code.toLong() shl 16) + ((hexCard[1]).code.toLong() shl 8) + (hexCard[0]).code.toLong()
            callContactLess!!.hex_a(rvStrCard, rvHexCard, 8)
            Log.d(TAG, "cardNum: $cardNum")
            mReadCallback?.readCardBack(cardNum.toString())
        }
    }

    @Synchronized
    override fun stopRead() {
        super.stopRead()
        Log.d(TAG, "停止读卡")
        callContactLess?.fw_exit(devNum);
        devNum = -1
    }
}