package com.ty.tybusbase.expand

import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.google.gson.Gson
import com.ty.grpclib.GrpcManager
import com.ty.identifty.base.TyFaceInfo
import com.ty.tybusbase.R
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.utils.ApplicationUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat

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

/**
 * 获取文件后缀（扩展名）
 * @param filePath 文件路径（可以是完整路径或文件名）
 * @return 后缀字符串，若无后缀则返回空字符串
 */
fun String.getFileExtension(): String {
    // 提取文件名（兼容带路径的情况）
    val fileName = substringAfterLast('/').substringAfterLast('\\')
    // 查找最后一个点
    val lastDotIndex = fileName.lastIndexOf('.')
    // 如果点不存在，或者点位于第一个字符（隐藏文件），则无后缀
    return if (lastDotIndex > 0) fileName.substring(lastDotIndex + 1) else ""
}

/**
 * 时间戳转换成字mm:ss符串
 */
fun Long.toMs(): String {
    var timeFormat = SimpleDateFormat("mm:ss")
    return timeFormat.format(this)
}

/**
 * 时间戳转换成字mm:ss:ss字符串
 * 比赛计时
 */
fun Long.toRaceTiming(): String {
    var dateTimes: String? = null
    val days = this / (60 * 60 * 24)
    val hours = (this % (60 * 60 * 24)) / (60 * 60)
    val minutes = (this % (60 * 60)) / 60
    val seconds = this % 60
    dateTimes = when {
        days > 0 -> {
            (days.toString() + "'" + hours + "'" + minutes + "'"
                    + seconds + "\"")
        }

        hours > 0 -> {
            (hours.toString() + "'" + minutes + "'"
                    + seconds + "\"")
        }

        minutes > 0 -> {
            (minutes.toString() + "'"
                    + seconds + "\"")
        }

        else -> {
            (0.toString() + "'"
                    + seconds + "\"")
        }
    }
    return dateTimes
}

/**
 * JSON字符串转对象
 * @param clazz 目标对象类型
 * @return 解析后的对象，解析失败或字符串为空时返回 null
 */
fun String.toObject(clazz: Class<*>): Any? {
    if (isBlank()) return null
    return try {
        Gson().fromJson(this, clazz)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * JSON字符串转对象（泛型版本）
 * 用法：val bean = jsonStr.toObject<MyBean>()
 * @return 解析后的对象，解析失败或字符串为空时返回 null
 */
inline fun <reified T> String.toObject(): T? {
    if (isBlank()) return null
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 对象转JSON字符串
 * @param obj 要转换的对象
 * @return JSON字符串，转换失败或对象为 null 时返回 null
 */
fun Any.toJson(): String {
    return try {
        Gson().toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 对象转JSON字符串（nullable 版本，可安全处理 null 对象）
 * @return JSON字符串，转换失败时返回 null，对象为 null 时返回 null
 */
fun Any?.toJsonOrNull(): String {
    if (this == null) return ""
    return try {
        Gson().toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun String.getDisplayType(leftSubScreenIp: String?, rightSubScreenIp: String?): DisplayType{
    val ip = GrpcManager.getClientIp(this)
    return when (ip) {
        leftSubScreenIp -> DisplayType.LEFT
        rightSubScreenIp -> DisplayType.RIGHT
        else -> DisplayType.OTHER
    }
}

/**
 * 创建一次性事件流（非粘性）：
 * - replay=0：事件仅在发射当下派发给已订阅方；错过即丢弃，页面重新订阅不会重放历史事件，
 *   适合所有"只处理一次"的 UI 消息（倒计时更新、识别结果、页面跳转等）。
 * - 大容量缓冲 + DROP_OLDEST：高频事件不会阻塞发射方，且不易丢失（仅极端积压时丢弃最旧值）
 */
fun <T> eventFlow(replay: Int = 0): MutableSharedFlow<T> =
    MutableSharedFlow(
        replay = replay,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

fun GroupSportBaseMan.checkHandUp(): Boolean {
    if (skeleton == null || skeleton.lShoulder == null || skeleton.rWrist == null)
        return false
    return skeleton.lWrist.y < skeleton.lShoulder.y
}