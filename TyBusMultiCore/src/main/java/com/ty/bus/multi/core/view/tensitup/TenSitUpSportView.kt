package com.ty.bus.multi.core.view.tensitup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.databinding.TenSitUpSportViewBinding
import com.ty.tybusbase.expand.Action

class TenSitUpSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, TenSitUpSportView>()
    private var mBinding = TenSitUpSportViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false
    private var startListener: Action? = null

    var listener: ResultViewBtnClickListener? = null

}