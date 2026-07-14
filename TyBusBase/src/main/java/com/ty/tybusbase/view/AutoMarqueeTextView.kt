package com.ty.tybusbase.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView

class AutoMarqueeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle) : AppCompatTextView(context, attrs,
    defStyleAttr) {

    /** 滚动速度：dp / 秒 */
    private val speedDpPerSec = 20f

    /** 两段文字间距：dp */
    private val gapDp = 40f

    private val density = resources.displayMetrics.density

    private val speedPxPerSec get() = speedDpPerSec * density
    private val gapPx get() = gapDp * density

    private var offsetX = 0f
    private var textWidth = 0f
    private var viewWidth = 0f
    private var lastFrameTimeNs = 0L
    private var running = false
    /** 子类字段初始化完成前，禁止触碰 animator */
    private var ready = false

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = Long.MAX_VALUE / 2
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            if (!shouldMarquee()) return@addUpdateListener
            val now = System.nanoTime()
            if (lastFrameTimeNs == 0L) {
                lastFrameTimeNs = now
                return@addUpdateListener
            }
            val dtSec = (now - lastFrameTimeNs) / 1_000_000_000f
            lastFrameTimeNs = now
            // 限幅，避免卡顿后一次跳太远
            val delta = speedPxPerSec * dtSec.coerceIn(0f, 0.05f)
            offsetX -= delta
            val loopWidth = textWidth + gapPx
            while (offsetX <= -loopWidth) {
                offsetX += loopWidth
            }
            invalidate()
        }
    }

    init {
        // 跑马灯通常按单行处理
        maxLines = 1
        ellipsize = null
        isSingleLine = true
        ready = true
        measureTextWidth()
        updateMarqueeState()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if (!ready) return
        measureTextWidth()
        updateMarqueeState()
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int,
        lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (!ready) return
        measureTextWidth()
        updateMarqueeState()
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        if (!ready) return
        measureTextWidth()
        updateMarqueeState()
    }

    override fun setTypeface(tf: android.graphics.Typeface?) {
        super.setTypeface(tf)
        if (!ready) return
        measureTextWidth()
        updateMarqueeState()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = (w - paddingLeft - paddingRight).coerceAtLeast(0).toFloat()
        measureTextWidth()
        updateMarqueeState()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateMarqueeState()
    }

    override fun onDetachedFromWindow() {
        stopMarquee()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateMarqueeState()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        updateMarqueeState()
    }

    override fun onDraw(canvas: Canvas) {
        if (!shouldMarquee()) {
            super.onDraw(canvas)
            return
        }
        val content = text?.toString()
        if (TextUtils.isEmpty(content)) {
            super.onDraw(canvas)
            return
        }
        val save = canvas.save()
        // 只在内容区绘制，避免画到 padding / 邻接控件
        canvas.clipRect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        val x = paddingLeft + offsetX
        val y = baseline.toFloat()
        val loopWidth = textWidth + gapPx
        paint.color = currentTextColor
        canvas.drawText(content!!, x, y, paint)
        canvas.drawText(content, x + loopWidth, y, paint)
        canvas.restoreToCount(save)
    }

    private fun measureTextWidth() {
        textWidth = paint.measureText(text?.toString() ?: "")
    }

    private fun shouldMarquee(): Boolean {
        return textWidth > viewWidth && viewWidth > 0f && !TextUtils.isEmpty(text)
    }

    private fun updateMarqueeState() {
        if (!ready) return
        val needRun = shouldMarquee() && isAttachedToWindow && windowVisibility == VISIBLE && isShown
        if (needRun) {
            startMarquee()
        } else {
            stopMarquee()
            // 文本未超宽或不显示时，回到默认位置
            if (!shouldMarquee()) {
                offsetX = 0f
            }
        }
    }

    private fun startMarquee() {
        if (running) return
        running = true
        lastFrameTimeNs = 0L
        if (offsetX == 0f) {
            // 从右侧内容起点开始滚（与 padding 对齐）
            offsetX = 0f
        }
        if (!animator.isStarted) {
            animator.start()
        } else if (animator.isPaused) {
            animator.resume()
        }
    }

    private fun stopMarquee() {
        if (!running && !animator.isStarted) return
        running = false
        lastFrameTimeNs = 0L
        if (animator.isStarted) {
            animator.cancel()
        }
    }
}