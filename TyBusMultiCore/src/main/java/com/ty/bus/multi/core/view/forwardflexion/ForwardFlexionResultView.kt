package com.ty.bus.multi.core.view.forwardflexion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ViewForwardFlexionResultBinding

class ForwardFlexionResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: ViewForwardFlexionResultBinding

    var listener: ForwardFlexionResultViewListener? = null

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
            listener?.onNextClick()
        }
        mBinding.tvFinish.setOnClickListener {
            listener?.onEndTestClick()
        }
    }

    /**
     * 显示下一步按钮
     */
    fun showNextBtn(){

    }


    fun reset(){

    }

}

interface ForwardFlexionResultViewListener {
    fun onRetestClick()
    fun onNextClick()
    fun onEndTestClick()
}