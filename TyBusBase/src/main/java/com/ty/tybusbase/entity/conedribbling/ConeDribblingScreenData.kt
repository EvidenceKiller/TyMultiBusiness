package com.ty.tybusbase.entity.conedribbling

import com.google.gson.Gson
import com.ty.tybusbase.utils.LogUtil
import java.io.Serializable

/**
 * 副屏目标设备枚举
 */
enum class SubScreenType(val code: Int) {
    SPORT(0),   // 运动副屏
    DISPLAY(1),  // 展示副屏
}

/**
 * 副屏业务状态/阶段枚举
 */
enum class SubScreenState(val code: Int) {
    PREPARE(0),
    INIT(1),
    IDENTIFY(2),
    IDENTIFIED(3),
    COUNTDOWN(4),
    SPORTING(5),
    SETTLEMENT(6),
    RESULT(7),
}

/**
 * 单条成绩记录数据类（用于即时成绩展示及最近成绩列表）
 */
data class SubScreenResultItem(
    var accountInfo: String? = null,
    var isViolation: Boolean = false,
    var violation: String? = null,
    var result: String? = null,
) : Serializable

/**
 * 发送给副屏的通用数据包结构（GSON 格式）
 * 包含副屏协同所需的所有状态与详细数据，供副屏正确解析与渲染
 */
data class SubScreenData(
    var subScreenType: Int,
    var subScreenState: Int,
    var sportSkuId: String,
    var sportSkuName: String,
    var accountInfo: String? = null,
    var promptText: String? = null,
    var sportResult: SubScreenResultItem? = null,
    var recentResult: List<SubScreenResultItem>? = null,
) : Serializable {
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): SubScreenData? {
            return try {
                Gson().fromJson(json, SubScreenData::class.java)
            } catch (e: Exception) {
                LogUtil.e("parse SubScreenData json error: ${e.message}")
                null
            }
        }
    }
}
