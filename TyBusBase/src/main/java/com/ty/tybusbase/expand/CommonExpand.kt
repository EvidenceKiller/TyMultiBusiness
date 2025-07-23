package com.aipower.modulecommon.expand

import android.graphics.PointF
import androidx.annotation.StringRes
import com.ty.tybusbase.utils.ApplicationUtil
import java.io.File
import kotlin.math.sqrt

const val TAG = "NSS-->"
fun PointF.distance(): Double = sqrt((this.x * this.x).toDouble() + this.y * this.y)

fun PointF.square(): Double = (this.x * this.x).toDouble() + this.y * this.y

typealias Action = () -> Unit
typealias Action1<T> = (t: T) -> Unit
typealias Action2<T, R> = (t: T, r: R) -> Unit
typealias Action3<T, R, W> = (t: T, r: R, w: W) -> Unit

/**
 * 获取文件后缀
 */
fun File.getFileExtension(): String {
    if (!this.isFile) {
        return ""
    }
    val lastDotIndex = this.name.lastIndexOf('.')
    return if (lastDotIndex >= 0) {
        this.name.substring(lastDotIndex + 1)
    } else {
        ""
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


fun getString(@StringRes id: Int, vararg fre: Any):String = ApplicationUtil.getContext().getString(id, *fre)