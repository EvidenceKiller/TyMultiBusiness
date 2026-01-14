package com.ty.bus.multi.core.entity
import com.adl.service.entity.StudentEntity


open class BluetoothSportResultInfo(
    var studentEntity: StudentEntity,
    var number: Int,
    var score: String = "",
)