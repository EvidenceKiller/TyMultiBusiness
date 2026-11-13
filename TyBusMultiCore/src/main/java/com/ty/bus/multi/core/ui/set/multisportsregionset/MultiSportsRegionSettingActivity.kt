package com.ty.bus.multi.core.ui.set.multisportsregionset

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCameraConfig
import com.adl.lib.AdlCameraPreviewView
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ActivityMultiSportsRegionSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

/**
 * 多人多运动框体设置页
 *
 * 顶部下拉框内容由外界传入（[MultiSportRegionOption]：运动名称、运动类型、运动人数、存储Key），
 * 选中某一项即切换到该 Key 对应的 Overlay 框体配置；切换下拉时自动保存上一项的框体设置。
 * 分辨率（res_type）与摄像头配置（ipc_key）等传参沿用 MultiRegionSetting 的约定。
 */
class MultiSportsRegionSettingActivity :
    BaseActivity<ActivityMultiSportsRegionSettingBinding, BaseViewModel>() {

    companion object {
        /** 下拉项列表：ArrayList<MultiSportRegionOption> */
        const val EXTRA_OPTIONS = "multi_sport_region_options"

        /** 页面级分辨率：0-1920*1080，1-2560*1440，2-3840*2160 */
        const val EXTRA_RESOLUTION_TYPE = "res_type"

        /** 页面级摄像头配置Key，默认主屏 */
        const val EXTRA_IPC_KEY = "ipc_key"
    }

    @Suppress("DEPRECATION")
    private val options: List<MultiSportRegionOption> by lazy {
        (intent.getSerializableExtra(EXTRA_OPTIONS) as? ArrayList<*>)
            ?.filterIsInstance<MultiSportRegionOption>()
            .orEmpty()
    }

    // 0:1920*1080; 1:2560*1440; 2:3840*2160
    private val defaultResolutionType by lazy { intent.getIntExtra(EXTRA_RESOLUTION_TYPE, 1) }

    private val defaultCameraKey by lazy {
        intent.getStringExtra(EXTRA_IPC_KEY) ?: MMKVBusiKey.MAIN_SCREEN_IPC_CAMERA_KEY
    }

    private var currentIndex = -1
    private var currentResolutionType = -1
    private var currentCameraKey = ""

    private var sportPopup: PopupWindow? = null

    //摄像头是否已打开（用于pause→resume后重新打开预览）
    private var cameraOpened = false

    //从后台返回时是否需要重开预览
    private var needReopenCamera = false

    override fun initObserver() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        if (options.isEmpty()) {
            ToastUtil.getInstance(this, getString(R.string.multi_sport_region_empty_tip)).show()
            finish()
            return
        }

        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.btnSave.setOnClickListener {
            mBinding.overlay.saveConfig()
            ToastUtil.getInstance(this, getString(R.string.save_successful)).show()
        }
        mBinding.btnReset.setOnClickListener { resetCurrentSport() }

        //初始化 Overlay 绘制开关
        mBinding.overlay.enableDraw(true)
        mBinding.overlay.enableDebug(true)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug = MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, false)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)

        initSportSelector()
        //默认加载第一项（首次不保存，避免把空配置写入上一项Key）
        switchSport(0, saveCurrent = false)
    }

    /**
     * 初始化运动切换下拉框：仅一项时隐藏下拉框
     */
    private fun initSportSelector() {
        if (options.size <= 1) {
            mBinding.llSportSelector.visibility = View.GONE
            return
        }
        mBinding.llSportSelector.visibility = View.VISIBLE
        updateSelectorArrow(false)
        mBinding.llSportSelector.setOnClickListener {
            if (sportPopup?.isShowing == true) {
                sportPopup?.dismiss()
            } else {
                showSportPopup()
            }
        }
    }

    /**
     * 弹出运动选择列表：选中项金色高亮
     */
    private fun showSportPopup() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_area_popup)
        }
        val checkedColor = Color.parseColor("#FDD401")
        options.forEachIndexed { index, option ->
            val item = LayoutInflater.from(this)
                .inflate(R.layout.item_area_option, content, false) as TextView
            item.text = getString(
                R.string.multi_sport_region_option_format,
                option.sportName,
                option.personCount
            )
            item.setTextColor(if (index == currentIndex) checkedColor else Color.WHITE)
            item.setOnClickListener {
                sportPopup?.dismiss()
                switchSport(index, saveCurrent = true)
            }
            content.addView(item)
        }
        sportPopup = PopupWindow(
            content,
            mBinding.llSportSelector.width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(0))
            setOnDismissListener {
                updateSelectorArrow(false)
                sportPopup = null
            }
        }
        updateSelectorArrow(true)
        val yOffset = (8 * resources.displayMetrics.density).toInt()
        sportPopup?.showAsDropDown(mBinding.llSportSelector, 0, yOffset)
    }

    /**
     * 切换到指定运动：先保存当前运动的框体配置，再按新运动的Key加载框体配置。
     * 分辨率或摄像头Key与当前不一致时重开预览。
     */
    private fun switchSport(index: Int, saveCurrent: Boolean) {
        val option = options.getOrNull(index) ?: return
        //切换下拉自动保存当前运动的设置
        if (saveCurrent) {
            mBinding.overlay.saveConfig()
        }
        val resolutionType =
            if (option.resolutionType >= 0) option.resolutionType else defaultResolutionType
        val cameraKey = option.cameraKey ?: defaultCameraKey
        if (!cameraOpened || resolutionType != currentResolutionType || cameraKey != currentCameraKey) {
            mBinding.previewView.stopPreview()
            openCamera(cameraKey, resolutionType)
        }
        currentIndex = index
        updateSelectedText(option)
        reloadOverlay(option)
    }

    /**
     * 按运动对应的Key重新加载 Overlay 框体配置
     */
    private fun reloadOverlay(option: MultiSportRegionOption) {
        val uiConfig = AdlUIConfig.instance().readConfig(
            option.regionKey,
            RoleConfigMulti::class.java
        )
        uiConfig.key = option.regionKey
        uiConfig.number = option.personCount
        uiConfig.isShowNumberText = true
        mBinding.overlay.setSportType(option.sportType)
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
        mBinding.overlay.refresh()
    }

    /**
     * 重置当前运动的框体配置
     */
    private fun resetCurrentSport() {
        val option = options.getOrNull(currentIndex) ?: return
        ToastUtil.getInstance(this, getString(R.string.reset_successful)).show()
        val multi = RoleConfigMulti()
        multi.key = option.regionKey
        multi.number = option.personCount
        multi.isShowNumberText = true
        mBinding.overlay.setConfig(multi, OnRenderScene.StatusSportSet)
        mBinding.overlay.saveConfig()
        mBinding.overlay.refresh()
    }

    private fun updateSelectedText(option: MultiSportRegionOption) {
        mBinding.tvSportSelected.text = getString(
            R.string.multi_sport_region_option_format,
            option.sportName,
            option.personCount
        )
    }

    private fun updateSelectorArrow(open: Boolean) {
        mBinding.ivSportArrow.setImageResource(
            if (open) R.drawable.ic_area_arrow_up else R.drawable.ic_area_arrow_down
        )
    }

    private fun openCamera(cameraKey: String, resolutionType: Int) {
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
        cameraOpened = true
        currentResolutionType = resolutionType
        currentCameraKey = cameraKey
    }

    override fun onResume() {
        super.onResume()
        if (needReopenCamera) {
            needReopenCamera = false
            openCamera(currentCameraKey, currentResolutionType)
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.previewView.stopPreview()
        needReopenCamera = cameraOpened
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.previewView.release()
    }
}
