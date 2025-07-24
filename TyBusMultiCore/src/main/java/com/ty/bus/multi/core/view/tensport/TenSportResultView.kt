package com.ty.bus.multi.core.view.tensport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.aipower.modulecommon.expand.Action1
import com.aipower.modulecommon.expand.getString
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ItemTenSportResult2Binding
import com.ty.bus.multi.databinding.ItemTenSportResultBinding
import com.ty.bus.multi.databinding.TenSportResultViewBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder

class TenSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var measureUnit: String = getString(R.string.ge)

    private val mBinding: TenSportResultViewBinding
    var top1Count: Int = Int.MAX_VALUE

    protected var reportClickListener:Action1<Int>? =null

    init {
        mBinding = TenSportResultViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    fun setReportOnclickListener(listener: (view: Int) -> Unit) {
        reportClickListener = listener
    }
    private fun initView() {
        mBinding.rvResult1.layoutManager = LinearLayoutManager(context)
        mBinding.rvResult1.adapter =
            object : BaseBindingAdapter<ItemTenSportResultBinding, MultiSportResult>() {
                override fun convert(
                    holder: VBViewHolder<ItemTenSportResultBinding>,
                    item: MultiSportResult
                ) {
                    holder.vb.tvVisitorNumber.text =
                        getString(
                            R.string.visitor_number,
                            item.number + 1
                        )
                    holder.vb.tvVisitorNumber.visibility = VISIBLE

                    when (item.rank) {
                        1 -> {
                            if (top1Count < item.count) {
                                holder.vb.groupBest.visibility = VISIBLE
                            }
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no1)
                        }

                        2 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no2)
                        }

                        3 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no3)
                        }
                    }
                    holder.vb.tvResult.text = "${item.count}$measureUnit"
                    holder.vb.tvRank.text = "${item.rank}"
                    holder.vb.tvName.text = item.accountInfo.userName
                    Glide.with(context)
                        .load(item.accountInfo.userImg)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.ivHeader)
                    holder.vb.btnReport.setOnClickListener { reportClickListener?.invoke(item.number) }
                }
            }
        mBinding.rvResult2.layoutManager = LinearLayoutManager(context)
        mBinding.rvResult2.adapter =
            object : BaseBindingAdapter<ItemTenSportResult2Binding, MultiSportResult>() {
                override fun convert(
                    holder: VBViewHolder<ItemTenSportResult2Binding>,
                    item: MultiSportResult
                ) {
                    holder.vb.tvVisitorNumber.text =
                        getString(
                            R.string.visitor_number,
                            item.number + 1
                        )
                    holder.vb.tvVisitorNumber.visibility = VISIBLE
                    holder.vb.tvResult.text = "${item.count}$measureUnit"
                    holder.vb.tvRank.text = "${item.rank}"
                    holder.vb.tvName.text = item.accountInfo.userName
                    Glide.with(context)
                        .load(item.accountInfo.userImg)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.ivHeader)
                    holder.vb.btnReport.setOnClickListener { reportClickListener?.invoke(item.number) }
                }
            }
    }

    fun setConfirmOnclickListener(listener: (view: View) -> Unit) {
        mBinding.tvConfirm.setOnClickListener {
            listener.invoke(it)
        }
    }


    /**
     * 设置结果，需要先取出结果进行成绩排序，再设置卡片结果值
     */
    fun setResult(
        resultList: MutableList<MultiSportResult>,
        top1Count: Int,
    ) {
        this.top1Count = top1Count
        if (resultList.size > 5) {
            val list = resultList.subList(0, 5)
            (mBinding.rvResult1.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                list
            )
            val list2 = resultList.subList(5, resultList.size)
            (mBinding.rvResult2.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                list2
            )
            mBinding.rvResult2.visibility = VISIBLE
        } else {
            (mBinding.rvResult1.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                resultList
            )
        }

    }
}