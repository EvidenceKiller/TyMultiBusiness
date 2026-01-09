package com.ty.bus.multi.core.state

enum class BluetoothSportCardState {
    //空闲
    IDLE,

    //蓝牙连接上
    CONNECTED,

    //已准备
    PREPARED,

    //运动中
    SPORTING,

    //运动中，无用户，该卡片未识别到用户
    SPORTING_NO_USER,

}