package com.ty.bus.multi.core.view.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ItemLungSportScoreBinding
import com.ty.bus.multi.databinding.ViewLungBluetoothResultBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder

class LungBluetoothResultView(context: Context) :
    AbstractBluetoothResultView(context) {
    private val scoreList = mutableListOf<String>().apply { add("-") }
    private var mBinding = ViewLungBluetoothResultBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var bestScore = 0

    init {
        initView()
    }

    private fun initView() {
        mBinding.rvResult.layoutManager = LinearLayoutManager(context)
        mBinding.rvResult.adapter =
            object : BaseBindingAdapter<ItemLungSportScoreBinding, String>(scoreList) {
                @SuppressLint("SetTextI18n")
                override fun convert(
                    holder: VBViewHolder<ItemLungSportScoreBinding>,
                    item: String
                ) {
                    holder.vb.apply {
                        tvIndex.text = "${scoreList.size - holder.layoutPosition}"
                        tvScore.text = item
                        if (holder.layoutPosition == 0) {
                            root.setBackgroundResource(R.drawable.bg_lung_result_item_layer_list)
                            tvScore.setTextColor(context.getColor(R.color.white))
                            tvIndex.setTextColor(context.getColor(R.color.white))
                            tvScoreUnit.setTextColor(context.getColor(R.color.white_80))
                        } else {
                            root.setBackgroundResource(R.drawable.bg_lung_result_item_shape)
                            tvScore.setTextColor(context.getColor(R.color.white_60))
                            tvIndex.setTextColor(context.getColor(R.color.white_60))
                            tvScoreUnit.setTextColor(context.getColor(R.color.white_60))
                        }
                    }
                }
            }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun addResult(result: String) {
        val value0 = scoreList[0]
        if (value0 == "-") {
            scoreList[0] = result
        } else {
            scoreList.add(0, result)
        }
        (mBinding.rvResult.adapter as BaseBindingAdapter<*, *>).notifyDataSetChanged()
        bestScore = result.toInt().coerceAtLeast(bestScore)
        mBinding.tvBestResult.text = bestScore.toString()
    }

    override fun getSpeechResult(): String {
        val result = mBinding.tvBestResult.text.toString()
        return if (result != "-") {
            mBinding.tvBestResult.text.toString() + mBinding.tvResultUnit.text.toString()
        } else {
            ""
        }
    }


}