package com.ai.ui.facetest3

import android.graphics.Point
import android.os.Bundle
import com.adl.mount.sport.TsSportRegion
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.demo.databinding.ActivityFaceTest3Binding
import com.ty.tybusbase.base.BaseSportStatusActivity
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.identify.IdentifyMachineListener
import com.ty.tybusbase.identify.SingleIdentityProcessor
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil

class FaceTest3Activity : BaseSportStatusActivity<ActivityFaceTest3Binding, BaseSportViewModel>() {

    private val identityProcessor by lazy { SingleIdentityProcessor() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initCamera()
        initOverlay()
        initIdentityProcessor()
    }

    private fun initCamera() {
        mBinding.cameraView.setRotate(MMKVUtil.decodeInt(MMKVBusiKey.SKELETON_ROTATE, 0))
        mBinding.cameraView.setMirror(
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.SKELETON_MIRROR,
                BusiBaseConstantsValue.DEFAULT_SKELETON_MIRROR
            )
        )
        mBinding.cameraView.addProcessor(identityProcessor)
    }

    private fun initOverlay() {
        val drawSkeleton = BusiBaseConfig.getSkeletonEnable()
        val isDebug: Boolean = BusiBaseConfig.getDebugEnable()
        mBinding.overlay.enableDrawSkeleton(drawSkeleton)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(isDebug)
    }

    private fun initIdentityProcessor() {

        identityProcessor.initSportEngine(
            mBinding.overlay,
            TsSportRegion(Point(960, 0), Point(1920, 0), Point(960, 1080), Point(1920, 1080))
        )
        identityProcessor.setIdentityListener(object : IdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityTimeOn() {
            }

            override fun onIdentityOk(accountInfo: AccountInfo?) {
                LogUtil.d("识别成功：${accountInfo?.studentEntity?.accountName}")
            }

            override fun onIdentityTimeFail() {
                LogUtil.d("识别失败:")
            }

        })
        mBinding.overlay.setListener {
            identityProcessor.start()
        }
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
    }
}