package com.ty.tybusbase.expand

import androidx.annotation.StringRes
import com.ty.tybusbase.utils.ApplicationUtil
import java.io.File

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

fun Int.toEven(): Int {
    return if (this % 2 > 0) {
        this - 1
    } else this
}