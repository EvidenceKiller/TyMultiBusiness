package com.ty.bus.multi.core.view.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.adl.base.element.ElementBase
import com.adl.sport.common.bean.Point

class PointElement : ElementBase() {

    private var pointsList: MutableList<Point>? = mutableListOf(Point(860f, 540f), Point(760f, 540f))
    private var rect: Rect? = null
    private val paint = Paint()
    private val r = 5f

    init {
        paint.color = Color.BLUE
        paint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas?) {
        pointsList?.forEach {
            canvas?.drawCircle(it.x, it.y, r, paint)
        }

        rect?.let {
            canvas?.drawRect(it, paint)
        }

        canvas?.drawCircle(960f, 540f, r, paint)
    }

    fun setPoints(pointsList: MutableList<Point>?) {
        this.pointsList = pointsList
    }

    fun setRect(rect: Rect?) {
        this.rect = rect
    }
}