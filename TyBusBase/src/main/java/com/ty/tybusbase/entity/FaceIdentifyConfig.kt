package com.ty.tybusbase.entity

data class FaceIdentifyConfig(
    //游客者重试次数
    var visitorRetryNum: Int = 2,

    //人脸最小置信分数
    var minScoreThreshold: Float = 0.5f

)