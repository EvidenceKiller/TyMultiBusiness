package com.ty.bus.multi.core.view.foursport

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.FourSportResultViewBinding
import com.ty.bus.multi.databinding.ItemFourSportResultBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.onDebouncedClick
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.helper.TypeFaceHelper

class FourSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs,
    defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val yousheFont = TypeFaceHelper.instance?.getYouSheFont(context)

    private val backgroundList: List<Int> = listOf(
        R.mipmap.pic_cpt_resultpage_no1,
        R.mipmap.pic_cpt_resultpage_no3,
        R.mipmap.pic_cpt_resultpage_no2,
        R.mipmap.pic_cpt_resultpage_no5,
    )

    private val mBinding = FourSportResultViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var resultList: List<MultiSportResult?>? = null

    init {
        initView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SpeechWrapper.stop()
    }

    fun setConfirmOnclickListener(listener: (view: View) -> Unit) {
        mBinding.tvConfirm.onDebouncedClick {
            listener.invoke(it)
        }
    }

    fun setResult(resultList: List<MultiSportResult>) {
        this.resultList = resultList
        (mBinding.rvResult.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(resultList.toMutableList())
    }

    fun setResultTitleText(text: String?) {
        if (TextUtils.isEmpty(text).not()) {
            mBinding.mtvFinish.text = text
        }
    }

    private fun initView() {
        mBinding.mtvFinish.typeface = yousheFont

        mBinding.rvResult.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvResult.adapter = object : BaseBindingAdapter<ItemFourSportResultBinding, MultiSportResult>() {
            override fun convert(holder: VBViewHolder<ItemFourSportResultBinding>, item: MultiSportResult) {
                val position = holder.adapterPosition
                if (item.accountInfo.studentEntity == null) {
                    holder.vb.root.setBackgroundResource(R.mipmap.pic_cpt_resultpage_nobody)
                    holder.vb.tvResult.text = context.getString(R.string.idle_state)
                    holder.vb.tvResult.textSize = context.resources.getDimension(R.dimen.sp_30)
                    holder.vb.tvResult.setTextColor(context.getColor(R.color.white_60))
                    holder.vb.tvResultUnit.gone()
                    holder.vb.ivHeader.gone()
                    holder.vb.ivHeaderBg.gone()
                    holder.vb.tvName.gone()
                    return
                }
                holder.vb.root.setBackgroundResource(backgroundList.getOrElse(position) { backgroundList.last() })
                holder.vb.tvName.text = item.accountInfo.studentEntity?.accountName
                holder.vb.tvResult.text = item.count.toString()

                holder.vb.tvResult.typeface = yousheFont
                holder.vb.tvResultUnit.typeface = yousheFont

                Glide.with(context)
                    .load(item.accountInfo.studentEntity?.userFaceImgUrl)
                    .placeholder(R.mipmap.ic_head_default)
                    .error(R.mipmap.ic_head_default)
                    .into(holder.vb.ivHeader)
                if (item.accountInfo.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
                    holder.vb.ivHeaderBg.visible()
                } else {
                    holder.vb.ivHeaderBg.gone()
                }
            }
        }
        val space = resources.getDimensionPixelSize(R.dimen.dp_70)
        mBinding.rvResult.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                if (position != RecyclerView.NO_POSITION && position < state.itemCount - 1) {
                    outRect.right = space
                }
            }
        })
    }

}
