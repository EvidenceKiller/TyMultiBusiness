package com.ty.bus.multi.core.view.threesport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.databinding.ViewThreeSportDetailBinding
import com.ty.tybusbase.helper.TypeFaceHelper

class ThreeSportDetailView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    BaseMultiSportViewNoAnimation(context,attrs,defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)
    private val mBinding: ViewThreeSportDetailBinding

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_three_sport_detail, this, true)
        mBinding = ViewThreeSportDetailBinding.bind(view)
        initView()
    }

    private fun initView() {
        mBinding.tvCountDownNotice.typeface = TypeFaceHelper.instance?.getYouSheBiaoTiHei(context)
        mBinding.srcvNumber2.setNumber("2")
        mBinding.srcvNumber3.setNumber("3")
        cardMap[0] = mBinding.srcvNumber1
        cardMap[1] = mBinding.srcvNumber2
        cardMap[2] = mBinding.srcvNumber3
        bottomShadowViewMap[0] = mBinding.ivShadow1
        bottomShadowViewMap[1] = mBinding.ivShadow2
        bottomShadowViewMap[2] = mBinding.ivShadow3
    }
    override fun getCountDownNoticeTv(): TextView = mBinding.tvCountDownNotice

    override fun getCountDownGoTv(): TextView = mBinding.tvCountDownGo
    fun retest() {
    }


}