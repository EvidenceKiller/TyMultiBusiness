package com.ai.test

import com.adl.base.AdlApp
import com.adl.service.entity.SportRecordEntity
import com.adl.service.outer.AdlService
import com.tencent.mmkv.MMKV
import com.ty.tybusbase.callback.DefaultUploadCallback
import com.ty.tybusbase.helper.RecordUploadHelper
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.BusiCoreEngine
import com.ty.tybusbase.utils.ResultCallback

class DemoApp : AdlApp() {
    override fun onCreate() {
        super.onCreate()
        loadXfSpeech()
        //ZySportContext.init(this);
        AdlService.create(this)
        SoundUtil.getInstance().init(this)
        ApplicationUtil.init(this)
        MMKV.initialize(this)

        BusiCoreEngine.init(this)
//        BusiCoreEngine.setResultCallback(DefaultUploadCallback())
    }
}
