package com.ty.bus.multi.core.view.common

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.ISportCardView
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.databinding.FourSportCardViewBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.TypeFaceHelper
import com.ty.tybusbase.utils.LogUtil

/**
 * 四人运动运动页面
 */
class FourSportCardSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : LinearLayout(context, attrs,
    defStyleAttr), ISportCardView {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val yousheFont = TypeFaceHelper.instance?.getYouSheFont(context)

    private lateinit var accountInfo: AccountInfo

    private val mBinding = FourSportCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    //上一次的状态
    private var preState = MultiSportCardViewState.IDLE

    private var isDebug = false

    //点号动画
    private var dotCount = 0
    private var dotsRunning = false
    private val dotsTimer = object : CountDownTimer(Long.MAX_VALUE, 500) {
        override fun onTick(millisUntilFinished: Long) {
            dotCount = (dotCount % 3) + 1
            mBinding.tvNotice.text = "识别中${".".repeat(dotCount)}"
        }

        override fun onFinish() {}
    }

    init {
        mBinding.tvNumber.typeface = yousheFont
        mBinding.tvPrepared.typeface = yousheFont
        mBinding.tvNobodySport.typeface = yousheFont

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.FourSportCardSportView)
        val index = attributes.getInt(R.styleable.FourSportCardSportView_sport_index, 1)
        mBinding.tvNumber.text = index.toString()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        stopDots()
//        identifyingTimer.cancel()
    }

    /**
     * 设置卡片Number
     */
    override fun setNumber(number: String) {
        mBinding.tvNumber.text = number
    }

    override fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    /**
     * 更新成绩
     */
    override fun updateScore(score: Int) {
//        if (preState != MultiSportCardViewState.SPORTING && preState != MultiSportCardViewState.INTERFERENCE){
//            LogUtil.d("updateScore error state:${preState.name}")
//            return
//        }
        if (score.toString() != mBinding.tvCount.text) {
            mBinding.tvCount.text = score.toString()
        }
    }

    /**
     * 设置用户信息
     */
    override fun setAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        setUserInfo(accountInfo.studentEntity!!.accountName, accountInfo.studentEntity!!.userFaceImgUrl)
        if (accountInfo.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
            mBinding.ivHeaderBg.visible()
        }
    }

    override fun setPrecastAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        mBinding.tvPrepared.text = accountInfo.studentEntity!!.accountName
    }

    /**
     * 设置时间
     */
    override fun setTime(time: String) {
    }

    override fun setTimeBackground(res: Int) {
    }

    override fun reset() {
        mBinding.tvCount.gone()
        mBinding.tvUnit.gone()
        mBinding.tvLeaveNotice.gone()
        mBinding.ivHeaderBg2.gone()
        mBinding.tvCount.text = "0"
        changeState(MultiSportCardViewState.NOBODY)
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
            MultiSportCardViewState.NOBODY -> {
                exitNobody()
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
                exitPrepared()
            }

            MultiSportCardViewState.SPORTING -> {
                exitSporting()
            }

            MultiSportCardViewState.LEAVE -> {
                exitSportingToLeave()
            }

            MultiSportCardViewState.INTERFERENCE -> {
                exitSportingToInterference()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                exitIdentifyFail()
            }

            MultiSportCardViewState.LEAVE_TO_SPORTING -> {
                exitLeaveToSporting()
            }

            MultiSportCardViewState.PRECAST_PERSON -> {
                exitPrecastPerson()
            }

            MultiSportCardViewState.PRECAST_PREPARE -> {
                exitPrecastPrepare()
            }

            else -> {}
        }

        when (state) {
            MultiSportCardViewState.NOBODY -> {
                enterNobody()
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
                enterPrepared()
            }

            MultiSportCardViewState.SPORTING -> {
                enterSporting()
            }

            MultiSportCardViewState.LEAVE -> {
                enterSportingToLeave()
            }

            MultiSportCardViewState.INTERFERENCE -> {
                enterSportingToInterference()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                enterIdentifyFail()
            }

            MultiSportCardViewState.LEAVE_TO_SPORTING -> {
                enterLeaveToSporting()
            }

            MultiSportCardViewState.PRECAST_PERSON -> {
                enterPrecastPerson()
            }

            MultiSportCardViewState.PRECAST_PREPARE -> {
                enterPrecastPrepare()
            }

            else -> {

            }
        }
        preState = state
    }

    override fun setUserInfo(name: String, headUrl: String?) {
        Glide.with(context)
            .load(headUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default)
            .into(mBinding.ivHeader)
        mBinding.tvName.text = name
        mBinding.tvPresetName.text = name
    }

    private fun exitNobody() {
    }

    /**
     * 切换到没人状态
     * 可从，standby，identifying,prepared,切换过来
     */
    fun enterNobody() {
        mBinding.tvNotice.visible()
        mBinding.tvNotice.text = getString(R.string.prompt_move_to_sport_area)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvSkipState.visible()
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_nobody_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.groupSport.gone()
        //已准备

        mBinding.ivHeaderBg.gone()
        stopDots()
    }

    private fun exitStandby() {
    }

    /**
     * 切换到举手准备状态
     */
    fun enterStandby() {
        //只有NOBODY和IDENTIFYING状态才能切换到STANDBY状态
//        if (preState != MultiSportCardViewState.NOBODY && preState != MultiSportCardViewState.IDENTIFYING) return
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_ready_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvNotice.text = getString(R.string.prompt_get_ready_by_raising_hand)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white))
        mBinding.tvNotice.visible()
        mBinding.tvSkipState.visibility = GONE
        stopDots()
    }

    private fun exitIdentifying() {
        //识别中
//        identifyingTimer.cancel()
//        mBinding.rlIdentityNotice.gone()
    }

    /**
     * 切换到识别中，由准备 ==》识别中
     */
    fun enterIdentifying() {
        startDots()
    }

    private fun exitNobodySport() {
    }

    /**
     * 切换到比赛开始，但是该位置无人状态，由Nobody ==> NobodySport
     */
    fun enterNobodySport() {
        //隐藏空闲状态提示
        stopDots()
        mBinding.tvSkipState.visibility = GONE
        mBinding.tvNotice.text = getString(R.string.prompt_join_next_group_later)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvNumber.setTextColor(resources.getColor(R.color.white_60))
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_start_nobody_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
    }

    private fun exitPrepared() {
        mBinding.groupPrepared.gone()
        mBinding.ivHeaderBg.gone()
        mBinding.tvNotice.gone()
    }

    /**
     * 切换到识别已准备状态
     */
    fun enterPrepared() {
        mBinding.groupPrepared.visibility = VISIBLE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvNotice.text = getString(R.string.prompt_waiting_for_players)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvSkipState.visibility = GONE
        mBinding.ivShadow.visibility = VISIBLE
        stopDots()
//        identifyingTimer.cancel()
    }

    private fun exitSporting() {
    }

    /**
     * 开始运动状态（含离区后回区：清离开文案并恢复计数）
     */
    fun enterSporting() {
        mBinding.groupSport.visible()
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvLeaveNotice.gone()
        mBinding.tvCount.visibility = VISIBLE
        mBinding.tvUnit.visibility = VISIBLE
        if (::accountInfo.isInitialized) {
            mBinding.tvNameSport.text = accountInfo.studentEntity!!.accountName
            Glide.with(context)
                .load(accountInfo.studentEntity!!.userFaceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default)
                .into(mBinding.civHeader)
            if (accountInfo.studentEntity!!.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
                mBinding.ivHeaderBg2.visible()
            }
        }
    }

    private fun exitSportingToLeave() {
    }

    /**
     * 运动中切换到离开状态
     */
    fun enterSportingToLeave() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_start_warn_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvLeaveNotice.visibility = VISIBLE
        //隐藏运动中的计数
        mBinding.tvCount.visibility = GONE
        mBinding.tvUnit.visibility = GONE
    }

    private fun exitSportingToInterference() {
    }

    /**
     * 切换到干扰模式
     */
    fun enterSportingToInterference() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already_interference_more)
    }

    private fun exitIdentifyFail() {
        exitStandby()
    }

    /**
     * 切换到识别失败
     */
    fun enterIdentifyFail() {
        enterStandby()
    }

    private fun exitLeaveToSporting() {
    }

    /**
     * 离开状态切换回运动中状态
     */
    fun enterLeaveToSporting() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvLeaveNotice.gone()
        //显示运动中的计数
        mBinding.tvCount.visibility = VISIBLE
        mBinding.tvUnit.visibility = VISIBLE
    }

    private fun exitPrecastPerson() {
        mBinding.tvPresetName.gone()
    }

    fun enterPrecastPerson() {
        mBinding.tvNotice.text = getString(R.string.prompt_move_to_sport_area)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvSkipState.visibility = GONE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_nobody_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvPresetName.visible()
        stopDots()
    }

    private fun exitPrecastPrepare() {
        mBinding.tvPresetName.gone()
    }

    fun enterPrecastPrepare() {
        mBinding.tvPresetName.visible()
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_ready_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
        mBinding.tvNotice.text = getString(R.string.prompt_ready_position)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white))
        mBinding.tvSkipState.visibility = GONE
        stopDots()
    }

    fun interferenceToSporting() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already_four)
        if (isDebug) {
            mBinding.ivBg.alpha = 0.6f
        }
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

}