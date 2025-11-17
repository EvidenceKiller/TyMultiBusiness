package com.ty.bus.multi.core.view.common

import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.databinding.MultiSportIdentityCardViewBinding
import com.ty.tybusbase.expand.getString


class MultiSportIdentityCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: MultiSportIdentityCardViewBinding

    private var state: MultiSportCardViewState = MultiSportCardViewState.NOBODY

    /**
     * 进度条假增
     */
    private val countDownTime = 1000L
    private var tickTimes = 0f
    private var totalTimes = 20f

    init {
        var view = LayoutInflater.from(context)
            .inflate(R.layout.multi_sport_identity_card_view, this, true)
        mBinding = MultiSportIdentityCardViewBinding.bind(view)
        toIdle()
    }

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
     * 切换状态
     */
    fun changeState(state: MultiSportCardViewState, value: Any? = null) {
        //如果是已准备，只有该道次的人，在其他道次识别了，此道次被置为NOBODY
        when (state) {
            MultiSportCardViewState.IDENTIFIED -> {
                toIdentified(value as? String)
            }

            MultiSportCardViewState.PREPARED -> {
                toPrepared()
            }

            MultiSportCardViewState.IDENTIFYING -> {
                toIdentifying()
            }

            MultiSportCardViewState.IDENTIFY_FAIL -> {
                toIdentifyFail()
            }

            MultiSportCardViewState.NOBODY -> {
                toNoBody()
            }

            MultiSportCardViewState.IDENTIFY_CHANGE -> {
                toIdentifyChange()
            }

            else -> {
            }
        }
        this.state = state
    }

    /**
     * 设置用户信息
     */
    fun setUserInfo(name: String, headUrl: String?) {
        headUrl?.let {
            Glide.with(context).load(headUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)
        }

        mBinding.tvName.text = name
    }

    /**
     * 切到已识别状态
     */
    private fun toIdentified(msg: String? = null) {
        mBinding.clIdentityNotice.visibility = INVISIBLE
        mBinding.groupAccount.visibility = VISIBLE
        mBinding.tvStatusTip.text =
            if (TextUtils.isEmpty(msg)) getString(R.string.lie_on_back_and_face_screen) else msg
        identifyingTimer.cancel()
    }

    /**
     * 识别失败
     */
    private fun toIdentifyFail() {
        toIdle()
    }

    /**
     * 已准备
     */
    private fun toPrepared() {
        mBinding.ivAccountInfoBg.setBackgroundResource(R.mipmap.bg_3user_already)
        mBinding.tvStatusTip.text = getString(R.string.ready_state)
    }


    /**
     * 切换到无人状态
     */
    private fun toNoBody() {
        if (this.state == MultiSportCardViewState.IDENTIFIED || this.state == MultiSportCardViewState.PREPARED) {
            return
        }
        toIdle()
    }

    /**
     * 切换人员状态
     */
    private fun toIdentifyChange() {
        toIdle()
    }

    /**
     * 正在识别
     */
    private fun toIdentifying() {
        mBinding.groupAccount.visibility = INVISIBLE
        mBinding.clIdentityNotice.visibility = VISIBLE
        mBinding.progressView.setProgress(10f)
        tickTimes = 0f
        identifyingTimer.start()
    }

    /**
     * 切换到空闲状态
     */
    private fun toIdle() {
        mBinding.groupAccount.visibility = INVISIBLE
        mBinding.clIdentityNotice.visibility = INVISIBLE
        identifyingTimer?.cancel()
    }

}
