package com.ty.bus.multi.core.state

enum class ForwardFlexionCardState {
    //空闲
    IDLE,

    //识别中
    IDENTIFYING,

    //已识别
    IDENTIFIED,

    //已准备
    PREPARED,

    //第一次运动中
    FIRST_SPORTING,

    //运动中
    SPORTING,

    //运动中，无用户，该卡片未识别到用户
    SPORTING_NO_USER,

    //出成绩但还在运动过程中，倒计时未结束（还有其他人未出成绩）
    RESULT_IN_SPORTING,

    //当次运动结束
    CURRENT_SPORT_END,

    //运动结束
    FINISHED

}