package com.ty.bus.multi.core.ui.set.thirtysportset

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
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
import com.ty.bus.multi.databinding.ActivityThirtySportSettingBinding
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.grpc.GrpcCmdCode
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

/**
 * 主屏 + 左右副屏 10 人运动框体设置
 *
 * 顶部下拉框可在"智慧屏多人运动 / 微操场左区 / 微操场右区"三屏之间切换，
 * 切换时自动保存当前区域配置，并重新加载对应屏的摄像头画面与框体配置。
 */
class ThirtySportSettingActivity :
    BaseActivity<ActivityThirtySportSettingBinding, BaseViewModel>() {
    /***************************视频播放调试*************************/
    var videoReader: AdlVideoReader? = null
    var playMode: AdlAvReader.PlayMode = AdlAvReader.PlayMode.Auto
    var enableRender: Boolean = true
    var frameTime: Long = 0

    //    val videoFilePath = "/sdcard/zy.sport.cache/resCollect/10_sit_up-2k.mp4"
    val videoFilePath = "/sdcard/com.adl.sport/testDebugVideo/site_up_ten/10人仰卧起坐-2k.mp4"

    /***************************视频播放调试*************************/

    private enum class AreaMode {
        MAIN, LEFT, RIGHT
    }

    private val number = 10
    private var currentMode = AreaMode.MAIN

    private val saveKey: String? by lazy {
        intent.getStringExtra("save_key")
    }

    private val commandCode: Int by lazy {
        intent.getIntExtra("commandCode", GrpcCmdCode.CMD_TEN_SPORT_REGION_SETTING)
    }

    // 0:1920*1080; 1:2560*1440; 2:3840*2160
    private val resolutionType by lazy { intent.getIntExtra("res_type", 1) }

    private var areaPopup: PopupWindow? = null
    private val areaOptions by lazy {
        resources.getStringArray(R.array.thirty_sport_area_options)
    }

    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        initAreaSpinner()

        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            saveConfig()
        }
        mBinding.btnReset.setOnClickListener {
            resetCurrentArea()
        }

        // 初始化 Overlay 绘制开关
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(true)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)

        // 默认加载主屏（智慧屏多人运动）
        openCamera(getCameraKey(currentMode))
        reloadOverlay()

