package com.ty.bus.multi.core.base

enum class MultiSportState {
    /**
     * 空闲
     */
    IDLE,

    /**
     * 识别中
     */
    IDENTIFYING,

    /**
     * 准备中,可进行人脸识别
     */
    PREPARING,

    /**
     * 开始倒计时中
     */
    COUNTDOWN,

    /**
     * 运动中
     */
    SPORTING

}