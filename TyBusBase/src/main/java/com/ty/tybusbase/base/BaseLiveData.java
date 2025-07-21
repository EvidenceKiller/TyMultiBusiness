package com.ty.tybusbase.base;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author: chenwei
 * @date :   2021/8/19 14:48
 */
public class BaseLiveData<T> extends MutableLiveData<T> {
    private static final String TAG = "SingleLiveEvent";

    private final AtomicBoolean mPending = new AtomicBoolean(false);
    /**
     * 是否只允许单次回调响应
     */
    public boolean isSingle = true;

    @Override
    @MainThread
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.");
        }

        // Observe the internal MutableLiveData
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (isSingle && mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }else if(!isSingle){
                    observer.onChanged(t);
                }
            }
        });
    }

    @Override
    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * 用于 T 为 Void 的情况，使调用更清晰
     */
    @MainThread
    public void call() {
        setValue(null);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }
}
