package com.ty.tybusbase.utils

import android.content.Context
import com.adl.service.entity.SportRecordEntity
import com.tencent.mmkv.MMKV
import com.ty.tybusbase.callback.DefaultUploadCallback

object BusiCoreEngine {
    private var resultCallback: ResultCallback? = null
    fun init(context: Context) {
        ApplicationUtil.init(context)
        MMKV.initialize(context)
        SoundPlayUtils.init(context)
    }

    fun setResultCallback(resultCallback: ResultCallback) {
        this.resultCallback = resultCallback
    }

    fun getResultCallback(): ResultCallback? {
        return resultCallback
    }
}

interface ResultCallback {
    fun onSportRecordEntity(sportRecordEntity: SportRecordEntity)
}
