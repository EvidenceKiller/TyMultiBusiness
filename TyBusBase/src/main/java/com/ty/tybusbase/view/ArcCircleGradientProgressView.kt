package com.ty.tybusbase.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ArcCircleGradientProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val colors = intArrayOf(
        Color.parseColor("#00E5FF"), // 青
        Color.parseColor("#7CFC00"), // 绿
        Color.parseColor("#FFD700"), // 黄
        Color.parseColor("#FF8C00"), // 橙
        Color.parseColor("#00E5FF"))  // 闭合，360° 更平滑

    private val positions = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)

    private var sweepGradient: SweepGradient? = null

    private var progress = 0f          // 0 ~ 100
    private val startAngle = 270f      // 12 点方向
    private val arcRect = RectF()
    private val strokeWidth = resources.getDimension(com.ty.tybusbase.R.dimen.dp_12)

    private var defaultText: String = ""
    private var defaultStatus: Boolean = true

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@ArcCircleGradientProgressView.strokeWidth
        color = Color.parseColor("#33B0D8FF")   // 底环，按设计稿微调
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@ArcCircleGradientProgressView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    init {
        val defaultTextSize = resources.getDimension(com.ty.tybusbase.R.dimen.sp_24)
        context.obtainStyledAttributes(attrs, com.ty.tybusbase.R.styleable.ArcCircleGradientProgressView)
            .apply {
                textPaint.textSize = getDimension(com.ty.tybusbase.R.styleable.ArcCircleGradientProgressView_acgp_textSize, defaultTextSize)
                defaultText = getString(com.ty.tybusbase.R.styleable.ArcCircleGradientProgressView_acgp_text) ?: ""
                recycle()
            }
    }

    fun resetStatus() {
        defaultStatus = true
        progress = 0f
        invalidate()
    }

    /** 设置百分比 0~100 */
    fun setPercent(percent: Int) {
        defaultStatus = false
        progress = percent.coerceIn(0, 100).toFloat()
        invalidate()
    }

    /** 设置百分比 0~100 */
    fun setPercent(percent: Float) {
        defaultStatus = false
        progress = percent.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 保证正圆
        val size = min(MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        val cx = w / 2f
        val cy = h / 2f
        sweepGradient = SweepGradient(cx, cy, colors, positions).apply {
            setLocalMatrix(Matrix().apply { setRotate(-90f, cx, cy) })
        }
        progressPaint.shader = sweepGradient
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - strokeWidth / 2f
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
        // 1. 底环
        canvas.drawArc(arcRect, startAngle, 360f, false, trackPaint)
        // 2. 渐变进度环
        if (progress > 0f) {
            canvas.drawArc(arcRect, startAngle, progress / 100f * 360f, false, progressPaint)
        }
        // 3. 居中百分比
        val text = if (defaultStatus) {
            defaultText
        } else {
            formatHitRateText(progress)
        }
        val fontMetrics = textPaint.fontMetrics
        val baseline = cy - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, cx, baseline, textPaint)
    }

    private fun formatHitRateText(progress: Float): String {
        val scaled = kotlin.math.round(progress * 10).toInt()   // 先保留 1 位小数
        return if (scaled % 10 == 0) {
            "${scaled / 10}%"
        } else {
            String.format("%.1f%%", scaled / 10f)
        }
    }
}