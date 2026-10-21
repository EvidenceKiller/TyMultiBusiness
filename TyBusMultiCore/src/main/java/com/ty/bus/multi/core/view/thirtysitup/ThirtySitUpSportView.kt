package com.ty.bus.multi.core.view.thirtysitup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.databinding.ThirtySitUpSportViewBinding

/**
 * 30人仰卧起坐单个区域（左区/中区/右区）的运动面板，每区10个道次。
 * 结构沿用十人仰卧起坐的道次卡片（MoreLandscapeSportCardSportView），
 * 结束/重测/下一组等按钮统一由结果页承载，故本面板只保留道次卡片。
 */
class ThirtySitUpSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportViewNoAnimation(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ThirtySitUpSportViewBinding

    private var countDownNoticeTv: TextView? = null
    private var countDownGoTv: TextView? = null

    init {
        mBinding = ThirtySitUpSportViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    private fun initView() {
        cardMap[0] = mBinding.mlSportView1
        cardMap[1] = mBinding.mlSportView2
        cardMap[2] = mBinding.mlSportView3
        cardMap[3] = mBinding.mlSportView4
        cardMap[4] = mBinding.mlSportView5
        cardMap[5] = mBinding.mlSportView6
        cardMap[6] = mBinding.mlSportView7
        cardMap[7] = mBinding.mlSportView8
        cardMap[8] = mBinding.mlSportView9
        cardMap[9] = mBinding.mlSportView10
    }

    /**
     * 绑定Activity层统一管理的倒计时提示/大数字TextView。
     * 倒计时、开始按钮、提示文案属于整个运动，由Activity持有，各区域面板共享引用。
     */
    fun bindCountDownViews(noticeTv: TextView, goTv: TextView) {
        countDownNoticeTv = noticeTv
        countDownGoTv = goTv
    }

    override fun getCountDownNoticeTv(): TextView = countDownNoticeTv ?: TextView(context)

    override fun getCountDownGoTv(): TextView = countDownGoTv ?: TextView(context)
}
