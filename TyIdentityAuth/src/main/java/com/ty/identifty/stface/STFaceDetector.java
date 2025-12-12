package com.ty.identifty.stface;


import static com.ty.identifty.stface.StContact.LOG_TAG;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.sensetime.sdk.video.module.StCommon;
import com.sensetime.sdk.video.module.StDb;
import com.sensetime.sdk.video.module.StDetect;
import com.sensetime.sdk.video.module.StQuality;
import com.sensetime.sdk.video.module.StVerify;
import com.sensetime.sdk.video.module.sdk.common.StImage;
import com.sensetime.sdk.video.module.sdk.common.StQualityInfo;
import com.sensetime.sdk.video.module.sdk.common.StQualityResult;
import com.sensetime.sdk.video.module.sdk.common.StRect;
import com.sensetime.sdk.video.module.sdk.common.StTrackInfo;
import com.sensetime.sdk.video.module.sdk.common.StTrackResult;
import com.sensetime.sdk.video.module.sdk.common.StVerifyResult;
import com.ty.identifty.base.TyFaceCallback;
import com.ty.identifty.base.TyFaceDetector;
import com.ty.identifty.base.TyFaceFeature;
import com.ty.identifty.base.TyFaceInfo;
import com.ty.identifty.base.TyLivingModel;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 1.初始化
 */
public final class STFaceDetector extends TyFaceDetector<StVerifyResult> {


    private static char[] chars = new char[]{'N', 'V', '2', '1'};
    private final StParameter mStParameter;
    private final String mTrackConfig;
    private StDetect mStDetect;
    private StQuality mStQuality;
    private StVerify mStVerify;
    private volatile boolean isInit;
    private StDb mStDb;
    private float mQualityThreshold;
    private float mConfidenceThreshold;
    private static STFaceDetector singleSTFaceDetector;


    private STFaceDetector(StParameter stParameter, String trackConfigStr) {
        mStParameter = stParameter;
        mTrackConfig = trackConfigStr;
    }

    public static STFaceDetector crateStDetector(StParameter stParameter, String trackConfigStr) {
        if (singleSTFaceDetector == null) {
            synchronized (STFaceDetector.class) {
                if (singleSTFaceDetector == null)
                    singleSTFaceDetector = new STFaceDetector(stParameter, trackConfigStr);

            }
        }
        return singleSTFaceDetector;
    }

    @Override
    public void enableDebug(boolean debug) {
        super.enableDebug(debug);
        StHelper.setDebugAble(debug);
    }

    @Override
    public boolean isOk() {
        return isInit;
    }


    /**
     * 初始化操作，建议在子线程中调用
     *
     * @param context
     * @param callback
     */
    @Override
    public synchronized void init(Context context, TyFaceCallback callback) {
        super.init(context.getApplicationContext(), callback);
        if (isOk()) {
            Log.d(LOG_TAG, "商汤人脸已经初始化，无需二次创建");
            return;
        }
        Log.d(LOG_TAG, "商汤人脸初始化开始");
        String basePath = mStParameter.getParams().getCommon().getBasePath();
        String detectModel = basePath + "/" + mStParameter.getModels().getDetectModelName();
        String alignModel = basePath + "/" + mStParameter.getModels().getAlignModelName();
        String headPoseModel = basePath + "/" + mStParameter.getModels().getHeadposeModelName();
        String blurModel = basePath + "/" + mStParameter.getModels().getBlurModelName();
        String featureModel = basePath + "/" + mStParameter.getModels().getFaceFeatureModelName();
        int ret = 0;
        ret = dbOpen(mStParameter);
        StHelper.parseSdkErrorCode(ret, "特征值库创建");
        if (ret != 0) return;
        //create det
        if (mStDetect == null) {
            mStDetect = new StDetect(detectModel);
            ret = mStDetect.sdkVideoDetectCreate();
            StHelper.parseSdkErrorCode(ret, "sdkVideoDetectCreate");
        }
        if (ret != 0) return;

        //create quality
        if (mStQuality == null) {
            mStQuality = new StQuality(alignModel, headPoseModel, blurModel);
            ret = mStQuality.sdkVideoQualityCreate();
            StHelper.parseSdkErrorCode(ret, "sdkVideoQualityCreate");
        }
        if (ret != 0) return;

        // ref verify
        if (mStVerify == null) {
            mStVerify = new StVerify(featureModel
                    , mStParameter.getParams().getFaceFeature().getPlatform()
                    , mStParameter.getParams().getFaceFeature().getAlgorithms()
                    , mStParameter.getParams().getCommon().getOclbinaryPath());
            ret = mStVerify.sdkVideoVerifyCreate();
            StHelper.parseSdkErrorCode(ret, "DbManage.sdkVideoVerifyCreate");
        }

        StCommon stCommon = new StCommon();
        stCommon.sdkVideoBindCpu(mStParameter.getParams().getCommon().getFaceFeatureCpuId());

        setFaceFeatureThreshold(mStParameter.getParams().getDb().getThreshold());
        mConfidenceThreshold = mStParameter.getParams().getDetectTrack().getConfidence();
        mQualityThreshold = mStParameter.getParams().getSelect().getThreshold();
        if (ret != 0) return;
        isInit = true;
        Log.d(LOG_TAG, "-------------商汤初始化完成------------");
    }

