package com.ty.bus.multi.core.view.fivesport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportView
import com.ty.bus.multi.databinding.ViewSkipRoseMultiSportViewBinding
import com.ty.tybusbase.helper.TypeFaceHelper

class MultiSport5SportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewSkipRoseMultiSportViewBinding

    init {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.view_skip_rose_multi_sport_view, this, true)
        mBinding = ViewSkipRoseMultiSportViewBinding.bind(view)
        initView()
    }

    private fun initView() {
        mBinding.tvCountDownNotice.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        mBinding.srcvNumber2.setNumber("2")
        mBinding.srcvNumber3.setNumber("3")
        mBinding.srcvNumber4.setNumber("4")
        mBinding.srcvNumber5.setNumber("5")
        cardMap[0] = mBinding.srcvNumber1
        cardMap[1] = mBinding.srcvNumber2
        cardMap[2] = mBinding.srcvNumber3
        cardMap[3] = mBinding.srcvNumber4
        cardMap[4] = mBinding.srcvNumber5
        bottomShadowViewMap[0] = mBinding.ivShadow1
        bottomShadowViewMap[1] = mBinding.ivShadow2
        bottomShadowViewMap[2] = mBinding.ivShadow3
        bottomShadowViewMap[3] = mBinding.ivShadow4
        bottomShadowViewMap[4] = mBinding.ivShadow5
    }

    override fun setCountDownNotice(notice: String) {
        mBinding.tvCountDownNotice.visibility = VISIBLE
        mBinding.tvCountDownNotice.text = notice
    }

    override fun getCountDownNoticeTv(): TextView = mBinding.tvCountDownNotice

    override fun getCountDownGoTv(): TextView = mBinding.tvCountDownGo
    override fun needCardAnimation(): Boolean = true
}