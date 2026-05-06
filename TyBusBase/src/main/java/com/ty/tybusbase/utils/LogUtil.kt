package com.ty.tybusbase.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

val dformat = SimpleDateFormat("yyyy-MM-dd")
val dformat2 = SimpleDateFormat("yyyyMMdd")
val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

object LogUtil {
    private var logPath = "/sdcard/zy.sport.cache/busi/log"
    private val writeSize = 1 * 1024 // 1K
    private val logcatLength = 256
    private var writeFileEnable = true
    private var consoleEnable = true
    private val stringBuffer by lazy { StringBuffer((writeSize * 1.2).toInt()) }
    private val TAG = "BUSI>>"

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

    private fun cacheLog(msg: String, flushNow: Boolean) {
        if (!writeFileEnable) return
        stringBuffer.append(format.format(Date()) + ": ")
        stringBuffer.append(msg + "\n")
        flush(flushNow)
    }

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

    @OptIn(DelicateCoroutinesApi::class)
    private fun flush(flush: Boolean) {
        if (flush || stringBuffer.length >= writeSize) {
            //写文件
            GlobalScope.launch(Dispatchers.IO) {
                synchronized(this){
                    writeLog(stringBuffer.toString())
                    stringBuffer.delete(0, stringBuffer.length)
                }
            }
        }
    }

    private fun writeLog(log: String) {
        try {
            val fileD = File(logPath)
            if (!fileD.exists()) {
                fileD.mkdirs()
            }
            val fileName: String = dformat.format(Date()) + ".log"
            val logFile = File(fileD, fileName)
//            if (!logFile.exists()) {
//                logFile.createNewFile()
//            }
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
}