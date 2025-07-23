package com.ty.tybusbase.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ty.tybusbase.R;

public class ArcCircleProgressView extends View {
    private Context mContext;
    /**
     * 文本画笔
     */
    private Paint mTextPaint;
    /**
     * 圆弧画笔
     */
    private Paint mArcCirclePaint;

    /**
     * 弧度
     */
    private int mAngleValue = 50;

    /**
     * 边宽
     */
    private int mStrokeWidth = getResources().getDimensionPixelOffset(R.dimen.dp_10);
    /**
     * 进度圆边宽
     */
    private int mInnerStrokeWidth = mStrokeWidth;


    public ArcCircleProgressView(Context context) {
        super(context);
        init(context);
    }

    public ArcCircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ArcCircleProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        mContext = context;

        mTextPaint  = new Paint();
        //设置抗锯齿
        mTextPaint.setAntiAlias(true);
        //使文本看起来更清晰
        mTextPaint.setLinearText(true);

        mArcCirclePaint  = new Paint();
        mArcCirclePaint.setAntiAlias(true);
        mArcCirclePaint.setStyle(Paint.Style.STROKE);

    }

    public void setStrokeWidth(int strokeWidth){
        this.mStrokeWidth = strokeWidth;
        invalidate();
    }

    public void setInnerStrokeWidth(int innerStrokeWidth){
        this.mInnerStrokeWidth = innerStrokeWidth;
        invalidate();
    }


    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress){
        int angleValue = (int) ((progress * 1.0)/100 * 360);
        if ( progress <= 100){
            mAngleValue  = angleValue;
        }else {
            mAngleValue = 360;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //1-圆弧的位置:整圆,再绘制进度圆弧
        mArcCirclePaint.setColor(Color.parseColor("#70ffffff"));
        mArcCirclePaint.setStrokeWidth(mStrokeWidth);
        //屏幕宽度
        int width = getMeasuredWidth();
        RectF rectF = new RectF();
        rectF.left = 0+mStrokeWidth/2;//左上角X
        rectF.top = 0+mStrokeWidth/2;//左上角Y
        rectF.right = width-mStrokeWidth/2;//右上角X
        rectF.bottom = width-mStrokeWidth/2;//右上角Y

        canvas.drawArc(rectF,270,360,false,mArcCirclePaint);

        mArcCirclePaint.setColor(Color.parseColor("#E94C71"));
        //设置边角为圆
        mArcCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mArcCirclePaint.setStrokeWidth(mInnerStrokeWidth);
        canvas.drawArc(rectF,270,mAngleValue,false,mArcCirclePaint);
    }
}
