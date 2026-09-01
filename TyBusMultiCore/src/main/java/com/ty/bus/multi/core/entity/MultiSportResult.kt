package com.ty.bus.multi.core.entity

import com.adl.service.entity.MeetGroupDetailsEntity
import com.ty.tybusbase.entity.AccountInfo


open class MultiSportResult(
    var accountInfo: AccountInfo,
    var number: Int,
    var count: Int = 0,
    var rank: Int = 0,
    var meetGroup: MeetGroupDetailsEntity? = null,
    var hasVoice: Boolean = false,
    var status:Int = 0,  //成绩状态，0: 正常，1: 违规,2: 未完成
)