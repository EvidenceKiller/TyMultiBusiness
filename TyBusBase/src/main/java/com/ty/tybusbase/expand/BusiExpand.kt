package com.ty.tybusbase.expand

import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.ty.identifty.base.TyFaceInfo
import com.ty.tybusbase.R
import com.ty.tybusbase.utils.ApplicationUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.nio.ByteBuffer

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


fun getString(@StringRes id: Int, vararg fre: Any): String =
    ApplicationUtil.getContext().getString(id, *fre)

fun Int.toEven(): Int {
    return if (this % 2 > 0) {
        this - 1
    } else this
}

fun getFaceBitmap(info: TyFaceInfo<*>): Bitmap {
    val fw: Int
    val fh: Int
    if (info.width * 1.5 <= info.faceWidth && info.height * 1.5 <= info.faceHeight) {
        fw = (info.width * 1.5).toInt()
        fh = (info.height * 1.5).toInt()
    } else {
        fw = info.width.toInt()
        fh = info.height.toInt()
    }

    val x: Int = (info.centerX - fw / 2).toInt()
    val y: Int = (info.centerY - fh / 2).toInt()

    val ic = bgr2Bitmap(info.faceIconData, info.faceWidth, info.faceHeight)

    var nx = 0.coerceAtLeast(x)
    var ny = 0.coerceAtLeast(y)

    if (nx + fw > info.faceWidth) {
        nx = info.faceWidth - fw
    }
    if (ny + fh > info.faceHeight) {
        ny = info.faceHeight - fh
    }

    return Bitmap.createBitmap(ic, nx, ny, fw, fh)
}

fun bgr2Bitmap(bytes: ByteArray, width: Int, height: Int): Bitmap {
    val stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val rgba = ByteArray(width * height * 4)

    for (i in 0 until width * height) {
        val idx1 = i * 3
        val b1 = bytes[idx1 + 2]
        val b2 = bytes[idx1 + 1]
        val b3 = bytes[idx1 + 0]
        val idx2 = i * 4
        rgba[idx2 + 0] = b3
        rgba[idx2 + 1] = b2
        rgba[idx2 + 2] = b1
        rgba[idx2 + 3] = -1
    }

    stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba))
    return stitchBmp
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun EditText.textChanges(): Flow<String> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.toString()?.let { trySend(it) }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    addTextChangedListener(textWatcher)
    awaitClose { removeTextChangedListener(textWatcher) }
}

var View.lastClickTime: Long
    get() = getTag(R.id.last_click_time) as? Long ?: 0
    set(value) = setTag(R.id.last_click_time, value)

fun View.onDebouncedClick(delay: Long = 500, action: (View) -> Unit) {
    setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= delay) {
            lastClickTime = now
            action(it)
        }
    }
}