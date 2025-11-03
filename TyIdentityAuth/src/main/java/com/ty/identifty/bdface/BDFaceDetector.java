package com.ty.identifty.bdface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.ty.identifty.base.TyFaceCallback;
import com.ty.identifty.base.TyFaceDetector;
import com.ty.identifty.base.TyFaceFeature;
import com.ty.identifty.base.TyFaceInfo;
import com.ty.identifty.base.TyFaceUtil;
import com.ty.identifty.base.TyLivingModel;
import com.ty.identifty.bdface.model.LivenessModel;

import java.util.List;

public final class BDFaceDetector extends TyFaceDetector<Object> {

    private FaceDetectCallBack mDetectCallBack;
    private final Object mLock = new Object();
    private Bitmap dstImage;
    private int[] dstRgb;
    private int[] primaryRgba;

    @Override
    public void init(Context context, TyFaceCallback callback) {
        super.init(context, callback);
        mDetectCallBack = new FaceDetectCallBack() {

            @Override
            public void onFaceDetectDrawCallback(LivenessModel model) {
                callback(1, model);
            }

            @Override
            public void onTip(int code, String msg) {
                // JfFaceLogger.d(msg);
            }

            @Override
            public void onFaceDetectCallback(LivenessModel model) {
                callback(2, model);
            }

        };
    }

    private TyFaceInfo toFaceInfo(FaceInfo info) {
        TyFaceInfo fi = new TyFaceInfo();
        fi.setCenterX(info.centerX);
        fi.setCenterY(info.centerY);
        fi.setScore(info.score);
        fi.setWidth((int) info.width);
        fi.setHeight((int) info.height);
        fi.setLandmarks(info.landmarks);
        return fi;
    }

    private void callback(int callbackType, LivenessModel model) {

        if (model == null) {
            if (callbackType == 1) {
                onFaceFast(null);
            } else {
                onFaceInfo(null);
            }
            return;
        }

        TyLivingModel livingModel = new TyLivingModel();
        FaceInfo info = model.getFaceInfo();
        FaceInfo[] all = model.getTrackFaceInfo();

        // debug
        //d(String.format("检测耗时 ：%s ms", model.getRgbDetectDuration()));
        //d(String.format("RGB活体检测耗时 ：%s ms", model.getRgbLivenessDuration()));
        //d(String.format("RGB活体得分 ：%s", model.getRgbLivenessScore()));
        //d(String.format("特征抽取耗时 ：%s ms", model.getFeatureDuration()));
        //d(String.format("特征比对耗时 ：%s ms", model.getCheckDuration()));
        //d(String.format("总耗时 ：%s ms", model.getAllDetectDuration()));

        if (info != null) {
            TyFaceInfo fi = toFaceInfo(info);
            fi.setKey(model.getKey());
            fi.setFeature(model.getFeature());
            fi.setExtendObject(model.getUser());
            BDFaceImageInstance faceImage = model.getBdFaceImageInstance();
            if (faceImage != null) {
                fi.setFaceIconData(faceImage.data);
                fi.setFaceBitmapType(faceImage.imageType.name());
                fi.setFaceWidth(faceImage.width);
                fi.setFaceHeight(faceImage.height);
            }
            livingModel.setFaceInfo(fi);
        }

        if (all != null && all.length > 0) {
            TyFaceInfo[] others = new TyFaceInfo[all.length];
            others[0] = livingModel.getFaceInfo();
            for (int i = 1; i < all.length; i++) {
                others[i] = toFaceInfo(all[i]);
            }
            livingModel.setTrackFaceInfo(others);
        }

        // callback
        if (callbackType == 1) {
            onFaceFast(livingModel);
        } else {
            onFaceInfo(livingModel);
        }
    }

    @Override
    public boolean isOk() {
        return FaceSDKManager.isOk();
    }

    @Override
    public void loadFace(List<TyFaceFeature<Object>> list) {
        BDFaceFactory.loadFeatures(list);
    }

    @Override
    public TyLivingModel detectByYuv(String key, byte[] yuv, int width, int height,
                                     RectF rectF, float scale, int featureCheckMode) {
        if (yuv == null || width <= 0 || height <= 0
                || mDetectCallBack == null || !FaceSDKManager.isOk()) return null;

        // 构建识别对象
        BDFaceImageInstance imageInstance = createFaceImageInstance(yuv, width, height, rectF, scale);

        // 人脸比较阀值
        float threholdScore = faceFeatureThreshold * 100;

        // 【1: 不提取特征】
        // 【2: 提取特征】
        // 【3: 提取特征+ 1：N检索】
        synchronized (mLock) {
            LivenessModel model = FaceSDKManager.getInstance().onDetectCheckByRGB(imageInstance,
                    featureCheckMode, key, threholdScore, mDetectCallBack);
            return toJfLivingModel(model);
        }
    }

