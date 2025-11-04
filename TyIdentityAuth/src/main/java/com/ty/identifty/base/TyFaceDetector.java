package com.ty.identifty.base;

import android.content.Context;
import android.graphics.RectF;

import java.util.List;

/**
 * 人脸识别功能抽象
 */
public abstract class TyFaceDetector<T> {

    protected Context mContext;
    private String mLicenseKey;
    protected boolean mDebug;
    protected float faceFeatureThreshold = 0.85f;
    private TyFaceCallback mCallback;


    public void init(Context context, TyFaceCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public void enableDebug(boolean debug) {
        mDebug = debug;
    }

    public void setLicenseKey(String key) {
        mLicenseKey = key;
    }

    public void setFaceFeatureThreshold(float score) {
        faceFeatureThreshold = Math.min(Math.max(score, 0.4f), 0.99f);
    }

    public boolean isOk() {
        return true;
    }

    public void loadFace(List<TyFaceFeature<T>> list) {

    }

    protected final void d(String msg) {
        if (mDebug) {
            TyFaceLogger.d(msg);
        }
    }

    protected final void e(String msg) {
        if (mDebug) {
            TyFaceLogger.e(msg);
        }
    }

    protected final void onFaceFast(TyLivingModel model) {
        if (mCallback != null) mCallback.onFaceFast(model);
    }

    protected final void onFaceInfo(TyLivingModel model) {
        if (mCallback != null) mCallback.onFaceInfo(model);
    }

    public TyLivingModel detectByYuv(String key, byte[] yuv, int width, int height,
                                     RectF rectF, float scale, int featureCheckMode) {
        return null;
    }

    public void release() {

    }

}
