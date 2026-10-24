package com.ty.bus.multi.core.ui.set.subscreenregionset

import android.os.Bundle
import com.adl.base.common.AdlAvReader
import com.adl.base.common.AdlVideoReader
import com.adl.base.common.VideoReaderCallback
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.lib.AdlCameraPreviewView
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ActivityMultiRegionSettingBinding
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.expand.toJson
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.grpc.GrpcCmdCode
import com.ty.tybusbase.grpc.GrpcSportCommonRegionConfig
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class SubScreenRegionSettingActivity :
    BaseActivity<ActivityMultiRegionSettingBinding, BaseViewModel>() {
    private val number by lazy { intent.getIntExtra("test_count", 0) }

    // 0:1920*1080;
    // 1:2560*1440;
    // 2:3840*2160
    private val resolutionType by lazy { intent.getIntExtra("res_type", 0) }

    /**
     * 副屏IP
     */
    private val subIp by lazy { intent.getStringExtra("sub_ip") ?: "" }

    /**
     * 存储Key
     */
    private val saveKey by lazy {
        intent.getStringExtra("save_key")
            ?: "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number"
    }

    /**
     * IPC Key
     */
    private val ipcCameraKey by lazy {
        intent.getStringExtra("ipc_key")
            ?: MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY
    }

    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

    //是否正在播报语音
    private var isSpeeching = false

    //    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/FM0640097_01_20260325145319339.mp4"
    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_sit_up-2k.mp4"

    /***************************视频播放调试*************************/

    private val streamType = 1  //1:IPC ,2:Video

    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {

        openCamera()
        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            saveConfig()
        }
        mBinding.btnReset.setOnClickListener {
            reset()
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
            saveKey,
            RoleConfigMulti::class.java
        )
        uiConfig.number = number
        uiConfig.isShowNumberText = true
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.refresh()

        if (streamType == 2) {
            initVideoView()
            initVideo()
        }
    }

    private fun saveConfig() {
        val uiConfig = AdlUIConfig.instance().readConfig(
            saveKey,
            RoleConfigMulti::class.java
        )
        val payload = uiConfig?.targetRegion ?: ""

        GrpcManager.sendToClientByIp(
            subIp, GrpcCmdCode.CMD_SPORT_COMMON_REGION_CONFIG,
            GrpcSportCommonRegionConfig(saveKey, payload).toJson(), true,
            {
                mBinding.overlay.saveConfig()
                ToastUtil.getInstance(
                    this,
                    getString(R.string.save_successful)
                ).show()
            },
            { _, _ ->
                ToastUtil.getInstance(
                    this,
                    getString(R.string.save_failed)
                ).show()
            })
    }

    private fun reset() {
        GrpcManager.sendToClientByIp(
            subIp, GrpcCmdCode.CMD_SPORT_COMMON_REGION_CONFIG,
            GrpcSportCommonRegionConfig(saveKey, "").toJson(), true,
            {
                runOnUiThread {
                    ToastUtil.getInstance(
                        this,
                        getString(R.string.reset_successful)
                    ).show()
                    val multi = RoleConfigMulti()
                    multi.key = saveKey
                    multi.number = number
                    multi.isShowNumberText = true
                    mBinding.overlay.setConfig(multi, OnRenderScene.StatusSportSet)
                    mBinding.overlay.saveConfig()
                    mBinding.overlay.refresh()
                }
            },
            { _, _ ->
                ToastUtil.getInstance(
                    this,
                    getString(R.string.save_failed)
                ).show()
            })

    }

    private fun initVideoView() {
        mBinding.videoLayout.visible()
        mBinding.llBtn.visible()
        mBinding.bntStart.setOnClickListener {
            videoReader?.onStart()
        }
        mBinding.btnPause.setOnClickListener {
            videoReader?.onPause()
        }
        mBinding.bntNext.setOnClickListener {
            videoReader?.nextFrame()
        }

        mBinding.bntChange.setOnClickListener {
            playMode = when (playMode) {
                AdlAvReader.PlayMode.Auto -> AdlAvReader.PlayMode.OneFrame
                AdlAvReader.PlayMode.OneFrame -> AdlAvReader.PlayMode.Auto
            }
            videoReader?.setPlayMode(playMode)
            videoReader?.onStart()
        }

        mBinding.bntReset.setOnClickListener {
            videoReader?.onReset()
        }
    }

    fun initVideo() {
        videoReader = AdlVideoReader()
        videoReader?.setPlayMode(playMode)
        videoReader?.setSurface(mBinding.surfaceView.holder.surface)
        videoReader?.enableRender(enableRender)
        videoReader?.onReset(videoFilePath)
        videoReader?.setCallback(object : VideoReaderCallback {
            override fun onError(e: Exception?) {
            }

            override fun onFrame(yuv: ByteArray?, width: Int, height: Int, time: Long) {
                frameTime += 1000 / 24
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
            }

            override fun onComplete() {

            }
        })
    }

    override fun onPause() {
        super.onPause()
        mBinding.previewView.stopPreview()
        mBinding.previewView.release()
    }

    private fun openCamera() {
        if (streamType != 1) return
        val cameraConfig =
            BusiBaseConfig.readIpcCameraConfig(ipcCameraKey)
        val pairResolution = when (resolutionType) {
            0 -> Pair(1920, 1080)
            1 -> Pair(2560, 1440)
            2 -> Pair(3840, 2160)
            else -> Pair(1920, 1080)
        }
        val camera = AdlCameraConfig.builder()
            .setPreviewSize(pairResolution.first, pairResolution.second)
            .setCameraType(AdlCameraConfig.CameraType.HK)
            .setConnect(
                cameraConfig.ip,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PORT,
                cameraConfig.account,
                cameraConfig.psw
            )
            .setStreamType(0)
            .build()
        mBinding.previewView.startPreview(camera, object :
            AdlCameraPreviewView.OnViewStatusListener {
            override fun onAvailable() {}
            override fun onCameraOpen() {
            }

            override fun onCameraReOpen() {
            }

            override fun onDestroyed() {}
            override fun onStreamLoss() {
            }

            override fun onOpenFail() {
            }
        })
        mBinding.previewView.textureView.scaleX = -1f
    }

}