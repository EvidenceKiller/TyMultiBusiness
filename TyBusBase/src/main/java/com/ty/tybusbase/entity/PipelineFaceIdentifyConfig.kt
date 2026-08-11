package com.ty.tybusbase.entity

data class PipelineFaceIdentifyConfig(
    //识别多少轮算游客，一轮帧数为校验时的最大帧数
    var visitorRoundCount: Int = 2,

    //人脸最小置信分数
    var minScoreThreshold: Float = 0.5f,

    //一轮多少帧
    var verifyRoundCount: Int = 6

)