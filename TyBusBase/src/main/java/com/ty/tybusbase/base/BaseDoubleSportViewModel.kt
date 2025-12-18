package com.ty.tybusbase.base

import android.app.Application
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.SportResult

open class BaseDoubleSportViewModel(application: Application):BaseSportViewModel(application) {
    val leftSportResult: SportResult by lazy { SportResult() }

    val rightSportResult: SportResult by lazy { SportResult() }
    val leftAccountInfo: BaseLiveData<AccountInfo> = BaseLiveData()
    val rightAccountInfo: BaseLiveData<AccountInfo> = BaseLiveData()
}