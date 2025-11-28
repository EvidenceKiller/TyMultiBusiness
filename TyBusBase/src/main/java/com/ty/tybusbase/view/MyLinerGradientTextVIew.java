package com.ty.tybusbase.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * @description:
 * @author: chenwei
 * @date :   2022/3/12 11:11 下午
 */
public class MyLinerGradientTextVIew extends AppCompatTextView {

    private Paint mPaint;
    private int mViewWidth = 0;
    private Rect mTextBound = new Rect();
    private int[] mGradientColors = new int[]{0xffFAB367, 0xffFDDD84, 0xffFFFFFC};
    private float[] mPositions = null;
    private LinearGradient mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0,
            mGradientColors,
            mPositions, Shader.TileMode.REPEAT);

    public MyLinerGradientTextVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mViewWidth = getMeasuredWidth();
        mPaint = getPaint();
        String mTipText = getText().toString();
        mPaint.getTextBounds(mTipText, 0, mTipText.length(), mTextBound);
        mPaint.setShader(mLinearGradient);
        canvas.drawText(mTipText, 0, getMeasuredHeight() / 2 + mTextBound.height() / 2, mPaint);
    }
    public void setmLinearGradient(LinearGradient mLinearGradient) {
        this.mLinearGradient = mLinearGradient;

    }

}