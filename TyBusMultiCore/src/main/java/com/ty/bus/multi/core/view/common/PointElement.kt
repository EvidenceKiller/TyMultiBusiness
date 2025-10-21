package com.ty.bus.multi.core.view.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.adl.base.element.ElementBase
import com.adl.sport.common.bean.Point

class PointElement : ElementBase() {

    private var pointsList: MutableList<Point>? = null
    private var rectList: MutableList<Rect>? = null
    private val paint = Paint()
    private val borderPaint = Paint()
    private val r = 5f

    init {
        paint.color = Color.BLUE
        paint.isAntiAlias = true

        borderPaint.color = Color.RED
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
    }

    override fun draw(canvas: Canvas?) {
        pointsList?.forEach {
            canvas?.drawCircle(it.x, it.y, r, paint)
        }

        rectList?.forEach {
            canvas?.drawRect(it, borderPaint)
        }
    }

    fun setPoints(pointsList: MutableList<Point>?) {
        this.pointsList = pointsList
    }

    fun setRects(rect: MutableList<Rect>?) {
        this.rectList = rect
    }
}