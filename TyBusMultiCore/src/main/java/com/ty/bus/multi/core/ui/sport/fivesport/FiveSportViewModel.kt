package com.ty.bus.multi.core.ui.sport.fivesport

import android.app.Application
import com.ty.bus.multi.core.base.BaseIPCMultiSportViewModel
import com.ty.bus.multi.core.config.FiveSportConfig
import com.ty.bus.multi.core.config.SportModel

class FiveSportViewModel(application: Application) : BaseIPCMultiSportViewModel(application) {
    var fiveSportConfig: FiveSportConfig? = null

    override fun isSupportReport(): Boolean {
        return fiveSportConfig?.isSupportReport != false
    }

    //如果是常规模式，则需要切换分辨率，运动会模式，不需要切分辨率
    override fun getIdentifyResolution(): Byte =
        if (getSportModel() == SportModel.NORMAL) super.getIdentifyResolution() else getSportResolution()

    override fun getSportModel(): SportModel {
        return fiveSportConfig?.sportModel ?: SportModel.NORMAL
    }
}