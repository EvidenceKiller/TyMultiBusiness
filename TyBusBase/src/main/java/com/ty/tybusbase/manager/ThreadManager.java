package com.ty.tybusbase.manager;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadManager {
    private static ThreadManager threadManager;
    private ExecutorService mExecutor;
    private HandlerThread handlerThread;
    private Handler backGroundHandler;

    public static ThreadManager getInstance() {
        if (threadManager == null) {
            synchronized (ThreadManager.class) {
                if (threadManager == null) {
                    threadManager = new ThreadManager();
                }
            }
        }
        return threadManager;
    }

    public void postBackTask(@NonNull Runnable runnable) {

        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        mExecutor.submit(runnable);
    }

    public void postBackTaskHandler(@NonNull Runnable runnable) {

        if (backGroundHandler == null) {
            handlerThread = new HandlerThread("BAXK");
            handlerThread.start();
            backGroundHandler = new Handler(handlerThread.getLooper());
        }
        backGroundHandler.post(runnable);
    }

    public void destory() {
        mExecutor = null;
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        if (backGroundHandler != null) {
            backGroundHandler.removeCallbacksAndMessages(null);
            handlerThread.quit();
            backGroundHandler = null;
            handlerThread = null;
        }

    }

    public void postDelayed(Runnable o, int i) {

        if (backGroundHandler == null) {
            handlerThread = new HandlerThread("BAXK");
            handlerThread.start();
            backGroundHandler = new Handler(handlerThread.getLooper());
        }
        backGroundHandler.postDelayed(o, i);
    }
}
