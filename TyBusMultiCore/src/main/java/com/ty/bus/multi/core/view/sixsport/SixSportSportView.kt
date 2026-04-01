package com.ty.bus.multi.core.view.sixsport

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.databinding.ViewSixSportSportBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.getString

class SixSportSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, SixSportSportCardView>()
    private var mBinding = ViewSixSportSportBinding.inflate(
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
            sportStarted()
            listener?.onStartClick()
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


    /**
     * 显示下一步按钮
     */
    fun showNextBtn() {
        isShowNext = true
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
    }

    fun changeState(index: Int, state: MultiSportCardViewState) {
        if (isStarted && state == MultiSportCardViewState.PREPARED) {
            return
        }
        if (state == MultiSportCardViewState.IDENTIFIED) {
            if (mBinding.tvBegin.isGone) {
                mBinding.tvBegin.visibility = VISIBLE
            }
        }
        cardMap[index]?.changeState(state)
    }

    //运动已开始
    fun sportStarted() {
        isStarted = true
        mBinding.tvBegin.visibility = GONE
    }

    fun setSportingTips(text: String) {
        mBinding.tvSportingTips.visibility = VISIBLE
        mBinding.tvSportingTips.text = text
    }

    fun sportFinish() {
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        mBinding.tvRetest.visibility = VISIBLE
        setSportingTips(getString(R.string.sport_finish))
        if (isShowNext) {
            mBinding.tvNext.visibility = VISIBLE
            updateNextText(NEXT_COUNT_DOWN_TIME)
            mHandler.postDelayed(runnable, 1000)
        }
    }

    fun updateResult(result: MutableList<GroupSportBaseMan>) {
        for (index in result.indices) {
            val skipMan = result[index]
            if (skipMan.score != 0) {
                cardMap[skipMan.positionNum]?.updateResult(skipMan.score)
            }
        }
    }

    fun updateTime(time: String) {
        setSportingTips(getString(R.string.sport_countdown_time, time))
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