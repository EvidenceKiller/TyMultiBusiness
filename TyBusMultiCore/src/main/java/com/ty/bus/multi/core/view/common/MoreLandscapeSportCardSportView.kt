package com.ty.bus.multi.core.view.common

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.ISportCardView
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.databinding.ViewMoreLandscapeSportCardBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil

class MoreLandscapeSportCardSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr), ISportCardView {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewMoreLandscapeSportCardBinding =
        ViewMoreLandscapeSportCardBinding.inflate(LayoutInflater.from(context), this, true)

    //上一次的状态
    private var preState = MultiSportCardViewState.IDLE

    private lateinit var accountInfo: AccountInfo

    //点号动画
    private var dotCount = 0
    private var dotsRunning = false
    private val dotsTimer = object : CountDownTimer(Long.MAX_VALUE, 500) {
        override fun onTick(millisUntilFinished: Long) {
            dotCount = (dotCount % 3) + 1
            mBinding.tvIdentifying.text = "识别中${".".repeat(dotCount)}"
        }

        override fun onFinish() {}
    }

    init {
        mBinding.tvNumber.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        mBinding.tvPrepared.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)

        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MoreSportCardSportView)
        val index = attributes.getInt(R.styleable.MoreSportCardSportView_pk_index, 1)
        mBinding.tvNumber.text = index.toString()
        mBinding.tvSportIndex.text = index.toString()
    }


    private fun startDots() {
        if (dotsRunning) return
        dotsRunning = true
        dotCount = 0
        dotsTimer.start()
    }

    private fun stopDots() {
        dotsTimer.cancel()
        dotsRunning = false
        dotCount = 0
    }

    override fun setNumber(number: String) {
        mBinding.tvNumber.text = number
    }

    override fun setDebug(debug: Boolean) {
    }


    /**
     * 切换状态
     */
    override fun changeState(state: MultiSportCardViewState) {
        LogUtil.d("changeState:道次：${mBinding.tvNumber.text} ${state.name}")
        if (state == preState) {
            LogUtil.d("MoreSportCardSportView changeState 状态未改变")
            return
        }
        when (preState) {
            MultiSportCardViewState.IDLE -> {
                exitIdle()
            }

            MultiSportCardViewState.STANDBY -> {
                exitStandby()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                exitIdentifying()
            }

            MultiSportCardViewState.NOBODY_SPORT -> {
                exitNobodySport()
            }

            MultiSportCardViewState.PREPARED -> {
                exitPrepared()
            }

            MultiSportCardViewState.IDENTIFIED -> {
                exitIdentified()
            }

            MultiSportCardViewState.SPORTING -> {
                exitSporting()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                exitIdentifyFail()
            }

            MultiSportCardViewState.FINISH -> {
                exitFinish()
            }

            else -> {}
        }

        when (state) {
            MultiSportCardViewState.IDLE -> {
                enterIdle()
            }

            MultiSportCardViewState.STANDBY -> {
                enterStandby()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                enterIdentifying()
            }

            MultiSportCardViewState.NOBODY_SPORT -> {
                enterNobodySport()
            }

            MultiSportCardViewState.PREPARED -> {
                enterPrepared()
            }

            MultiSportCardViewState.IDENTIFIED -> {
                enterIdentified()
            }

            MultiSportCardViewState.SPORTING -> {
                enterSporting()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                enterIdentifyFail()
            }

            MultiSportCardViewState.FINISH -> {
                enterFinish()
            }

            else -> {

            }
        }
        preState = state
    }

    override fun setUserInfo(name: String, headUrl: String?) {
        Glide.with(context).load(headUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)
        mBinding.tvName.text = name
    }

    private fun exitIdle() {
        mBinding.groupIdle.gone()
    }

    /**
     * 切换到没人状态
     * 可从，standby，identifying,prepared,切换过来
     */
    fun enterIdle() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_empty)
        mBinding.groupIdle.visible()
        stopDots()
    }

    private fun exitStandby() {
        mBinding.groupIdentifyTip.gone()
    }

    /**
     * 切换到举手准备状态
     */
    fun enterStandby() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_ready_nouser)
        mBinding.groupIdentifyTip.visible()
    }

    private fun exitIdentifying() {
        mBinding.groupIdentifying.gone()
    }

    /**
     * 切换到识别中，由准备 ==》识别中
     */
    fun enterIdentifying() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_ready_identifying)
        mBinding.groupIdentifying.visible()
        startDots()
    }

    private fun exitPrepared() {
        mBinding.groupPrepared.gone()
        mBinding.groupAccountInfo.gone()
    }

    /**
     * 切换到识别已准备状态
     */
    fun enterPrepared() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_completed)
        mBinding.groupAccountInfo.visible()
        mBinding.groupPrepared.visible()
        stopDots()
    }

    private fun exitIdentified() {
        mBinding.groupIdentified.gone()
        mBinding.groupAccountInfo.gone()
    }

    /**
     * 切换到识别已准备状态
     */
    fun enterIdentified() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_completed)
        mBinding.groupAccountInfo.visible()
        mBinding.groupIdentified.visible()
        stopDots()
    }

    /**
     * 设置用户信息
     */
    override fun setAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        setUserInfo(
            accountInfo.studentEntity!!.accountName,
            accountInfo.studentEntity!!.userFaceImgUrl
        )
//        if (accountInfo.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
//            mBinding.ivHeaderBg.visible()
//        }
    }

    override fun setPrecastAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        mBinding.tvPrepared.text = accountInfo.studentEntity!!.accountName
    }

    private fun exitSporting() {
        mBinding.groupSport.gone()
        mBinding.groupScore.gone()
    }

    /**
     * 开始运动状态
     */
    fun enterSporting() {
        mBinding.groupSport.visible()
        mBinding.groupScore.visible()
        if (::accountInfo.isInitialized) {
            mBinding.tvSportName.text = accountInfo.studentEntity!!.accountName
            Glide.with(context).load(accountInfo.studentEntity!!.userFaceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.ivSportHeader)
//            if (accountInfo.studentEntity!!.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
//                mBinding.ivHeaderBg2.visible()
//            }
        }
    }

    private fun exitFinish() {
        mBinding.groupScore.gone()
        mBinding.groupAccountInfo.gone()
    }

    private fun enterFinish() {
        mBinding.groupAccountInfo.visible()
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_more_landscape_card_completed)
        mBinding.groupScore.visible()
    }

    private fun exitNobodySport() {

    }


    /**
     * 切换到比赛开始，但是该位置无人状态，由Nobody ==> NobodySport
     */
    fun enterNobodySport() {
        //隐藏空闲状态提示
        enterIdle()
        stopDots()
    }

    private fun exitIdentifyFail() {
    }

    /**
     * 切换到识别失败
     */
    fun enterIdentifyFail() {
        enterStandby()
    }

    /**
     * 更新成绩
     */
    override fun updateScore(score: Int) {
        if (score.toString() != mBinding.tvScore.text) {
            mBinding.tvScore.text = score.toString()
        }
    }

    /**
     * 设置时间
     */
    override fun setTime(time: String) {
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        stopDots()
    }

    override fun setTimeBackground(res: Int) {
    }

    override fun reset() {
        mBinding.tvScore.gone()
        mBinding.tvUnit.gone()
        mBinding.tvScore.text = "0"
        changeState(MultiSportCardViewState.IDLE)
    }

}