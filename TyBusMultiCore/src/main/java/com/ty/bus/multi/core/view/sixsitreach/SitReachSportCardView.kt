package com.ty.bus.multi.core.view.sixsitreach

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.databinding.ItemSitReachSportScoreBinding
import com.ty.bus.multi.databinding.ViewSitReachSportCardBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.utils.LogUtil


@SuppressLint("CustomViewStyleable", "Recycle")
class SitReachSportCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewSitReachSportCardBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var scoreList: MutableList<MultiSportResult> = mutableListOf()

    private var preState: ForwardFlexionCardState = ForwardFlexionCardState.IDLE

    private var bestScore = -1000 //最高分

    //点号动画
    private var dotCount = 0
    private var dotsRunning = false
    private val uiHandler = Handler(Looper.getMainLooper())
    private val dotsRunnable = object : Runnable {
        override fun run() {
            dotCount = (dotCount % 3) + 1
            mBinding.tvIdentifying.text = "识别中${".".repeat(dotCount)}"
            uiHandler.postDelayed(this, 500)
        }
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

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MoreSportCardSportView)
        val index = attributes.getInt(R.styleable.MoreSportCardSportView_pk_index, 1)
        mBinding.tvIndex.text = index.toString()
        initView()
    }

    /**
     * 切换状态
     */
    fun changeState(state: ForwardFlexionCardState) {
        if (preState == state) {
//            LogUtil.d("SitReachSportCardView状态未改变")
            return
        }
        LogUtil.d("SitReachSportCardView ${mBinding.tvIndex.text} 状态切换: $preState -> $state")
        //退出上一状态
        when (preState) {
            ForwardFlexionCardState.IDLE -> {
                exitIdle()
            }

            ForwardFlexionCardState.IDENTIFYING -> {
                exitIdentifying()
            }

            ForwardFlexionCardState.IDENTIFIED -> {
                exitIdentified()
            }

            ForwardFlexionCardState.PREPARED -> {
                exitPrepared()
            }

            ForwardFlexionCardState.FIRST_SPORTING -> {
                exitFirstSporting()
            }

            ForwardFlexionCardState.SPORTING -> {
                exitSporting()
            }

            ForwardFlexionCardState.RESULT_IN_SPORTING -> {
                exitResultInSporting()
            }

            ForwardFlexionCardState.CURRENT_SPORT_END -> {
                exitCurrentSportEnd()
            }

            ForwardFlexionCardState.FINISHED -> {
                exitFinished()
            }

            ForwardFlexionCardState.SPORTING_NO_USER -> {
                exitSportingNoUser()
            }

            ForwardFlexionCardState.IDENTIFY_FAIL -> {
                exitIdentifyFail()
            }

            ForwardFlexionCardState.UNAVAILABLE -> {
                exitUnavailable()
            }
        }

        //进入新状态处理
        when (state) {
            ForwardFlexionCardState.IDLE -> {
                enterIdle()
            }

            ForwardFlexionCardState.IDENTIFYING -> {
                enterIdentifying()
            }

            ForwardFlexionCardState.IDENTIFY_FAIL -> {
                enterIdentifyFail()
            }

            ForwardFlexionCardState.UNAVAILABLE -> {
                enterUnavailable()
            }

            ForwardFlexionCardState.IDENTIFIED -> {
                enterIdentified()
            }

            ForwardFlexionCardState.PREPARED -> {
                enterPrepared()
            }

            ForwardFlexionCardState.FIRST_SPORTING -> {
                enterFirstSporting()
            }

            ForwardFlexionCardState.SPORTING -> {
                enterSporting()
            }

            ForwardFlexionCardState.RESULT_IN_SPORTING -> {
                enterResultInSporting()
            }

            ForwardFlexionCardState.CURRENT_SPORT_END -> {
                enterCurrentSportEnd()
            }

            ForwardFlexionCardState.FINISHED -> {
                enterFinished()
            }

            ForwardFlexionCardState.SPORTING_NO_USER -> {
                enterSportingNoUser()
            }
        }

        preState = state
    }

    private fun enterUnavailable() {
        stopDots()
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_fl_card_ready_disabled)
        mBinding.llUnavailableTips.visibility = VISIBLE
    }

    private fun exitUnavailable() {
        mBinding.llUnavailableTips.visibility = GONE
    }

    private fun enterSportingNoUser() {
        stopDots()
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_testing_empty)
        mBinding.groupStartNoUser.visibility = VISIBLE
    }

    private fun exitSportingNoUser() {
        mBinding.groupStartNoUser.visibility = GONE
    }

    private fun enterIdle() {
        stopDots()
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_nouser)
        mBinding.groupIdle.visibility = VISIBLE
    }

    private fun exitIdle() {
        mBinding.groupIdle.visibility = GONE
    }

    private fun startDots() {
        if (dotsRunning) return
        dotsRunning = true
        dotCount = 0
        uiHandler.postDelayed(dotsRunnable, 500)
    }

    private fun stopDots() {
        uiHandler.removeCallbacks(dotsRunnable)
        dotsRunning = false
        dotCount = 0
    }

    private fun enterIdentifying() {
        tickTimes = 0f
//        identifyingTimer.start()
        mBinding.tvIdentifying.visibility = VISIBLE
        startDots()
    }

    private fun enterIdentifyFail() {
        tickTimes = 0f
//        identifyingTimer.start()
        mBinding.tvIdentifying.visibility = VISIBLE
        startDots()
    }

    private fun exitIdentifying() {
//        identifyingTimer.cancel()
        mBinding.tvIdentifying.visibility = GONE
    }

    private fun exitIdentifyFail() {
//        identifyingTimer.cancel()
        mBinding.tvIdentifying.visibility = GONE
    }

    private fun enterIdentified() {
        stopDots()
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
        stopDots()
        mBinding.ivBg.setBackgroundResource(R.mipmap.bg_card_ready_ready)
        mBinding.groupAccountInfo.visibility = VISIBLE
        mBinding.groupPrepared.visibility = VISIBLE
    }

    private fun exitPrepared() {
        mBinding.groupAccountInfo.visibility = GONE
        mBinding.groupPrepared.visibility = GONE
    }

    private fun enterFirstSporting() {
        stopDots()
        val frameAnimation = mBinding.ivSportAni.getDrawable() as AnimationDrawable?
        frameAnimation?.start()
        mBinding.groupAccountInfo.visibility = VISIBLE
        mBinding.ivSportAni.visibility = VISIBLE
    }

    private fun exitFirstSporting() {
        val frameAnimation = mBinding.ivSportAni.getDrawable() as AnimationDrawable?
        frameAnimation?.stop()
        mBinding.groupAccountInfo.visibility = GONE
        mBinding.ivSportAni.visibility = GONE
    }

    private fun enterSporting() {
        stopDots()
        mBinding.groupAccountInfo.visibility = VISIBLE
        mBinding.rvScore.visibility = VISIBLE
    }

    private fun exitSporting() {
        mBinding.groupAccountInfo.visibility = GONE
        mBinding.rvScore.visibility = GONE
    }

    private fun enterResultInSporting() {
        enterSporting()
        mBinding.groupResultInSporting.visibility = VISIBLE
    }

    private fun exitResultInSporting() {
        exitSporting()
        mBinding.groupResultInSporting.visibility = GONE
        // 保留 groupBest：结果浮层关闭后仍显示本轮最佳，仅下一组 reset() 时清除
    }

    private fun enterCurrentSportEnd() {
        stopDots()
    }

    private fun exitCurrentSportEnd() {

    }

    private fun enterFinished() {
        stopDots()
    }

    private fun exitFinished() {

    }

    fun setAccountInfo(accountInfo: AccountInfo) {
        Glide.with(this)
            .load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        mBinding.tvName.text = accountInfo.studentEntity?.accountName
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addScore(score: MultiSportResult) {
        if (scoreList.isNullOrEmpty()) {
            initScoreRv()
        }
        scoreList.add(
            0,
            MultiSportResult(
                score.accountInfo,
                score.number,
                count = score.count,
                status = score.status
            )
        )
        changeState(ForwardFlexionCardState.RESULT_IN_SPORTING)
        setCurrentScore(score)
        updateBestScore(score)
        mBinding.rvScore.adapter?.notifyDataSetChanged()
    }

    private fun updateBestScore(result: MultiSportResult) {
        if (result.status != 0) return
        val score = result.count
        if (score !in -200..400) return
        mBinding.groupBest.visibility = VISIBLE
        bestScore = maxOf(bestScore, score)
        mBinding.tvBestScore.text = "${bestScore / 10.0}"
    }

    private fun setCurrentScore(result: MultiSportResult) {
        mBinding.tvSportTimes.text = getString(R.string.how_many_times, scoreList.size)
        mBinding.tvSportCurrentResult.text = when (result.status) {
            0 -> {
                mBinding.tvSportCurrentResult.setTextColor(Color.WHITE)
                mBinding.tvSportCurrentResultUnit.visibility = VISIBLE
                mBinding.tvSportCurrentResultUnit.text = "cm"
                "${result.count / 10.0}"
            }

            1 -> {
                mBinding.tvSportCurrentResult.setTextColor(Color.RED)
                mBinding.tvSportCurrentResultUnit.visibility = GONE
                getString(R.string.violations)
            }

            else -> {
                mBinding.tvSportCurrentResult.setTextColor(Color.RED)
                mBinding.tvSportCurrentResultUnit.visibility = GONE
                getString(R.string.un_do)
            }
        }
    }

    private fun initScoreRv() {
        mBinding.rvScore.layoutManager = LinearLayoutManager(context)
        mBinding.rvScore.adapter =
            object :
                BaseBindingAdapter<ItemSitReachSportScoreBinding, MultiSportResult>(scoreList) {
                @SuppressLint("SetTextI18n")
                override fun convert(
                    holder: VBViewHolder<ItemSitReachSportScoreBinding>,
                    item: MultiSportResult
                ) {
                    if (holder.layoutPosition == 0) {
                        holder.vb.ivThisResult.visibility = VISIBLE
                    } else {
                        holder.vb.ivThisResult.visibility = INVISIBLE
                    }
                    holder.vb.tvIndex.text = "${scoreList.size - holder.layoutPosition}"
                    LogUtil.d("===================${item.number} -- ${item.status}")
                    holder.vb.tvScore.text = when (item.status) {
                        0 -> {
                            holder.vb.tvScore.setTextColor(Color.WHITE)
                            holder.vb.tvScoreUnit.visibility = VISIBLE
                            holder.vb.tvScoreUnit.text = "cm"
                            "${item.count / 10.0}"
                        }

                        1 -> {
                            holder.vb.tvScore.setTextColor(Color.RED)
                            holder.vb.tvScoreUnit.visibility = GONE
                            getString(R.string.violations)
                        }

                        else -> {
                            holder.vb.tvScore.setTextColor(Color.RED)
                            holder.vb.tvScoreUnit.visibility = GONE
                            getString(R.string.un_do)
                        }
                    }
                }

            }
    }

    private fun initView() {
    }

    fun reset() {
        tickTimes = 0f
        scoreList.clear()
        bestScore = -1000
        mBinding.rvScore.adapter?.notifyDataSetChanged()
        mBinding.rvScore.visibility = GONE
        mBinding.groupResultInSporting.visibility = GONE
        mBinding.groupBest.visibility = GONE
        changeState(ForwardFlexionCardState.IDLE)
    }

    fun retest() {
//        changeState(ForwardFlexionCardState.SPORTING)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopDots()
    }
}
