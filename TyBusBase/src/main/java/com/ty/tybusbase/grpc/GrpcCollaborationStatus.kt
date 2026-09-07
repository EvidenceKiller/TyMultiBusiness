package com.ty.tybusbase.grpc

/**
 * 协同状态Grpc消息
 * @param status 状态int值，对应 [CollaborationStatusEnum] 的 ordinal
 */
data class GrpcCollaborationStatus(
    val status: Int,
    val skuId: String? = "",  //运动SKU ID
)

/**
 * 协同状态
 * 发送端通过Grpc传递int值，接收端用 [fromCode] 转换后判断
 */
enum class CollaborationStatusEnum {
    /** 已加入协同状态 */
    JOINED,

    /** 开始运动 */
    SPORTING,

    /** 可加入（未加入，空闲状态） */
    JOINABLE,

    /** 准备状态（未加入，准备状态） */
    PREPARED,
    ;

    /** 通过 gRPC 传输的状态 int 值（等价于 ordinal） */
    val code: Int get() = ordinal

    companion object {
        /**
         * 根据Grpc传递的状态int值转换为协同状态，越界返回null
         */
        fun fromCode(code: Int): CollaborationStatusEnum? = values().getOrNull(code)
    }
}
