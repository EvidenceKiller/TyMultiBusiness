package com.ty.tybusbase.view;



import static com.ty.tybusbase.constants.SportConstant.SPORT_COME_ON;
import static com.ty.tybusbase.constants.SportConstant.SPORT_PERFECT;
import static com.ty.tybusbase.constants.SportConstant.SPORT_TERRIFIC;
import static com.ty.tybusbase.expand.BusiExpandKt.getString;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;

import com.ty.tybusbase.R;
import com.ty.tybusbase.base.SurfaceAnimationBase;
import com.ty.tybusbase.constants.SportConstant;
import com.ty.tybusbase.helper.TypeFaceHelper;
import com.ty.tybusbase.utils.ApplicationUtil;

/**
 * @author :WEI_CP
 * Time:2023/4/19 16:47
 * Description: this is SportTextAnimation
 */
public class SportLevelAnimation extends SurfaceAnimationBase {
    private int startY;
    private int startX;
    private int distance;
    private TextPaint mPaint;
    private String levelStr;

    public SportLevelAnimation(int startY, int startX, int distance) {
        this.startY = startY;
        this.startX = startX;
        this.distance = distance;
        mPaint = new TextPaint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(44);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setFakeBoldText(true);
        mPaint.setTypeface(TypeFaceHelper.Companion.getInstance().getYouSheBiaoTiHei(ApplicationUtil.INSTANCE.getContext()));
    }

    public void setLevel(@SportConstant.SingleSportLevel int level) {
        reset();
        switch (level) {
            case SPORT_PERFECT:
                levelStr = getString(R.string.perfect);
                break;
            case SPORT_TERRIFIC:
                levelStr = getString(R.string.great);
                break;
            case SPORT_COME_ON:
                levelStr = getString(R.string.keep_it_up);
                break;
            default:
                levelStr = null;
        }
        setPlay(true);
    }

    @Override
    protected void onDraw(Canvas canvas, float percentage) {
        if (TextUtils.isEmpty(levelStr)) return;
        int currentY = startY;
        if (percentage != 0) {
            currentY = (int) (startY - distance * percentage);
            canvas.drawText(levelStr, startX, currentY, mPaint);
        }
        // mPaint.setAlpha((int)((1-percentage)*100));
    }
}
