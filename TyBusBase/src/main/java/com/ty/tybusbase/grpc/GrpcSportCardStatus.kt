package com.ty.tybusbase.grpc

data class GrpcSportCardStatus(
    val index: Int = 0,  //卡片索引
    /**
     * 值对应 MultiSportCardViewState中枚举的原始值
     */
    val status: Int = 0, //卡片状态

)