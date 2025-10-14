package com.ty.bus.multi.core.view.forwardflexion

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ViewForwardFlexionResultBinding
import com.ty.tybusbase.expand.getString

class ForwardFlexionResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: ViewForwardFlexionResultBinding

    var listener: ForwardFlexionResultViewListener? = null

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
        var view =
            LayoutInflater.from(context).inflate(R.layout.view_forward_flexion_result, this, true)
        mBinding = ViewForwardFlexionResultBinding.bind(view)
        initView()
    }

    private fun initView() {
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

    /**
     * 设置运动结果
     */
    fun setSportResult(result: SparseArray<MultiSportResult>) {
        showResult(result)
    }

    private fun showResult(result: SparseArray<MultiSportResult>) {
        result.forEach { index, multiSportResult ->
            when (index) {
                1 -> {
                    mBinding.clScore1.visibility = VISIBLE
                    setResult(
                        mBinding.tvName1,
                        mBinding.tvScore1,
                        mBinding.civHeader1,
                        multiSportResult
                    )
                }

                2 -> {
                    mBinding.clScore2.visibility = VISIBLE
                    setResult(
                        mBinding.tvName2,
                        mBinding.tvScore2,
                        mBinding.civHeader2,
                        multiSportResult
                    )
                }

                3 -> {
                    mBinding.clScore3.visibility = VISIBLE
                    setResult(
                        mBinding.tvName3,
                        mBinding.tvScore3,
                        mBinding.civHeader3,
                        multiSportResult
                    )
                }
            }
        }
    }

    private fun setResult(
        tvName: TextView,
        tvScore: TextView,
        ivHeader: ImageView,
        result: MultiSportResult
    ) {
        Glide.with(context)
            .load(result.accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default)
            .into(ivHeader)
        tvName.text = result.accountInfo.studentEntity?.accountName
        tvScore.text = "${result.count / 10.0}"
    }

}

interface ForwardFlexionResultViewListener {
    fun onRetestClick()
    fun onNextClick()
    fun onEndTestClick()
}