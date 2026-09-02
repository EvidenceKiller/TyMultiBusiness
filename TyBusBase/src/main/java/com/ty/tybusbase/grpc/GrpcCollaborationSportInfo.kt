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
enum class CollaborationCmdEnum(val code: Int) {
    /** 加入或者强制加入 */
    JOIN(0),
    /** 运动已开始 */
    START(1),
    /** 准备中 */
    PREPARE(2),
    /** 运动中 */
    SPORT(3),
    /** 运动结束 */
    FINISH(4),
    /** 暂停停用(非激活)状态 */
    DEACTIVATE(5),
    /** 解散-退出 */
    DISBAND(6),
    /** 身份认证 */
    IDENTITY_AUTHENTICATION(7),
    /** 查询命令 */
    QUERY(8);

    companion object {
        /**
         * 根据Grpc传递的指令int值转换为协同指令，越界返回null
         */
        fun fromCode(code: Int): CollaborationCmdEnum? {
            return values().firstOrNull { it.code == code }
        }
    }
}

/**
 * 副屏状态机枚举
 */
enum class SubScreenSportState {
    /** 空闲/未协同 */
    IDLE,
    /** 已加入协同运动 */
    JOINED,
    /** 运动已开始 */
    STARTED,
    /** 运动准备中 */
    PREPARE,
    /** 运动进行中 */
    SPORT,
    /** 运动结束 */
    FINISH,
    /** 暂停停用(非激活)状态 */
    DEACTIVATE,
    /** 身份识别状态 */
    IDENTITY_AUTHENTICATION
}