package com.ty.tybusbase.entity

class PipelineIdentifyInfo {
    //是否已识别OK
    var isOk = false

    //每帧识别成功的信息，userId和置信分数
    var identifiedList = mutableListOf<Pair<String, Float>>()

    //已识别的累积帧
    var frameCount = 0
}