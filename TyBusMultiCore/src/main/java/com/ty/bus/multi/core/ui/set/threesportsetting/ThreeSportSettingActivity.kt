package com.ty.bus.multi.core.ui.set.threesportsetting

import android.os.Bundle
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.ty.bus.multi.databinding.BmActivityThreeSportSettingBinding
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class ThreeSportSettingActivity:BaseActivity<BmActivityThreeSportSettingBinding,BaseViewModel>() {
    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.overlay.enableDebug(true)
        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            mBinding.overlay.saveConfig()
            ToastUtil.getInstance(this,getString(R.string.save_successful)).show()
        }
        mBinding.btnReset.setOnClickListener {
            ToastUtil.getInstance(this,getString(R.string.reset_successful)).show()
            val multi = RoleConfigMulti()
            multi.key = BusiBaseConstantsValue.THREE_SPORT_CONFIG_KEY
            multi.number = 3
            multi.targetRegion = BusiBaseConstantsValue.THREE_SPORT_CONFIG_DEFAULT_REGION
            mBinding.overlay.setConfig(multi, OnRenderScene.StatusSportSet)
            mBinding.overlay.saveConfig()
            mBinding.overlay.refreshNow()
        }
        initCamera()
        initOverlay()
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
    }

    private fun initOverlay() {
        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            BusiBaseConstantsValue.THREE_SPORT_CONFIG_KEY,
            RoleConfigMulti::class.java
        )
        uiConfig.number = 3
        if (uiConfig.targetRegion == null){
            uiConfig.targetRegion = BusiBaseConstantsValue.THREE_SPORT_CONFIG_DEFAULT_REGION
        }
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
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
        mBinding.overlay.release()
    }
}