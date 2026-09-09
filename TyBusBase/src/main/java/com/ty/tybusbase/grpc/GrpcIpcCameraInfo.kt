package com.ty.tybusbase.grpc

import com.ty.tybusbase.entity.IpcCameraConfig

data class GrpcIpcCameraInfo(
    val type: Int,
    val cameraInfo: IpcCameraConfig,
)