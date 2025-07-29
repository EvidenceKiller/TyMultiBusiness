package com.ty.tybusbase.view.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.ty.tybusbase.R;


/**
 * @author :WEI_CP
 * Time:2023/1/12 9:30
 * Description: this is WhiteboradSurfaceView
 */
public class WhiteboardView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;

    private HandlerThread mHandlerThread;

    private Handler mHandler;

    //画笔的颜色，默认设置为红色
    private @PenSettingPopWindow.PenColor
    String paintColor = PenSettingPopWindow.RED;

    //画笔的粗细，默认设置为最细
    private @PenSettingPopWindow.PenWidth
    int penWidth = PenSettingPopWindow.SMALL;

    //橡皮檫粗细
    private @EraserPopWindow.EraserWidth
    int eraserWidth = EraserPopWindow.SMALL;

    //主画笔
    private Paint mPaint;

    //触点画笔
    private Paint mFingerPaint;

    //缓存图片
    private volatile Bitmap mBufferBitmap;

    //缓存画布
    private Canvas mBufferCanvas;

    private Path mPath;
    private boolean fingerUp;

    //画笔模式
    public static final int PEN = 0;

    //橡皮模式
    public static final int ERASER = 1;
    @IntDef({PEN, ERASER})
    public @interface PaintModel {
    }

    //模式
    private @PaintModel
    int mPaintModel = PEN;

    //上一次的触摸点x坐标
    private float preX = 0.0f;

    private float preY = 0.0f;

    public WhiteboardView(Context context) {
        this(context, null);
    }

    public WhiteboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBufferCanvas == null) {
            mBufferBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            mBufferCanvas = new Canvas(mBufferBitmap);
        }

    }

    private void init() {
        this.setFocusable(true);
        this.setZOrderOnTop(true);
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        mFingerPaint = new Paint();
        mFingerPaint.setStrokeCap(Paint.Cap.ROUND);
        mFingerPaint.setStyle(Paint.Style.FILL);
        mFingerPaint.setAntiAlias(true);
        mFingerPaint.setAlpha(80);

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        setModel(mPaintModel);
    }

    public void setModel(@PaintModel int model) {
        mPaintModel = model;
        if (mPaintModel == PEN) {
            mPaint.setXfermode(null);
            setPaintColor(paintColor);
            setPenWidth(penWidth);
        } else {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            setEraserWidth(eraserWidth);
        }
    }

    public void setPaintColor(@PenSettingPopWindow.PenColor String paintColor) {
        this.paintColor = paintColor;
        mPaint.setColor(Color.parseColor(paintColor));
    }

    public void setPenWidth(@PenSettingPopWindow.PenWidth int penWidth) {
        this.penWidth = penWidth;
        mPaint.setStrokeWidth(penWidth);
    }

    public @PenSettingPopWindow.PenColor
    String getPaintColor() {
        return paintColor;
    }

    public @PenSettingPopWindow.PenWidth
    int getPenWidth() {
        return penWidth;
    }

    public@EraserPopWindow.EraserWidth
    int getEraserWidth() {
        return eraserWidth;
    }

    public@PaintModel
    int getPaintModel() {
        return mPaintModel;
    }

    public void setEraserWidth(@EraserPopWindow.EraserWidth int eraserWidth) {
        this.eraserWidth = eraserWidth;
        mPaint.setStrokeWidth(eraserWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();
        fingerUp = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                if(event.getPointerCount()>=2){
//                    setModel(ERASER);
//                }
                Log.e("模式", "onTouchEvent: "+event.getPointerCount() );
                //手指按下的时候
                //将起始点移动到当前坐标
                mPath.moveTo(currentX, currentY);
                //记录上次触摸的坐标
                preX = currentX;
                preY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(preX, preY, currentX, currentY);
                //在缓存里面绘制
                mBufferCanvas.drawPath(mPath, mPaint);
                preX = currentX;
                preY = currentY;
                break;
            case MotionEvent.ACTION_UP :
                //清除路径的内容
                mPath.reset();
                fingerUp = true;

                //setModel(PEN);
        }
        mHandler.post(drawTask);

        return true;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mHandlerThread = new HandlerThread("whiteboardTh");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mBufferBitmap != null && !mBufferBitmap.isRecycled()) {
            mBufferBitmap.recycle();
            mBufferBitmap = null;
        }
    }

    private Runnable drawTask = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(drawTask);
            Canvas canvas = mHolder.lockCanvas();
            if (canvas == null) return;
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if(!fingerUp){
                if(mPaintModel==PEN){
                    mFingerPaint.setStyle(Paint.Style.FILL);
                    mFingerPaint.setColor(Color.parseColor(paintColor));
                    canvas.drawCircle(preX,preY,30,mFingerPaint);
                    mFingerPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(preX,preY,40,mFingerPaint);
                }else {
                    mFingerPaint.setColor(getResources().getColor(R.color.white_30));
                    mFingerPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(preX,preY,eraserWidth/4,mFingerPaint);
                    mFingerPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(preX,preY,eraserWidth/2,mFingerPaint);
                }
            }
            try{
                if(mBufferBitmap!=null){
                    canvas.drawBitmap(mBufferBitmap, 0, 0, null);
                }

            }catch (Exception e){
                e.printStackTrace();
            }


            mHolder.unlockCanvasAndPost(canvas);
        }
    };

    public void clearWhiteBoard(){
        if(mBufferCanvas==null||mPath==null||mBufferCanvas==null||mHandler==null)return;
        mBufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mPath.reset();
        mHandler.post(drawTask);
    }
}
