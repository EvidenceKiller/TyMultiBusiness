package com.ty.tybusbase.config

import android.content.Context
import android.text.TextUtils
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.sport.standard.ui.SportOverlayRopeSkipEx
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.IpcCameraConfig
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

    fun setDebugEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.MODEL_DEBUG, enable)
    }

    /**
     * 获取骨骼开关
     */
    fun getSkeletonEnable(): Boolean {
        return MMKVUtil.decodeBoolean(
            MMKVBusiKey.SKELETON_ABLE,
            BusiBaseConstantsValue.DEFAULT_SKELETON
        )
    }

    fun setSkeletonEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.SKELETON_ABLE, enable)
    }

    /**
     * 获取骨骼旋转角度
     */
    fun getSkeletonRotate(): Int {
        return MMKVUtil.decodeInt(
            MMKVBusiKey.SKELETON_ROTATE,
            0
        )
    }

    /**
     * 设置骨骼旋转角度
     */
    fun setSkeletonRotate(value: Int) {
        MMKVUtil.encode(MMKVBusiKey.SKELETON_ROTATE, value)
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

    /**
     * 设置国标区域
     */
    fun setDoubleSkipGbDefaultRegion(context: Context) {
        val configLeft = RoleConfigRopeSkip()
        val configRight = RoleConfigRopeSkip()
        val sportOverlay = SportOverlayRopeSkipEx(context, null)
        if (TextUtils.isEmpty(configLeft.key)) {
            configLeft.key = sportOverlay.configKey + "LEFT"
        }
        if (TextUtils.isEmpty(configRight.key)) {
            configRight.key = sportOverlay.configKey + "RIGHT"
        }
        val readLeft =
            AdlUIConfig.instance().readConfig(configLeft.key, RoleConfigRopeSkip::class.java)
        val readRight =
            AdlUIConfig.instance().readConfig(configRight.key, RoleConfigRopeSkip::class.java)
        //已经有值则不再设置
        if (readLeft?.targetRegion != null && readRight?.targetRegion != null) return
        configLeft.targetRegion =
            "[{\"x\":271,\"y\":704},{\"x\":664,\"y\":703},{\"x\":179,\"y\":1058},{\"x\":762,\"y\":1060}]"
        configRight.targetRegion =
            "[{\"x\":1229,\"y\":697},{\"x\":1632,\"y\":698},{\"x\":1157,\"y\":1055},{\"x\":1746,\"y\":1059}]"
        AdlUIConfig.instance().saveConfig(configLeft)
        AdlUIConfig.instance().saveConfig(configRight)
    }


    fun getSkipTSCJEnable(): Boolean {
        return MMKVUtil.decodeBoolean(MMKVBusiKey.PARAM_SKIP_TSJC, true)
    }

    fun updateSkipTSCJEnable(enable: Boolean) {
        MMKVUtil.encode(MMKVBusiKey.PARAM_SKIP_TSJC, enable)
    }

    /**
     * 读取存储的IPC摄像头配置
     */
    fun readIpcCameraConfig(key: String): IpcCameraConfig {
        return MMKVUtil.decodeParcelable<IpcCameraConfig>(
            key,
            IpcCameraConfig::class.java
        ) ?: IpcCameraConfig("172.16.30.200", "admin", "zy132456")
    }

    /**
     * 更新IPC摄像头配置
     */
    fun saveIpcCameraConfig(key: String, config: IpcCameraConfig) {
        MMKVUtil.encode(key, config)
    }

    fun readPingPangBluetoothAddress(): String? {
        return MMKVUtil.decodeString(MMKVBusiKey.PINGPANG_BLUETOOTH_ADDRESS_CONFIG_KEY)
    }

    fun savePingPangBluetoothAddress(address: String) {
        MMKVUtil.encode(MMKVBusiKey.PINGPANG_BLUETOOTH_ADDRESS_CONFIG_KEY, address)
    }
}