package com.ty.bus.multi.core.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ViewSkipRoseCardResultViewBinding
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.visible

class SportCardResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mBinding: ViewSkipRoseCardResultViewBinding =
        ViewSkipRoseCardResultViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var reportClickListener: OnClickListener? = null

    init {
        mBinding.btnReport.setOnClickListener {
            reportClickListener?.onClick(it)
        }
    }

    /**
     * 设置结果
     */
    fun setResult(
        rank: Int,
        skipRoseMultiResult: MultiSportResult?,
        isBest: Boolean,
        unit: String = "个",
        energyPoint: Int = 0
    ) {
        mBinding.groupResult.visibility = VISIBLE
        mBinding.tvNobody.visibility = GONE
        mBinding.tvUnit.text = unit
        skipRoseMultiResult?.let {
            val layoutParams = mBinding.ivHeaderCrown.layoutParams as LayoutParams
            val layoutParams1 = mBinding.ivHeader.layoutParams as LayoutParams
            if (rank != 1) {
                layoutParams.width = 72
                layoutParams.height = 72
                mBinding.ivHeaderCrown.layoutParams = layoutParams
                layoutParams1.topMargin = 0
                mBinding.ivHeader.layoutParams = layoutParams1
            } else {
                layoutParams.width =
                    resources.getDimension(R.dimen.dp_128).toInt()
                layoutParams.height =
                    resources.getDimension(R.dimen.dp_80).toInt()
                mBinding.ivHeaderCrown.layoutParams = layoutParams
                layoutParams1.topMargin =
                    resources.getDimension(R.dimen.dp_6).toInt()
                mBinding.ivHeader.layoutParams = layoutParams1
            }
            when (rank) {
                1 -> {
                    mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_no1)
                    mBinding.ivHeaderCrown.setBackgroundResource(R.mipmap.ic_avatar_no1)
                    if (isBest) {
                        mBinding.groupBest.visibility = VISIBLE
                    }
                }

                2 -> {
                    mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_no2)
                    mBinding.ivHeaderCrown.setBackgroundResource(R.mipmap.ic_avatar_no2)
                }

                3 -> {
                    mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_no3)
                    mBinding.ivHeaderCrown.setBackgroundResource(R.mipmap.ic_avatar_no3)
                }

                4 -> {
                    mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_no5)
                    mBinding.ivHeaderCrown.setBackgroundResource(R.mipmap.ic_avatar_no45)
                }

                5 -> {
                    mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_no5)
                    mBinding.ivHeaderCrown.setBackgroundResource(R.mipmap.ic_avatar_no45)
                }
            }
            Glide.with(context).load(skipRoseMultiResult.accountInfo.studentEntity!!.userFaceImgUrl)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default).into(mBinding.ivHeader)
            if (skipRoseMultiResult.accountInfo.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
                mBinding.ivHeaderBg.visible()
            }
            if (energyPoint > 0 && skipRoseMultiResult.accountInfo.studentEntity?.accountId != BusiBaseConstantsValue.VISITOR_USER_ID) {
                mBinding.layoutEnergy.root.visible()
                mBinding.layoutEnergy.tvEnergyNumber.text = "+${energyPoint}"
            }
            mBinding.tvName.text = skipRoseMultiResult.accountInfo.studentEntity?.accountName
            mBinding.tvCount.text = skipRoseMultiResult.count.toString()
        } ?: run {
            mBinding.ivBg.setBackgroundResource(R.mipmap.pic_cpt_resultpage_nobody)
            mBinding.tvNobody.visibility = VISIBLE
            mBinding.groupResult.visibility = GONE
        }
    }

    fun setReportOnclickListener(listener: OnClickListener) {
        reportClickListener = listener
    }

    fun showReportButton(show: Boolean) {
        mBinding.btnReport.visibility = if (show) VISIBLE else GONE
    }
}