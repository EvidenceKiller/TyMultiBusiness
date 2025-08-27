package com.ty.tybusbase.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager


object DisplayUtil {
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getSize(context: Context): Point {
        // 通过上下文的getSystemService()方法获取系统服务来获取WindowManager对象
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // 通过WindowManager对象的getDefaultDisplay()方法获取Display对象
        val display = windowManager.defaultDisplay
        // 使用Point来保存屏幕宽、高两个数据
        val outSize = Point()
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display.getRealSize(outSize)
        return outSize
    }
}