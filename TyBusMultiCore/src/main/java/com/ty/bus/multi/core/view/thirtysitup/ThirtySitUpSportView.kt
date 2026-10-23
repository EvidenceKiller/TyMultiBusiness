package com.ty.bus.multi.core.view.thirtysitup

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.entity.MultiSportResult
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

    /**
     * 运动已开始：只收起倒计时文案，不改动卡片状态。
     *
     * 基类实现会按 preparedMap 把"未准备"的道次置为 NOBODY_SPORT（显示为空闲），
     * 但仰卧起坐识别完成后卡片停在"已识别"，不会经历"已准备"，
     * 会导致主屏所有道次在开始瞬间被重置成空闲，故此处不再重置卡片。
     * 卡片状态统一由 [onSportStarted] 与 ViewModel/副屏上报驱动。
     */
    override fun onStarted() {
        getCountDownGoTv().visibility = View.GONE
        getCountDownNoticeTv().visibility = View.GONE
    }

    /**
     * 按本区域已识别人员切换卡片到运动状态：有人的道次进运动状态，无人道次进"无人运动"状态
     */
    fun onSportStarted(personArray: SparseArray<MultiSportResult>?) {
        cardMap.keys.forEach { index ->
            val hasPerson = personArray?.get(index, null) != null
            changeState(
                index,
                if (hasPerson) MultiSportCardViewState.SPORTING
                else MultiSportCardViewState.NOBODY_SPORT
            )
        }
    }
}
