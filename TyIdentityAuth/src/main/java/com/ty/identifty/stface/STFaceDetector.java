package com.ty.identifty.stface;


import static com.ty.identifty.stface.StContact.LOG_TAG;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sensetime.sdk.video.module.StCommon;
import com.sensetime.sdk.video.module.StDb;
import com.sensetime.sdk.video.module.StDetect;
import com.sensetime.sdk.video.module.StQuality;
import com.sensetime.sdk.video.module.StVerify;
import com.sensetime.sdk.video.module.sdk.common.StImage;
import com.sensetime.sdk.video.module.sdk.common.StPoint;
import com.sensetime.sdk.video.module.sdk.common.StQualityInfo;
import com.sensetime.sdk.video.module.sdk.common.StQualityResult;
import com.sensetime.sdk.video.module.sdk.common.StRect;
import com.sensetime.sdk.video.module.sdk.common.StTrackInfo;
import com.sensetime.sdk.video.module.sdk.common.StTrackResult;
import com.sensetime.sdk.video.module.sdk.common.StVerifyResult;
import com.ty.identifty.base.FailType;
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
        if (!isInit) return errorReturn("初始化未完成", yuv, FailType.NOT_INIT);
        TyLivingModel jfFaceResult = null;
        //构建图片信息
        StImage stImage = createStImage(yuv, width, height, rectF);

        /*---------人脸检测----------*/
        int ret = mStDetect.sdkVideoDetectOne(stImage);
        StHelper.parseSdkErrorCode(ret, "检测");
        if (ret != 0) return errorReturn("检测异常", yuv, FailType.NO_FACE);
        //获取人脸检测的结果
        StTrackResult stDetectResult = mStDetect.getStDetectResult();
        if (stDetectResult == null) {
            return errorReturn("检测结果为空", yuv, FailType.NO_FACE);
        }
        //获取人员检测中的人脸信息
        StTrackInfo[] detectInfo = stDetectResult.getTrackInfo();
        if (detectInfo == null || detectInfo.length == 0) {
            return errorReturn("未检测到人脸", yuv, FailType.NO_FACE);
        }

        /*---------人脸质量检测----------*/
        ret = mStQuality.sdkVideoQualityDetect(stImage, detectInfo);
        StHelper.parseSdkErrorCode(ret, "质量");
        if (ret != 0) {
            return errorReturn("质量检测异常", yuv, FailType.NO_QUALITY);
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

            // 关键点越界校验：越界直接丢弃，避免特征提取对齐时 native 越界崩溃
            if (!isPointsInBounds(qualityInfo.getPointsArray(), width, height)) {
                StHelper.INSTANCE.d("关键点数字越界", false);
                return false;
            }

            String fileName = "detect_" + key + ".jpg";
            float quality = 0.0f;
            String ss = "水平角: " + qualityInfo.getYaw()
                    + ", 俯仰角: " + qualityInfo.getPitch()
                    + ", 遮挡比例: " + qualityInfo.getOcclusionRatio()
                    + ", 左眼状态: " + qualityInfo.getEyestateLeft()
                    + ", 综合质量: " + (quality = qualityInfo.getQuality());
            StHelper.INSTANCE.d(ss, false);
            // 打印关键点，用于判断坐标格式（临时排查用，定位后可删）
            for (StQualityInfo qi : stQualityInfo) {
                StringBuilder sb = getStringBuilder(width, height, qi, stImage);
                StHelper.INSTANCE.d(sb.toString(), false);
            }

            fileName = "invalide_" + qualityInfo.getYaw() + "_" + qualityInfo.getPitch() + "_" + quality + ".jpg";
            // 综合质量低于0.55的时候，开启丢弃策略
            // 角度硬丢弃：俯仰角/水平角超过45度直接丢弃（不依赖综合质量阈值，
            // 否则综合质量>=0.55时大角度人脸会绕过丢弃进入特征提取，导致SDK对齐越界崩溃）
            if (Math.abs(qualityInfo.getPitch()) > 45) {
                return false;
            }
            if (Math.abs(qualityInfo.getYaw()) > 45) {
                return false;
            }
            // 遮挡硬丢弃：超过0.35直接丢弃
            if (Math.abs(qualityInfo.getOcclusionRatio()) > 0.35f) {
                return false;
            }
            // 综合质量低于0.55时，开启更严格的丢弃策略
            if (quality < 0.55f) {
                // 综合质量 < 0.2 直接丢弃
                return !(Math.abs(qualityInfo.getQuality()) < 0.2f);
            }
            return true;
        }).collect(Collectors.toList());

        /**************************************************************/
        if (qualityInfos.isEmpty()) {
            return errorReturn("质量角度检测异常", yuv, FailType.NO_QUALITY);
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
            if (faceNum > 1)
                return errorReturn("特征抽取失败，图片中发现多个人脸", yuv, FailType.MORE_FACE);
            //提取特征值
            StVerifyResult stVerifyResult = getFeature(stImage, detectInfos.get(0), qualityInfos.get(0));
            if (stVerifyResult == null)
                return errorReturn("特征提取异常", yuv, FailType.NO_FEATURE);
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
            return errorReturn("未查询到人员", yuv, FailType.NO_SEARCH);
        }
    }

    @NonNull
    private static StringBuilder getStringBuilder(int width, int height, StQualityInfo qi, StImage stImage) {
        StPoint[] pts = qi.getPointsArray();
        StringBuilder sb = new StringBuilder("points[frame=" + width + "x" + height + ", len=" + (pts == null ? -1 : pts.length) + "] ");
        if (pts != null) {
            for (int i = 0; i < Math.min(pts.length, 20); i++) {
                sb.append(pts[i].getX()).append("--").append(pts[i].getY()).append(",");
            }
        }
        StRect r = qi.getRect();
        if (r != null) {
            sb.append(" rect=[").append(r.getLeft()).append(",").append(r.getTop())
                    .append(",").append(r.getRight()).append(",").append(r.getBottom()).append("]");
        }
        sb.append("stimage: ").append(stImage.getWidth()).append(" height: ").append(stImage.getHeight()).append(" datasize: ").append(stImage.getData().length);
        return sb;
    }

    private TyLivingModel errorReturn(String errorMsg, byte[] yuv, FailType failType) {
        StHelper.INSTANCE.e(errorMsg, true);
        onFaceFast(null);
        TyLivingModel model = new TyLivingModel();
        model.setFailType(failType);
        TyFaceInfo faceInfo = new TyFaceInfo();
        faceInfo.setFaceIconData(yuv);
        model.setFaceInfo(faceInfo);
        model.setMsg(errorMsg);
        return model;
    }

    /**
     * 校验人脸关键点是否越界。
     * 关键点为像素坐标、x,y 交错存储（如 53 点 = 106 个 float）。
     * 任一关键点坐标超出 [0,width]x[0,height] 视为越界，避免进入 SDK 对齐时 native 越界崩溃。
     */
    private boolean isPointsInBounds(StPoint[] points, int width, int height) {
        if (points == null || points.length < 2 || (points.length % 2) != 0) {
            return false;
        }
        for (int i = 0; i + 1 < points.length; i += 2) {
            float x = points[i].getX();
            float y = points[i].getY();
            if (x < 0f || y < 0f || x > width || y > height) {
                return false;
            }
        }
        return true;
    }

    private StVerifyResult getFeature(StImage stImage, StTrackInfo stTrackInfo, StQualityInfo stQualityInfo) {
        if (!isInit) return null;
        float quality = stQualityInfo.getQuality();
        float confidence = stTrackInfo.getConfidence();
        int ret = -1;
        if (quality >= mQualityThreshold && confidence >= mConfidenceThreshold
                && stQualityInfo.getPointsArray() != null)
            ret = mStVerify.sdkVideoVerifyOne(stImage, stQualityInfo.getPointsArray());
        StHelper.parseSdkErrorCode(ret, "特征提取");
        if (ret == 0) {
            return mStVerify.getStVerifyResult();
        }
        return null;
    }


    // 创建识别对象
    private StImage createStImage(byte[] yuv, int width, int height, RectF rectF) {
        // 注意：历史实现曾在传入 rectF 时把 dstWidth/dstHeight/stride 改为 rectF 的宽高，
        // 但 yuv 数据仍是整图未裁剪，导致 StImage 声明的尺寸/stride 与实际数据不一致，
        // 在 SDK 人脸对齐(仿射变换 WarpAffineLinear)阶段越界读内存崩溃。
        // 此处统一使用整图，保证数据、宽高、stride 三者严格一致；如需按 rectF 裁剪，
        // 必须先把 yuv 正确裁剪（含偏移与偶对齐）后再传入对应宽高。
        return new StImage(width, height, width, StImage.StPixelFormat.PixFmtNV21, yuv);
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
