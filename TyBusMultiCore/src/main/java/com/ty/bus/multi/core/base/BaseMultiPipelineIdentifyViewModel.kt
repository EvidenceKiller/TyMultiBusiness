package com.ty.bus.multi.core.base

import android.app.Application
import com.ty.tybusbase.identify.MultiIdentityEngine

class BaseMultiPipelineIdentifyViewModel(application: Application) :
    BaseMultiSportViewModel(application) {
    protected val multiIdentityEngine by lazy { MultiIdentityEngine() } //人脸识别引擎类

    override fun onCreate() {
        super.onCreate()

    }


    /**
     * 初始化流水线人脸识别
     */
    private fun initMultiIdentify() {

    }
}