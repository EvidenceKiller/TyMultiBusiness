package com.aipower.modulepk.ui.fivesport

enum class MultiSportCardViewState {
    /**
     * 无人进入
     */
    NOBODY,
    /**
     * 检测到人，等待识别
     */
    STANDBY,

    /**
     * 识别中
     */
    IDENTIFYING,

    /**
     * 已识别
     */
    IDENTIFIED,

    /**
     * 准备完成，已识别
     */
    PREPARED,

    /**
     * 运动中
     */
    SPORTING,

    /**
     *
     */
    LEAVE_TO_SPORTING,

    /**
     * 无人，但运动已开始
     */
    NOBODY_SPORT,

    /**
     * 离开状态
     */
    LEAVE,

    /**
     * 干扰模式；多人站在同一筐内
     */
    INTERFERENCE,

    /**
     * 识别失败
     */
    IDENTIFY_FAIL,

    /**
     * 识别切换，同一人识别切换到另一道次
     */
    IDENTIFY_CHANGE,

    /**
     * 预制人员
     */
    PRECAST_PERSON,

    /**
     * 预制人员已准备好
     */
    PRECAST_PREPARE,

}