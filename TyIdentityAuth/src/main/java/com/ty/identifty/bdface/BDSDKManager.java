package com.ty.identifty.bdface;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.ty.identifty.base.TyFaceLogger;

public class BDSDKManager {

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    private static volatile int initStatus = SDK_UNACTIVATION;
    private FaceAuth faceAuth;

    private BDSDKManager() {
        faceAuth = new FaceAuth();
        faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 1);
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_LOW, 2);
    }

    private static class HolderClass {
        private static final BDSDKManager instance = new BDSDKManager();
    }

    public static BDSDKManager getInstance() {
        return HolderClass.instance;
    }

    public static boolean isLicenseOk() {
        return initStatus == SDK_INIT_SUCCESS;
    }

    /**
     * 初始化鉴权，如果鉴权通过，直接初始化模型
     *
     * @param context
     * @param listener
     */
    public void init(final Context context, final SdkInitListener listener) {

        PreferencesUtil.initPrefs(context.getApplicationContext());
        final String licenseOfflineKey = PreferencesUtil.getString("activate_offline_key", "");
        final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");
        final String licenseBatchlineKey = PreferencesUtil.getString("activate_batchline_key", "");

        String deviceId = faceAuth.getDeviceId(context);
        TyFaceLogger.d("设备硬件指纹： " + deviceId);

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)
                && TextUtils.isEmpty(licenseBatchlineKey)) {
            TyFaceLogger.d("未授权设备，请完成授权激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
            return;
        }

        // todo 增加判空处理
        if (listener != null) {
            listener.initStart();
        }

        // 在线激活
        if (!TextUtils.isEmpty(licenseOnlineKey)) {
            faceAuth.initLicenseOnLine(context, licenseOnlineKey, (code, response) -> {
                if (code == 0) {
                    initStatus = SDK_INIT_SUCCESS;
                    if (listener != null) {
                        listener.initLicenseSuccess();
                    }
                    return;
                } else {
                    listener.initLicenseFail(code, response);
                }
            });
        }
        // 离线激活
        else if (!TextUtils.isEmpty(licenseOfflineKey)) {
            faceAuth.initLicenseOffLine(context, (code, response) -> {
                if (code == 0) {
                    initStatus = SDK_INIT_SUCCESS;
                    if (listener != null) {
                        listener.initLicenseSuccess();
                    }
                    initFaceModel(context, listener);
                    return;
                } else {
                    listener.initLicenseFail(code, response);
                }
            });
        }
        // 应用激活
        else if (!TextUtils.isEmpty(licenseBatchlineKey)) {
            TyFaceLogger.d("开始Batch模式授权");
            faceAuth.initLicenseBatchLine(context, licenseBatchlineKey, (code, response) -> {
                TyFaceLogger.d("开始Batch模式授权  code=" + code);
                if (code == 0) {
                    initStatus = SDK_INIT_SUCCESS;
                    if (listener != null) {
                        listener.initLicenseSuccess();
                    }
                    initFaceModel(context, listener);
                    return;
                } else {
                    listener.initLicenseFail(code, response);
                }
            });
        } else {
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
        }
    }

    // 初始化人脸模型SDK
    private void initFaceModel(Context context, SdkInitListener listener) {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(context, new SdkSampleListener() {

                @Override
                public void initModelSuccess() {
                    FaceSDKManager.initModelSuccess = true;
                    if (listener != null) listener.initModelSuccess();
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    TyFaceLogger.d("模型加载失败，请尝试重启应用, code=" + errorCode + ",msg=" + msg);
                    if (listener != null) listener.initLicenseFail(errorCode, msg);
                }
            });
        } else {
            if (listener != null) listener.initModelSuccess();
        }
    }
}