package com.ty.bus.multi.core.view.forwardflexion

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ViewForwardFlexionSportBinding

class ForwardFlexionSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: ViewForwardFlexionSportBinding
    protected val cardMap = HashMap<Int, ForwardFlexionScoreView>()

    init {
        var view =
            LayoutInflater.from(context).inflate(R.layout.view_forward_flexion_sport, this, true)
        mBinding = ViewForwardFlexionSportBinding.bind(view)
        initView()
    }

    private fun initView() {
        cardMap.put(1, mBinding.ffScore1)
        cardMap.put(2, mBinding.ffScore2)
        cardMap.put(3, mBinding.ffScore3)
    }

    fun addScore(index: Int, score: Int) {
        cardMap[index]?.addScore(score)
    }

    fun getScore(index: Int): List<Int>? {
        return cardMap[index]?.getScoreList()
    }

    fun setAccountInfo(msr:SparseArray<MultiSportResult>){
        msr.forEach { index, multiSportResult ->
            cardMap[index]?.setAccountInfo(multiSportResult.accountInfo)
        }
    }

}