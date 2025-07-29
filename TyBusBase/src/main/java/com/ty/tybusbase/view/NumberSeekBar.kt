package com.ty.tybusbase.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * @author zhouwenhua
 * @date 2023/9/25
 * @describe 带数字的进度条
 */
class NumberSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatSeekBar(context, attrs) {

    private val textPaint = TextPaint()

    init {
        textPaint.color = Color.WHITE
        textPaint.textSize = 22f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        val saveCount = canvas.save()
        val thumbTopY = thumb.bounds.top
        // 计算拖拽组件的中点
        val thumbCenterX =
            paddingStart + progress.toFloat() / max * (width - paddingStart - paddingEnd)
        canvas.drawText(progress.toString(), thumbCenterX, thumbTopY.toFloat() - 6, textPaint)
        canvas.restoreToCount(saveCount)
    }
}