package com.ty.bus.multi.core.view.sixsport

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.base.MultiSportState
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.databinding.ItemForwardFlexionSportScoreBinding
import com.ty.bus.multi.databinding.ViewForwardFlexionSportCardBinding
import com.ty.bus.multi.databinding.ViewSixSportSportCardBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.utils.LogUtil


@SuppressLint("CustomViewStyleable", "Recycle")
class SixSportSportCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewSixSportSportCardBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var scoreList: MutableList<Int> = mutableListOf()

    private var preState: MultiSportCardViewState = MultiSportCardViewState.IDLE

    private var errorMap = mutableMapOf<Int, Pair<Int, TextView>>()  //错误信息map

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

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MoreSportCardSportView)
        val index = attributes.getInt(R.styleable.MoreSportCardSportView_pk_index, 1)
        mBinding.tvIndex.text = index.toString()
        mBinding.tvSportingIndex.text = index.toString()
        initView()
    }

    /**
     * 切换状态
     */
    fun changeState(state: MultiSportCardViewState) {
        if (preState == state) {
            LogUtil.d("SixSportSportCardView状态未改变")
            return
        }
        LogUtil.d("SixSportSportCardView ${mBinding.tvIndex.text} 状态切换: $preState -> $state")
        //退出上一状态
        when (preState) {
            MultiSportCardViewState.IDLE -> {
                exitIdle()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                exitIdentifying()
            }

            MultiSportCardViewState.IDENTIFIED -> {
                exitIdentified()
            }

            MultiSportCardViewState.PREPARED -> {
                exitPrepared()
            }

            MultiSportCardViewState.SPORTING -> {
                exitSporting()
            }

            MultiSportCardViewState.FINISH -> {
                exitFinished()
            }

            MultiSportCardViewState.NOBODY_SPORT -> {
                exitSportingNoUser()
            }

            else -> {
                LogUtil.d("SixSportSportCardView状态未处理")
            }
        }

        //进入新状态处理
        when (state) {
            MultiSportCardViewState.IDLE -> {
                enterIdle()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                enterIdentifying()
            }

            MultiSportCardViewState.IDENTIFIED -> {
                enterIdentified()
            }

            MultiSportCardViewState.PREPARED -> {
                enterPrepared()
            }

            MultiSportCardViewState.SPORTING -> {
                enterSporting()
            }

            MultiSportCardViewState.FINISH -> {
                enterFinished()
            }

            MultiSportCardViewState.NOBODY_SPORT -> {
                enterSportingNoUser()
            }

            else -> {
                LogUtil.d("SixSportSportCardView状态未处理")
            }
        }

        preState = state
    }

    private fun enterSportingNoUser() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_testing_empty)
        mBinding.groupStartNoUser.visibility = VISIBLE
    }

    private fun exitSportingNoUser() {
        mBinding.groupStartNoUser.visibility = GONE
    }

    private fun enterIdle() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_nouser)
        mBinding.groupIdle.visibility = VISIBLE
    }

    private fun exitIdle() {
        mBinding.groupIdle.visibility = GONE
    }

    private fun enterIdentifying() {
        tickTimes = 0f
        identifyingTimer.start()
        mBinding.groupIdentifying.visibility = VISIBLE
    }

    private fun exitIdentifying() {
        identifyingTimer.cancel()
        mBinding.groupIdentifying.visibility = GONE
    }

    private fun enterIdentified() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_ready)
        mBinding.groupAccountInfo.visibility = VISIBLE
        mBinding.groupIdentified.visibility = VISIBLE
    }

    private fun exitIdentified() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_ready)
        mBinding.groupAccountInfo.visibility = GONE
        mBinding.groupIdentified.visibility = GONE
    }

    private fun enterPrepared() {
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_ready)
        mBinding.groupAccountInfo.visibility = VISIBLE
        mBinding.groupPrepared.visibility = VISIBLE
    }

    private fun exitPrepared() {
        mBinding.groupAccountInfo.visibility = GONE
        mBinding.groupPrepared.visibility = GONE
    }

    private fun enterSporting() {
        mBinding.ivSportArrow.gone()
        mBinding.groupResult.visible()
        mBinding.groupSport.visible()
    }

    private fun exitSporting() {
        mBinding.groupResult.gone()
        mBinding.groupSport.gone()
    }

    private fun enterFinished() {
        mBinding.groupResult.visible()
        mBinding.ivSportArrow.visible()
        mBinding.tvSportResult.text = mBinding.tvSportingResult.text
    }

    private fun exitFinished() {
        mBinding.groupResult.gone()
        mBinding.ivSportArrow.gone()
        mBinding.tvError1.gone()
        mBinding.tvError2.gone()
        mBinding.tvError3.gone()
        mBinding.viewLine1.gone()
        mBinding.viewLine2.gone()
    }

    fun setAccountInfo(accountInfo: AccountInfo) {
        Glide.with(this)
            .load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        mBinding.tvName.text = accountInfo.studentEntity?.accountName

        Glide.with(this)
            .load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.civSportingResultHeader)
        mBinding.tvSportingResultName.text = accountInfo.studentEntity?.accountName

        Glide.with(this)
            .load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.civResultHeader)
        mBinding.tvResultName.text = accountInfo.studentEntity?.accountName

    }

    fun updateResult(result: Int) {
        mBinding.tvSportingResult.text = result.toString()
    }

    /**
     * 添加错误信息
     */
    @SuppressLint("SetTextI18n")
    fun addErrorMsg(code: Int, msg: String?) {
        if (preState != MultiSportCardViewState.SPORTING) return
        var pair = errorMap.getOrDefault(code, null)
        if (pair == null) {
            when (errorMap.size) {
                0 -> {
                    pair = 1 to mBinding.tvError1
                    mBinding.tvError1.visible()
                }

                1 -> {
                    pair = 1 to mBinding.tvError2
                    mBinding.tvError2.visible()
                    mBinding.viewLine1.visible()
                }

                2 -> {
                    pair = 1 to mBinding.tvError3
                    mBinding.tvError3.visible()
                    mBinding.viewLine2.visible()
                }
            }
            errorMap.put(code, pair!!)
        }
        pair.second.text = "${msg}: ${pair.first + 1}次"
    }

    private fun initView() {
    }

    fun reset() {
        tickTimes = 0f
        scoreList.clear()
        changeState(MultiSportCardViewState.IDLE)
    }

    fun retest() {
//        changeState(ForwardFlexionCardState.SPORTING)
    }
}