package com.ty.tybusbase.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import com.ty.tybusbase.base.SurfaceAnimationBase;
import com.ty.tybusbase.helper.TypeFaceHelper;
import com.ty.tybusbase.utils.ApplicationUtil;

/**
 * @author :WEI_CP
 * Time:2023/4/19 17:59
 * Description: this is SportCombleAnimal
 */
public class SportComboAnimal extends SurfaceAnimationBase {

    private TextPaint mPaint;
    private final int sizeDistance;
    private int positionX = 50;
    private int positionY = 500;
    private int comboCount;
    private boolean isLeft;

    public SportComboAnimal(int minSize, int maxSize, int positionX, int positionY, boolean isLeft) {
        mPaint = new TextPaint();
        mPaint.setTextSize(minSize);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setFakeBoldText(true);
        mPaint.setTypeface(TypeFaceHelper.Companion.getInstance().getYouSheBiaoTiHei(ApplicationUtil.INSTANCE.getContext()));
        sizeDistance = maxSize - minSize;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isLeft = isLeft;
    }

    public void combo() {
        reset();
        comboCount++;
        setPlay(true);
    }

    public int comboInterrupt() {
        int totalCombo = comboCount;
        comboCount = 0;
        return totalCombo;
    }

    @Override
    protected void onDraw(Canvas canvas, float percentage) {
//        if (percentage >= 0.5f) {
//            percentage = 1 + (1 - percentage);
//        } else {
//            percentage = 1 + percentage;
//        }
        // int currentSize = (int) (sizeDistance * percentage);
        // mPaint.setTextSize(currentSize);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (!isLeft) {
            float v = mPaint.measureText("X" + comboCount);
            canvas.drawText("X" + comboCount, 1920 - 50 - v, positionY - 44, mPaint);
            mPaint.setTextSize(44);
            // mPaint.setStyle(Paint.Style.FILL);
            float v1 = mPaint.measureText("COMBO");
            canvas.drawText("COMBO", 1920 - 50 - v1, positionY, mPaint);
            return;
        }
        canvas.drawText("X" + comboCount, positionX, positionY - 44, mPaint);
        mPaint.setTextSize(44);
        // mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("COMBO", positionX, positionY, mPaint);

    }
}
