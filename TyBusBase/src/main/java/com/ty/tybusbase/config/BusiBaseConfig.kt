package com.ty.tybusbase.config

import com.google.gson.Gson
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil

object BusiBaseConfig {

    /**
     * 更新游客模式
     */
    fun updateVisitorEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.VISITOR_ABLE, enable)
    }

    /**
     * 获取游客模式
     */
    fun getVisitorEnable(): Boolean {
        return MMKVUtil.decodeBoolean(MMKVBusiKey.VISITOR_ABLE, false)
    }

    /**
     * 获取DEBUG模式
     */
    fun getDebugEnable(): Boolean {
        return MMKVUtil.decodeBoolean(
            MMKVBusiKey.MODEL_DEBUG,
            BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
        )
    }

    /**
     * 设置默认调试开关
     */
    fun setDebugEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.MODEL_DEBUG, enable)
    }

    /**
     * 0:前置，1：后置，
     */
    fun setCameraId(id: Int) {
        MMKVUtil.encode(MMKVBusiKey.CAMERA_CHOOSE, id)
    }

    fun getCameraId(): Int {
        return MMKVUtil.decodeInt(MMKVBusiKey.CAMERA_CHOOSE, BusiBaseConstantsValue.CAMERA_BACK)
    }

    /**
     * 设置摄像头类型
     */
    fun setCameraType(cameraType: Int) {
        MMKVUtil.encode(MMKVBusiKey.CAMERA_TYPE_CHOOSE, cameraType)
    }

    /**
     * 获取摄像头类型
     */
    fun getCameraType(): Int {
        return MMKVUtil.decodeInt(
            MMKVBusiKey.CAMERA_TYPE_CHOOSE,
            BusiBaseConstantsValue.CAMERA_TYPE_DEFAULT
        )
    }

    /**
     * 设置骨骼镜像
     */
    fun setSkeletonMirror(mirror: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.SKELETON_MIRROR, mirror)
    }

    fun getSkeletonMirror(): Boolean {
        return MMKVUtil.decodeBoolean(
            MMKVBusiKey.SKELETON_MIRROR,
            BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
        )
    }

    /**
     * 设置摄像头镜像
     */
    fun setCameraMirror(mirror: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.CAMERA_MIRROR, mirror)
    }

    fun getCameraMirror(): Boolean {
        return MMKVUtil.decodeBoolean(
            MMKVBusiKey.CAMERA_MIRROR,
            BusiBaseConstantsValue.DEFAULT_CAMERA_MIRROR
        )
    }

    /**
     * 更新识别类型
     */
    fun updateIdentityType(type: Int) {
        MMKVUtil.encode(MMKVBusiKey.IDENTITY_TYPE, type)
    }

    /**
     * 获取识别类型
     */
    fun getIdentityType(): Int {
        return MMKVUtil.decodeInt(
            MMKVBusiKey.IDENTITY_TYPE,
            BusiBaseConstantsValue.IDENTITY_TYPE_FACE
        )
    }

    /**
     * 获取仰卧起坐设置参数
     */
    fun getSiteUpParamBean(): SiteUpParamBean {

        return MMKVUtil.decodeParcelable<SiteUpParamBean>(
            MMKVBusiKey.PARAM_FREE_SPORT_SITE_UP,
            SiteUpParamBean::class.java
        ) ?: SiteUpParamBean()
    }

    /**
     * 更新仰卧起坐设置参数
     */
    fun updateSiteUpParamBean(bean: SiteUpParamBean) {
        MMKVUtil.encode(MMKVBusiKey.PARAM_FREE_SPORT_SITE_UP, bean)
    }

    /**
     * 更新NFC读卡器类型
     */
    fun updateNfcReaderType(type: Int) {
        MMKVUtil.encode(MMKVBusiKey.NFC_READER_TYPE, type)
    }

    fun getReportVersion(): String = "V1.1"

    /**
     * 获取NFC读卡器类型
     */
    fun getNfcReaderType(): Int {
        return MMKVUtil.decodeInt(
            MMKVBusiKey.NFC_READER_TYPE,
            BusiBaseConstantsValue.DEFAULT_NFC_READER_TYPE
        )
    }

}