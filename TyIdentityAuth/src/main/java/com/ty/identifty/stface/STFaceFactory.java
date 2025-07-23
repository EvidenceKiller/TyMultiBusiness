package com.ty.identifty.stface;



import static com.ty.identifty.stface.StContact.AUTH_ACTIVE;
import static com.ty.identifty.stface.StContact.AUTH_ESAM;
import static com.ty.identifty.stface.StContact.AUTH_ON_LING;
import static com.ty.identifty.stface.StHelper.parseSdkErrorCode;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;


import com.alibaba.fastjson.JSON;
import com.sensetime.sdk.video.module.StCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public final class STFaceFactory {

    /*配置信息*/
    private final String paramJson = "param.json";
    private final String configJson = "config.json";
    private String dbSuffix = ".enroll.index";
    private String dbSuffix1 = ".enroll.k";
    private String dbSuffix2 = ".enroll.v";
    private String dbSuffix3 = "-journal";
    private String trackConfigStr;

    //授权方式
    private @StContact.SdkVideoMode
    String mSdkVideoMode = AUTH_ACTIVE;
    //参数实体
    private StParameter mStParameter;
    //人脸识别能力
    private STFaceDetector mStFaceDetector;
    //人脸识别工厂
    private static STFaceFactory stFaceFactory = null;

    /**
     * 单例获取工厂实例
     *
     * @return
     */
    public static STFaceFactory getInstance() {
        if (stFaceFactory == null) {
            synchronized (STFaceFactory.class) {
                if (stFaceFactory == null) {
                    stFaceFactory = new STFaceFactory();
                }
            }
        }
        return stFaceFactory;
    }

    /**
     * 创建人脸识别能力，建议在子线程中调用，此函数将完成模型文件及授权
     *
     * @param context
     * @return
     */
    public synchronized STFaceDetector detectorCreate(@NonNull Context context, BeforeSenseActiveCallBack beforeSenseActiveCallBack) {
        if (mStFaceDetector == null || !mStFaceDetector.isOk()) {
            StHelper.logI("-----------开始初始化商汤人脸----------");
            String myJson = getJson(context, paramJson);
            mStParameter = JSON.parseObject(myJson, StParameter.class);
            int ret = sdkFileInit(mStParameter.getParams(), context);
            int licenseRet;
            if (oldLicenseSignCodeExist()) {
                licenseRet = sdkLicenseInit(context, beforeSenseActiveCallBack);
            } else {
                licenseRet = sdkNewLicenseInit(context, beforeSenseActiveCallBack);
            }
            ret &= licenseRet;
            if (ret == 0)
                mStFaceDetector = STFaceDetector.crateStDetector(mStParameter, trackConfigStr);
            else StHelper.logE("模型/license文件准备失败,放弃创建");
        }
        return mStFaceDetector;
    }

    private String getJson(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        AssetManager assetManager = context.getAssets();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName), "utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private int sdkFileInit(StParameter.Params stParams, Context mContext) {
        // get params
        String basePath = stParams.getCommon().getBasePath();
        String modelPath = basePath + "/model/";
        String licenseName = stParams.getCommon().getLicenseName();
        String dbName = stParams.getDb().getName();

        String licenseFile = basePath + '/' + licenseName;
        String featureFile = basePath + '/' + dbName;

        // copy file(model) from assert to device
        boolean ret = true;
        //copyAssetsFileToPath(mContext, licenseName, licenseFile, false); //  对拷贝license不做检查, 依设备目录下的license文件为准
        ret &= copyAssetsFileToPath(mContext, dbName + dbSuffix, featureFile + dbSuffix, false);
        ret &= copyAssetsFileToPath(mContext, dbName + dbSuffix1, featureFile + dbSuffix1, false);
        ret &= copyAssetsFileToPath(mContext, dbName + dbSuffix2, featureFile + dbSuffix2, false);
        ret &= copyAssetsFileToPath(mContext, dbName, featureFile, false);
        ret &= copyAssetsFileToPath(mContext, dbName + dbSuffix3, featureFile + dbSuffix3, false);
        ret &= copyAssetsDirToPath(mContext, "model", modelPath, false); // not cover

        return ret ? 0 : -1;
    }

    private int sdkLicenseInit(Context context, BeforeSenseActiveCallBack beforeforActive) {
        StCommon stCommon = new StCommon();
        StParameter.Params stParams = mStParameter.getParams();
        String licenseName = stParams.getCommon().getLicenseName();
        String licenseSignCode = stParams.getCommon().getLicenseSignCode();
        String basePath = stParams.getCommon().getBasePath();
        String licenseFile = basePath + '/' + licenseName;
        String licenseSignCodeFile = basePath + '/' + licenseSignCode;
        trackConfigStr = getJson(context, configJson);

        int ret = -1;
        if (mSdkVideoMode.equals(AUTH_ON_LING)) {
            ret = stCommon.sdkVideoAddLicense(licenseFile);
            StHelper.logI("license mode is online");
        } else if (mSdkVideoMode.equals(AUTH_ESAM)) {
            ret = stCommon.sdkVideoAddChipLicense(licenseFile, "/dev/i2c-2");
            StHelper.logI("license mode is esam--- ret = " + ret);
        } else {
            if (null != beforeforActive) {
                beforeforActive.beforeDo(stParams);
            }
            ret = stCommon.sdkVideoAddLicenseActivated(licenseFile, licenseSignCodeFile);
            StHelper.logI("license mode is activated");
        }
        parseSdkErrorCode(ret, "授权");
        String sdkVersion = stCommon.sdkVideoVersion();
        StHelper.logI("sdkVersion:" + sdkVersion);
        return ret;
    }

    /**
     * 新的证书初始化逻辑，虽然和老的逻辑基本一样，以防万一还是单独写个初始化
     */
    private int sdkNewLicenseInit(Context context, BeforeSenseActiveCallBack beforeActive) {
        StCommon stCommon = new StCommon();
        StParameter.Params stParams = mStParameter.getParams();
        String licenseName = stParams.getCommon().getNewLicenseName();
        String licenseSignCode = stParams.getCommon().getNewLicenseSignCode();
        String basePath = stParams.getCommon().getBasePath();
        String licenseFile = basePath + '/' + licenseName;
        String licenseSignCodeFile = basePath + '/' + licenseSignCode;
        trackConfigStr = getJson(context, configJson);

        int ret = -1;
        if (mSdkVideoMode.equals(AUTH_ON_LING)) {
            ret = stCommon.sdkVideoAddLicense(licenseFile);
            StHelper.logI("license mode is online");
        } else if (mSdkVideoMode.equals(AUTH_ESAM)) {
            ret = stCommon.sdkVideoAddChipLicense(licenseFile, "/dev/i2c-2");
            StHelper.logI("license mode is esam--- ret = " + ret);
        } else {
            if (null != beforeActive) {
                beforeActive.beforeDo(stParams);
            }
            ret = stCommon.sdkVideoAddLicenseActivated(licenseFile, licenseSignCodeFile);
            StHelper.logI("license mode is activated");
        }
        parseSdkErrorCode(ret, "授权");
        String sdkVersion = stCommon.sdkVideoVersion();
        StHelper.logI("sdkVersion:" + sdkVersion);
        return ret;
    }

    /**
     * 老的证书签名文件是否存在 ，存在的话就是老的不用新的证书进行重新签名生成
     *
     * @return 是否存在
     */
    private boolean oldLicenseSignCodeExist() {
        StParameter.Params stParams = mStParameter.getParams();
        String licenseSignCode = stParams.getCommon().getLicenseSignCode();
        String basePath = stParams.getCommon().getBasePath();
        String licenseSignCodeFile = basePath + '/' + licenseSignCode;
        File file = new File(licenseSignCodeFile);
        return file.exists();
    }

    private boolean copyAssetsFileToPath(Context context, String fileName, String outPath, boolean delete) {
        boolean result;
        File dirFile = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dirFile.mkdirs();
        }
        File outFile = new File(outPath);
        if (outFile.exists() && outFile.isFile()) {
            if (delete) {
                outFile.delete();
            } else {
                return true;
            }
        }
        try {
            InputStream in = context.getAssets().open(fileName);
            if (in == null) {
                parseSdkErrorCode(-110, fileName + " the src file is not existed");
                return false;
            }
            OutputStream out = new FileOutputStream(outFile);

            /*
            byte[] buffer = new byte[1024];
            int length = in.read(buffer);
            while (length > 0) {
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }*/

            byte[] buffer = new byte[1024];
            int ret = -1;

            while ((ret = in.read(buffer)) != -1) {
                out.write(buffer, 0, ret);
            }


            out.flush();
            in.close();
            out.close();
            result = true;
        } catch (Exception e) {
            outFile.delete();
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    private boolean copyAssetsDirToPath(Context context, String assetDir, String targetDir, boolean delete) {
        if (TextUtils.isEmpty(assetDir) || TextUtils.isEmpty(targetDir)) {
            return false;
        }
        String separator = File.separator;
        try {
            // 获取assets目录assetDir下一级所有文件以及文件夹
            String[] fileNames = context.getResources().getAssets().list(assetDir);
            if (fileNames.length > 0) {
                File targetFile = new File(targetDir);
                if (!targetFile.exists() && !targetFile.mkdirs()) {
                    return false;
                }
                for (String fileName : fileNames) {
                    copyAssetsDirToPath(context, assetDir + separator + fileName, targetDir + separator + fileName, delete);
                }
            } else {
                copyAssetsFileToPath(context, assetDir, targetDir, delete);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
