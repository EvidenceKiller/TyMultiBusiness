package com.ty.tybusbase.grpc

object GrpcCmdCode {
    //运动状态 payload = GrpcSportStatus
    const val CMD_SPORT_STATUS = 0x20

    //用户信息 payload = GrpcAccountInfo
    const val CMD_ACCOUNT_INFO = 0x21

    //运动结果 payload = GrpcSportResult
    const val CMD_SPORT_RESULT = 0x22

    //协同运动 payload = GrpcCollaborationSportInfo
    const val CMD_COLLABORATION_SPORT = 0x23

    //协同状态回复 payload = GrpcCollaborationStatus
    const val CMD_COLLABORATION_STATUS = 0x24

    //设置设备IP地址 payload = GrpcDeviceIpInfo
    const val CMD_DEVICE_IP_SETTING = 0x25

    //设置设备IP地址 payload = GrpcIpcCameraInfo
    const val CMD_IPC_CAMERA_SETTING = 0x26


    //运动卡片状态 payload = GrpcSportCardStatus
    const val CMD_SPORT_CARD_STATUS = 0x50

    //已识别多少人 payload = GrpcSportIdentifiedCount
    const val CMD_SPORT_IDENTIFIED_COUNT = 0x51

    //清除已识别的信息 payload = ""
    const val CMD_CLEAR_IDENTIFY_INFO = 0x52

    //倒计时结束
    const val CMD_COUNTDOWN_END = 0x53

    //十人框体设置 payload = String
    const val CMD_TEN_SPORT_REGION_SETTING = 0x54




    //实心球运动卡片状态 payload =
    const val CMD_SOLID_BALL_SPORT_CARD_STATUS = 0x70
}