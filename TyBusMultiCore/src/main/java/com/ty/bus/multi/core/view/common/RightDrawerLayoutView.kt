package com.ty.bus.multi.core.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.databinding.ViewRightDrawerLayoutBinding

class RightDrawerLayoutView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    val mBinding: ViewRightDrawerLayoutBinding =
        ViewRightDrawerLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private var drawerLayoutWidth = 0

    private var isOpen = true //抽屉是否是打开的

    private val duration = 150 //抽屉动画时长

    init {
        mBinding.clDrawer.post { drawerLayoutWidth = mBinding.clDrawer.width }
        initView()
    }

    private fun initView() {
        mBinding.ivSideToggle.setOnClickListener {
            toggleDrawerLayout()
        }


    }

    private fun toggleDrawerLayout() {

        animate().translationX(if (isOpen) drawerLayoutWidth.toFloat() else 0f)
            .setDuration(duration.toLong())
            .start()
        mBinding.ivArrow.animate().rotation(if (isOpen) 180f else 0f)
            .setDuration(duration * 2.toLong())
            .start()
        isOpen = !isOpen
    }


}