package com.ty.tybusbase.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ty.tybusbase.R;


/**
 * Created by daxieAdmin on 2018/3/27.
 */

public class ToastUtil {

    private Toast mToast;
    private static TextView mTextView;
    private static ImageView mImageView;
    private static LinearLayout mLinearLayout;
    private TimeCount timeCount;
    private String message;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean canceled = true;
    private static ToastUtil instance;

    public static ToastUtil getInstance(Context context, String msg) {
        return getInstance(context.getApplicationContext(), msg, false);
    }

    public static void showErrorHint(Context context, String error) {
        getInstance(context.getApplicationContext(),
                error, true).show();
    }

    public static ToastUtil getInstance(Context context, String msg, boolean isErr) {
        return getInstance(context.getApplicationContext(), msg, isErr ? R.mipmap.toast_err_bg : 0, false);
    }

    public static ToastUtil getInstance(Context context, String msg, int res, boolean isAround) {
        if (instance == null) {
            instance = new ToastUtil(context.getApplicationContext(), msg, res, isAround);
        } else {
            mHandler.post(() -> {
                if (res <= 0) {
                    mImageView.setVisibility(View.GONE);
                } else {
                    mImageView.setVisibility(View.VISIBLE);
                    mImageView.setImageResource(res);
                }

                if (isAround) {
                    mLinearLayout.setBackgroundResource(R.drawable.shape_toast_radius_around);
                } else {
                    mLinearLayout.setBackgroundResource(R.drawable.shape_toast_radius);
                }
                mTextView.setText(msg);
            });

        }
        return instance;
    }


    public ToastUtil(Context context, String msg, int res, boolean isAround) {
        mHandler.post(() -> {
            message = msg;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //自定义布局
            View view = inflater.inflate(R.layout.view_toast, null);
            //自定义toast文本
            mTextView = (TextView) view.findViewById(R.id.toast_msg);
            mImageView = view.findViewById(R.id.img_err);
            mLinearLayout = view.findViewById(R.id.root);

            if (res <= 0) {
                mImageView.setVisibility(View.GONE);
            } else {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(res);
            }

            if (isAround) {
                mLinearLayout.setBackgroundResource(R.drawable.shape_toast_radius_around);
            } else {
                mLinearLayout.setBackgroundResource(R.drawable.shape_toast_radius);
            }

            mTextView.setText(msg);
            if (mToast == null) {
                mToast = new Toast(context);
                Log.i("ToastUtil", "Toast create...");
            }
            //设置toast底部显示
            mToast.setGravity(Gravity.TOP, 0, context.getResources().getDimensionPixelOffset(R.dimen.dp_70));
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(view);
        });
    }

    /**
     * 自定义居中显示toast
     */
    public void show() {
        mHandler.post(() -> mToast.show());

    }

    /**
     * 自定义时长、居中显示toast
     */
    public void show(int duration) {
        timeCount = new TimeCount(duration, 1000);
        Log.i("ToastUtil", "Toast show...");
        if (canceled) {
            timeCount.start();
            canceled = false;
            showUntilCancel();
        }
    }

    /**
     * 隐藏toast
     */
    private void hide() {
        if (mToast != null) {
            mToast.cancel();
        }
        canceled = true;
        Log.i("ToastUtil", "Toast that customed duration hide...");
    }

    private void showUntilCancel() {
        if (canceled) { //如果已经取消显示，就直接return
            return;
        }
        mToast.show();
        mHandler.postDelayed(() -> {
            Log.i("ToastUtil", "Toast showUntilCancel...");
            showUntilCancel();
        }, Toast.LENGTH_LONG);
    }

    /**
     * 自定义计时器
     */
    private class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval); //millisInFuture总计时长，countDownInterval时间间隔(一般为1000ms)
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTextView.setText(message + ": " + millisUntilFinished / 1000 + mTextView.getContext().getString(R.string.disappear_after_s));
        }

        @Override
        public void onFinish() {
            hide();
        }
    }
}
