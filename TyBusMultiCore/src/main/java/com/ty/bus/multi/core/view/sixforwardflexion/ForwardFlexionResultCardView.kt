package com.ty.bus.multi.core.view.sixforwardflexion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ViewForwardFlexionResultCardBinding
import com.ty.tybusbase.expand.getString

class ForwardFlexionResultCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewForwardFlexionResultCardBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    fun setResult(
        result: MultiSportResult
    ) {
        mBinding.apply {
            mBinding.groupResult.visibility = VISIBLE
            mBinding.tvIdle.visibility = GONE
            Glide.with(context)
                .load(result.accountInfo.studentEntity?.userFaceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default)
                .into(civHeader)
            tvName.text = result.accountInfo.studentEntity?.accountName
            val item = result.count
            tvScore.text = when {
                item == -300 -> {
                    ivBg.setBackgroundResource(R.mipmap.bg_card_completed_failed)
                    getString(R.string.violations)
                }

                item > -200 && item <= 400 -> {
                    ivBg.setBackgroundResource(R.mipmap.bg_card_completed_default)
                    tvScoreUnit.text = "cm"
                    "${item / 10.0}"
                }

                else -> {
                    ivBg.setBackgroundResource(R.mipmap.bg_card_completed_failed)
                    getString(R.string.un_do)
                }

            }
        }
    }

}