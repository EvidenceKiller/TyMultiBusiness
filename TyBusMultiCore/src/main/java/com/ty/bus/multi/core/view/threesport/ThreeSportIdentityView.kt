package com.ty.bus.multi.core.view.threesport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.view.common.MultiSportIdentityCardView
import com.ty.bus.multi.databinding.ViewThreeSportIdentityBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.entity.AccountInfo

class ThreeSportIdentityView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding: ViewThreeSportIdentityBinding

    protected val cardMap = HashMap<Int, MultiSportIdentityCardView>()

    init {
        var view =
            LayoutInflater.from(context).inflate(R.layout.view_three_sport_identity, this, true)
        mBinding = ViewThreeSportIdentityBinding.bind(view)
        initView()
    }

    private fun initView() {
        cardMap[0] = mBinding.identityCard1
        cardMap[1] = mBinding.identityCard2
        cardMap[2] = mBinding.identityCard3
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
            p.second.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE
        )
    }

    fun reset() {
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }

}