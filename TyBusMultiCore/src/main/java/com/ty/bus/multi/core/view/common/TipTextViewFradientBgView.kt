package com.ty.bus.multi.core.view.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class TipTextViewFradientBgView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    /**#00FFFF9B 11%, #87FDDB82 39%, #B7F8914F 66%, #E2FFFFFF 100%);**/

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        /**
         * background: linear-gradient(90deg, rgba(0, 92, 162, 0) 0%, rgba(0, 92, 162, 0.3456) 25%, rgba(0, 92, 162, 0.48) 50%, rgba(0, 92, 162, 0.3456) 75%, rgba(0, 92, 162, 0) 100%);
         *
         */

        val linGradient = LinearGradient(
            0f, 0f, width.toFloat(), 0f, intArrayOf(
                Color.parseColor("#00005CA2"),
                Color.parseColor("#58005CA2"),
                Color.parseColor("#7A005CA2"),
                Color.parseColor("#58005CA2"),
                Color.parseColor("#00005CA2"),
            ),
            floatArrayOf(0f,0.25f,0.5f,0.75f,1f), Shader.TileMode.CLAMP
        )
        paint.shader = linGradient
        canvas?.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(),26f,26f,paint)
    }
}