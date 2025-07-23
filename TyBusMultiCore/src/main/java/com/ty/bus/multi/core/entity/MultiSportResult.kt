package com.ty.bus.multi.core.entity

import com.ty.tybusbase.entity.AccountInfo


open class MultiSportResult(
    var accountInfo: AccountInfo,
    var number: Int,
    var count: Int = 0,
    var rank: Int = 0
)