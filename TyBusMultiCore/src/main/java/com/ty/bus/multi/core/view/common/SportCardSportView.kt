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
import com.ty.bus.multi.databinding.ViewSkipRoseCardViewBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.helper.TypeFaceHelper

/**
 */
class SportCardSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayout(context, attrs, defStyleAttr), ISportCardView {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private lateinit var accountInfo: AccountInfo

    private val mBinding: ViewSkipRoseCardViewBinding
    private var state: MultiSportCardViewState = MultiSportCardViewState.NOBODY

    init {
        var view =
            LayoutInflater.from(context).inflate(R.layout.view_skip_rose_card_view, this, true)
        mBinding = ViewSkipRoseCardViewBinding.bind(view)
        mBinding.tvNumber.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        mBinding.tvPrepared.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
    }

    /**
     * 进度条假增
     */
    private val countDownTime = 1000L
    private var tickTimes = 0f
    private var totalTimes = 20f
    private val identifyingTimer = object : CountDownTimer(
        countDownTime,
        (countDownTime / totalTimes).toLong()
    ) {
        override fun onTick(millisUntilFinished: Long) {
            tickTimes++
            mBinding.progressView.setProgress(tickTimes * 100 / totalTimes)
        }

        override fun onFinish() {
            mBinding.progressView.setProgress(100f)
        }
    }

    /**
     * 设置卡片Number
     */
    override fun setNumber(number: String) {
        mBinding.tvNumber.text = number
    }

    /**
     * 切换状态
     */
    override fun changeState(state: MultiSportCardViewState) {
        when (state) {
            MultiSportCardViewState.NOBODY -> {
                toNobody()
            }

            MultiSportCardViewState.STANDBY -> {
                toStandby()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                toIdentifying()
            }

            MultiSportCardViewState.NOBODY_SPORT -> {
                toNobodySport()
            }

            MultiSportCardViewState.PREPARED -> {
                toPrepared()
            }

            MultiSportCardViewState.IDENTIFIED -> {
                toPrepared()
            }

            MultiSportCardViewState.SPORTING -> {
                toSporting()
            }

            MultiSportCardViewState.LEAVE -> {
                sportingToLeave()
            }

            MultiSportCardViewState.INTERFERENCE -> {
                sportingToInterference()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                toIdentifyFail()
            }

            MultiSportCardViewState.LEAVE_TO_SPORTING -> {
                leaveToSporting()
            }

            MultiSportCardViewState.PRECAST_PERSON -> {
                toPrecastPerson()
            }

            MultiSportCardViewState.PRECAST_PREPARE -> {
                toPrecastPrepare()
            }

            else -> {}


        }
    }

    override fun setUserInfo(name: String, headUrl: String?) {
        Glide.with(context).load(headUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)

        mBinding.tvName.text = name
    }

    /**
     * 切换到没人状态
     * 可从，standby，identifying,prepared,切换过来
     */
    override fun toNobody() {
        state = MultiSportCardViewState.NOBODY
        mBinding.tvNotice.text = getString(R.string.prompt_move_to_sport_area)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvSkipState.visibility = VISIBLE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_nobody)
        mBinding.tvLeaveNotice.visibility = GONE
        mBinding.tvCount.text = "0"
        setTimeBackground(R.drawable.pk_skip_rose_time_shape)
        //识别中
        mBinding.rlIdentityNotice.visibility = GONE

        //已准备
        mBinding.groupPrepared.visibility = GONE

