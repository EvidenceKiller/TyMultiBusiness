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
class MyLinerGradientTextVIewType2(context: Context?, attrs: AttributeSet?) : AppCompatTextView(
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
        /***
         * intArrayOf(
         *                 Color.parseColor("#FFFF69"),
         *                 Color.parseColor("#F56933"),
         *             )
         */
        mLinearGradient = LinearGradient(
            0f,
            mViewHeight.toFloat(),
            mViewWidth.toFloat(),
            0f,
            Color.parseColor("#F56933"),
            Color.parseColor("#FFFF69"),
            Shader.TileMode.CLAMP
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