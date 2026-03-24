package com.ai.ui.glframe

import android.os.Bundle
import com.ty.bus.multi.demo.databinding.ActivityOpenGlFrameBinding
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.utils.LogUtil

class OpenGlFrameActivity: BaseActivity<ActivityOpenGlFrameBinding, BaseViewModel>() {
    override fun initObserver() {
    }

    override fun initData() {
    }

    override fun initView(savedInstanceState: Bundle?) {
        initGlView()
    }

    private fun initGlView() {
        mBinding.glView.post {
            mBinding.glView.setDataSource(
                assetPrefix = "nezha/nezha_",   // 图片命名规则 frame_0.png
                assetSuffix = "webp",
                totalFrames = 330,
                width = mBinding.glView.width,
                height = mBinding.glView.height,
                hasAlpha = true,                // 无透明通道
                fps = 30
            )
            mBinding.glView.start()
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.glView.start()
    }

    override fun onPause() {
        super.onPause()
        mBinding.glView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.glView.release()
    }
}