package com.ty.tybusbase.grpc

/**
 * 用户信息（CMD_ACCOUNT_INFO = 0x21 的 payload）
 */
data class GrpcAccountInfo(
    val index: Int = 1,       // 道次
    val name: String = "",    // 用户名
    val accountId: String = "", //用户ID
    val avatar: String = ""   // 头像 URL
)