        mBinding.clGr.visibility = INVISIBLE

    }

    /**
     * 切换到举手准备状态
     */
    override fun toStandby() {
        //只有NOBODY和IDENTIFYING状态才能切换到STANDBY状态
        if (state != MultiSportCardViewState.NOBODY && state != MultiSportCardViewState.IDENTIFYING) return
        state = MultiSportCardViewState.STANDBY
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_ready)
        mBinding.tvNotice.text = getString(R.string.prompt_get_ready_by_raising_hand)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white))
        mBinding.tvSkipState.visibility = GONE
        mBinding.rlIdentityNotice.visibility = GONE
        identifyingTimer.cancel()
    }

    /**
     * 切换到识别中，由准备 ==》识别中
     */
    override fun toIdentifying() {
        state = MultiSportCardViewState.IDENTIFYING
        mBinding.rlIdentityNotice.visibility = VISIBLE
        mBinding.progressView.setProgress(10f)
        tickTimes = 0f
        identifyingTimer.start()
        mBinding.clGr.visibility = INVISIBLE
    }

    /**
     * 切换到，已准备状态，由识别中 ==》已准备（游客或已识别到人）
     */
    override fun toPrepared(accountInfo: AccountInfo) {
        state = MultiSportCardViewState.PREPARED
        //设置头像和名字
        mBinding.groupPrepared.visibility = VISIBLE
        mBinding.tvName.text = accountInfo.studentEntity?.accountName
        Glide.with(context.applicationContext).load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)
        //更换背景
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already)
        //设置提示
        mBinding.tvNotice.text =
            getString(R.string.prompt_waiting_for_players)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.rlIdentityNotice.visibility = GONE
        mBinding.tvSkipState.visibility = GONE
        identifyingTimer.cancel()
    }

    /**
     * 切换到识别已准备状态
     */
    override fun toPrepared() {
        state = MultiSportCardViewState.PREPARED
        //设置头像和名字
        mBinding.groupPrepared.visibility = VISIBLE
        mBinding.tvCount.text = "0"
        //更换背景
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already)
        //设置提示
        mBinding.tvNotice.text =
            getString(R.string.prompt_waiting_for_players)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.rlIdentityNotice.visibility = GONE
        mBinding.tvSkipState.visibility = GONE
        identifyingTimer.cancel()
    }

    override fun toPrecastPrepare() {
        state = MultiSportCardViewState.PRECAST_PREPARE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_ready)
        mBinding.tvNotice.text = getString(R.string.prompt_ready_position)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white))
        mBinding.tvSkipState.visibility = GONE
        mBinding.rlIdentityNotice.visibility = GONE
    }

    override fun toPrecastPerson() {
        state = MultiSportCardViewState.PRECAST_PERSON
        mBinding.tvNotice.text =
            getString(R.string.prompt_move_to_sport_area)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvSkipState.visibility = GONE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_nobody)

        //识别中
        mBinding.rlIdentityNotice.visibility = GONE

        //已准备
        mBinding.groupPrepared.visibility = VISIBLE

        mBinding.clGr.visibility = INVISIBLE
    }

    /**
     * 设置用户信息
     */
    override fun setAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        mBinding.tvName.text = accountInfo.studentEntity?.accountName
        Glide.with(context.applicationContext).load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)
    }

    override fun setPrecastAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
        mBinding.tvPrepared.text = accountInfo.studentEntity?.accountName
    }

    /**
     * 开始运动状态
     */
    override fun toSporting() {
        state = MultiSportCardViewState.SPORTING
        mBinding.groupSport.visibility = VISIBLE
        if (::accountInfo.isInitialized) {
            mBinding.tvNameSport.text = accountInfo.teacherEntity?.accountName
            Glide.with(context.applicationContext).load(accountInfo.studentEntity?.userFaceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        }
        //隐藏准备中的UI
        mBinding.groupPrepared.visibility = GONE
        mBinding.tvNotice.visibility = GONE
        mBinding.tvLeaveNotice.visibility = GONE
    }

    /**
     * 切换到比赛开始，但是该位置无人状态，由Nobody ==> NobodySport
     */
    override fun toNobodySport() {
        state = MultiSportCardViewState.NOBODY_SPORT
        //隐藏空闲状态提示
        mBinding.tvSkipState.visibility = GONE
        mBinding.rlIdentityNotice.visibility = GONE
        mBinding.tvNotice.text =
            getString(R.string.prompt_join_next_group_later)
        mBinding.tvNotice.setTextColor(resources.getColor(R.color.white_60))
        mBinding.tvNumber.setTextColor(resources.getColor(R.color.white_60))
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_start_nobody)
        mBinding.clGr.visibility = INVISIBLE
    }


    /**
     * 运动中切换到离开状态
     */
    override fun sportingToLeave() {
        state = MultiSportCardViewState.LEAVE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_start_warn)
        mBinding.tvLeaveNotice.visibility = VISIBLE
        //隐藏运动中的计数
        mBinding.tvCount.visibility = GONE
        mBinding.tvUnit.visibility = GONE
        mBinding.clGr.visibility = INVISIBLE
    }

    /**
     * 离开状态切换回运动中状态
     */
    override fun leaveToSporting() {
        state = MultiSportCardViewState.SPORTING
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already)
        mBinding.tvLeaveNotice.visibility = GONE
        //显示运动中的计数
        mBinding.tvCount.visibility = VISIBLE
        mBinding.tvUnit.visibility = VISIBLE
    }

    /**
     * 切换到干扰模式
     */
    override fun sportingToInterference() {
        state = MultiSportCardViewState.INTERFERENCE
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_disturb)
        mBinding.clGr.visibility = VISIBLE
        mBinding.tvGr.text = getString(R.string.interference_detected)
    }

    override fun interferenceToSporting() {
        state = MultiSportCardViewState.SPORTING
        mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_already)
        mBinding.clGr.visibility = INVISIBLE
    }

    /**
     * 切换到识别失败
     */
    override fun toIdentifyFail() {
        toStandby()
        state = MultiSportCardViewState.IDENTIFY_FAIL
        mBinding.clGr.visibility = VISIBLE
        mBinding.tvGr.text = getString(R.string.rec_face_fail)
    }

    /**
     * 比赛开始，如果已识别到人 ==> toSporting,如果未识别到人 ==>toNobodySport
     */
    override fun toStarted() {
        if (state == MultiSportCardViewState.PREPARED) {
            toSporting()
        } else {
            toNobodySport()
        }

    }

    /**
     * 更新成绩
     */
    override fun updateScore(score: Int) {
        if (state != MultiSportCardViewState.SPORTING && state != MultiSportCardViewState.INTERFERENCE) return
        mBinding.tvCount.text = score.toString()
    }

    /**
     * 设置时间
     */
    override fun setTime(time: String) {
        if (state != MultiSportCardViewState.SPORTING && state != MultiSportCardViewState.LEAVE && state != MultiSportCardViewState.INTERFERENCE) return
        mBinding.tvTime.text = time
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
        identifyingTimer.cancel()
    }

    override fun setTimeBackground(res: Int) {
        mBinding.llTime.setBackgroundResource(res)
    }

    override fun reset() {
        toNobody()
    }

}