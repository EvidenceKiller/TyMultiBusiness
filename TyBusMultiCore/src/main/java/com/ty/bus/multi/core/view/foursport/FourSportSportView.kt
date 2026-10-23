package com.ty.bus.multi.core.view.foursport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.databinding.FourSportSportViewBinding

class FourSportSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportViewNoAnimation(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding = FourSportSportViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var countDownNoticeTv: TextView? = null
    private var countDownGoTv: TextView? = null

    init {
        initView()
    }

    private fun initView() {
        cardMap[0] = mBinding.sportView1
        cardMap[1] = mBinding.sportView2
        cardMap[2] = mBinding.sportView3
        cardMap[3] = mBinding.sportView4
    }

    fun setDebug(debug: Boolean) {
        cardMap.forEach { (_, view) ->
            view.setDebug(debug)
        }
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
