package com.ty.tybusbase.base;

import android.graphics.Canvas;
import android.view.animation.Interpolator;

import com.ty.tybusbase.callback.OnRefreshObserver;


/**
 * @author :WEI_CP
 * Time:2023/4/19 15:06
 * Description: this is SurfaceAnimationBase
 */
public abstract class SurfaceAnimationBase implements OnRefreshObserver {
    private int duration = 1000;
    private long startTime;
    private AnimationListener animationListener;
    private Interpolator mInterpolator;
    private boolean finish;
    private  boolean play;
    public void setPlay(boolean play) {
        this.play = play;
    }


    public void setAnimationListener(AnimationListener animationListener) {
        this.animationListener = animationListener;
    }

    public void setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        this.duration = duration;
    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    @Override
    public boolean onRefreshTick(Canvas canvas) {
        if(!play)return true;
        if(finish)return true;
        long currentTime = System.currentTimeMillis();
        float percentage = 0;
        if (startTime == 0) {
            startTime = currentTime;
            if(animationListener!=null)animationListener.onAnimationStart();
            percentage = 0;
        }else {
            percentage = getAnimationValue(currentTime);
        }
        onDraw(canvas,percentage);
        if (System.currentTimeMillis() - startTime >= duration) {
            if(animationListener!=null)animationListener.onAnimationEnd();
            finish = true;
           // return false;
        }
        return true;
    }

    protected  float getAnimationValue(long current){
        long time = current - startTime;
        if(time >=duration){
            return 1;
        }
        float percentage = (float) time / duration;
        if(mInterpolator!=null){
           return mInterpolator.getInterpolation(percentage);
        }
        return percentage;
    }

    protected abstract void onDraw(Canvas canvas,float percentage);

    public static interface AnimationListener {

        void onAnimationStart();

        void onAnimationEnd();

    }
    public void reset(){
        finish=false;
        startTime = 0;
    }
}
