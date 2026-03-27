package com.ty.tybusbase.callback

import com.adl.service.entity.SportRecordEntity
import com.ty.tybusbase.helper.RecordUploadHelper
import com.ty.tybusbase.utils.ResultCallback

class DefaultUploadCallback : ResultCallback {
    override fun onSportRecordEntity(sportRecordEntity: SportRecordEntity) {
        RecordUploadHelper.uploadRecord(sportRecordEntity)
    }
}