package com.ty.bus.multi.core.view.sixsitreach

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.databinding.ViewSixSitReachSportBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.utils.LogUtil

class SixSitReachSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, SitReachSportCardView>()
    private var mBinding = ViewSixSitReachSportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false
    private var startListener: Action? = null

    var listener: ResultViewBtnClickListener? = null

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
    private val countDownTime = 10000L
    private var currentTime = 10
    private var countDownTimer: CountDownTimer? = null
    /** 防止 onTick(0) 与 onFinish / 点击重复触发开测 */
    private var beginStartFired = false

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
            fireBeginStart("click")
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

    private fun fireBeginStart(from: String) {
        if (beginStartFired || isStarted) return
        beginStartFired = true
        countDownTimer?.cancel()
        countDownTimer = null
        mBinding.tvBegin.visibility = GONE
        LogUtil.d("SitReachSix::SportView: begin start from=$from")
        startListener?.invoke()
    }

    private fun setConfirmText(lng: Int) {
        if (lng <= 0) {
            fireBeginStart("countdown")
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
        if (state == ForwardFlexionCardState.IDENTIFIED && !isStarted) {
            // 仅首次识别启动倒计时；后续道次识别不得重置 currentTime，否则会与真实倒计时错位
            if (countDownTimer == null && !beginStartFired) {
                currentTime = 10
                beginStartFired = false
                LogUtil.d("SitReachSix::SportView: show begin button, lane=$index")
                mBinding.tvBegin.visibility = VISIBLE
                mBinding.tvBegin.text = getString(R.string.start_now_num, currentTime)
                countDownTimer = object : CountDownTimer(
                    countDownTime,
                    1000L
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        // 用剩余毫秒计算；>0 才刷新，到 0 交给 onFinish，避免与 onFinish 双触发
                        val sec = ((millisUntilFinished + 999) / 1000).toInt()
                        if (sec > 0) {
                            currentTime = sec
                            setConfirmText(sec)
                        }
                    }

                    override fun onFinish() {
                        setConfirmText(0)
                    }
                }
                countDownTimer?.start()
            }
        }
        cardMap[index]?.changeState(state)
    }

    //运动已开始
    fun sportStarted() {
        LogUtil.d("SitReachSix::SportView.sportStarted: hide begin button")
        isStarted = true
        mBinding.tvBegin.visibility = GONE
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun sportingTips(text: String) {
        if (text.isBlank()) {
            mBinding.tvSportingTips.visibility = GONE
        } else {
            mBinding.tvSportingTips.visibility = VISIBLE
            mBinding.tvSportingTips.text = text
        }
    }

    fun sportFinish() {
        LogUtil.d("SitReachSix::SportView.sportFinish: showRetest=true, showNext=$isShowNext")
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        mHandler.removeCallbacks(runnable)
        mBinding.tvRetest.visibility = VISIBLE
        mBinding.tvSportingTips.visibility = GONE
        if (isShowNext) {
            // 组织测试：仅展示「下一组」，不自动倒计时跳组
            mBinding.tvNext.visibility = VISIBLE
            mBinding.tvNext.text = getString(R.string.next_group)
        }
    }

    fun addScore(index: Int, score: MultiSportResult) {
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
        LogUtil.d("SitReachSix::SportView.next: clear cards then onNextClick")
        mHandler.removeCallbacks(runnable)
        isStarted = false
        beginStartFired = false
        countDownTimer?.cancel()
        countDownTimer = null
        currentTime = 10
        mBinding.tvBegin.visibility = GONE
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        // 先清卡片成绩 UI，再通知业务 reset/识别，避免迟到成绩写回
        cardMap.forEach { (_, value) ->
            value.reset()
        }
        mBinding.tvSportingTips.visibility = GONE
        listener?.onNextClick()
    }

    /**
     * 重测，当前人员继续测试
     */
    fun retest() {
        LogUtil.d("SitReachSix::SportView.retest")
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
