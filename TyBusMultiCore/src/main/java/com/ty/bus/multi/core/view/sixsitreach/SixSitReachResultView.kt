package com.ty.bus.multi.core.view.sixsitreach

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import com.ty.bus.multi.R
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ViewSixSitReachResultBinding
import com.ty.tybusbase.expand.getString

class SixSitReachResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var listener: ResultViewBtnClickListener? = null
    private var cardMap = HashMap<Int, SitReachResultCardView>()
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
    private var mBinding = ViewSixSitReachResultBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        initView()
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
    }

    /**
     * 显示下一步按钮
     */
    fun showNextBtn() {
        mBinding.tvNext.visibility = VISIBLE
        updateNextText(COUNT_DOWN_TIME)
        mHandler.postDelayed(runnable, 1000)
    }


    fun reset() {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacks(runnable)
    }

    private fun initView() {
        cardMap.put(1, mBinding.ffsSport1)
        cardMap.put(2, mBinding.ffsSport2)
        cardMap.put(3, mBinding.ffsSport3)
        cardMap.put(4, mBinding.ffsSport4)
        cardMap.put(5, mBinding.ffsSport5)
        cardMap.put(6, mBinding.ffsSport6)

        mBinding.tvRetest.setOnClickListener {
            listener?.onRetestClick()
        }
        mBinding.tvNext.setOnClickListener {
            mHandler.removeCallbacks(runnable)
            listener?.onNextClick()
        }
        mBinding.tvFinish.setOnClickListener {
            listener?.onEndTestClick()
        }
    }

    fun setSportResult(result: SparseArray<MultiSportResult>) {
        result.forEach { index, result ->
            cardMap[index]?.setResult(result)
        }
    }
}
