package com.ty.bus.multi.core.view.common

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.core.base.ISportCardView
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.tybusbase.entity.AccountInfo

class MoreLandscapeSportCardSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr), ISportCardView {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)





    override fun setNumber(number: String) {
    }

    override fun changeState(state: MultiSportCardViewState) {
    }

    override fun setAccountInfo(accountInfo: AccountInfo) {
    }

    override fun setPrecastAccountInfo(accountInfo: AccountInfo) {
    }

    override fun updateScore(score: Int) {
    }

    override fun setTime(time: String) {
    }

    override fun setTimeBackground(res: Int) {
    }

    override fun reset() {
    }

}