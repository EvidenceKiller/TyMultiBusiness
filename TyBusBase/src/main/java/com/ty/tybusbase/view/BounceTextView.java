package com.ty.tybusbase.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BounceTextView extends androidx.appcompat.widget.AppCompatTextView {

    private AnimatorSet countAnimalSet;
    private boolean isBouncing;
    private boolean autoDismiss;
    private Runnable animateCallBack;
    public BounceTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BounceTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        countAnimalSet = new AnimatorSet();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "scaleX", 1.0F, 1.5F, 1.0F);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, "scaleY", 1.0F, 1.5F, 1.0F);
        countAnimalSet.setDuration(150);
        countAnimalSet.playTogether(objectAnimator1, objectAnimator2);
        countAnimalSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setTextColor(Color.WHITE);
                isBouncing = false;
                if (autoDismiss) {
                    setVisibility(View.GONE);
                }
                if(null!=animateCallBack){
                    animateCallBack.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setTextColor(Color.WHITE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void startBounce(int number) {
        startBounce(String.valueOf(number));
    }

    public void startBounce(String text) {
        setText(text);
        clearAnimation();
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        if (!isBouncing) {
            countAnimalSet.start();
            isBouncing = true;
        }
    }

    public void startBounce(String text, boolean autoDismiss,Runnable animationEndCallBack) {
        this.startBounce(text);
        this.autoDismiss = autoDismiss;
        this.animateCallBack=animationEndCallBack;
    }

}
