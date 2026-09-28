package com.ty.tybusbase.grpc

data class GrpcDeviceAuthInfo(
    val registerInfo: String,   //注册信息
    val registerCode: String,   //注册码
    val sn: String,             //设置SN
)