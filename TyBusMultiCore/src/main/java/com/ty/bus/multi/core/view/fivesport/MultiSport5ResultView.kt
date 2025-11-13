package com.ty.bus.multi.core.view.fivesport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.util.forEach
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportResultView
import com.ty.bus.multi.databinding.ViewSkipRoseMultiResultViewBinding
import com.ty.tybusbase.helper.TypeFaceHelper

class MultiSport5ResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportResultView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewSkipRoseMultiResultViewBinding

    init {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.view_skip_rose_multi_result_view, this, true)
        mBinding = ViewSkipRoseMultiResultViewBinding.bind(view)
        initView()
    }

    private fun initView() {
        resultCardArray.put(0, mBinding.srvNumber1)
        resultCardArray.put(1, mBinding.srvNumber2)
        resultCardArray.put(2, mBinding.srvNumber3)
        resultCardArray.put(3, mBinding.srvNumber4)
        resultCardArray.put(4, mBinding.srvNumber5)
        mBinding.tvFinish.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        resultCardArray.forEach { key, value ->
            value.setReportOnclickListener {
                reportClickListener?.invoke(key)
            }
        }
    }

    override fun getConfirmView(): View = mBinding.tvConfirm

    override fun getCount(): Int = 5

}