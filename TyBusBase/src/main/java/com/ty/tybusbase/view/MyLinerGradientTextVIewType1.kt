package com.ty.tybusbase.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Rect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView

/**
 * @description:
 * @author: chenwei
 * @date :   2022/3/12 11:11 下午
 */
class MyLinerGradientTextVIewType1(context: Context?, attrs: AttributeSet?) : AppCompatTextView(
    context!!, attrs
) {
    private var mLinearGradient: LinearGradient? = null
    private var mViewWidth = 0
    private var mViewHeight = 0
    private val mTextBound = Rect()

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        mViewWidth = measuredWidth
        mViewHeight = measuredHeight
        val mTipText = text.toString()
        paint.getTextBounds(mTipText, 0, mTipText.length, mTextBound)
        mLinearGradient = LinearGradient(
            0f, 0f, 0f, mViewHeight.toFloat(), intArrayOf(
                Color.parseColor("#EDDEAE"),
                Color.parseColor("#FFFD80"),
                Color.parseColor("#F3D26E"),
            ),
            floatArrayOf(0f,0.5f,1f), Shader.TileMode.REPEAT
        )
        paint.shader = mLinearGradient
        canvas.drawText(
            mTipText,
            (measuredWidth / 2 - mTextBound.width() / 2).toFloat(),
            (measuredHeight / 2 + mTextBound.height() / 2).toFloat(),
            paint
        )
    }
}