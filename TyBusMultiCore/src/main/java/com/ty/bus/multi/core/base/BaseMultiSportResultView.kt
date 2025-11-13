package com.ty.bus.multi.core.base

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.view.common.SportCardResultView
import com.ty.tybusbase.expand.Action1

abstract class BaseMultiSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    protected val resultCardArray = SparseArray<SportCardResultView>()

    var measureUnit: String = context.getString(R.string.ge)

    protected var reportClickListener:Action1<Int>? =null

    abstract fun getConfirmView():View
    /**
     * 设置按钮回调
     */
    fun setConfirmOnclickListener(listener: (view: View) -> Unit) {
        getConfirmView().setOnClickListener {
            listener.invoke(it)
        }
    }

    fun setReportOnclickListener(listener: (view: Int) -> Unit) {
        reportClickListener = listener
    }

    fun showReportButton(index:Int,show: Boolean) {
        resultCardArray.get(index).showReportButton(show)
    }

    fun setResult(resultArray: MutableList< out MultiSportResult>, top1Count: Int){
        setResult(resultArray,top1Count,getCount())
    }
    /**
     * 设置结果，需要先取出结果进行成绩排序，再设置卡片结果值
     */
    private fun setResult(resultArray: MutableList<out MultiSportResult>, top1Count: Int, count:Int) {
        val resultList = mutableListOf<MultiSportResult>()
        for (index in 0 until  count) {
            val skipRoseMultiResult = resultArray.getOrNull(index)
            if (skipRoseMultiResult != null) {
                //该位置有人
                resultList.add(skipRoseMultiResult)
            }
        }
        resultList.sortWith(compareBy { -it.count })
        var preRank = 0
        var preScore = -1
        for (index in resultList.indices){
            val skipRoseMultiResult = resultList[index]
            var rank = 0
            if (skipRoseMultiResult.count == preScore) {
                rank = preRank
            } else {
                rank = preRank + 1
            }
            preRank = rank
            preScore = skipRoseMultiResult.count
            resultCardArray.get(skipRoseMultiResult.number)?.setResult(rank,skipRoseMultiResult,skipRoseMultiResult.count > top1Count,measureUnit)
        }
    }

    abstract fun getCount():Int
}