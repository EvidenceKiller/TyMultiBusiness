package com.ty.bus.multi.core.view.threesport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportResultView
import com.ty.bus.multi.databinding.ViewThreeSportResultBinding
import com.ty.tybusbase.helper.TypeFaceHelper

class ThreeSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportResultView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewThreeSportResultBinding

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
    }

    override fun getConfirmView(): View = mBinding.tvConfirm

    override fun getCount(): Int = 5

}