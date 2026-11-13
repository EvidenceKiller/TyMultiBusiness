package com.ty.bus.multi.core.view.sitreacheighteen

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.core.view.sixsitreach.SitReachSportCardView
import com.ty.bus.multi.databinding.ViewEighteenSitReachSportBinding
import com.ty.tybusbase.entity.AccountInfo

/**
 * 18人坐位体前屈单个区域（左区/中区/右区）的运动面板，每区6个道次。
 *
 * 结束测试/重测/下一组等按钮属于整个运动（主屏+副屏协同），统一由Activity承载，
 * 本面板只保留6个道次卡片。
 */
class EighteenSitReachSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val cardMap = HashMap<Int, SitReachSportCardView>()

    private var isStarted = false

    private val mBinding =
        ViewEighteenSitReachSportBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initView()
    }

    private fun initView() {
        cardMap[1] = mBinding.ffsSport1
        cardMap[2] = mBinding.ffsSport2
        cardMap[3] = mBinding.ffsSport3
        cardMap[4] = mBinding.ffsSport4
        cardMap[5] = mBinding.ffsSport5
        cardMap[6] = mBinding.ffsSport6
    }

    /**
     * 运动已开始
     */
    fun sportStarted() {
        isStarted = true
    }

    fun changeState(index: Int, state: ForwardFlexionCardState) {
        //运动开始后，不再切换到"已准备"状态
        if (isStarted && state == ForwardFlexionCardState.PREPARED) {
            return
        }
        cardMap[index]?.changeState(state)
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    fun addScore(index: Int, score: MultiSportResult) {
        cardMap[index]?.addScore(score)
    }

    /**
     * 重置，恢复初始状态（下一组）
     */
    fun reset() {
        isStarted = false
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }

    /** 重测：清本区卡片成绩 UI，保留人员信息 */
    fun retest() {
        cardMap.forEach { (_, value) ->
            value.retest()
        }
    }
}
