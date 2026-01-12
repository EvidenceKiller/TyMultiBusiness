package com.ty.bus.multi.core.view.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import com.ty.bus.multi.R

class IdentifyRectDragView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var rectBitmap: Bitmap? = null
    private var rect = Rect(0, 0, 420, 420)
    private var srcRect = Rect(0, 0, 420, 420)
    private var paint = Paint()

    //是否能拖动
    private var canDrag = true

    init {
        paint.isAntiAlias = true
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.IdentifyRectDragView)
        val res =
            attributes.getResourceId(R.styleable.IdentifyRectDragView_img_res, R.mipmap.ic_face_can)
        setRectBitmap(res)
    }

    /**
     * 使能拖动
     */
    fun enableDrag(value: Boolean) {
        canDrag = value
    }

    fun setRectBitmap(bitmap: Bitmap) {
        rectBitmap = bitmap
        postInvalidate()
    }

    fun setRectBitmap(res: Int) {
        rectBitmap = BitmapFactory.decodeResource(resources, res)
        post {
            val centerX = width / 2
            val centerY = height / 2
            val newTop = centerY - rect.centerY()
            val newLeft = centerX - rect.centerX()
            rect.offsetTo(newLeft, newTop)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectBitmap?.let {
            canvas.drawBitmap(it, null, rect, paint)
        }
    }

    private var preX = 0f
    private var preY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (canDrag.not()) {
            return super.onTouchEvent(event)
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                preX = event.x
                preY = event.y
                if (rect.contains(preX.toInt(), preY.toInt())) {
                    return true
                } else {
                    return super.onTouchEvent(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = preX - event.x
                val dy = preY - event.y
                val newLeft = (rect.left - dx).coerceAtLeast(0f).coerceAtMost((width - rect.width()).toFloat())
                val newTop = (rect.top - dy).coerceAtLeast(0f).coerceAtMost((height - rect.height()).toFloat())
                rect.offsetTo(newLeft.toInt(), newTop.toInt())
                preX = event.x
                preY = event.y
                invalidate()
            }
        }
        return true
    }

    /**
     * 获取识别框
     */
    fun getRect(cameraWidth: Int, cameraHeight: Int): Rect? {
        var xRiot = cameraWidth.toFloat() / width
        var yRiot = cameraHeight.toFloat() / height
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val newLeft = (centerX - rect.width() / 2) * xRiot
        val newTop = (centerY - rect.height() / 2) * yRiot
        val newRight = (centerX + rect.width() / 2) * xRiot
        val newBottom = (centerY + rect.height() / 2) * yRiot
        return RectF(newLeft, newTop, newRight, newBottom).toRect()
    }

}