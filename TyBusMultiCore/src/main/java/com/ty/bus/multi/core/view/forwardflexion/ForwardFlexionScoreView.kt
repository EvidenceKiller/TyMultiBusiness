package com.ty.bus.multi.core.view.forwardflexion

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ItemForwardFlexionScoreBinding
import com.ty.bus.multi.databinding.ViewForwardFlexionScoreBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.getString

class ForwardFlexionScoreView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)


    private var mBinding: ViewForwardFlexionScoreBinding
    private var scoreList: MutableList<Int> = mutableListOf()

    init {
        var view =
            LayoutInflater.from(context).inflate(R.layout.view_forward_flexion_score, this, true)
        mBinding = ViewForwardFlexionScoreBinding.bind(view)
        initView()
    }

    private fun initView() {
        mBinding.rvScore.layoutManager = LinearLayoutManager(context)
        mBinding.rvScore.adapter =
            object : BaseBindingAdapter<ItemForwardFlexionScoreBinding, Int>(scoreList) {
                @SuppressLint("SetTextI18n")
                override fun convert(
                    holder: VBViewHolder<ItemForwardFlexionScoreBinding>,
                    item: Int
                ) {
                    holder.vb.tvIndex.text = "${holder.layoutPosition + 1}"
                    holder.vb.tvScore.text = when {
                        item == -300 -> {
                            holder.vb.tvScore.setTextColor(Color.RED)
                            getString(R.string.violations)
                        }

                        item > -200 && item <= 400 -> {
                            holder.vb.tvScore.setTextColor(Color.WHITE)
                            "${item / 10.0}"
                        }

                        else -> {
                            holder.vb.tvScore.setTextColor(Color.RED)
                            getString(R.string.un_do)
                        }
                    }
                }

            }
    }


    /**
     * -20 ~ 400
     * -300 违规
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addScore(score: Int) {
        scoreList.add(score)
        mBinding.rvScore.adapter?.notifyDataSetChanged()
    }

    fun getScoreList(): List<Int> {
        return scoreList
    }

    fun setAccountInfo(accountInfo: AccountInfo) {
        Glide.with(context)
            .load(accountInfo.studentEntity?.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default)
            .into(mBinding.ivHeader)
        mBinding.tvName.text = accountInfo.studentEntity?.accountName
    }

    fun reset() {
        mBinding.ivHeader.setImageBitmap(null)
        mBinding.tvName.text = ""
        scoreList.clear()
        mBinding.rvScore.adapter?.notifyDataSetChanged()
    }

}