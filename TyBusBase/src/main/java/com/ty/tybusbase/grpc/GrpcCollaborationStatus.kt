package com.ty.tybusbase.grpc

/**
 * 协同状态Grpc消息
 * @param status 状态int值，对应 [CollaborationStatusEnum] 的 ordinal
 */
data class GrpcCollaborationStatus(
    val status: Int
)

/**
 * 协同状态
 * 发送端通过Grpc传递int值，接收端用 [fromCode] 转换后判断
 */
enum class CollaborationStatusEnum {
    /** 加入 */
    JOIN,

    /** 运动中 */
    SPORTING,

    /** 加入且当前有已识别的人员 */
    JOIN_WITH_IDENTIFIED,

    /** 可加入但不是当前运动 */
    JOINABLE,

    ;

    companion object {
        /**
         * 根据Grpc传递的状态int值转换为协同状态，越界返回null
         */
        fun fromCode(code: Int): CollaborationStatusEnum? = values().getOrNull(code)
    }
}
