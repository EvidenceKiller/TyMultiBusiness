package com.ty.tybusbase.utils;


import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.ty.tybusbase.base.BaseLiveData;

/**
 * @author :WEI_CP
 * Time:2023/4/20 15:06
 * Description: this is Timer
 */
public class Timer implements LifecycleEventObserver {

    private boolean pause;
    private boolean stop;
    private Lifecycle mLifecycle;

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        mLifecycle = source.getLifecycle();
        if (event == (Lifecycle.Event.ON_STOP)) {
            source.getLifecycle().removeObserver(this);
            stop();
        }

    }

    public enum TimeModel {
        time,
        countdownTime
    }

    private Handler handler;
    private long countdownTime;
    //每秒回调
    public BaseLiveData<Long> timeTick = new BaseLiveData<>();
    private boolean timeInit = false;
    //结束时回调，可能是自动结束也可能是手动结束
    public BaseLiveData<Long> timeDown = new BaseLiveData<>();

    public void start(TimeModel model, long timeSecondS) {
        //如果已经停止，则不处理开始
        if (stop) return;
        if (model == TimeModel.countdownTime) {
            if (timeSecondS > 1) {
                countdownTime = timeSecondS * 1000;
            } else {
                LogUtil.INSTANCE.e("倒计时模式下，初始时间不能小于1s",false);
                return;
            }
        }
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper(), msg -> {
                if (msg.what == 1) {
                    timeTick.setValue(!timeInit ? 0 : timeTick.getValue() + 1000);
                    timeInit = true;
                    if (model == TimeModel.countdownTime && timeTick.getValue() == countdownTime)
                        stop();
                    else
                        handler.sendEmptyMessageDelayed(1, 1000);
                }
                return false;
            });
        }
        handler.sendEmptyMessage(1);
    }

    public void pause() {
        pause = true;
        if (handler != null) handler.removeMessages(1);
    }

    public void resume() {
        if (pause && handler != null) {
            handler.sendEmptyMessageDelayed(1, 1000);
        }

    }

    public synchronized void stop() {
        if (stop) return;
        stop = true;
        if (mLifecycle != null) mLifecycle.removeObserver(this);
        if (handler != null) {
//            handler.removeMessages(1);
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (timeDown != null && timeTick != null) {
            timeDown.setValue(timeInit ? timeTick.getValue() : 0);
            timeDown = null;
            timeTick = null;
        }


    }


}
