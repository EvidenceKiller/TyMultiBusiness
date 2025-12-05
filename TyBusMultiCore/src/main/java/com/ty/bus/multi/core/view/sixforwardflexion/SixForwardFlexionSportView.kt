package com.ty.bus.multi.core.view.sixforwardflexion

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.databinding.ViewSixForwardFlexionSportBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.getString

class SixForwardFlexionSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, ForwardFlexionSportCardView>()
    private var mBinding = ViewSixForwardFlexionSportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false
    private var startListener: Action? = null

    /**
     * 进度条假增
     */
    private val countDownTime = 50000L
    private var currentTime = 5
    private var countDownTimer: CountDownTimer? = null

    init {
        initView()
    }

    private fun initView() {
        cardMap.put(1, mBinding.ffsSport1)
        cardMap.put(2, mBinding.ffsSport2)
        cardMap.put(3, mBinding.ffsSport3)
        cardMap.put(4, mBinding.ffsSport4)
        cardMap.put(5, mBinding.ffsSport5)
        cardMap.put(6, mBinding.ffsSport6)
        mBinding.tvBegin.setOnClickListener {
            countDownTimer?.cancel()
            startListener?.invoke()
        }
    }

    fun setStartListener(listener: Action) {
        startListener = listener
    }

    private fun setConfirmText(lng: Int) {
        if (lng <= 0) {
            countDownTimer?.cancel()
            startListener?.invoke()
            mBinding.tvBegin.visibility = GONE
        } else {
            mBinding.tvBegin.text = getString(R.string.start_now_num, lng)
        }
    }

    fun changeState(index: Int, state: ForwardFlexionCardState) {
        if (isStarted && state == ForwardFlexionCardState.PREPARED) {
            return
        }
        if (state == ForwardFlexionCardState.IDENTIFIED) {
            currentTime = 5
            if (countDownTimer == null) {
                mBinding.tvBegin.visibility = VISIBLE
                countDownTimer = object : CountDownTimer(
                    countDownTime,
                    1000L
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        currentTime--

                        setConfirmText(currentTime)
                    }

                    override fun onFinish() {
                    }
                }
                countDownTimer?.start()
            }


        }
        cardMap[index]?.changeState(state)
    }

    //运动已开始
    fun sportStarted() {
        isStarted = true
        mBinding.tvBegin.visibility = GONE
        countDownTimer?.cancel()
    }

    fun sportingTips(text: String) {
        mBinding.tvSportingTips.visibility = VISIBLE
        mBinding.tvSportingTips.text = text
    }

    fun sportFinish(index: Int) {

    }

    fun addScore(index: Int, score: Int) {
        cardMap[index]?.addScore(score)
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    /**
     * 重置，恢复初始状态（下一组）
     */
    fun reset() {
        isStarted = false
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }

    /**
     * 重测，当前人员继续测试
     */
    fun retest() {
        cardMap.forEach { (_, value) ->
            value.retest()
        }
    }
}