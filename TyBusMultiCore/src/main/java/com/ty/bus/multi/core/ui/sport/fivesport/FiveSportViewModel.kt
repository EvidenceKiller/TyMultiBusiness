package com.ty.bus.multi.core.ui.sport.fivesport

import android.app.Application
import com.ty.bus.multi.core.base.BaseIPCMultiSportViewModel
import com.ty.bus.multi.core.config.FiveSportConfig
import com.ty.bus.multi.core.config.TenSportConfig

class FiveSportViewModel(application: Application):BaseIPCMultiSportViewModel(application) {
    var fiveSportConfig: FiveSportConfig? = null

    override fun isSupportReport(): Boolean {
        return fiveSportConfig?.isSupportReport != false
    }
}