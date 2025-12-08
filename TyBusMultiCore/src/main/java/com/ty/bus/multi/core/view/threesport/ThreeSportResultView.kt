package com.ty.bus.multi.core.view.threesport

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportResultView
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.databinding.ViewThreeSportResultBinding
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.helper.TypeFaceHelper

class ThreeSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportResultView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewThreeSportResultBinding

    var listener: ResultViewBtnClickListener? = null

    private val COUNT_DOWN_TIME = 5 //倒计时时间

    private var countDownTime = COUNT_DOWN_TIME //倒计时时间

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private val runnable by lazy {
        object : Runnable {
            override fun run() {
                countDownTime--
                if (countDownTime > 0) {
                    mHandler.postDelayed(this, 1000)
                } else {
                    listener?.onNextClick()
                }
                updateNextText(countDownTime)
            }
        }
    }

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_three_sport_result, this, true)
        mBinding = ViewThreeSportResultBinding.bind(view)
        initView()
    }

    private fun initView() {
        resultCardArray.put(0, mBinding.srvNumber1)
        resultCardArray.put(1, mBinding.srvNumber2)
        resultCardArray.put(2, mBinding.srvNumber3)
        mBinding.tvFinish.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        mBinding.tvFinish.setOnClickListener {
            listener?.onEndTestClick()
        }

        mBinding.tvNext.setOnClickListener {
            listener?.onNextClick()
        }

        mBinding.tvRetest.setOnClickListener {
            listener?.onRetestClick()
        }

    }

    /**
     * 显示下一步按钮
     */
    fun showNextBtn() {
        mBinding.tvNext.visibility = VISIBLE
        updateNextText(COUNT_DOWN_TIME)
        mHandler.postDelayed(runnable, 1000)
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacks(runnable)
    }

    override fun getConfirmView(): View = mBinding.tvFinish

    override fun getCount(): Int = 5

}