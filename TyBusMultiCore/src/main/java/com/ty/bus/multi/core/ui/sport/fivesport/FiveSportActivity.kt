package com.ty.bus.multi.core.ui.sport.fivesport

import adl.ssp.common.report.model.IndicatorsResultDto
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportActivity
import com.ty.bus.multi.core.base.BaseMultiSportResultView
import com.ty.bus.multi.core.base.BaseMultiSportView
import com.ty.bus.multi.core.ui.sport.tensport.TenSportActivity
import com.ty.bus.multi.core.view.fivesport.MultiSport5ResultView
import com.ty.bus.multi.core.view.fivesport.MultiSport5SportView
import com.ty.bus.multi.databinding.ActivityFiveSportLayoutBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.ui.report.SportReportActivity
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import com.ty.tybusbase.utils.ToastUtil

class FiveSportActivity :
    BaseMultiSportActivity<ActivityFiveSportLayoutBinding, FiveSportViewModel>() {
    private var top1Count = 0
    private lateinit var srsvSport: BaseMultiSportView
    private lateinit var srrvResult: BaseMultiSportResultView
    private val number = 5

    override fun initData() {
        super.initData()
        viewModel.ptPlanId = intent.getStringExtra("pt_plan_id")
        viewModel.ptStandardId = intent.getStringExtra("pt_standard_id")
        viewModel.initEngine(number)
        viewModel.getBestRankTop()
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面打开")
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //如果是赛事，则隐藏今日最佳
        viewModel.matchId?.let {
            mBinding.groupTodayTop.visibility = View.GONE
        }

        //自主，组织，随堂测隐藏今日最佳
        if (viewModel.sceneId == SceneConstants.ORGANIZE_TEST || viewModel.sceneId == SceneConstants.INDEPENDENT_TEST || viewModel.sceneId == SceneConstants.CLASS_TEST) {
            mBinding.groupTodayTop.visibility = View.GONE
        }

        val isDebug = MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, false)
        mBinding.overlay.enableDraw(isDebug)
        mBinding.overlay.enableDebug(isDebug)
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
        viewModel.combineOverlay(mBinding.overlay)
        mBinding.overlay.post {
            viewModel.openTsProcess()
        }
        mBinding.tvTitle.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(this)
        mBinding.tvTitle.text = viewModel.sportInfo?.sportSkuName
        srsvSport = MultiSport5SportView(this)
        mBinding.clSkipSportViewContainer.addView(
            srsvSport,
            ViewGroup.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.startPreviewLD.observe(this) {
            //开启摄像头
            mBinding.cameraView.startPreview(it.first, object :
                AdlCameraPreviewView.OnViewStatusListener {
                override fun onAvailable() {

                }

                override fun onCameraOpen() {
                    viewModel.onCameraOpen()
                    hideTips()
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
            mBinding.cameraView.addFrameProcessor(it.second)
            mBinding.cameraView.textureView.scaleX = -1f
        }

        viewModel.cameraLoginStateLD.observe(this) {
            if (it) hideTips() else setTips(getString(R.string.ipc_camera_fail_tip))
        }

        viewModel.stopPreviewLD.observe(this) {
            mBinding.cameraView.stopPreview()
        }

        viewModel.resultLD.observe(this) {
            srsvSport.updateScore(it)
        }

        viewModel.identifiedLD.observe(this) {
            LogUtil.d("identifiedLD: 道次：${it.first}--人员：${it.second.studentEntity?.accountName}")
            srsvSport.setUserInfo(it)
            timeOutHelper.cancel()
        }

        viewModel.changeSportViewStateLD.observe(this) {
            srsvSport.changeState(it.first, it.second)
        }

        viewModel.countDownNoticeLD.observe(this) {
            srsvSport.setCountDownNotice("${com.ty.tybusbase.expand.getString(R.string.race_start_countdown)}：$it")
        }

        viewModel.countDownFinishLD.observe(this) {
            srsvSport.readyGo {
                //比赛开始
                viewModel.toStatusSportStart()
            }
        }

        viewModel.sportCountDownLD.observe(this) {
            srsvSport.updateTime(it)
        }

        viewModel.toDoneLD.observe(this) {
            //结束清除骨骼数据等
            mBinding.overlay.clearScreen()
            //创建结果页，并添加到页面中
            srrvResult = MultiSport5ResultView(this).apply { measureUnit = viewModel.measureUnit }
            if (viewModel.sportInfo?.sportItemCode == SportConstant.SPORT_SKIP_ROPE) {
                it.forEach {
                    srrvResult.showReportButton(it.number, true)
                }
                srrvResult.setReportOnclickListener {
                    viewReport(viewModel.getIndicatorsResultDto(it))
                }
            }
            val params =
                ConstraintLayout.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    mBinding.clSkipSportViewContainer.height
                )
            params.topToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            mBinding.clSkipSportViewContainer.addView(
                srrvResult,
                params
            )
            srrvResult.setConfirmOnclickListener {
                finish()
            }
            doResultViewTranslateAnimation(srrvResult)
            srrvResult.setResult(
                it,
                if (viewModel.matchId != null) 100000 else top1Count,
                viewModel.energyPoint
            )
        }

        viewModel.changeTimeTextBgLD.observe(this) {
            srsvSport.changeTimeTextBg(it)
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }

    }

    private fun setTips(text: String) {
        LogUtil.d(text)
        mBinding.tvCameraTips.visibility = View.VISIBLE
        mBinding.tvCameraTips.text = text
    }

    private fun hideTips() {
        mBinding.tvCameraTips.visibility = View.GONE
    }

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
        super.onStatusSport()
    }


    override fun onStatusEnd() {
        super.onStatusEnd()
        mBinding.cameraView.stopPreview()
    }

    /**
     * 初始化今日最佳
     */
    private fun initTopView(topEntity: BestRankTopEntity) {
        if (!TextUtils.isEmpty(topEntity.originalSportResult)) {
            mBinding.tvCount.visibility = View.VISIBLE
            mBinding.tvUnit.visibility = View.VISIBLE
            mBinding.tvUnit.text = viewModel.measureUnit
            mBinding.tvName.text = topEntity.accountName
            mBinding.tvCount.text = topEntity.originalSportResult
            top1Count = topEntity.originalSportResult.toInt()
            Glide.with(this).load(AdlService.getService().facePrefix + topEntity.faceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        }
    }

    private fun viewReport(indicatorDo: IndicatorsResultDto?) {
        indicatorDo?.apply {
            val intent = Intent(this@FiveSportActivity, SportReportActivity::class.java)
            intent.putExtra("report_indicator", this@apply)
            intent.putExtra("type", 1)
            startActivity(intent)
        } ?: run {
            ToastUtil.getInstance(this, getString(R.string.no_report))
                .show()
        }
    }

    private fun doResultViewTranslateAnimation(view: View) {
        view.post {
            ObjectAnimator.ofFloat(
                view,
                "translationY",
                view.height.toFloat(),
                -view.height.toFloat()
            ).setDuration(500).start()
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.cameraView.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("${viewModel.sportInfo?.sportSkuName}运动页面关闭")
        mBinding.cameraView.release()
        srsvSport.release()
        mBinding.overlay.release()
    }
}