//        mBinding.groupVideo.visible()
//        initVideo()
//        initVideoView()
    }

    private fun initVideoView() {
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

            override fun onFrame(yuv: ByteArray, width: Int, height: Int, time: Long) {
                frameTime += 1000 / 24
                val info = AdlCameraPreviewInfo(width, height)
                info.frameTimestamp = frameTime
                info.imageId = frameTime.toString()
//                viewModel.onFrame(yuv, info)
            }

            override fun onComplete() {

            }
        })
    }

    private fun saveConfig() {
        // 先本地保存当前屏的框体配置
        mBinding.overlay.saveConfig()

        // 主屏：直接按本地保存结果提示
        if (currentMode == AreaMode.MAIN) {
            ToastUtil.getInstance(this, getString(R.string.save_successful)).show()
            return
        }

        // 副屏：读出刚保存的配置，将 targetRegion 通过 gRPC 下发到对应副屏，发送成功/失败后再提示
        val subScreenIp = when (currentMode) {
            AreaMode.LEFT -> BusiBaseConfig.readLeftSubScreenIp()
            AreaMode.RIGHT -> BusiBaseConfig.readRightSubScreenIp()
            else -> null
        }
        val clientId = subScreenIp?.let { GrpcManager.getClientIdByIp(it) }
        if (clientId.isNullOrEmpty()) {
            ToastUtil.getInstance(this, getString(R.string.save_failed)).show()
            return
        }

        val uiConfig = AdlUIConfig.instance().readConfig(
            getSaveKey(currentMode),
            RoleConfigMulti::class.java
        )
        val payload = uiConfig?.targetRegion ?: ""
        GrpcManager.sendToClient(
            clientId,
            commandCode,
            payload,
            true,
            {
                ToastUtil.getInstance(
                    this@ThirtySportSettingActivity,
                    getString(R.string.save_successful)
                ).show()
            },
            { _, _ ->
                ToastUtil.getInstance(
                    this@ThirtySportSettingActivity,
                    getString(R.string.save_failed)
                ).show()
            }
        )
    }

    /**
     * 初始化顶部区域切换下拉框。
     * 下拉内容根据左右副屏 gRPC 连接情况动态生成：仅展示已连接的副屏；
     * 左右副屏均未连接时不显示下拉框，仅保留主屏设置。
     */
    private fun initAreaSpinner() {
        val modes = availableAreaModes()
        if (modes.size <= 1) {
            mBinding.llAreaSelector.visibility = View.GONE
            currentMode = AreaMode.MAIN
            return
        }
        mBinding.llAreaSelector.visibility = View.VISIBLE
        updateAreaText(currentMode)
        updateAreaArrow(false)
        mBinding.llAreaSelector.setOnClickListener {
            if (areaPopup?.isShowing == true) {
                areaPopup?.dismiss()
            } else {
                showAreaPopup(modes)
            }
        }
    }

    /**
     * 可切换屏列表：主屏始终存在；左右副屏仅当对应 gRPC 客户端已连接时加入
     */
    private fun availableAreaModes(): List<AreaMode> {
        val modes = mutableListOf(AreaMode.MAIN)
        if (isSubScreenConnected(BusiBaseConfig.readLeftSubScreenIp())) {
            modes.add(AreaMode.LEFT)
        }
        if (isSubScreenConnected(BusiBaseConfig.readRightSubScreenIp())) {
            modes.add(AreaMode.RIGHT)
        }
        return modes
    }

    /**
     * 副屏是否已连接主屏：通过 gRPC 客户端注册表判定（与三十人运动页协同逻辑一致）
     */
    private fun isSubScreenConnected(ip: String?): Boolean =
        !ip.isNullOrEmpty() && GrpcManager.getClientIdByIp(ip) != null

    /**
     * 弹出区域选择列表：仅显示已连接区域，选中项以金色高亮，同时右侧箭头切换为向上
     */
    private fun showAreaPopup(modes: List<AreaMode>) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_area_popup)
        }
        val checkedColor = Color.parseColor("#FDD401")
        modes.forEach { mode ->
            val item = LayoutInflater.from(this)
                .inflate(R.layout.item_area_option, content, false) as TextView
            item.text = areaOptions[mode.ordinal]
            item.setTextColor(if (mode == currentMode) checkedColor else Color.WHITE)
            item.setOnClickListener {
                areaPopup?.dismiss()
                onAreaModeSelected(mode)
            }
            content.addView(item)
        }
        areaPopup = PopupWindow(
            content,
            mBinding.llAreaSelector.width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(0))
            setOnDismissListener {
                updateAreaArrow(false)
                areaPopup = null
            }
        }
        updateAreaArrow(true)
        val yOffset = (8 * resources.displayMetrics.density).toInt()
        areaPopup?.showAsDropDown(mBinding.llAreaSelector, 0, yOffset)
    }

    /**
     * 选中区域选项：区域不同则切换对应屏
     */
    private fun onAreaModeSelected(mode: AreaMode) {
        updateAreaText(mode)
        if (mode != currentMode) {
            switchArea(mode)
        }
    }

    private fun updateAreaText(mode: AreaMode) {
        mBinding.tvAreaSelected.text = areaOptions[mode.ordinal]
    }

    private fun updateAreaArrow(open: Boolean) {
        mBinding.ivAreaArrow.setImageResource(
            if (open) R.drawable.ic_area_arrow_up else R.drawable.ic_area_arrow_down
        )
    }

    /**
     * 切换到指定屏：先停止摄像头预览，保存当前区域配置，再重新打开摄像头并重载 Overlay
     */
    private fun switchArea(mode: AreaMode) {
        currentMode = mode
        stopPreview()
        mBinding.overlay.saveConfig()
        openCamera(getCameraKey(mode))
        reloadOverlay()
    }

    private fun getCameraKey(mode: AreaMode): String = when (mode) {
        AreaMode.MAIN -> MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY
        AreaMode.LEFT -> MMKVBusiKey.LEFT_SUB_SCREEN_IPC_CAMERA_KEY
        AreaMode.RIGHT -> MMKVBusiKey.RIGHT_SUB_SCREEN_IPC_CAMERA_KEY
    }

    private fun getSaveKey(mode: AreaMode): String = when (mode) {
        AreaMode.MAIN -> saveKey ?: "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number"
        AreaMode.LEFT -> "${saveKey ?: BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}left"
        AreaMode.RIGHT -> "${saveKey ?: BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}right"
    }

    /**
     * 重置当前选中区域的框体配置
     */
    private fun resetCurrentArea() {
        ToastUtil.getInstance(this, getString(R.string.reset_successful)).show()
        val multi = RoleConfigMulti()
        multi.key = getSaveKey(currentMode)
        multi.number = number
        mBinding.overlay.setConfig(multi, OnRenderScene.StatusSportSet)
        mBinding.overlay.saveConfig()
        mBinding.overlay.refresh()
    }

    /**
     * 重新加载当前选中区域的 Overlay 配置（使用对应的 saveKey）
     */
    private fun reloadOverlay() {
        val saveKey = getSaveKey(currentMode)
        val uiConfig = AdlUIConfig.instance().readConfig(
            saveKey,
            RoleConfigMulti::class.java
        )
        uiConfig.number = number
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.refresh()
    }

    private fun stopPreview() {
        mBinding.previewView.stopPreview()
    }

    override fun onPause() {
        super.onPause()
        stopPreview()
        mBinding.previewView.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoReader?.release()
    }

    private fun openCamera(cameraKey: String) {
        val cameraConfig = BusiBaseConfig.readIpcCameraConfig(cameraKey)
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
            override fun onCameraOpen() {}
            override fun onCameraReOpen() {}
            override fun onDestroyed() {}
            override fun onStreamLoss() {}
            override fun onOpenFail() {}
        })
        mBinding.previewView.getTextureView().setScaleX(-1f)
    }

}
