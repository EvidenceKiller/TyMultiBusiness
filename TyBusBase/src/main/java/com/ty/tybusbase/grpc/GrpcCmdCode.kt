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

    //运动卡片状态 payload = GrpcSportCardStatus
    const val CMD_SPORT_CARD_STATUS = 0x30

    //已识别多少人 payload = GrpcSportIdentifiedCount
    const val CMD_SPORT_IDENTIFIED_COUNT = 0x31

    //清除已识别的信息 payload = ""
    const val CMD_CLEAR_IDENTIFY_INFO = 0x32
}