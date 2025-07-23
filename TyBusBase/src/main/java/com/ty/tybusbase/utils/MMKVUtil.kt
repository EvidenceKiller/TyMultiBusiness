package com.ty.tybusbase.utils

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import java.util.Collections

object MMKVUtil {
    private val mmkv by lazy { MMKV.defaultMMKV() }

    /**
     * 存值
     */
    fun encode(key: String, value: Any?) {
        when (value) {
            is String -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is ByteArray -> mmkv.encode(key, value)
            is Nothing -> return
        }
    }

    /**
     * 存值
     */
    fun <T : Parcelable> encode(key: String, t: T?) {
        if (t == null) {
            return
        }
        mmkv.encode(key, t)
    }

    /**
     * 存值
     */
    fun encode(key: String, sets: Set<String>?) {
        if (sets == null) {
            return
        }
        mmkv.encode(key, sets)
    }

    /**
     * 获取Int值
     */
    fun decodeInt(key: String,defaultValue: Int = 0): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    /**
     * 获取Double值
     */
    fun decodeDouble(key: String,defaultValue: Double = 0.0): Double {
        return mmkv.decodeDouble(key, defaultValue)
    }

    /**
     * 获取Long值
     */
    fun decodeLong(key: String,defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    /**
     * 获取Boolean值
     */
    fun decodeBoolean(key: String,defaultValue:Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    /**
     * 获取Float值
     */
    fun decodeFloat(key: String,defaultValue: Float = 0F): Float {
        return mmkv.decodeFloat(key, defaultValue)
    }

    /**
     * 获取ByteArray值
     */
    fun decodeByteArray(key: String): ByteArray? {
        return mmkv.decodeBytes(key)
    }

    /**
     * 获取String值
     */
    fun decodeString(key: String,defaultValue: String = ""): String? {
        return mmkv.decodeString(key, defaultValue)
    }

    fun <T : Parcelable> decodeParcelable(key: String, tClass: Class<T>): T? {
        return mmkv.decodeParcelable(key, tClass)
    }

    fun decodeStringSet(key: String): Set<String>? {
        return mmkv.decodeStringSet(key, Collections.emptySet())
    }

    /**
     * 移除Key
     */
    fun removeKey(key: String) {
        mmkv?.removeValueForKey(key)
    }

    /**
     * 清除所有
     */
    fun clearAll() {
        mmkv?.clearAll()
    }

    /**
     * 获取所有String值
     */
    fun getAll() {
        val vales = mmkv?.allKeys()
        vales?.forEach {
            val temp = mmkv?.getString(it, "")
        }
    }
}
