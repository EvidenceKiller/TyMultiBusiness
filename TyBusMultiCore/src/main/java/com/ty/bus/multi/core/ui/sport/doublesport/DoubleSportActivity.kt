package com.ty.bus.multi.core.ui.sport.doublesport

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.adl.base.common.AdlHandlerThread
import com.adl.mount.impl.TsDoubleProcessor
import com.adl.mount.sport.TsSportChannel
import com.ty.bus.multi.core.callback.DoubleBaseCallback
import com.ty.bus.multi.core.config.DoubleSportConfig
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.factory.DoubleSportDetector
import com.ty.bus.multi.databinding.ActivityDoubleSportBinding
import com.ty.tybusbase.base.BaseDoubleSportIdentifyActivity
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.SoundPlayUtils
import com.ty.tybusbase.view.SportComboAnimal
import com.ty.tybusbase.view.SportLevelAnimation
import java.io.File

class DoubleSportActivity :
    BaseDoubleSportIdentifyActivity<ActivityDoubleSportBinding, DoubleSportViewModel>() {

    private var createTime: Long = 0
    private lateinit var videoFile: File
    private var leftComboAnimal: SportComboAnimal? = null
    private var rightComboAnimal: SportComboAnimal? = null

    private var leftLevelAnimal: SportLevelAnimation? = null
    private var rightLevelAnimal: SportLevelAnimation? = null

    private var upLeftComboTime: Long = 0
    private var upRightComboTime: Long = 0

    private var doubleBaseCallback: DoubleBaseCallback = object : DoubleBaseCallback() {
        override fun onLeftCompleteOnce(result: SportDetectResult) {
            runOnUiThread {
                onLeftComplete(result)
            }
        }

        override fun onRightCompleteOnce(result: SportDetectResult) {
            runOnUiThread {
                onRightComplete(result)
            }
        }

        override fun onChannel(left: TsSportChannel?, right: TsSportChannel?) {
            runOnUiThread {
                checkArea(left, right)
            }
        }

    }

    override fun initData() {
        super.initData()
        viewModel.doubleSportConfig =
            intent.getSerializableExtra("sport_config") as? DoubleSportConfig
    }

    override fun createTsDoubleProcessor(): TsDoubleProcessor {
        return DoubleSportDetector(viewModel.getSportCode(), doubleBaseCallback)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.viewSportResult.setResultBtnListener { finish() }
        lifecycle.addObserver(mBinding.surfaceOverlay)
    }

    fun configSurfaceOverlay() {
        leftLevelAnimal = SportLevelAnimation(100, 450, 150)
        rightLevelAnimal = SportLevelAnimation(100, 1400, 150)
        leftComboAnimal = SportComboAnimal(60, 100, 50, 500, true)
        rightComboAnimal = SportComboAnimal(60, 100, 960 + 50, 500, false)
        mBinding.surfaceOverlay.subscribe(leftLevelAnimal)
        mBinding.surfaceOverlay.subscribe(rightLevelAnimal)
        mBinding.surfaceOverlay.subscribe(leftComboAnimal)
        mBinding.surfaceOverlay.subscribe(rightComboAnimal)
        mBinding.surfaceOverlay.start()
    }

    override fun onStatusIdentifySuccess() {
        super.onStatusIdentifySuccess()
        //识别成功
        mBinding.overLayout.setLeftPlayer(viewModel.leftAccountInfo.value)
        mBinding.overLayout.setRightPlayer(viewModel.rightAccountInfo.value)
        viewModel.leftSportResult.userId = viewModel.leftAccountInfo.value?.studentEntity?.accountId
        viewModel.rightSportResult.userId =
            viewModel.rightAccountInfo.value?.studentEntity?.accountId
        viewModel.leftSportResult.cardType =
            viewModel.leftAccountInfo.value?.studentEntity?.cardType ?: ""
        viewModel.rightSportResult.cardType =
            viewModel.rightAccountInfo.value?.studentEntity?.cardType ?: ""
        viewModel.leftSportResult.energyPoint = viewModel.energyPoint
        viewModel.rightSportResult.energyPoint = viewModel.energyPoint
    }

    override fun onStatusSport() {
        super.onStatusSport()
        mBinding.overLayout.visibility = View.VISIBLE
        mBinding.overLayout.setSportName(viewModel.sportInfo!!.sportSkuName ?: "--")

//        if (asyncPlayer == null && sportInfo.sportBgmUrl != null) {
//            asyncPlayer = AsyncPlayer("AsyncPlayer")
//            asyncPlayer!!.play(
//                AppApplication.getInstance(),
//                Uri.parse(sportInfo.sportBgmUrl),
//                true,
//                AudioManager.STREAM_MUSIC
//            )
//        }
//        vm.statusLD.value = vm.STATUS_SPORT

        //todo 录像
        videoFile = viewModel.getVideoPath()
        createTime = System.currentTimeMillis()
        viewModel.sportEngine.startRecorder(videoFile)
        //配置overlay动画
        Thread {
            configSurfaceOverlay()
        }.start()
    }

    @SuppressLint("ImplicitSamInstance")
    override fun onStatusEnd() {
        super.onStatusEnd()
        SoundPlayUtils.play(2)
        //如果配置了自动结束
        viewModel.doubleSportConfig?.autoFinishTime?.let {
            if (it > 0) {
                AdlHandlerThread.getUIHandler().postDelayed({ finish() }, it)
            }
        }
        LogUtil.d("---运动结束了")
        mBinding.viewSportResult.setResult(
            viewModel.rightSportResult,
            viewModel.leftSportResult,
            viewModel.energyPoint
        )
        val multiId = System.currentTimeMillis().toString()
        viewModel.rightSportResult.multiPersonId = multiId
        viewModel.leftSportResult.multiPersonId = multiId


        mBinding.surfaceOverlay.stop()
        mBinding.surfaceOverlay.visibility = View.GONE
        mBinding.overLayout.visibility = View.GONE
        mBinding.groupResult.visibility = View.VISIBLE

        //todo 停止录像
        viewModel.sportEngine.stopRecorder {
            viewModel.uploadSportResult(
                SceneConstants.PK,
                viewModel.rightSportResult,
                viewModel.rightAccountInfo.value?.studentEntity?.accountId
            )
            viewModel.uploadSportResult(
                SceneConstants.PK,
                viewModel.leftSportResult,
                viewModel.leftAccountInfo.value?.studentEntity?.accountId
            )
        }
    }

    override fun refreshTime(time: Long) {
        mBinding.overLayout.refreshTime(time)
    }

    override fun warningTime() {
        mBinding.overLayout.warnTime()
    }

    private fun onLeftComplete(result: SportDetectResult) {
        viewModel.refreshLeftValue(result.sportAchievement, result.singleSportLevel)
        leftLevelAnimal?.setLevel(result.singleSportLevel)
        //播放计数音效
        SoundPlayUtils.play(1)
        if (System.currentTimeMillis() - upLeftComboTime < 2000) {
            leftComboAnimal?.combo()
        } else {
            val i: Int = leftComboAnimal?.comboInterrupt() ?: 0
            viewModel.setLeftComboCount(i)
        }
        upLeftComboTime = System.currentTimeMillis()
        if (viewModel.getEndType() == SportConstant.PATTERN_LIMIT_COUNT && viewModel.getEndValue() == viewModel.leftValue.toLong()) {
            if (viewModel.rightValue.toLong() == viewModel.getEndValue()) {
                viewModel.toStatusSportEnd()
            }
        }
        mBinding.overLayout.post { mBinding.overLayout.setLeftValue(viewModel.leftValue) }
    }

    private fun onRightComplete(result: SportDetectResult) {
        viewModel.refreshRightValue(result.sportAchievement, result.singleSportLevel)
        rightLevelAnimal?.setLevel(result.singleSportLevel)
        //播放计数音效
        SoundPlayUtils.play(1)
        if (System.currentTimeMillis() - upRightComboTime < 2000) {
            rightComboAnimal?.combo()
        } else {
            val i: Int = rightComboAnimal?.comboInterrupt() ?: 0
            viewModel.setRightComboCount(i)
        }
        upRightComboTime = System.currentTimeMillis()
        if (viewModel.getEndType() == SportConstant.PATTERN_LIMIT_COUNT && viewModel.getEndValue() == viewModel.rightValue.toLong()) {
            if (viewModel.leftValue.toLong() == viewModel.getEndValue()) {
                viewModel.toStatusSportEnd()
            }
        }
        mBinding.overLayout.post { mBinding.overLayout.setRightValue(viewModel.rightValue) }
    }

    private fun checkArea(left: TsSportChannel?, right: TsSportChannel?) {
        mBinding.overLayout.checkLeftArea(left?.skeleton, viewModel.sportInfo!!.sportItemCode)
        mBinding.overLayout.checkRightArea(right?.skeleton, viewModel.sportInfo!!.sportItemCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.surfaceOverlay.stop()
        AdlHandlerThread.getUIHandler().removeCallbacksAndMessages(null)
    }

}