package com.ty.tybusbase.grpc

/**
 * 协同运动信息Grpc消息
 * @param cmd 协同指令int值，对应 [CollaborationCmdEnum] 的 ordinal
 * @param sportSkuId 协同运动ID
 */
data class GrpcCollaborationSportInfo(
    val cmd: Int = 0,          //协同指令
    val sportSkuId: String,    //协同运动ID
    val testCount: Int = 1     //协同运动人数
)

/**
 * 协同指令
 * 发送端通过Grpc传递int值，接收端用 [fromCode] 转换后判断
 */
enum class CollaborationCmdEnum(val code: Int) {
    /** 强制加入协同 */
    FORCE_JOIN(0),
    /** 发起协同 */
    START(1),
    /** 准备加入协同 */
    PREPARE(2),
    /** 取消准备加入协同 */
    CANCEL_PREPARE(3),
    /** 开启协作 */
    SPORT(4),
    /** 结束协同 */
    FINISH(5),
    /** 查询命令 */
    QUERY(6);

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
}