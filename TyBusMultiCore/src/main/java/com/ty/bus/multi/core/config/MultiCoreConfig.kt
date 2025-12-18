package com.ty.bus.multi.core.config

import com.google.gson.Gson
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.constants.MultiCoreConstant.MULTI_THEME_STYLE_LARGE_SCREEN
import com.ty.bus.multi.core.constants.MultiCoreConstant.MULTI_THEME_STYLE_SMALL_SCREEN
import com.ty.bus.multi.core.entity.ForwardFlexionParam
import com.ty.bus.multi.core.entity.IpcCameraInfo
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil

object MultiCoreConfig {
    /**
     * 更新体前屈参数
     */
    fun updateForwardFlexionParam(param: ForwardFlexionParam?) {
        val gson = Gson()
        MMKVUtil.encode(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM, gson.toJson(param))
    }

    /**
     * 获取体前屈参数
     */
    fun getForwardFlexionParam(): ForwardFlexionParam {
        val gson = Gson()
        val str = MMKVUtil.decodeString(MultiCoreConstant.FORWARD_FLEXION_SET_PARAM)
        return if (str.isNullOrEmpty()) ForwardFlexionParam() else gson.fromJson<ForwardFlexionParam>(
            str,
            ForwardFlexionParam::class.java
        )
    }

    /**
     * 更新多人运动性能参数
     */
    fun updateMultiSportPerformance(value: Boolean) {
        MMKVUtil.encode(MultiCoreConstant.MULTI_SKIP_PERFORMANCE, value)
    }

    /**
     * 获取多人运动性能参数
     */
    fun getMultiSportPerformance(): Boolean =
        MMKVUtil.decodeBoolean(MultiCoreConstant.MULTI_SKIP_PERFORMANCE, true)

    /**
     * 更新IPC摄像头参数
     */
    fun updateIPCCameraParam(ipcCameraInfo: IpcCameraInfo) {
        MMKVUtil.encode(MMKVBusiKey.HTTP_CAMERA_IP, ipcCameraInfo.ip)
        MMKVUtil.encode(
            MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
            ipcCameraInfo.account
        )
        MMKVUtil.encode(MMKVBusiKey.HTTP_CAMERA_PSW, ipcCameraInfo.psw)
    }

    /**
     * 获取IPC摄像头参数
     */
    fun getIPCCameraParam(): IpcCameraInfo {
        return IpcCameraInfo(
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_IP,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP
            ) ?: BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP,
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
            ) ?: BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT,
            MMKVUtil.decodeString(
                MMKVBusiKey.HTTP_CAMERA_PSW,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
            ) ?: BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
        )
    }

        /**
         * 更新多人运动风格
         */
        fun setMultiSportThemeFixedStyle() {
            MMKVUtil.encode(MultiCoreConstant.MULTI_THEME_STYLE, MULTI_THEME_STYLE_SMALL_SCREEN)
        }

        /**
         * 获取多人运动风格
         */
        fun isMultiSportThemeLargeScreenStyle(): Boolean =
            MMKVUtil.decodeInt(MultiCoreConstant.MULTI_THEME_STYLE, MULTI_THEME_STYLE_LARGE_SCREEN) == MULTI_THEME_STYLE_LARGE_SCREEN
}