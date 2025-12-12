package com.ty.identifty.stface

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author :WEI_CP
 * Time:2022/12/29 13:32
 * Description: this is ST
 */
object StHelper {
    @JvmStatic
    fun setDebugAble(debugAble: Boolean) {
        StHelper.debugAble = debugAble
    }

    val dformat = SimpleDateFormat("yyyy-MM-dd")
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private var debugAble = true

    private var logPath = "/sdcard/zy.sport.cache/busi/log"
    private val writeSize = 1 * 1024 // 1K
    private val logcatLength = 256
    private var writeFileEnable = true
    private var consoleEnable = true
    private val stringBuffer by lazy { StringBuffer() }
    private val TAG = StContact.LOG_TAG

    /**
     * 设置私有外部存储路径，兼容Android高版本无法访问公共目录的情况
     */
    fun setInternalDir(context: Context) {
        context.getExternalFilesDir("log")?.apply {
            logPath = this.absolutePath
        }
    }

    @Synchronized
    fun d(msg: String, flushNow: Boolean = false) {
        if (consoleEnable) {
            val splitMsg = splitMsg(msg)
            splitMsg.forEach {
                Log.d(TAG, it)
            }
        }
        cacheLog(msg, flushNow)
    }

    @Synchronized
    fun e(msg: String, flushNow: Boolean = false) {
        if (consoleEnable) {
            val splitMsg = splitMsg(msg)
            splitMsg.forEach {
                Log.e(TAG, it)
            }
        }
        cacheLog(msg, flushNow)
    }

    @Synchronized
    private fun cacheLog(msg: String, flushNow: Boolean) {
        if (!writeFileEnable) return
        stringBuffer.append(format.format(Date()) + ": ")
        stringBuffer.append(msg + "\n")
        flush(flushNow)
    }

    @Synchronized
    private fun splitMsg(msg: String): List<String> {
        val list = mutableListOf<String>()
        var start = 0
        var end = 0
        while (start < msg.length) {
            end = start + logcatLength
            if (end > msg.length) {
                end = msg.length
            }
            list.add(msg.substring(start, end))
            start = end
        }
        return list
    }

    @Synchronized
    private fun flush(flush: Boolean) {
        if (flush || stringBuffer.length >= writeSize) {
            //写文件
            GlobalScope.launch(Dispatchers.IO) {
                writeLog(stringBuffer.toString())
                stringBuffer.delete(0, stringBuffer.length)
            }
        }
    }

    @Synchronized
    private fun writeLog(log: String) {
        try {
            val fileD = File(logPath)
            if (!fileD.exists()) {
                fileD.mkdirs()
            }
            val fileName: String = dformat.format(Date()) + ".log"
            val logFile = File(fileD, "face_$fileName")
            val fw = FileWriter(logFile, true)
            fw.write(log)
            fw.flush()
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun flushNow() {
        flush(true)
    }

    fun enableWriteFile(value: Boolean) {
        writeFileEnable = value
    }

    fun setConsoleEnable(vaule: Boolean) {
        consoleEnable = vaule
    }

    fun getLogPath() = logPath

    @JvmStatic
    fun parseSdkErrorCode(ret: Int, msg: String?) {
        if (ret == 0) {
            return
        }
        val errMsg = msg + "错误码: " + ret + "! "
        if (ret == -13) {
            d(errMsg + "please check if the license file exists!")
        } else if (ret == -17) {
            d(errMsg + "please check if you are connected to the network!")
        } else if (ret == -29) {
            d(errMsg + "please try turn on you wifi!")
        } else if (ret == -32) {
            d(errMsg + "please check if the I2C communication is normal!")
        } else if (ret == -33) {
            d(errMsg + "please check if the license key is correct!")
        } else if (ret == -1) {
            d(errMsg + "please check if the plugin path is correct!")
        } else if (ret == -4) {
            d(errMsg + "please check if the model path is correct!")
        } else {
            d(errMsg)
        }
        //System.exit(0);
    }
}
