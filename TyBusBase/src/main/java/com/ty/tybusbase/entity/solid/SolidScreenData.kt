package com.ty.tybusbase.entity.solid

import com.google.gson.Gson
import com.ty.tybusbase.grpc.GrpcAccountInfo
import com.ty.tybusbase.utils.LogUtil
import java.io.Serializable

/**
 * 副屏目标设备枚举
 */
enum class SubScreenTarget {
    LEFT,   // 左副屏
    RIGHT,  // 右副屏
    BOTH    // 左右副屏
}

/**
 * 副屏业务状态/阶段枚举
 */
enum class SubScreenState(val code: Int, val description: String) {
    PREPARE(1, "准备阶段（清空数据与状态，左屏提示暂停使用，右屏待发起）"),
    MODEL_READY(2, "模型初始化成功（右屏待识别/可举手/刷脸，左屏提示暂停使用）"),
    IDENTIFIED(3, "人脸识别成功（右屏显示学员信息并提示请开始投掷，左屏提示暂停使用）"),
    MEASURING(4, "正在测距（右屏显示正在测距，左屏提示暂停使用）"),
    RESULT(5, "成绩显示（左右屏显示本次成绩/违规，左屏倒计时5秒后切至最近成绩）"),
    RECENT_SCORES(6, "左屏显示最近成绩（最多显示6条，按时间排列）")
}

/**
 * 单条成绩记录数据类（用于即时成绩展示及最近成绩列表）
 */
data class SubScreenScoreItem(
    var attempt: Int = 1,                           // 第几次测试
    var accountName: String? = null,                // 学员姓名
    var accountId: String? = null,                  // 学员ID
    var avatarUrl: String? = null,                  // 学员头像地址
    var score: Float? = null,                       // 成绩数值（如实心球距离米数）
    var scoreText: String = "",                     // 格式化成绩文案（如 "12.5米" 或 "违规"）
    var isViolation: Boolean = false,               // 是否违规
    var violationReason: String? = null,            // 违规原因（如 "踩线"、"未过肩" 等）
    var timestamp: Long = System.currentTimeMillis(), // 记录产生时间戳
    var sportSkuId: String? = null,                 // 运动SkuId
    var sportSkuName: String? = null                // 运动名称（如 "实心球"）
) : Serializable

/**
 * 发送给副屏的通用数据包结构（GSON 格式）
 * 包含副屏协同所需的所有状态与详细数据，供副屏正确解析与渲染
 */
data class SubScreenData(
    var state: Int ,          // 当前业务状态码
    var stateName: String ,   // 状态名称
    var targetScreen: String ,  // 目标副屏：LEFT / RIGHT / BOTH
    var sportSkuId: String,
    var sportSkuName: String,
    var promptText: String? = null,                        // 提示文案（如 "本区暂停使用"、"请在投掷区进行人脸识别"、"正在测距..." 等）
    var accountInfo: GrpcAccountInfo? = null,              // 学员基础信息
    var accountName: String? = null,                       // 学员姓名
    var accountId: String? = null,                         // 学员ID
    var avatarUrl: String? = null,                         // 学员头像URL
    var isViolation: Boolean = false,                      // 是否违规
    var violationReason: String? = null,                   // 违规原因
    var score: Float? = null,                              // 成绩米数
    var scoreText: String? = null,                         // 成绩展示文字（如 "12.5米" / "违规"）
    var attempt: Int = 1,                                  // 第几次投掷
    var totalScore: Int = 0,                               // 整数总分或换算分
    var sportTime: Long = System.currentTimeMillis(),       // 成绩时间戳
    var countdownSeconds: Int = 0,                         // 倒计时秒数（左屏即时成绩倒计时5秒）
    var recentScores: List<SubScreenScoreItem>? = null,    // 最近成绩列表（最多6条）
    var extra: MutableMap<String, Any?>? = null            // 扩展字段
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
