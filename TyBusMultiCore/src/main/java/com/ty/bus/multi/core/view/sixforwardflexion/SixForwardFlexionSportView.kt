package com.ty.bus.multi.core.view.sixforwardflexion

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.core.view.forwardflexion.ForwardFlexionResultViewListener
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

    var listener: ForwardFlexionResultViewListener? = null

    private val NEXT_COUNT_DOWN_TIME = 5 //倒计时时间

    private var nextCountDownTime = NEXT_COUNT_DOWN_TIME //倒计时时间

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private var isShowNext = false

    private val runnable by lazy {
        object : Runnable {
            override fun run() {
                nextCountDownTime--
                if (nextCountDownTime > 0) {
                    mHandler.postDelayed(this, 1000)
                } else {
                    next()
                }
                updateNextText(nextCountDownTime)
            }
        }
    }

    /**
     * 开始按钮
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
        mBinding.tvNext.setOnClickListener {
            next()
        }
        mBinding.tvFinish.setOnClickListener {
            listener?.onEndTestClick()
        }

        mBinding.tvRetest.setOnClickListener {
            retest()
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

    /**
     * 显示下一步按钮
     */
    fun showNextBtn() {
        isShowNext = true
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
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

    fun sportFinish() {
        mBinding.tvRetest.visibility = VISIBLE
        mBinding.tvSportingTips.visibility = GONE
        if (isShowNext) {
            mBinding.tvNext.visibility = VISIBLE
            updateNextText(NEXT_COUNT_DOWN_TIME)
            mHandler.postDelayed(runnable, 1000)
        }
    }

    fun addScore(index: Int, score: Int) {
        cardMap[index]?.addScore(score)
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacks(runnable)
    }

    /**
     * 重置，恢复初始状态（下一组）
     */
    fun next() {
        mHandler.removeCallbacks(runnable)
        listener?.onNextClick()
        isStarted = false
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        cardMap.forEach { (_, value) ->
            value.reset()
        }
        mBinding.tvSportingTips.visibility = GONE
    }

    /**
     * 重测，当前人员继续测试
     */
    fun retest() {
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        listener?.onRetestClick()
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        mBinding.tvSportingTips.visibility = GONE
        cardMap.forEach { (_, value) ->
            value.retest()
        }
    }
}