package com.ty.tybusbase.grpc

/**
 * 运动结果（CMD_SPORT_RESULT = 0x22 的 payload）
 */
data class GrpcSportResult(
    val index: Int = 1,   //道次
    val sportResult: String = "",    // 运动成绩
    var status:Int = 0,  //成绩状态，0: 正常，1: 违规,2: 未完成
    val totalScore: Int = 0,         // 总分
    val sportTime: Long = 0,          // 运动时长
    var accountName: String = "",
    var accountId: String = "",
    var sportSkuId: String = "",
    var sportSkuName: String = "",
    val recordId: String? = null  //运动记录ID，可查询在线报告
)