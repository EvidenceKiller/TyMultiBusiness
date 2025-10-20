package com.ty.bus.multi.core.ui.set.multiregionset

import android.os.Bundle
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraPreviewView
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ActivityMultiRegionSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class MultiRegionSettingActivity:BaseActivity<ActivityMultiRegionSettingBinding, BaseViewModel>() {
    private val number by lazy { intent.getIntExtra("test_count", 0) }
    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {

        openCamera()
        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            mBinding.overlay.saveConfig()
            ToastUtil.getInstance(
                this,
                getString(R.string.save_successful)
            ).show()
        }
        mBinding.btnReset.setOnClickListener {
            ToastUtil.getInstance(
                this,
                getString(R.string.reset_successful)
            ).show()
            val multi = RoleConfigMulti()
            multi.key = "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number"
            multi.number = number
            mBinding.overlay.setConfig(multi, OnRenderScene.StatusSportSet)
            mBinding.overlay.saveConfig()
            mBinding.overlay.refresh()
        }

        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(true)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)

        val uiConfig = AdlUIConfig.instance().readConfig(
            "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number",
            RoleConfigMulti::class.java
        )
        uiConfig.number = number
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
    }


    override fun onPause() {
        super.onPause()
        mBinding.previewView.stopPreview()
        mBinding.previewView.release()
    }

    private fun openCamera() {
        val ip =
            MMKVUtil.decodeString(MMKVBusiKey.HTTP_CAMERA_IP, BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP)
        val account = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
        )
        val psw = MMKVUtil.decodeString(
            MMKVBusiKey.HTTP_CAMERA_PSW,
            BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
        )
        val camera = AdlCameraConfig.builder()
            .setPreviewSize(1920,1080)
            .setCameraType(AdlCameraConfig.CameraType.HK)
            .setConnect(ip, BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT, account, psw)
            .setStreamType(0)
            .build()
        mBinding.previewView.startPreview(camera, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {}
            override fun onCameraOpen() {
            }

            override fun onDestroyed() {}
            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
        mBinding.previewView.getTextureView().setScaleX(-1f)
    }
    
}