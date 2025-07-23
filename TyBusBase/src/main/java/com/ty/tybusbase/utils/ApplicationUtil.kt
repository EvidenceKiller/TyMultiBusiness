package com.ty.tybusbase.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@SuppressLint("StaticFieldLeak")
object ApplicationUtil {

    private var context: Context? = null

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    fun getContext(): Context {
        if (context != null) {
            return context!!
        }
        throw NullPointerException("should be initialized in application")
    }

    fun isAppInstalled(context: Context, packageName: String?): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 获取版本号
     *
     * @param mContext 上下文
     * @return
     */
    fun getVersionCode(mContext: Context): Int {
        var versionCode = 0
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode =
                mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }

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

    /**
     * 获取打包时间
     */
    fun getBuildTime(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val simpleFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            return simpleFormatter.format(Date(packageInfo.lastUpdateTime))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun copyFilesFromAssets(context: Context, assetsPath: String, savePath: String) {
        try {
            val fileNames = context.assets.list(assetsPath) // 获取assets目录下的所有文件及目录名
            if (fileNames!!.size > 0) { // 如果是目录
                val file = File(savePath)
                file.mkdirs() // 如果文件夹不存在，则递归
                for (fileName in fileNames) {
                    copyFilesFromAssets(
                        context, "$assetsPath/$fileName",
                        "$savePath/$fileName"
                    )
                }
            } else { // 如果是文件
                val `is` = context.assets.open(assetsPath)
                val fos = FileOutputStream(File(savePath))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (`is`.read(buffer).also { byteCount = it } != -1) { // 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount) // 将读取的输入流写入到输出流
                }
                fos.flush() // 刷新缓冲区
                `is`.close()
                fos.close()
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

}