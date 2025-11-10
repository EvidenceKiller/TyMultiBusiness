package com.ai.ui.facetest

import com.ty.bus.multi.demo.databinding.ActivityFaceTestBinding
import com.ty.tybusbase.base.BaseSingleIdentifyActivity
import com.ty.tybusbase.base.BaseSportViewModel

class FaceTestActivity: BaseSingleIdentifyActivity<ActivityFaceTestBinding, BaseSportViewModel>() {

    override fun onStatusPrepare() {
        super.onStatusPrepare()
        viewModel.toStatusIdentifyFace()
    }
}