package com.ty.identifty.bdface;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.ty.identifty.base.TyFaceAuthCallback;
import com.ty.identifty.base.TyFaceFeature;
import com.ty.identifty.base.TyFaceLogger;

import java.util.List;

public final class BDFaceFactory {

    // 百度人脸初始化
    public static void auth(Context context, String licenseKey, TyFaceAuthCallback callback) {

        if (context == null || TextUtils.isEmpty(licenseKey)) return;
        PreferencesUtil.initPrefs(context);
        PreferencesUtil.putString("activate_batchline_key", licenseKey);
        GateConfigUtils.isConfigExit(context);
        GateConfigUtils.initConfig();

        BDSDKManager.getInstance().init(context, new SdkSampleListener() {

            @Override
            public void initModelSuccess() {
                TyFaceLogger.d("----->模型加载成功，欢迎使用<-----");
                if (callback != null) callback.initLicenseSuccess();
            }

            public void initLicenseSuccess() {
                TyFaceLogger.d("百度授权加载成功");
            }

            @Override
            public void initLicenseFail(int errorCode, String msg) {
                TyFaceLogger.e("百度授权加载失败, code=" + errorCode + ",msg=" + msg);
                if (callback != null) callback.initLicenseFail(errorCode, msg);
            }
        });
    }

    // 加载底库
    public static void loadFeatures(List<TyFaceFeature<Object>> features) {

        if (!BDSDKManager.isLicenseOk()) {
            TyFaceLogger.d("加载人脸， SDK 授权失败");
            return;
        }

        if (features == null || features.isEmpty()) {
            TyFaceLogger.d("底库为空");
            return;
        }

        FaceSDKManager.getInstance().loadFace(features);
    }
}