    // 数据转换
    private TyLivingModel toJfLivingModel(LivenessModel model) {
        if (model == null) return null;

        TyLivingModel livingModel = new TyLivingModel();
        FaceInfo info = model.getFaceInfo();
        TyFaceInfo fi = toFaceInfo(info);
        fi.setKey(model.getKey());
        fi.setFeature(model.getFeature());
        fi.setExtendObject(model.getUser());
        BDFaceImageInstance faceImage = model.getBdFaceImageInstance();
        if (faceImage != null) {
            fi.setFaceIconData(faceImage.data);
            fi.setFaceBitmapType(faceImage.imageType.name());
            fi.setFaceWidth(faceImage.width);
            fi.setFaceHeight(faceImage.height);
        }
        livingModel.setFaceInfo(fi);
        return livingModel;
    }

    // 创建识别对象
    private BDFaceImageInstance createFaceImageInstance(int[] rgb, int width, int height,
                                                        RectF rectF, float scale) {
        int left = 0;
        int top = 0;
        int dstWidth = width;
        int dstHeight = height;
        if (rectF != null && rectF.width() > 0 && rectF.height() > 0) {
            left = (int) rectF.left;
            top = (int) rectF.top;
            dstWidth = (int) rectF.width();
            dstHeight = (int) rectF.height();
        }

        if (dstImage == null || dstImage.isRecycled()
                || dstImage.getWidth() != dstWidth || dstImage.getHeight() != dstHeight) {
            if (dstImage != null) dstImage.recycle();
            dstImage = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
            dstRgb = new int[dstWidth * dstHeight * 4];
        }

        // copy rgb
        if (dstWidth != width || dstHeight != height) {
            // copy
            TyFaceUtil.copyPixels(rgb, width, height, dstRgb, left, top, dstWidth, dstHeight);
            // 更新像素
            dstImage.setPixels(dstRgb, 0, dstWidth, 0, 0, dstWidth, dstHeight);
        } else {
            // 更新像素
            dstImage.setPixels(rgb, 0, width, 0, 0, width, height);
        }

        // 识别对象
        return new BDFaceImageInstance(dstImage);
    }

    // 创建识别对象
    private BDFaceImageInstance createFaceImageInstance(byte[] yuv, int width, int height,
                                                        RectF rectF, float scale) {
        int size = width * height * 4;
        if (primaryRgba == null || primaryRgba.length != size) {
            primaryRgba = new int[size];
        }

        int left = 0;
        int top = 0;
        int dstWidth = width;
        int dstHeight = height;
        if (rectF != null && rectF.width() > 0 && rectF.height() > 0) {
            left = (int) rectF.left;
            top = (int) rectF.top;
            dstWidth = (int) rectF.width();
            dstHeight = (int) rectF.height();
        }

        if (dstImage == null || dstImage.isRecycled()
                || dstImage.getWidth() != dstWidth || dstImage.getHeight() != dstHeight) {
            if (dstImage != null) dstImage.recycle();
            dstImage = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
            dstRgb = new int[dstWidth * dstHeight * 4];
        }

        // yuv to rgba
        TyFaceUtil.yuv420ToRGB(primaryRgba, yuv, width, height);

        // copy rgb
        if (dstWidth != width || dstHeight != height) {
            // copy
            TyFaceUtil.copyPixels(primaryRgba, width, height, dstRgb, left, top, dstWidth, dstHeight);
            // 更新像素
            dstImage.setPixels(dstRgb, 0, dstWidth, 0, 0, dstWidth, dstHeight);
        } else {
            // 更新像素
            dstImage.setPixels(primaryRgba, 0, width, 0, 0, width, height);
        }

        // 识别对象
        return new BDFaceImageInstance(dstImage);
    }

    @Override
    public void release() {
        super.release();
        synchronized (mLock) {
            if (dstImage != null) {
                dstImage.recycle();
            }
            dstImage = null;
            dstRgb = null;
            primaryRgba = null;
            FaceSDKManager.getInstance().release();
        }
    }
}
