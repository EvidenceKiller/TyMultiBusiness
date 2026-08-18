package com.ty.tybusbase.grpc

/**
 * 运动状态（CMD_SPORT_STATUS = 0x20 的 payload）
 */
data class GrpcSportStatus(
    var status: Int = 0, //运动状态
)