    public int dbOpen(StParameter stParams) {
        String basePath = stParams.getParams().getCommon().getBasePath();
        String featureModel = basePath + "/" + stParams.getModels().getFaceFeatureModelName();
        String dbName = basePath + "/" + stParams.getParams().getDb().getName();
        int dbSize = stParams.getParams().getDb().getSize();
        int ret = 0;
        //create db
        if (mStDb == null) {
            mStDb = new StDb(dbName, featureModel, dbSize);
            File dbFile = new File(dbName + ".enroll.index");
            if (dbFile.exists()) {
                dbFile.delete();
            }
            ret = mStDb.sdkVideoDbCreate();
            if (ret == 0) {
                mStDb.sdkVideoDbClear();
            }
            StHelper.parseSdkErrorCode(ret, "sdkVideoDbCreate");
        }
        return ret;
    }

    private String bitmapDic = "/sdcard/zy.sport.cache/pic";
    private ExecutorService executor = Executors.newFixedThreadPool(2, Executors.defaultThreadFactory());

    private void saveBitmap(byte[] yuv, int width, int height, String name) {
        executor.execute(() -> {
            SaveBitmapUtil.INSTANCE.saveBitmap(yuv, width, height, bitmapDic, name);
        });
    }

    /**
     * 特征值入库，需要在子线程中调用
     *
     * @param list
     */
    @Override
    public void loadFace(List<TyFaceFeature<StVerifyResult>> list) {

        if (!isInit || list == null || list.size() == 0) return;
        int secCount = 0;
        int failCount = 0;
        for (TyFaceFeature<StVerifyResult> faceFeature : list) {
            Log.d(LOG_TAG, "底库更新开始");
            int i = mStDb.sdkVideoDbAdd(faceFeature.getId(), faceFeature.getFeatureObj());
            if (i == 0) secCount++;
            else {
                failCount++;
                StHelper.INSTANCE.e(faceFeature.getUserId() + "入库失败", true);
            }
        }
        Log.d(LOG_TAG, "底库更新结束：入库成功" + secCount + "条 失败：" + failCount + "条 ");
    }

    /**
     * 单个特征入库，临时API
     *
     * @param id
     * @param stVerifyResult
     * @return
     */
    public boolean addDb(int id, StVerifyResult stVerifyResult) {
        int i = mStDb.sdkVideoDbAdd(id, stVerifyResult);
        StHelper.parseSdkErrorCode(i, "入库");
        if (i == 0) return true;
        return false;
    }

    public boolean deleteDb(int id) {
        int i = mStDb.sdkVideoDbRemove(id);
        StHelper.parseSdkErrorCode(i, "出库");
        if (i == 0) return true;
        return false;
    }

