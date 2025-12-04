package com.ty.bus.multi.core.view.sixforwardflexion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.core.state.ForwardFlexionCardState
import com.ty.bus.multi.databinding.ViewSixForwardFlexionSportBinding
import com.ty.tybusbase.entity.AccountInfo

class SixForwardFlexionSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, ForwardFlexionSportCardView>()
    private var mBinding = ViewSixForwardFlexionSportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false

    init {
        initView()
    }

    private fun initView() {
        cardMap.put(1, mBinding.ffsSport1)
        cardMap.put(2, mBinding.ffsSport2)
        cardMap.put(3, mBinding.ffsSport3)
        cardMap.put(4, mBinding.ffsSport4)
        cardMap.put(5, mBinding.ffsSport5)
        cardMap.put(6, mBinding.ffsSport6)
    }

    fun changeState(index: Int, state: ForwardFlexionCardState) {
        if (isStarted && state == ForwardFlexionCardState.PREPARED) {
            return
        }
        cardMap[index]?.changeState(state)
    }

    //运动已开始
    fun sportStarted() {
        isStarted = true
        mBinding.tvBegin.visibility = GONE

    }

    fun sportingTips(text: String) {
        mBinding.tvSportingTips.text = text
    }

    fun sportFinish(index: Int) {

    }

    fun addScore(index: Int, score: Int) {
        cardMap[index]?.addScore(score)
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    fun reset() {
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }
}