package com.ai.ui.doubleface

import com.adl.mount.impl.TsDoubleProcessor
import com.adl.mount.impl.TsSportFrameDouble
import com.ty.bus.multi.demo.databinding.ActivityDoubleFaceTestBinding
import com.ty.tybusbase.base.BaseDoubleSportIdentifyActivity
import com.ty.tybusbase.base.BaseDoubleSportViewModel

class DoubleFaceTestActivity: BaseDoubleSportIdentifyActivity<ActivityDoubleFaceTestBinding, BaseDoubleSportViewModel>() {
    override fun refreshTime(time: Long) {

    }

    override fun warningTime() {
    }

    override fun createTsDoubleProcessor(): TsDoubleProcessor {
        return object : TsDoubleProcessor {
            override fun onInit() {

            }

            override fun onReset() {

            }

            override fun onRelease() {

            }

            override fun version(): String {
                return "V2.0.3.0"
            }

            override fun onAnalyzer(frame: TsSportFrameDouble?) {
                if (frame != null) {

                }
            }

        }
    }
}