package com.ty.tybusbase.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridSpaceDecoration(private val vSpace: Int, private val hSpace:Int):RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        
        val position = parent.getChildAdapterPosition(view)
        var mSpanCount = 1
        val layoutM = parent.layoutManager
        if (layoutM is GridLayoutManager) {
            mSpanCount = layoutM.spanCount
        }
        val column = position % mSpanCount // 列数
        // 第一行顶部间距为0
        outRect.top = if (position < mSpanCount) 0 else vSpace
        outRect.left = column * hSpace / mSpanCount
        outRect.right = hSpace - (column + 1) * hSpace / mSpanCount
    }
}