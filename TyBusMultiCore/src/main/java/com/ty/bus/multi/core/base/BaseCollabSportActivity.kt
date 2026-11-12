package com.ty.bus.multi.core.base

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.SportOverlayMultiRegion
import com.adl.lib.AdlCameraPreviewView
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.core.view.thirtysport.ThirtySportResultView
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.ui.webview.CommonWebViewActivity
import com.ty.tybusbase.utils.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 多人协同运动 Activity 基类：承载与具体运动无关的公共 UI 逻辑，
 * 与 [BaseCollabSportViewModel] 配套使用。
 *
 * 公共部分：
 * - 区域导航：Tab 文案/颜色（未连接、占用中红字不可点）、区域切换、识别人数统计
 * - 协同弹窗：清空已识别结果、强制加入协同（展示期间内容实时更新）
 * - 全局事件：摄像头预览/提示、识别倒计时文案、竞技开始、运动倒计时时间栏、结果页
 * - 页面生命周期：onPause/onDestroy 释放相机与面板
 *
 * 子类通过抽象 getter 提供各自布局中的控件引用（两套布局控件 id 保持一致即可无缝接入），
 * 运动特有逻辑通过 open 钩子覆写（见各钩子注释）。
 */
abstract class BaseCollabSportActivity<VB : ViewBinding, VM : BaseCollabSportViewModel> :
    BaseMultiSportActivity<VB, VM>() {

    /***************************抽象控件 getter：由子类从各自 binding 提供***************************/
    protected abstract fun cameraView(): AdlCameraPreviewView
    protected abstract fun overlayView(): SportOverlayMultiRegion
    protected abstract fun titleView(): TextView
    protected abstract fun cameraTipsView(): TextView
    protected abstract fun sportViewByArea(area: DisplayType): BaseMultiSportViewNoAnimation
    protected abstract fun timeBar(): View
    protected abstract fun timeText(): TextView
    protected abstract fun resultContainer(): ViewGroup

    /***************************可选控件 getter：单屏页面可返回 null***************************/

    /** 区域导航Tab（无区域导航的单屏运动返回 null） */
    protected open fun tabLeft(): TextView? = null
    protected open fun tabMain(): TextView? = null
    protected open fun tabRight(): TextView? = null

    /** 当前区域名称（无区域导航的单屏运动返回 null） */
    protected open fun areaNameView(): TextView? = null

    /** 倒计时提示文案控件（提示内嵌在运动面板中的页面返回 null 并覆写 showCountDownNotice） */
    protected open fun countDownNoticeView(): TextView? = null

    /** 3-2-1倒计时动画控件（动画内嵌在运动面板中的页面返回 null 并覆写 playCountDownGoAnimation） */
    protected open fun countDownGoView(): View? = null

    /** 独立的"立即开始"按钮（按钮内嵌在运动面板中的页面返回 null 并覆写 bindStartNow/showStartNowButton） */
    protected open fun startNowView(): View? = null

    /** 是否有区域导航（主/左/右副屏切换）；单屏运动返回 false */
    protected open fun hasAreaNavigation(): Boolean =
        tabLeft() != null || tabMain() != null || tabRight() != null

    /** 页面上的运动面板集合（默认主屏+左右副屏；单屏运动只返回主屏） */
    protected open fun sportViews(): List<BaseMultiSportViewNoAnimation> = listOf(
        sportViewByArea(DisplayType.MAIN),
        sportViewByArea(DisplayType.LEFT),
        sportViewByArea(DisplayType.RIGHT)
    )

    /**
     * 本次运动实时最高展示区（主/左/右三屏最高者）。
     * 布局中没有该区域的页面返回 null，对应观察者不生效。
     */
    protected open fun currentBestViews(): CurrentBestViews? = null

    /** 今日最佳实时最高展示区控件集合 */
    data class CurrentBestViews(
        val group: View,
        val name: TextView,
        val count: TextView,
        val unit: TextView
    )

    /***************************运动特有钩子***************************/

    /** 读取运动专属配置（子类从 intent 中反序列化并赋给 viewModel） */
    protected abstract fun readSportConfig(intent: Intent)

    /** 无配置传入时的默认运动信息（调试用，无则返回 null） */
    protected open fun provideDefaultSportInfo(): SceneSportInfoEntity? = null

    /** 运动面板数量（识别路数） */
    protected open fun sportNumber(): Int = 10

    /** overlay 区域配置 key（默认多人运动通用 key；有独立配置的页面覆写） */
    protected open fun overlayConfigKey(): String =
        "${com.ty.tybusbase.constants.BusiBaseConstantsValue.MULTI_SPORT_CONFIG_KEY}${sportNumber()}"

    /** 结果页是否支持本地报告（默认跟随 ViewModel 配置；暂不支持报告的运动覆写为 false） */
    protected open fun supportResultReport(): Boolean = viewModel.isSupportReport()

    /**
     * 3-2-1倒计时动画结束后的处理
     * （默认：显示时间栏 → 已加入协同的副屏进入运动态 → 通知ViewModel开始计时）
     */
    protected open fun onCountDownGoFinished() {
        timeBar().visibility = View.VISIBLE
        //副屏：只有已加入协同的才进入运动状态
        if (viewModel.isSubScreenJoined(DisplayType.LEFT)) {
            getAreaSportViewByDisplay(DisplayType.LEFT).onStarted()
        }
        if (viewModel.isSubScreenJoined(DisplayType.RIGHT)) {
            getAreaSportViewByDisplay(DisplayType.RIGHT).onStarted()
        }
        viewModel.toStatusSportStart()
    }

    /** 绑定"立即开始"点击（默认操作独立按钮；按钮内嵌在运动面板中的页面覆写） */
    protected open fun bindStartNow() {
        startNowView()?.setOnClickListener { onStartNowClicked() }
    }

    /** 显示/隐藏"立即开始"按钮（默认操作独立按钮；按钮内嵌在运动面板中的页面覆写） */
    protected open fun showStartNowButton(visible: Boolean) {
        startNowView()?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /** 显示倒计时提示文案（默认写入文案控件；文案内嵌在运动面板中的页面覆写） */
    protected open fun showCountDownNotice(text: String) {
        countDownNoticeView()?.apply {
            this.text = text
            visibility = View.VISIBLE
        }
    }

    /** 隐藏倒计时提示文案（默认隐藏文案控件；文案内嵌在运动面板中的页面覆写） */
    protected open fun hideCountDownNotice() {
        countDownNoticeView()?.visibility = View.GONE
    }

    /** 3-2-1倒计时动画（默认由主屏运动面板播放；动画位置不同的页面覆写） */
    protected open fun playCountDownGoAnimation(onFinished: () -> Unit) {
        sportViewByArea(DisplayType.MAIN).readyGo { onFinished() }
    }

    /** 点击"立即开始"：弹出二次确认对话框后开赛 */
    private fun onStartNowClicked() {
        startDialog = CommonTipDialog().setCallback {
            //如果是运动会模式，则切到准备状态
            if (isMeetingMode()) {
                viewModel.skipPersonArray.forEach { _, value ->
                    sportViewByArea(DisplayType.MAIN).changeState(
                        value.number,
                        MultiSportCardViewState.PREPARED
                    )
                }
            }
            startDialog = null
            viewModel.readyGo()
        }
        startDialog?.show(supportFragmentManager, "tips")
    }

    /** 运动开始后的面板文案（如"开始跳绳"），无则返回 null */
    protected open fun sportStartText(): String? = null

    /** 识别倒计时前缀文案（无则用默认"比赛开始倒计时"） */
    protected open fun countdownText(): String? = null

    /** 是否运动会模式（显示"立即开始"按钮、开始后面板切准备态） */
    protected open fun isMeetingMode(): Boolean = false

    /** 结果页标题文案 */
    protected open fun resultTitleText(): String? = null

    /** 结果页是否支持"下一组" */
    protected open fun isSupportNextGroup(): Boolean = false

    /** 结果页是否支持成绩语音播报 */
    protected open fun voiceBroadcastSupported(): Boolean = false

    /** 结果页点击报告回调（默认副屏走在线报告；主屏本地报告由子类覆写） */
    protected open fun onResultReportClick(item: ThirtySportResultItem) {
        when (item.displayType) {
            DisplayType.LEFT -> viewModel.queryOnlineReport(viewModel.leftPersonArray[item.trackNumber].recordId)
            DisplayType.RIGHT -> viewModel.queryOnlineReport(viewModel.rightPersonArray[item.trackNumber].recordId)
            else -> {}
        }
    }

    /** 结束后自动关闭页面的延迟时间（毫秒），无则返回 null */
    protected open fun autoFinishTime(): Long? = null

    /** initView 末尾的运动特有初始化（如赛事隐藏今日最佳、视频调试） */
    protected open fun initViewExtra() {
    }

    /***************************内部状态***************************/

    private var top1Count = 0
    private lateinit var srrvResult: ThirtySportResultView
    private var startDialog: CommonTipDialog? = null

    /** "是否清空已识别结果"对话框（协同识别计数提示，展示期间内容可动态更新） */
    private var clearIdentifyDialog: CommonTipDialog? = null

    /** "是否强制加入协同"对话框（副屏占用中提示，展示期间内容可动态更新） */
    private var forceJoinDialog: CommonTipDialog? = null

    private var currentArea = DisplayType.MAIN

    /** 导航区协同异常状态字体颜色 */
    private val AREA_TAB_RED = "#F44134"

    /***************************生命周期与初始化***************************/

    override fun initData() {
        super.initData()
        viewModel.streamType = 2
        timeOutHelper.cancel()
        val studentList =
            intent.getSerializableExtra("student_list") as? ArrayList<MeetGroupDetailsEntity>
        viewModel.personIndex = intent.getIntExtra("person_index", 0)
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        readSportConfig(intent)
        if (viewModel.sportInfo == null) {
            provideDefaultSportInfo()?.let { viewModel.sportInfo = it }
        }
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面打开")
        viewModel.initEngine(sportNumber())
        viewModel.loadPerson(studentList)
        //注册Grpc指令
        viewModel.registerGrpcCmd()
        //检查副屏连接并发送协同运动开始指令（无副屏运动自动退化）
        viewModel.initCollaboration()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //运动开始前隐藏"当前最高"，有实时成绩后再显示
        currentBestViews()?.group?.visibility = View.GONE

        val isDebug =
            com.ty.tybusbase.utils.MMKVUtil.decodeBoolean(
                com.ty.tybusbase.constants.MMKVBusiKey.MODEL_DEBUG,
                com.ty.tybusbase.constants.BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
            )
        overlayView().enableDraw(isDebug)
        overlayView().enableDebug(isDebug)
        overlayView().enableDrawBox(false)
        overlayView().enableFace(true)
        val debug =
            com.ty.tybusbase.utils.MMKVUtil.decodeBoolean(
                com.ty.tybusbase.constants.MMKVBusiKey.SKELETON_ABLE,
                com.ty.tybusbase.constants.BusiBaseConstantsValue.DEFAULT_SKELETON
            )
        overlayView().skeletonDrawer.enableMarker(debug)
        overlayView().skeletonDrawer.enableLine(debug)
        overlayView().enableDrawSkeleton(debug)
        val uiConfig = com.adl.base.config.AdlUIConfig.instance().readConfig(
            overlayConfigKey(),
            com.adl.base.config.RoleConfigMulti::class.java
        )
        uiConfig.number = sportNumber()
        overlayView().setConfig(uiConfig, com.adl.base.overlay.OnRenderScene.StatusSportSet)
        viewModel.combineOverlay(overlayView())
        overlayView().post {
            viewModel.openTsProcess()
        }
        titleView().typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        titleView().text = viewModel.sportInfo?.sportSkuName

        initAreaViews()

        bindStartNow()

        sportViews().forEach { it.setSportStartedText(sportStartText()) }

        if (isMeetingMode()) {
            showStartNowButton(true)
        }

        initViewExtra()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.startPreviewLD.collectOnLifecycle {
            //开启摄像头
            cameraView().startPreview(it.first, object :
                AdlCameraPreviewView.OnViewStatusListener {
                override fun onAvailable() {
                }

                override fun onCameraOpen() {
                    viewModel.onCameraOpen()
                    hideTips()
                }

                override fun onCameraReOpen() {
                }

                override fun onDestroyed() {
                }

                override fun onStreamLoss() {
                    setTips(getString(R.string.camera_loss_tips))
                }

                override fun onOpenFail() {
                    setTips(getString(R.string.ipc_camera_fail_tip))
                }
            })
            cameraView().addFrameProcessor(it.second)
            cameraView().textureView.scaleX = -1f
        }

        viewModel.cameraLoginStateLD.collectOnLifecycle {
            if (it) hideTips() else setTips(getString(R.string.ipc_camera_fail_tip))
        }

        viewModel.stopPreviewLD.collectOnLifecycle {
            cameraView().stopPreview()
        }

        viewModel.resultLD.collectOnLifecycle {
            getAreaSportViewByDisplay(it.first).updateScore(it.second)
        }

        viewModel.singleResultLD.collectOnLifecycle {
            getAreaSportViewByDisplay(it.first).updateScore(it.second.first, it.second.second)
        }

        viewModel.identifiedLD.collectOnLifecycle {
            AdlLogger.d("identifiedLD: 道次：${it.first}--人员：${it.second.second.studentEntity!!.accountName}")
            getAreaSportViewByDisplay(it.first).setUserInfo(it.second)
            timeOutHelper.cancel()
            //识别到人员，刷新底部导航栏"已识别x人"文案
            updateAreaTabs()
        }

        viewModel.sportStartedLD.collectOnLifecycle {
            //运动开始，底部导航栏切换为"x人运动中"文案
            updateAreaTabs()
        }

        //本次运动实时最高（主/左/右三屏），有成绩才显示
        currentBestViews()?.let { views ->
            viewModel.currentBestLD.collectOnLifecycle { best ->
                if (best == null) {
                    views.group.visibility = View.GONE
                    return@collectOnLifecycle
                }
                views.name.text = "${best.areaName} | ${best.accountName}"
                views.count.text = best.score.toString()
                views.count.visibility = View.VISIBLE
                views.unit.text = viewModel.measureUnit
                views.unit.visibility = View.VISIBLE
                views.group.visibility = View.VISIBLE
            }
        }

        viewModel.collaborationStateLD.collectOnLifecycle {
            //协同状态变化，刷新导航区（未连接/设备占用中红字不可点击）
            updateAreaTabs()
        }

        viewModel.changeSportViewStateLD.collectOnLifecycle {
            getAreaSportViewByDisplay(it.first).changeState(it.second.first, it.second.second)
        }

        //协同已识别人数变化：弹窗展示或更新"是否清空已识别结果"提示
        viewModel.identifiedCountLD.collectOnLifecycle {
            showOrUpdateClearIdentifyDialog()
        }

        //副屏占用中变化：弹窗展示或更新"是否强制加入协同"提示
        viewModel.subScreenOccupiedLD.collectOnLifecycle {
            showOrUpdateForceJoinDialog(it)
        }

        viewModel.countDownNoticeLD.collectOnLifecycle {
            //倒计时提示文案与开始按钮属于整个运动，统一在Activity层展示
            val configText = countdownText()
            val str = if (!TextUtils.isEmpty(configText)) "${configText}: ${it.second}" else "${
                getString(R.string.race_start_countdown)
            }：${it.second}"
            showCountDownNotice(str)
            showStartNowButton(true)
        }

        viewModel.countDownFinishLD.collectOnLifecycle {
            startDialog?.dismiss()
            //竞技开始，隐藏开始按钮与等待文案，显示运动倒计时时间栏
            showStartNowButton(false)
            hideCountDownNotice()
            playCountDownGoAnimation {
                onCountDownGoFinished()
            }
        }

        viewModel.sportCountDownLD.collectOnLifecycle {
            //运动倒计时属于整个运动，统一在Activity层更新时间栏
            timeText().text = it.second
        }

        viewModel.waitOtherAreaLD.collectOnLifecycle {
            //主屏识别倒计时已结束，但已加入的副屏尚未倒计时结束：提示等待其他区域
            showCountDownNotice(getString(R.string.wait_other_area_join))
        }

        viewModel.toDoneLD.collectOnLifecycle {
            //结束清除骨骼数据等
            overlayView().clearScreen()
            addResultView(it)
        }

        viewModel.changeTimeTextBgLD.collectOnLifecycle {
            //时间栏背景属于整个运动，统一在Activity层设置
            timeBar().setBackgroundResource(R.mipmap.pic_time_warm_more)
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.showReportLD.collectOnLifecycle {
            val intent = Intent(this, CommonWebViewActivity::class.java)
            intent.putExtra("url", it)
            intent.putExtra("show_white_board", true)
            startActivity(intent)
        }
    }

    /***************************区域导航***************************/

    private fun initAreaViews() {
        //单屏运动没有区域导航
        if (!hasAreaNavigation()) return
        updateAreaTabs()
        tabLeft()?.setOnClickListener { switchArea(DisplayType.LEFT) }
        tabMain()?.setOnClickListener { switchArea(DisplayType.MAIN) }
        tabRight()?.setOnClickListener { switchArea(DisplayType.RIGHT) }

        switchArea(DisplayType.MAIN)
    }

    private fun switchArea(area: DisplayType) {
        currentArea = area
        sportViewByArea(DisplayType.MAIN).visibility =
            if (area == DisplayType.MAIN) View.VISIBLE else View.GONE
        sportViewByArea(DisplayType.LEFT).visibility =
            if (area == DisplayType.LEFT) View.VISIBLE else View.GONE
        sportViewByArea(DisplayType.RIGHT).visibility =
            if (area == DisplayType.RIGHT) View.VISIBLE else View.GONE

        areaNameView()?.text = when (area) {
            DisplayType.LEFT -> getString(R.string.area_left)
            DisplayType.MAIN -> getString(R.string.area_main)
            DisplayType.RIGHT -> getString(R.string.area_right)
            else -> "Other"
        }

        updateAreaTabs()
    }

    private fun updateAreaTabs() {
        if (!hasAreaNavigation()) return
        //已识别人数从各区域人员数组中实时统计，避免重复计数
        val mainIdentifiedCount = viewModel.skipPersonArray.size()
        val leftIdentifiedCount = viewModel.leftPersonArray.size()
        val rightIdentifiedCount = viewModel.rightPersonArray.size()

        //运动开始后，底部导航栏文案切换为"x人运动中"
        val sporting = viewModel.sportStartedLD.value == true
        updateAreaTab(
            tabLeft(),
            DisplayType.LEFT,
            leftIdentifiedCount,
            sporting,
            R.string.area_left_identified,
            R.string.area_left_sporting,
            R.string.area_left_unconnected,
            R.string.area_left_occupied
        )
        tabMain()?.text = getString(
            if (sporting) R.string.area_main_sporting else R.string.area_main_identified,
            mainIdentifiedCount
        )
        updateAreaTab(
            tabRight(),
            DisplayType.RIGHT,
            rightIdentifiedCount,
            sporting,
            R.string.area_right_identified,
            R.string.area_right_sporting,
            R.string.area_right_unconnected,
            R.string.area_right_occupied
        )

        tabLeft()?.isSelected = currentArea == DisplayType.LEFT
        tabMain()?.isSelected = currentArea == DisplayType.MAIN
        tabRight()?.isSelected = currentArea == DisplayType.RIGHT
    }

    /**
     * 更新单个区域Tab：协同异常状态（未连接/设备占用中）显示红色且不可点击
     */
    private fun updateAreaTab(
        tvTab: TextView?,
        displayType: DisplayType,
        count: Int,
        sporting: Boolean,
        identifiedRes: Int,
        sportingRes: Int,
        unconnectedRes: Int,
        occupiedRes: Int
    ) {
        if (tvTab == null) return
        //未有协同状态记录时按无连接处理，收到副屏回复后（NORMAL/OCCUPIED）自动刷新
        val state = viewModel.collaborationStateLD.value?.get(displayType)
            ?: CollaborationAreaState.UNCONNECTED
        when (state) {
            CollaborationAreaState.UNCONNECTED -> {
                tvTab.text = getString(unconnectedRes)
                tvTab.setTextColor(Color.parseColor(AREA_TAB_RED))
                tvTab.isEnabled = false
            }

            CollaborationAreaState.OCCUPIED -> {
                tvTab.text = getString(occupiedRes)
                tvTab.setTextColor(Color.parseColor(AREA_TAB_RED))
                tvTab.isEnabled = false
            }

            else -> {
                tvTab.text = getString(
                    if (sporting) sportingRes else identifiedRes,
                    count
                )
                tvTab.setTextColor(
                    ContextCompat.getColorStateList(this, R.color.selector_area_tab_text)
                )
                tvTab.isEnabled = true
            }
        }
    }

    protected fun getAreaSportViewByDisplay(displayType: DisplayType = DisplayType.MAIN): BaseMultiSportViewNoAnimation =
        when (displayType) {
            DisplayType.LEFT -> sportViewByArea(DisplayType.LEFT)
            DisplayType.RIGHT -> sportViewByArea(DisplayType.RIGHT)
            else -> sportViewByArea(DisplayType.MAIN)
        }

    /***************************协同弹窗***************************/

    /**
     * 展示或更新"是否清空已识别结果"对话框。
     * 可能左区先上报、右区后上报，或仅一端上报；对话框展示期间（含新用户到达）内容实时更新。
     */
    @Synchronized
    private fun showOrUpdateClearIdentifyDialog() {
        val text = buildClearIdentifyText()
        val dialog = clearIdentifyDialog
        if (dialog != null) {
            LogUtil.d("更新内容：$text")
            dialog.updateContent(text)
            return
        }
        LogUtil.d("首次显示内容：$text")
        clearIdentifyDialog = CommonTipDialog()
            .setContent(text)
            .setCallback {
                //用户选择"是"：按需向已上报识别人数的区域发送清除指令
                viewModel.confirmClearIdentifyInfo()
                clearIdentifyDialog = null
                //清空后刷新底部导航栏"已识别x人"文案
                updateAreaTabs()
            }
            .setCancelCallback {
                //用户选择"否"：关闭本次提示
                viewModel.cancelClearIdentifyInfo()
                clearIdentifyDialog = null
            }
        clearIdentifyDialog?.show(supportFragmentManager, "clear_identify")
    }

    /**
     * 展示或更新"是否强制加入协同"对话框。
     * 可能左屏先上报占用、右屏后上报，或两屏同时占用；对话框展示期间内容实时更新；
     * 占用状态全部解除则关闭提示。
     */
    @Synchronized
    private fun showOrUpdateForceJoinDialog(occupied: Set<DisplayType>) {
        if (occupied.isEmpty()) {
            //副屏已恢复空闲：关闭提示
            forceJoinDialog?.dismiss()
            forceJoinDialog = null
            return
        }
        val dialog = forceJoinDialog
        if (dialog != null) {
            return
        }
        forceJoinDialog = CommonTipDialog()
            .setContent(getString(R.string.force_join_occupied_tip))
            .setCallback {
                //用户选择"是"：向占用中的设备发送强制加入协同指令
                viewModel.confirmForceJoin()
                forceJoinDialog = null
            }
            .setCancelCallback {
                //用户选择"否"：仅关闭本次提示
                viewModel.cancelForceJoin()
                forceJoinDialog = null
            }
        forceJoinDialog?.show(supportFragmentManager, "force_join")
    }

    private fun buildClearIdentifyText(): String {
        val sb = StringBuilder()
        viewModel.identifiedCountMap[DisplayType.LEFT]?.let {
            sb.append("左区已识别${it}人，")
        }
        viewModel.identifiedCountMap[DisplayType.RIGHT]?.let {
            sb.append("右区已识别${it}人，")
        }
        sb.append("是否清空已识别结果")
        return sb.toString()
    }

    /***************************结果页***************************/

    private fun addResultView(resultList: List<ThirtySportResultItem>) {
        //创建结果页，并添加到页面中
        srrvResult = ThirtySportResultView(this).apply {
            measureUnit = viewModel.measureUnit
            setEnergyPoint(viewModel.energyPoint)
        }
        srrvResult.setSupportReport(supportResultReport())
        srrvResult.setReportOnclickListener {
            onResultReportClick(it)
        }

        val params =
            ConstraintLayout.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                resultContainer().height
            )
        params.topToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        resultContainer().addView(
            srrvResult,
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        srrvResult.setConfirmOnclickListener {
            finish()
        }
        srrvResult.setResultTitleText(resultTitleText())
        if (isSupportNextGroup()) {
            srrvResult.setNextOnclickListener {
                resultContainer().removeView(srrvResult)
                reset()
            }
        }

        srrvResult.setResult(
            resultList,
            if (viewModel.matchId != null) 100000 else top1Count,
            voiceBroadcastSupported()
        )
    }

    /** 今日最佳数据展示（有"今日最佳"区域的页面覆写 initView 时调用） */
    protected fun setTop1Count(count: Int) {
        top1Count = count
    }

    /***************************提示与生命周期***************************/

    private fun setTips(text: String) {
        AdlLogger.d(text)
        cameraTipsView().visibility = View.VISIBLE
        cameraTipsView().text = text
    }

    private fun hideTips() {
        cameraTipsView().visibility = View.GONE
    }

    /**
     * 收集 Flow。页面处于 STARTED 及以上状态才派发事件，退到后台暂停。
     * 注意：flowWithLifecycle 在每次回到 STARTED 时会重新订阅，因此 replay>0（粘性）的 Flow
     * 会把最近一次缓存事件重放；一次性事件请在 ViewModel 侧用 replay=0 的非粘性流。
     */
    protected fun <T> Flow<T>.collectOnLifecycle(block: suspend (T) -> Unit) {
        lifecycleScope.launch {
            flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect(block)
        }
    }

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusEnd() {
        super.onStatusEnd()
        cameraView().stopPreview()
        autoFinishTime()?.let {
            if (it > 0) {
                com.adl.base.common.AdlHandlerThread.getUIHandler().postDelayed({ finish() }, it)
            }
        }
    }

    open fun reset() {
        sportViews().forEach { it.reset() }
        timeBar().visibility = View.GONE
        viewModel.reset()
    }

    override fun onPause() {
        super.onPause()
        cameraView().stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面关闭")
        cameraView().release()
        sportViews().forEach { it.release() }
        overlayView().release()
        com.adl.base.common.AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }
}
