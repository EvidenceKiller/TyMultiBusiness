package com.ty.bus.multi.core.ui.set.multisportsregionset

import java.io.Serializable

/**
 * 多人多运动框体设置页的下拉项，由外界通过 Intent 传入。
 *
 * @param sportName 运动名称（下拉框展示）
 * @param sportType 运动类型（业务标识，页面透传保留）
 * @param personCount 运动人数（框体道数）
 * @param regionKey 框体配置的存储 Key（Overlay 配置按此 Key 读写）
 * @param resolutionType 该运动使用的分辨率：0-1920*1080，1-2560*1440，2-3840*2160；
 *                       传 -1 表示使用页面级传参 res_type
 * @param cameraKey 该运动使用的摄像头配置 Key；传 null 表示使用页面级传参 ipc_key（默认主屏）
 */
data class MultiSportRegionOption(
    val sportName: String = "",
    val sportType: Int = 1,
    val personCount: Int = 1,
    val regionKey: String = "",
    val resolutionType: Int = -1,
    val cameraKey: String? = null
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
