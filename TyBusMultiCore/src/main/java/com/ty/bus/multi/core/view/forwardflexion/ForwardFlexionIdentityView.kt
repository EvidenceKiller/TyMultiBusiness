package com.ty.bus.multi.core.view.forwardflexion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.view.common.MultiSportIdentityCardView
import com.ty.bus.multi.databinding.ForwardFlexionIdentityViewBinding
import com.ty.tybusbase.entity.AccountInfo

class ForwardFlexionIdentityView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: ForwardFlexionIdentityViewBinding

    protected val cardMap = HashMap<Int, MultiSportIdentityCardView>()

    init {
        var view =
            LayoutInflater.from(context).inflate(R.layout.forward_flexion_identity_view, this, true)
        mBinding = ForwardFlexionIdentityViewBinding.bind(view)
        initView()
    }

    private fun initView() {
        cardMap[1] = mBinding.identityCard1
        cardMap[2] = mBinding.identityCard2
        cardMap[3] = mBinding.identityCard3
    }

    /**
     * 改变状态
     */
    fun changeState(p: Pair<Int, MultiSportCardViewState>, value: Any? = null) {
        cardMap[p.first]?.changeState(p.second, value)
    }

    /**
     * 设置用户信息
     */
    fun setUserInfo(p: Pair<Int, AccountInfo>) {
        cardMap[p.first]?.setUserInfo(
            p.second.studentEntity!!.accountName,
            p.second.studentEntity!!.userFaceImgUrl,
            true
        )
    }

    fun reset() {
        cardMap.forEach { (_, value) ->
            value.changeState(MultiSportCardViewState.NOBODY)
        }
    }

}