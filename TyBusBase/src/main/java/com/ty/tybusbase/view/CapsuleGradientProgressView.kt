package com.ty.tybusbase.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.ty.tybusbase.R

class CapsuleGradientProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val rect = RectF()
    private var progress = 0f // 0 ~ 100
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33B0D8FF") // 底轨，可按设计稿微调
        style = Paint.Style.FILL
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CapsuleGradientProgressView).apply {
            progress = getFloat(R.styleable.CapsuleGradientProgressView_cgp_progress, 0f).coerceIn(
                    0f, 100f)
            recycle()
        }
    }

    /** 设置进度 0 ~ 100 */
    fun setProgress(percent: Float) {
        progress = percent.coerceIn(0f, 100f)
        invalidate()
    }

    fun setProgress(percent: Int) {
        setProgress(percent.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        progressPaint.shader = LinearGradient(0f, 0f, w.toFloat(), 0f, intArrayOf(
            Color.parseColor("#5CEBFF"), // 左：浅青
            Color.parseColor("#00C8FF"), // 右：深青
        ), null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paddingH = 0f
        val left = paddingLeft + paddingH
        val top = paddingTop.toFloat()
        val right = width - paddingRight - paddingH
        val bottom = height - paddingBottom.toFloat()
        val radius = (bottom - top) / 2f
        rect.set(left, top, right, bottom)
        // 1. 底轨
        canvas.drawRoundRect(rect, radius, radius, trackPaint)
        // 2. 进度
        if (progress <= 0f) return
        val progressRight = left + rect.width() * progress / 100f
        rect.set(left, top, progressRight, bottom)
        canvas.drawRoundRect(rect, radius, radius, progressPaint)
    }
}