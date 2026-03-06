package com.ty.tybusbase.entity

//[{"time":2.9（秒）,    "invalidFlag": 0/1(1：无效状态 0：有效) }]  //时间数组 秒
data class IndicatorSportStartTimeInvalid(
    var startTime: Float = 0f,
    var endTime: Float = 0f,
    var invalidFlag: Int = 0
)