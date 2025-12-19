package com.ty.bus.multi.core.view.doublesport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.ty.tybusbase.callback.OnRefreshObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :WEI_CP
 * Time:2023/4/19 13:42
 * Description: this is AnimationPower
 * 通过固定帧率驱动动画执行，在子线程中绘制动画
 */
public class SurfaceOverlay extends SurfaceView implements LifecycleEventObserver {

    private final String tag="surface";
    private List<OnRefreshObserver> observers = new ArrayList<>();
    private HandlerThread thread ;
    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private boolean stop;
    private int fps ;
    private int fpsInterval;
    private boolean isInit;
    private Lifecycle lifecycle;

    public SurfaceOverlay(Context context) {
        this(context,null);
    }

    public SurfaceOverlay(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SurfaceOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public SurfaceOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setFps(int fps) {
        if(fps>10&&fps<=30){
            this.fps = fps;
            fpsInterval = 1000/fps;
        }
    }

    private void  init(){
        fps = 30;
        fpsInterval = 1000/fps;
        mSurfaceHolder = getHolder();
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                thread = new HandlerThread("AnimationPower");
                thread.start();
                mHandler = new Handler(thread.getLooper());
                isInit = true;
                //有订阅者就自动启动，没有就自动暂停
                if(observers!=null&&observers.size()>0){
                    mHandler.post(refreshRunnable);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                stop = true;
            }
        });
    }

    public void unSubscribe(OnRefreshObserver observer){
        if(observers!=null&&observers.size()>0&&observers.contains(observer)){
            observers.remove(observer);
        }
    }

    public void start(){
        if(isInit&&observers!=null&&observers.size()>0){
            mHandler.post(refreshRunnable);
        }
    }
    public boolean subscribe(OnRefreshObserver observer){
        if(observers!=null&&!observers.contains(observer)){
            observers.add(observer);
            return true;
        }
        return false;
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if(stop)return;
            if(mSurfaceHolder==null)return;
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if(canvas==null)return;
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (OnRefreshObserver observer:observers) {
                boolean b = observer.onRefreshTick(canvas);
                if(!b){observers.remove(observer);}
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            if(observers!=null&&observers.size()>0&&!stop){
                mHandler.postDelayed(refreshRunnable,fpsInterval);
            }
        }
    };

    public void resume() {
        if(observers!=null&&observers.size()>0&&!stop){
            mHandler.postDelayed(refreshRunnable,fpsInterval);
        }
    }

    public void pause() {
        if(mHandler!=null)
            mHandler.removeCallbacksAndMessages(null);
    }



    public void stop(){
        stop = true;
        if(lifecycle!=null){
            lifecycle.removeObserver(this);
            lifecycle = null;
        }
        if(thread!=null){
            thread.quit();
            thread=null;
        }
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler=null;
        }
        if(mSurfaceHolder!=null){
            mSurfaceHolder.removeCallback(null);
        }
    }


    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        lifecycle  = source.getLifecycle();
        if(event.equals(Lifecycle.Event.ON_STOP)){
            stop();
        }
    }


}
