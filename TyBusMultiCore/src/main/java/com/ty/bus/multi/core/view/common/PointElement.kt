package com.ty.bus.multi.core.view.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.adl.base.element.ElementBase
import com.ty.sport.common.basic.bean.Point

class PointElement : ElementBase() {

    private var pointsList: MutableList<Point>? = null
    private var rectList: MutableList<Rect>? = null
    private val paint = Paint()
    private val borderPaint = Paint()
    private val r = 5f
    private var previewWidth = 1920
    private var previewHeight = 1080

    init {
        paint.color = Color.BLUE
        paint.isAntiAlias = true

        borderPaint.color = Color.RED
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f
    }

    fun setPreviewSize(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
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
        val wRiot = viewWidth / previewWidth.toFloat()
        val hRiot = viewHeight / previewHeight.toFloat()
        pointsList?.forEach {
            it.x = it.x * wRiot
            it.y = it.y * hRiot
        }
        this.pointsList = pointsList
    }

    fun setRects(rect: MutableList<Rect>?) {
        val wRiot = viewWidth / previewWidth.toFloat()
        val hRiot = viewHeight / previewHeight.toFloat()
        rect?.forEach {
            it.left = (it.left * wRiot).toInt()
            it.top = (it.top * hRiot).toInt()
            it.right = (it.right * wRiot).toInt()
            it.bottom = (it.bottom * hRiot).toInt()
        }
        this.rectList = rect
    }
}