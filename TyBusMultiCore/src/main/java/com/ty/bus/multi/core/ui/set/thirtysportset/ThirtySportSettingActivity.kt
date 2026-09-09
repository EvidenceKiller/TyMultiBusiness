package com.ty.bus.multi.core.ui.set.thirtysportset

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraPreviewView
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ActivityThirtySportSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
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

    private enum class AreaMode {
        MAIN, LEFT, RIGHT
    }

    private val number = 10
    private var currentMode = AreaMode.MAIN

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
            mBinding.overlay.saveConfig()
            ToastUtil.getInstance(this, getString(R.string.save_successful)).show()
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
    }

    /**
     * 初始化顶部区域切换下拉框，默认选中"智慧屏多人运动"
     */
    private fun initAreaSpinner() {
        updateAreaText(currentMode.ordinal)
        updateAreaArrow(false)
        mBinding.llAreaSelector.setOnClickListener {
            if (areaPopup?.isShowing == true) {
                areaPopup?.dismiss()
            } else {
                showAreaPopup()
            }
        }
    }

    /**
     * 弹出区域选择列表：选中项以金色高亮，同时右侧箭头切换为向上
     */
    private fun showAreaPopup() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_area_popup)
        }
        val checkedColor = Color.parseColor("#FDD401")
        areaOptions.forEachIndexed { index, option ->
            val item = LayoutInflater.from(this)
                .inflate(R.layout.item_area_option, content, false) as TextView
            item.text = option
            item.setTextColor(if (index == currentMode.ordinal) checkedColor else Color.WHITE)
            item.setOnClickListener {
                areaPopup?.dismiss()
                onAreaOptionSelected(index)
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
     * 选中区域选项：index 与 AreaMode 顺序一致(0 主屏 / 1 左区 / 2 右区)
     */
    private fun onAreaOptionSelected(position: Int) {
        updateAreaText(position)
        val mode = when (position) {
            1 -> AreaMode.LEFT
            2 -> AreaMode.RIGHT
            else -> AreaMode.MAIN
        }
        if (mode != currentMode) {
            switchArea(mode)
        }
    }

    private fun updateAreaText(position: Int) {
        mBinding.tvAreaSelected.text = areaOptions[position]
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
        AreaMode.MAIN -> "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}$number"
        AreaMode.LEFT -> "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}left"
        AreaMode.RIGHT -> "${BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}right"
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
