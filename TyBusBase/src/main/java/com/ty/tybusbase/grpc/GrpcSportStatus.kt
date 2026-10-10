package com.ty.tybusbase.grpc

/**
 * 运动状态（CMD_SPORT_STATUS = 0x20 的 payload）
 */
data class GrpcSportStatus(
    var status: Int = 0, //运动状态
    val skuId: String? = ""
)

/**
 * 运动状态
 * 发送端通过Grpc传递int值，接收端用 [fromCode] 转换后判断
 */
enum class GrpcSportStatusEnum {
    /** 开始运动 */
    SPORTING,

    /** 运动结束 */
    FINISHED,

    /** 准备状态（未加入，准备状态） */
    PREPARED,
    ;

    /** 通过 gRPC 传输的状态 int 值（等价于 ordinal） */
    val code: Int get() = ordinal

    companion object {
        /**
         * 根据Grpc传递的状态int值转换为协同状态，越界返回null
         */
        fun fromCode(code: Int): GrpcSportStatusEnum? = values().getOrNull(code)
    }
}