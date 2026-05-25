package com.ty.identifty.bdface;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDarkEnhance;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.model.Feature;
import com.ty.identifty.base.FailType;
import com.ty.identifty.base.TyFaceFeature;
import com.ty.identifty.base.TyFaceLogger;
import com.ty.identifty.bdface.model.GlobalSet;
import com.ty.identifty.bdface.model.LivenessModel;

import java.util.ArrayList;
import java.util.List;

public class FaceSDKManager {

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    public static volatile int initStatus = SDK_UNACTIVATION;
    public static volatile boolean initModelSuccess = false;
    private FaceAuth faceAuth;

    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;

    private FaceDarkEnhance faceDarkEnhance;

    private int featureSize = 0;

    private FaceSDKManager() {
        faceAuth = new FaceAuth();
        setActiveLog();
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_NO_BIND, 2);
    }

    public void setActiveLog() {
        if (faceAuth != null) {
            faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_ERROR_MESSAGE, 0);
            faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 0);
            faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_VALUE_MESSAGE, 0);
            faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_DEBUG, 0);
            faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_PERF, 0);
        }
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static boolean isOk() {
        return initModelSuccess;
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetect getFaceDetect() {
        return faceDetect;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public FaceLive getFaceLiveness() {
        return faceLiveness;
    }

    /**
     * 初始化模型，目前包含检查，活体，识别模型；因为初始化是顺序执行，可以在最好初始化回掉中返回状态结果
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {

        // 默认检测
        BDFaceInstance bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.creatInstance();
        faceDetect = new FaceDetect(bdFaceInstance);

        // 默认识别
        faceFeature = new FaceFeature();

        faceLiveness = new FaceLive();
        // 暗光檢測
        faceDarkEnhance = new FaceDarkEnhance();

        initConfig();

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_TRACK_MODEL,
                BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_RGB_MODEL, BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        faceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL,
                GlobalSet.EMOTION_MODEL, (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        faceDetect.initBestImage(context, GlobalSet.BEST_IMAGE, (code, response) -> {
            if (code != 0 && listener != null) {
                listener.initModelFail(code, response);
            }
        });

        // 初始化暗光恢复
        faceDarkEnhance.initFaceDarkEnhance(context,
                GlobalSet.DARK_ENHANCE_MODEL, (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
                GlobalSet.LIVE_NIR_MODEL,
                GlobalSet.LIVE_DEPTH_MODEL,
                (code, response) -> {
                    if (code != 0 && listener != null) {
                        listener.initModelFail(code, response);
                    }
                });

        // 初始化特征提取模型
        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                GlobalSet.RECOGNIZE_NIR_MODEL,
                GlobalSet.RECOGNIZE_RGBD_MODEL,
                (code, response) -> {
                    if (code != 0) {
                        if (listener != null) {
                            listener.initModelFail(code, response);
                        }
                    } else {
                        initStatus = SDK_MODEL_LOAD_SUCCESS;
                        // 模型初始化成功，加载人脸数据
                        if (listener != null) {
                            listener.initModelSuccess();
                        }
                    }
                });
    }

    // 加载人脸地库
    public synchronized void loadFace(List<TyFaceFeature<Object>> faces) {
        if (!isOk() || faces == null || faces.isEmpty()) return;
        featureSize = faces.size();
        List<Feature> features = new ArrayList<>();
        long time = System.currentTimeMillis();
        faces.stream().forEach(item -> {
            Feature feature = new Feature();
            feature.setId(item.getId());
            feature.setUserId(item.getUserId());
            feature.setFeature(item.getFeature());
            features.add(feature);
        });
        Log.d("BUSI>>", "loadFace time:" + (System.currentTimeMillis() - time));
        faceFeature.featurePush(features);
    }

    /**
     * 初始化配置
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: 最小人脸个数检查，默认设置为1,用户根据自己需求调整
            config.maxDetectNum = 1;

            // TODO: 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
            config.minFaceSize = 35;// SingleBaseConfig.getBaseConfig().getMinimumFace();

            // TODO: 默认为0.5。可传入大于0.3的数值
            config.notRGBFaceThreshold = 0.35f;//SingleBaseConfig.getBaseConfig().getFaceThreshold();
            config.notNIRFaceThreshold = 0.35f;//SingleBaseConfig.getBaseConfig().getFaceThreshold();

            // 是否进行属性检测，默认关闭
            config.isAttribute = false;// SingleBaseConfig.getBaseConfig().isAttribute();

            // TODO: 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);

            return true;
        }
        return false;
    }

    /**
     * 检测-活体-特征-人脸检索流程
     * <p>
     * //     * @param rgbData            可见光YUV 数据流
     * //     * @param nirData            红外YUV 数据流
     * //     * @param depthData          深度depth 数据流
     * <p>
     * //     * @param liveCheckMode      活体检测类型【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
     *
     * @param faceDetectCallBack
     */
    public LivenessModel onDetectCheckByRGB(BDFaceImageInstance rgbInstance,
                                            final int featureCheckMode,
                                            final String key,
                                            float threholdScore,
                                            final FaceDetectCallBack faceDetectCallBack) {

        // 【1: 不提取特征】
        // 【2: 提取特征】
        // 【3: 提取特征+ 1：N检索】
        // liveCheckMode : 1.RGB活体
        return onDetectCheck(rgbInstance, 1, featureCheckMode, key, threholdScore, faceDetectCallBack);
    }

    /**
     * 检测-活体-特征- 全流程
     * <p>
     * //     * @param rgbData            可见光YUV 数据流
     * //     * @param nirData            红外YUV 数据流
     * //     * @param depthData          深度depth 数据流
     *
     * @param liveCheckMode      活体检测模式【不使用活体：0】；【RGB活体：1】；【RGB+NIR活体：2】；【RGB+Depth活体：3】；【RGB+NIR+Depth活体：4】
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    private LivenessModel onDetectCheck(BDFaceImageInstance rgbInstance,
                                        final int liveCheckMode,
                                        final int featureCheckMode,
                                        final String key,
                                        float threholdScore,
                                        final FaceDetectCallBack faceDetectCallBack) {

        LivenessModel livenessModel = new LivenessModel();
        // 模型加载
        if (!initModelSuccess) {
            TyFaceLogger.e("模型没有初始化");
            livenessModel.setFailType(FailType.NOT_INIT);
            livenessModel.setMsg("模型没有初始化");
            return livenessModel;
        }

        long startTime = System.currentTimeMillis();

        // 创建检测结果存储数据
        livenessModel.setKey(key);

        LivenessModel result = null;

        // TODO: getImage() 获取送检图片,如果检测数据有问题，可以通过image view 展示送检图片
        livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

        // 检查函数调用，返回检测结果
        long startDetectTime = System.currentTimeMillis();

        // 快速检测获取人脸信息，仅用于绘制人脸框，详细人脸数据后续获取
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .track(BDFaceSDKCommon.DetectType.DETECT_VIS,
                        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                        rgbInstance);
        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);

        // 检测结果判断
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo info = faceInfos[0];
            livenessModel.setTrackFaceInfo(faceInfos);
            livenessModel.setFaceInfo(info);
            livenessModel.setTrackStatus(1);
            livenessModel.setLandmarks(info.landmarks);
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onFaceDetectDrawCallback(livenessModel);
            }

            // 活体检测
            result = onLivenessCheck(rgbInstance, livenessModel.getLandmarks(),
                    livenessModel, startTime, featureCheckMode,
                    threholdScore, faceDetectCallBack);

        } else {
            // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
            rgbInstance.destory();
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onFaceDetectCallback(null);
                faceDetectCallBack.onFaceDetectDrawCallback(null);
                faceDetectCallBack.onTip(0, "未检测到人脸");
            }
            livenessModel.setFailType(FailType.NO_FACE);
            livenessModel.setMsg("未检测到人脸");
            result = livenessModel;
        }

        return result;
    }

    /**
     * 活体-特征-人脸检索全流程
     *
     * @param rgbInstance        可见光底层送检对象
     *                           //     * @param nirData            红外YUV 数据流
     *                           //     * @param depthData          深度depth 数据流
     * @param landmark           检测眼睛，嘴巴，鼻子，72个关键点
     * @param livenessModel      检测结果数据集合
     * @param startTime          开始检测时间
     * @param featureCheckMode   特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     * @param faceDetectCallBack
     */
    private LivenessModel onLivenessCheck(final BDFaceImageInstance rgbInstance,
                                          final float[] landmark,
                                          final LivenessModel livenessModel,
                                          final long startTime,
                                          final int featureCheckMode,
                                          float threholdScore,
                                          final FaceDetectCallBack faceDetectCallBack) {

        // TODO 特征提取+人脸检索
        LivenessModel result = onFeatureCheck(rgbInstance, landmark, livenessModel,
                threholdScore, featureCheckMode);

        // 流程结束,记录最终时间
        livenessModel.setAllDetectDuration(System.currentTimeMillis() - startTime);

        // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
        rgbInstance.destory();

        // 显示最终结果提示
        if (faceDetectCallBack != null) {
            faceDetectCallBack.onFaceDetectCallback(livenessModel);
        }

        return result;
    }

    /**
     * 特征提取-人脸识别比对
     *
     * @param rgbInstance      可见光底层送检对象
     * @param landmark         检测眼睛，嘴巴，鼻子，72个关键点
     *                         //     * @param faceInfos        nir人脸数据
     *                         //     * @param nirInstance      nir 图像句柄
     * @param livenessModel    检测结果数据集合
     * @param featureCheckMode 特征抽取模式【不提取特征：1】；【提取特征：2】；【提取特征+1：N检索：3】；
     *                         //     * @param featureType      特征抽取模态执行 【生活照：1】；【证件照：2】；【混合模态：3】；
     */
    private LivenessModel onFeatureCheck(BDFaceImageInstance rgbInstance, float[] landmark,
                                         LivenessModel livenessModel, float threholdScore,
                                         final int featureCheckMode) {

        // 如果不抽取特征，直接返回
        if (featureCheckMode == 1) {
            return null;
        }

        // 获取特征值
        byte[] feature = new byte[512];
        long startFeatureTime = System.currentTimeMillis();
        float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
        livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
        livenessModel.setFeature(feature);
        livenessModel.setFeatureCode(featureSize);

        // 如果只提去特征，不做检索，此处返回
        if (featureCheckMode == 2) {
            return livenessModel;
        }

        // 人脸检索
        return featureSearch(livenessModel, feature, featureSize, threholdScore,
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
    }

    /**
     * 人脸库检索
     *
     * @param livenessModel 检测结果数据集合
     * @param feature       特征点
     * @param featureSize   特征点的size
     * @param type          特征提取类型
     */
    private LivenessModel featureSearch(LivenessModel livenessModel,
                                        byte[] feature,
                                        float featureSize,
                                        float threholdScore,
                                        BDFaceSDKCommon.FeatureType type) {

        // 如果提取特征+检索，调用search 方法
        if (featureSize == GlobalSet.FEATURE_SIZE / 4f) {

            // 特征提取成功
            // TODO 阈值可以根据不同模型调整
            long startFeature = System.currentTimeMillis();
            ArrayList<Feature> featureResult =
                    FaceSDKManager.getInstance().getFaceFeature().featureSearch(feature,
                            type, 1, true);
            if (featureResult == null || featureResult.isEmpty()){
                livenessModel.setFailType(FailType.NO_SEARCH);
            }

            // TODO 返回top num = 1 个数据集合，此处可以任意设置，会返回比对从大到小排序的num 个数据集合
            if (featureResult != null && !featureResult.isEmpty()) {

                // 获取第一个数据
                Feature topFeature = featureResult.get(0);

                // 底库比较
                if (topFeature != null) {

                    TyFaceLogger.d("从底库查询到人脸，"
                            + ",id=" + topFeature.getId()
                            + ",特征分数=" + topFeature.getScore()
                            + ",底库大小=" + this.featureSize
                            + ",是否满足条件=" + (topFeature.getScore() >= threholdScore));

                    if (topFeature.getScore() >= threholdScore) {
                        // 特征分数
                        livenessModel.setFeatureScore(topFeature.getScore());
                        TyFaceFeature user = new TyFaceFeature();
                        user.setId(topFeature.getId());
                        livenessModel.setUser(user);

                        // 从内存中用户数据详情
//                        for (TyFaceFeature user : allFeature) {
//                            if (topFeature.getId() == user.getId()) {
//                                livenessModel.setUser(user);
//                                break;
//                            }
//                        }
                    }
                }
            }

            livenessModel.setCheckDuration(System.currentTimeMillis() - startFeature);
        }else{
            livenessModel.setFailType(FailType.NO_FEATURE);
        }

        return livenessModel;
    }

    public void release() {

    }
}