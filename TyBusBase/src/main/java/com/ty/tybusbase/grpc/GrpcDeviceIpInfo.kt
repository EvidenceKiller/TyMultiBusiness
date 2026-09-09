package com.ty.tybusbase.grpc

/**
 * type: 0: LED
 */
data class GrpcDeviceIpInfo(
    val type: Int,  //设备类型
    val ip: String, //IP地址
)