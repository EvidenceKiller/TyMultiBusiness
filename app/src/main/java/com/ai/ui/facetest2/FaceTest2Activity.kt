package com.ai.ui.facetest2

import android.os.Bundle
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.demo.databinding.ActivityFaceTest2Binding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil

class FaceTest2Activity : BaseActivity<ActivityFaceTest2Binding, BaseSportViewModel>() {

    private val sportFaceProcessor = SportFaceProcessor()
    override fun initObserver() {

    }

    override fun initData() {
        sportFaceProcessor.open()
    }

    override fun initView(savedInstanceState: Bundle?) {
        sportFaceProcessor.setDisplaySize(
            1920,
            1080,
            960,
            540
        )
            .centerAlign(1920 / 2, 1080 / 2)
        initCamera()
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBinding.cameraView.addProcessor(sportFaceProcessor)
    }

    override fun onResume() {
        super.onResume()
        mBinding.cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.cameraView.onRelease()
        sportFaceProcessor.close()
//        SpeechWrapper.stop()
    }
}