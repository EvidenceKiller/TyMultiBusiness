package com.ty.bus.multi.core.ui.tensport

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigMulti
import com.adl.base.log.AdlLogger
import com.adl.base.overlay.OnRenderScene
import com.adl.lib.AdlCamera
import com.adl.lib.AdlCameraPreviewView
import com.adl.lib.camera.hk.NsCameraHKSource
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.outer.AdlService
import com.aipower.modulepk.base.BaseMultiSportActivity
import com.bumptech.glide.Glide
import com.hcnetsdk.control.DevConfigGuider
import com.ty.bus.multi.R
import com.ty.bus.multi.core.view.tensport.TenSportResultView
import com.ty.bus.multi.databinding.AcTenSportBinding
import com.ty.tybusbase.constants.ConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.MMKVUtil

class TenSportActivity : BaseMultiSportActivity<AcTenSportBinding, TenSportViewModel>() {
    private var top1Count = 0

    private lateinit var srrvResult: TenSportResultView
    private val number = 10
    override fun initData() {
        super.initData()
        if (viewModel.sportInfo == null) {
            viewModel.sportInfo = SceneSportInfoEntity().apply {
                sportSkuId = "0201010001"
                sportItemCode = SportConstant.SPORT_SKIP_ROPE
                endValue = "60000"
                testCount = "10"
            }
        }
        viewModel.initEngine(number)
        viewModel.getBestRankTop()
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //如果是赛事，则隐藏今日最佳
        viewModel.matchId?.let {
            mBinding.groupTodayTop.visibility = View.GONE
        }

        val isDebug =
            MMKVUtil.decodeBoolean(MMKVBusiKey.MODEL_DEBUG, ConstantsValue.DEFAULT_DEBUG_MODULE)
        mBinding.overlay.enableDraw(isDebug)
        mBinding.overlay.enableDebug(isDebug)
        mBinding.overlay.enableDrawBox(false)
        mBinding.overlay.enableFace(true)
        val debug =
            MMKVUtil.decodeBoolean(MMKVBusiKey.SKELETON_ABLE, ConstantsValue.DEFAULT_SKELETON)
        mBinding.overlay.skeletonDrawer.enableMarker(debug)
        mBinding.overlay.skeletonDrawer.enableLine(debug)
        mBinding.overlay.enableDrawSkeleton(debug)
        val uiConfig = AdlUIConfig.instance().readConfig(
            "${ConstantsValue.MULTI_SPORT_CONFIG_KEY}$number",
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

        mBinding.sportView.startNowCallback = {
            CommonTipDialog().setCallback {
                viewModel.readyGo()
            }.show(supportFragmentManager, "tips")
        }
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
                }

                override fun onDestroyed() {

                }
            })
            mBinding.cameraView.addFrameProcessor(it.second)
            mBinding.cameraView.textureView.scaleX = -1f
        }

        viewModel.stopPreviewLD.observe(this) {
            mBinding.cameraView.stopPreview()
        }

        viewModel.resultLD.observe(this) {
            mBinding.sportView.updateScore(it)
        }

        viewModel.identifiedLD.observe(this) {
            AdlLogger.d("identifiedLD: 道次：${it.first}--人员：${it.second.studentEntity!!.accountName}")
            mBinding.sportView.setUserInfo(it)
        }

        viewModel.changeSportViewStateLD.observe(this) {
            mBinding.sportView.changeState(it.first, it.second)
        }

        viewModel.countDownNoticeLD.observe(this) {
            mBinding.sportView.setCountDownNotice(it)
        }

        viewModel.countDownFinishLD.observe(this) {
            mBinding.sportView.readyGo {
                //比赛开始
                viewModel.toStatusSportStart()
            }
        }

        viewModel.sportCountDownLD.observe(this) {
            mBinding.sportView.updateTime(it)
        }

        viewModel.toDoneLD.observe(this) {
            //结束清除骨骼数据等
            mBinding.overlay.clearScreen()
            //创建结果页，并添加到页面中
            srrvResult = TenSportResultView(this).apply { measureUnit = viewModel.measureUnit }

//            if (viewModel.sportInfo?.sportItemCode == SportConstant.SPORT_SKIP_ROPE) {
//                srrvResult.setReportOnclickListener {
//                    viewReport(viewModel.getIndicatorsResultDto(it))
//                }
//            }

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
            srrvResult.setResult(it, if (viewModel.matchId != null) 100000 else top1Count)
        }

        viewModel.changeTimeTextBgLD.observe(this) {
            mBinding.sportView.changeTimeTextBg(it)
        }

        viewModel.timeOutLD.observe(this) {
            finish()
        }

        viewModel.bestRankLD.observe(this) {
            initTopView(it)
        }

    }

    override fun onStatusIdentify() {
    }

    override fun onStatusIdentifySuccess() {
    }

    override fun onStatusSport() {
    }

    override fun onStatusEnd() {
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

    //    private fun viewReport(indicatorDo: IndicatorsResultDto?) {
//        indicatorDo?.apply {
//            ARouter.getInstance().build(ARouterPath.MODULE_TEST_SPORT_REPORT)
//                .withSerializable("report_indicator", this)
//                .withInt("type", 1)
//                .navigation()
//        } ?: run {
//            ToastUtil.getInstance(this, getString(com.aipower.modulecommon.R.string.no_report))
//                .show()
//        }
//    }
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
        mBinding.cameraView.release()
        mBinding.sportView.release()
        mBinding.overlay.release()
    }
}