    public void clearDb() {
        try {
            if (mStDb != null) {
                int i = mStDb.sdkVideoDbClear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 人脸能力api,需要在子线程中调用.未成功会调用onFaceFast(null)且返回null.
     *
     * @param key              无意义，会原路返回，建议时间戳
     * @param yuv              目标图片数据nv21
     * @param width            图片宽
     * @param height           图片高
     * @param rectF            图片裁剪区域，相对于YUV图片坐标
     * @param scale            缩放，未使用
     * @param featureCheckMode 人脸功能选项 ：
     *                         1.只检查是否有人脸并调用onFaceFast返回人脸位置
     *                         2.抽取图片中唯一的人脸特征值并通过onFaceFast返回特征信息
     *                         3.图片中的人脸与底库进行比对，返回首先识别成功的人员信息onFaceInfo
     * @return 返回函数结果，失败返回null
     */
    @Override
    public TyLivingModel detectByYuv(String key, byte[] yuv, int width, int height, RectF rectF, float scale, int featureCheckMode) {
        if (!isInit) return errorReturn("初始化未完成");
        TyLivingModel jfFaceResult = null;
        //构建图片信息
        StImage stImage = createStImage(yuv, width, height, rectF);

        /*---------人脸检测----------*/
        int ret = mStDetect.sdkVideoDetectOne(stImage);
        StHelper.parseSdkErrorCode(ret, "检测");
        if (ret != 0) return errorReturn("检测异常");
        //获取人脸检测的结果
        StTrackResult stDetectResult = mStDetect.getStDetectResult();
        //获取人员检测中的人脸信息
        StTrackInfo[] detectInfo = stDetectResult.getTrackInfo();

        /*---------人脸质量检测----------*/
        ret = mStQuality.sdkVideoQualityDetect(stImage, detectInfo);
        StHelper.parseSdkErrorCode(ret, "质量");
        if (ret != 0) {
            return errorReturn("质量检测异常");
        }
        //获取质量检测结果
        StQualityResult stQualityResult = mStQuality.getStQualityResult();
        StQualityInfo[] stQualityInfo = stQualityResult.getQualityInfo();
        /**************************************************************/
        // 俯仰角(Pitch): 如果大于45度，直接丢弃, 35-45 暂时有效, 低于35 有效
// 水平角(Yaw) :  大于45度，直接丢弃
// 遮挡比例(OcclusionRatio) :  大于0.35 直接丢弃，0.3 - 0.35 暂时有效, 其他有效
// 综合质量(Quality) : 小于0.2 直接丢弃， 0.2 -0.5 暂时有用, 其他有效
/*if (stTrackInfo != null) {
    //StRect rect = stTrackInfo.getRect();
    d(" 人脸置信度: " + stTrackInfo.getConfidence()
            + ", 人脸大小: " + (rect != null ? (rect.getRight() - rect.getLeft())
            + "," + (rect.getBottom() - rect.getTop()) : " 0,0,0,0"));
}*/
        StHelper.INSTANCE.d("人脸检测结果人脸数：" + stQualityResult.getQualityCount(), false);
        List<StQualityInfo> qualityInfos = Arrays.stream(stQualityInfo).filter(qualityInfo -> {

            String fileName = "detect_" + key + ".jpg";
            float quality = 0.0f;
            String ss = "水平角: " + qualityInfo.getYaw()
                    + ", 俯仰角: " + qualityInfo.getPitch()
                    + ", 遮挡比例: " + qualityInfo.getOcclusionRatio()
                    + ", 左眼状态: " + qualityInfo.getEyestateLeft()
                    + ", 综合质量: " + (quality = qualityInfo.getQuality());
            StHelper.INSTANCE.d(ss, false);

            fileName = "invalide_" + qualityInfo.getYaw() + "_" + qualityInfo.getPitch() + "_" + quality + ".jpg";
            // 综合质量低于0.55的时候，开启丢弃策略
            if (quality < 0.55f) {
                // 俯仰角(Pitch): 如果大于45度，直接丢弃, 35-45 暂时有效, 低于35 有效
                if (Math.abs(qualityInfo.getPitch()) > 45) {
                    saveBitmap(yuv, width, height, fileName);
                    return false;
                }

                // 水平角(Yaw) :  大于45度，直接丢弃
                if (Math.abs(qualityInfo.getYaw()) > 45) {
                    //d(" 警告： 水平角异常，直接丢弃 !");
                    saveBitmap(yuv, width, height, fileName);
                    return false;
                }

                // 遮挡比例(OcclusionRatio) :  大于0.35 直接丢弃，0.3 - 0.35 暂时有效, 其他有效
                if (Math.abs(qualityInfo.getOcclusionRatio()) > 0.35f) {
                    //d(" 警告： 遮挡比例异常，直接丢弃 !");
                    saveBitmap(yuv, width, height, fileName);
                    return false;
                }

                // 综合质量(Quality) : 小于0.2 直接丢弃， 0.2 -0.5 暂时有用, 其他有效
                if (Math.abs(qualityInfo.getQuality()) < 0.2f) {
                    saveBitmap(yuv, width, height, fileName);
                    //d(" 警告： 综合质量异常，直接丢弃 !");
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        /**************************************************************/
        if (qualityInfos.isEmpty()) {
            return errorReturn("质量角度检测异常");
        }
        int faceNum = qualityInfos.size();

//        int faceNum = stQualityResult.getQualityCount();
        //人脸数量校验
        List<StTrackInfo> detectInfos = Arrays.stream(detectInfo).filter(stTrackInfo -> {
            return qualityInfos.stream().anyMatch(qi -> qi.getId() == stTrackInfo.getId());
        }).collect(Collectors.toList());


        /*---------人脸功能逻辑----------*/
        // 【1: 不提取特征】只返回人脸位置
        if (featureCheckMode == 1) {
            jfFaceResult = new TyLivingModel();
            TyFaceInfo[] faceInfoArray = new TyFaceInfo[faceNum];
            for (int i = 0; i < faceNum; i++) {
                TyFaceInfo<float[]> jfFaceInfo = new TyFaceInfo();
                StRect rect = stQualityInfo[i].getRect();
                jfFaceInfo.setKey(key);
                jfFaceInfo.setFaceRect(new RectF(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom()));
                faceInfoArray[i] = jfFaceInfo;
            }
            jfFaceResult.setFaceInfo(faceInfoArray[0]);
            jfFaceResult.setTrackFaceInfo(faceInfoArray);
            onFaceFast(jfFaceResult);
            return jfFaceResult;
        }
        //【2: 提取特征】，抽取特征值得方式->图片多张人脸时，直接报错;//退出了不继续,返回人脸位置}
        else if (featureCheckMode == 2) {
            if (faceNum > 1) return errorReturn("特征抽取失败，图片中发现多个人脸");
            //提取特征值
            StVerifyResult stVerifyResult = getFeature(stImage, detectInfos.get(0), qualityInfos.get(0));
            if (stVerifyResult == null) return errorReturn("特征提取异常");
            jfFaceResult = new TyLivingModel();
            TyFaceInfo<float[]> jfFaceInfo = new TyFaceInfo();
            jfFaceInfo.setFeatureFloat(stVerifyResult.getVerifyFeature());
            jfFaceInfo.setKey(key);
            //人脸位置
            StRect verifyRect = stVerifyResult.getVerifyRect();
            jfFaceInfo.setFaceRect(new RectF(verifyRect.getLeft(), verifyRect.getTop(), verifyRect.getRight(), verifyRect.getBottom()));
            jfFaceResult.setFaceInfo(jfFaceInfo);
            //特征信息
            jfFaceInfo.setExtendObject(stVerifyResult);
            onFaceFast(jfFaceResult);
            return jfFaceResult;
        }
        // 【3: 提取特征+ m：N检索】多张人脸时，首先比对的成功返回
        else {
            StVerifyResult[] stVerifyResults = new StVerifyResult[1];
            for (int i = 0; i < faceNum; i++) {
                //提取特征
                StVerifyResult stVerifyResult = getFeature(stImage, detectInfos.get(i), qualityInfos.get(i));
                if (stVerifyResult == null) {
                    continue;
                }
                stVerifyResults[0] = stVerifyResult;
                //比对
                ret = mStDb.sdkVideoDbSearch(1, stVerifyResults);
                if (ret != 0) continue;
                int id = mStDb.getStDbResult().getCompareResultId()[0];
                float score = mStDb.getStDbResult().getCompareResultScore()[0];
                if (faceFeatureThreshold > score) {
                    continue;
                }
                jfFaceResult = new TyLivingModel();
                TyFaceInfo<float[]> jfFaceInfo = new TyFaceInfo();
                jfFaceInfo.setId(id);
                jfFaceInfo.setKey(key);
                jfFaceInfo.setUserId(String.valueOf(id));
                jfFaceInfo.setExtendObject(stVerifyResult);
                jfFaceInfo.setScore(score);
                jfFaceResult.setFaceInfo(jfFaceInfo);
                onFaceInfo(jfFaceResult);
                return jfFaceResult;
            }
            return errorReturn("未查询到人员");
        }
    }

    private TyLivingModel errorReturn(String errorMsg) {
        StHelper.INSTANCE.e(errorMsg, true);
        onFaceFast(null);
        return null;
    }

    private StVerifyResult getFeature(StImage stImage, StTrackInfo stTrackInfo, StQualityInfo stQualityInfo) {
        if (!isInit) return null;
        float quality = stQualityInfo.getQuality();
        float confidence = stTrackInfo.getConfidence();
        int ret = -1;
        if (quality >= mQualityThreshold && confidence >= mConfidenceThreshold)
            ret = mStVerify.sdkVideoVerifyOne(stImage, stQualityInfo.getPointsArray());
        StHelper.parseSdkErrorCode(ret, "特征提取");
        if (ret == 0) {
            return mStVerify.getStVerifyResult();
        }
        return null;
    }


    // 创建识别对象
    private StImage createStImage(byte[] yuv, int width, int height, RectF rectF) {
        StImage stImage;
        int dstWidth = width;
        int dstHeight = height;
        if (rectF != null && rectF.width() > 0 && rectF.height() > 0) {
            dstWidth = (int) rectF.width();
            dstHeight = (int) rectF.height();
//            byte[] i420 = YuvTool.convertToI420(yuv, width, height, (int) rectF.left, (int) rectF.top, dstWidth, dstHeight, 0, chars);
//            yuv = YuvTool.I420ToNV21(i420, dstWidth, dstHeight);
        }
        stImage = new StImage(dstWidth, dstHeight, dstWidth, StImage.StPixelFormat.PixFmtNV21, yuv);
        return stImage;
    }


    @Override
    public void release() {
        super.release();
        if (mStDetect != null) mStDetect.sdkVideoDetectDestroy();
        if (mStQuality != null) mStQuality.sdkVideoQualityDestroy();
        if (mStVerify != null) mStVerify.sdkVideoVerifyDestroy();
        if (mStDb != null) mStDb.sdkVideoDbDestroy();
        isInit = false;
        Log.d(LOG_TAG, "--------退出");

    }

}
