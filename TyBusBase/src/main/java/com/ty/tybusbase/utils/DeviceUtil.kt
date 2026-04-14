package com.ty.tybusbase.utils

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.adl.base.common.AdlDeviceHelper
import com.adl.base.common.AdlDevicePlatform
import java.io.DataOutputStream
import java.io.File
import java.io.IOException


object DeviceUtil {

    /*   settings put system show_navigation_bar true  //显示导航按键，
    / *   settings put system notification_panel_enable false  //显示导航按键，
    settings put system show_navigation_bar false  //隐藏导航按键
    settings get system show_navigation_bar  //获取导航按键状态*/
    //山东设备
    //settings put global hide_status_bar 1  //显示状态栏
    //settings put global hide_status_bar 0 //隐藏状态栏
    //settings put global hide_navigation 1 //显示导航
    //settings put global hide_navigation 0 //隐藏导航
    var EQUIPMENT_MODEL_ONE = "Konaforarm64"
    var EQUIPMENT_MODEL_TWO = "哪吒体育智慧屏"
    var EQUIPMENT_MODEL_THREE = "Kalamaforarm64"

    var EQUIPMENT_MODEL_FOUR = "rk3588"

    var EQUIPMENT_MODEL_TS2 = "A10R-RK3588"

    var EQUIPMENT_MODEL_RK3399 = "rk3399-Android10"

    var EQUIPMENT_MODEL_Xm6Pro = "23046RP50C"

    var EQUIPMENT_MODEL_Xm7Pro = "24091RPADC"

    var EQUIPMENT_MODEL_HONOR = "ROD2-W09" //荣耀平板


    fun canDownLoad(): Boolean {
        return getModel() == EQUIPMENT_MODEL_ONE
                || getModel() == EQUIPMENT_MODEL_TWO
                || getModel() == EQUIPMENT_MODEL_THREE
                || getModel() == EQUIPMENT_MODEL_FOUR
                || getModel() == EQUIPMENT_MODEL_TS2
    }

    fun isKunL2(): Boolean = getModel() == EQUIPMENT_MODEL_THREE

    fun isKunL1(): Boolean = getModel() == EQUIPMENT_MODEL_ONE || getModel() == EQUIPMENT_MODEL_TWO

    fun isTsRk3588(): Boolean = AdlDeviceHelper.getPlatform() == AdlDevicePlatform.TShan

    fun isTsRk35882(): Boolean = getModel() == EQUIPMENT_MODEL_TS2

    fun isXm6Pro(): Boolean = getModel() == EQUIPMENT_MODEL_Xm6Pro

    fun isXm7Pro(): Boolean = getModel() == EQUIPMENT_MODEL_Xm7Pro

    fun isHonor(): Boolean = getModel() == EQUIPMENT_MODEL_HONOR

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    fun getVerName(context: Context): String {
        var verName = ""
        try {
            verName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return verName
    }


    fun getTopActivityPkgName(context: Context): String {
        var topPkgName = "";
        //get top activity package name
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses = am.runningAppProcesses
//
//        if (appProcesses != null && appProcesses.isNotEmpty()){
//            topPkgName = appProcesses[0].processName
//        }

        val comp = am.getRunningTasks(10)[0].topActivity
        if (comp != null) {
            topPkgName = comp.packageName
        }
        getTopActivityByUsageStatsManager(context)
        return topPkgName
    }

    fun getTopActivityByUsageStatsManager(context: Context): String? {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10000
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
        var activityInfo = ""
        val event = UsageEvents.Event()
        val usageEvents = usageStatsManager!!.queryEvents(beginTime, endTime)
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                activityInfo = event.packageName + "/" + event.className
            }
        }
        return activityInfo
    }

    /**
     *
     * @return 设备型号
     */
    fun getModel(): String {
        var model = Build.MODEL
        model = model?.trim { it <= ' ' }?.replace("\\s*".toRegex(), "") ?: ""
        return model
    }

    fun execShellCmd(cmd: String?) {
        var auth = getRootCommand()
        if (TextUtils.isEmpty(auth)) {
            return
        }
        try {
            val process = Runtime.getRuntime().exec(auth)
            val outputStream = process.outputStream
            val dataOutputStream = DataOutputStream(outputStream)
            dataOutputStream.writeBytes(cmd)
            dataOutputStream.flush()
            dataOutputStream.close()
            outputStream.close()
            process.waitFor()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun execShellCmdResult(cmd: String?): String {
        var auth = getRootCommand()
        if (TextUtils.isEmpty(auth)) {
            return ""
        }
        try {
            val process = Runtime.getRuntime().exec(auth)
            val outputStream = process.outputStream
            val dataOutputStream = DataOutputStream(outputStream)
            dataOutputStream.writeBytes(cmd)
            dataOutputStream.flush()
            dataOutputStream.close()
            outputStream.close()
            process.waitFor()
            val inputStream = process.inputStream
            val available = inputStream.available()
            val byteA = ByteArray(available)
            inputStream.read(byteA)
            inputStream.close()
            return String(byteA)
        } catch (e: IOException) {
            return e.toString()
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return e.toString()
        }
    }

    private fun getRootCommand(): String {
        return when (getModel()) {
            EQUIPMENT_MODEL_ONE, EQUIPMENT_MODEL_THREE -> {
                "ubiot"
            }

            EQUIPMENT_MODEL_TWO, EQUIPMENT_MODEL_FOUR, EQUIPMENT_MODEL_TS2, EQUIPMENT_MODEL_RK3399 -> {
                "su"
            }

            else -> "su"
        }
    }

    fun getSerialNumber(): String? {
        var serial: String? = null
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            serial = get.invoke(c, "ro.serialno") as String
            println(serial)
        } catch (ignored: Exception) {
        }
        return serial
    }

    fun getToday(): String {


        return ""
    }
}