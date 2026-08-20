package com.ty.tybusbase.grpc

/**
 * 协同运动信息Grpc消息
 * @param cmd 协同指令int值，对应 [CollaborationCmdEnum] 的 ordinal
 * @param sportSkuId 协同运动ID
 */
data class GrpcCollaborationSportInfo(
    val cmd: Int = 0,          //协同指令
    val sportSkuId: String,    //协同运动ID
)

/**
 * 协同指令
 * 发送端通过Grpc传递int值，接收端用 [fromCode] 转换后判断
 */
enum class CollaborationCmdEnum {
    /** 开始 */
    START,

    /** 准备 */
    PREPARE,

    /** 开始运动  */
    SPORT,

    /** 结束 */
    FINISH,

    ;

    companion object {
        /**
         * 根据Grpc传递的指令int值转换为协同指令，越界返回null
         */
        fun fromCode(code: Int): CollaborationCmdEnum? = values().getOrNull(code)
    }
}
