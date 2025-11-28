package com.ty.tybusbase.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class SkipMultiGradientRightView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    /**#00FFFF9B 11%, #87FDDB82 39%, #B7F8914F 66%, #E2FFFFFF 100%); floatArrayOf(0.11f,0.39f,0.66f,1f) **/
    /**#00FFFF9B 100%, #87FDDB82 66%, #B7F8914F 39%, #E2FFFFFF 11%);0.89 0.61 0.34 0**/

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val linGradient = LinearGradient(
            width.toFloat(), 0f, 0f, 0f, intArrayOf(
                Color.parseColor("#00FFFF9B"),
                Color.parseColor("#87FDDB82"),
                Color.parseColor("#B7F8914F"),
                Color.parseColor("#E2FFFFFF"),
            ),
            floatArrayOf(0.11f,0.39f,0.66f,1f), Shader.TileMode.CLAMP
        )
        paint.shader = linGradient
        canvas?.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(),26f,26f,paint)
    }
}