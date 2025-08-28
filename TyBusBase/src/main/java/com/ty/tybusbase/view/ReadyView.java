package com.ty.tybusbase.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.ty.tybusbase.R;
import com.ty.tybusbase.databinding.LayoutReadyBinding;
import com.ty.tybusbase.helper.SpeechWrapper;
import com.ty.tybusbase.utils.RxUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author pc
 */
public class ReadyView extends ConstraintLayout {
    private ReadyCallback readyCallback;

    private ScaleAnimation scaleAnimation;

    private Disposable disposable;
    private int count = 3;

    private boolean isStop;
    private boolean isReading;
    private LayoutReadyBinding vb;

    public ReadyView(Context context) {
        this(context, null);
    }

    public ReadyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ReadyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        vb = LayoutReadyBinding.inflate(LayoutInflater.from(context), this, true);
        scaleAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(context, R.anim.scale_ready);
        this.setVisibility(GONE);
        vb.root.setOnClickListener((view) -> {
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            vb.tvReady.setText(count + "");
            vb.tvReady.startAnimation(scaleAnimation);
            count -= 1;
            if (count != 0) {
                getHandler().postDelayed(runnable, 500);
            } else {
                if (readyCallback != null) {
                    stop();
                    readyCallback.callbackReady();
                }

            }
        }
    };


    public synchronized void startReady(ReadyCallback readyCallback) {

        if (null == getHandler() || isStop) {
            return;
        }

        this.readyCallback = readyCallback;
        if (isReading) {
            return;
        }
       setVisibility(VISIBLE);
        vb.tvReady.setText("");
        isReading = true;
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(aLong -> count - aLong)
                .compose(RxUtils.INSTANCE.schedulersTransformer())//ui线程中进行控件更新
                .subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long num) {
                if (num > 0) {
                    SpeechWrapper.INSTANCE.speech(num + "");
                }
                vb.tvReady.setText(num + "");
                vb.tvReady.startAnimation(scaleAnimation);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
//                TextToSpeechUtil.speech("开始");
                //如果已经停止，则返回
                if (isStop) return;
                vb.tvReady.setText("GO");
                SpeechWrapper.INSTANCE.speech("开始");
                //回复原来初始状态
                if (readyCallback != null) {
                    stop();
                    readyCallback.callbackReady();
                }
            }
        });
    }


    public void stop() {
        this.setVisibility(GONE);
        isStop = true;
        readyCallback = null;
        isReading = false;
        if (runnable != null && getHandler() != null) {
            getHandler().removeCallbacksAndMessages(runnable);
        }
        if (scaleAnimation != null) {
            scaleAnimation.cancel();
        }
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    public void reset(){
        isStop = false;
    }

    public interface ReadyCallback {
        void callbackReady();
    }
}
