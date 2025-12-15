package com.ty.tybusbase.identify;


import static com.ty.tybusbase.expand.BusiExpandKt.getFaceBitmap;
import static com.ty.tybusbase.expand.BusiExpandKt.getString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.adl.lib.pose.AdlPoseKeyPoint;
import com.adl.lib.pose.AdlPoseSkeleton;
import com.adl.service.entity.StudentEntity;
import com.adl.service.entity.TeacherEntity;
import com.adl.service.outer.AdlService;
import com.adl.service.outer.UserCaller;
import com.adl.sport.general.EuHelper;
import com.ty.identifty.base.TyFaceInfo;
import com.ty.tybusbase.R;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.constants.MMKVBusiKey;
import com.ty.tybusbase.databinding.LayoutSingleFaceBinding;
import com.ty.tybusbase.entity.AccountInfo;
import com.ty.tybusbase.face.FaceDetectorHelper;
import com.ty.tybusbase.face.OnFaceCallback;
import com.ty.tybusbase.helper.SpeechWrapper;
import com.ty.tybusbase.helper.TimeOutHelper;
import com.ty.tybusbase.utils.ApplicationUtil;
import com.ty.tybusbase.utils.DisplayUtil;
import com.ty.tybusbase.utils.LogUtil;
import com.ty.tybusbase.utils.MMKVUtil;
import com.ty.tybusbase.utils.ToastUtil;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import kotlin.Unit;

/**
 * @author pc
 */
public class SingleIdentityMachine extends RelativeLayout implements IdentifyMachine {
    LayoutSingleFaceBinding mBinding;
    /**
     * 检测区域
     */
    private Rect sportRect;

    /**
     * 屏幕大小
     */
    private Point size;
    /**
     * 开始识别时间
     */
    private long identityStartTime;

    private IdentifyMachineListener identityListener;

    private AccountInfo accountInfo;

    /**
     * 身份识别成功
     */
    private boolean identityOk;
    /**
     * 游客模式
     */
    private boolean visitorAble;
    /**
     * 这是的识别方式
     */
    private int identity_type;

    private Context mContext;
    /**
     * 是否已经刷卡
     */
    private boolean cardOk;

    /**
     * 识别完成时，是否需要检测人在区域里
     */
    private boolean needFinishAreaCheck = false;

    /**
     * 人脸检测是否需要做区域检测
     */
    private boolean needIdentifyAreaCheck = true;

    //提示文案
    private String noticeText;

    private AdlPoseSkeleton currentSkeleton;

    /**
     * 扫码OK
     */
    private boolean qrOk;

    private Handler mHandler;
    private float scW;
    private float scH;

    private CompositeDisposable mQrDisposable = new CompositeDisposable();

    private boolean speakResult = true;//是否播放识别结果

    private Activity mActivity;

    private TimeOutHelper timeOutHelper = new TimeOutHelper(() -> {
        ToastUtil.getInstance(ApplicationUtil.INSTANCE.getContext(), getString(R.string.identify_time_out)).show();
        if (mActivity != null && mHandler != null) {
            mHandler.postDelayed(() -> {
                mActivity.finish();
            }, BusiBaseConstantsValue.FINISH_TIME_OUT);
        }
        return Unit.INSTANCE;
    }, BusiBaseConstantsValue.IDENTITY_TIME_OUT);

    public SingleIdentityMachine(Context context) {
        this(context, null);
    }

    public SingleIdentityMachine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SingleIdentityMachine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setNeedFinishAreaCheck(boolean value) {
        this.needFinishAreaCheck = value;
    }

    public void setNeedIdentifyAreaCheck(boolean value) {
        if (mBinding != null) {
            mBinding.ivAreaNotice.setVisibility(value ? View.VISIBLE : View.GONE);
        }
        this.needIdentifyAreaCheck = value;
    }

    public void setNoticeText(String noticeText) {
        if (mBinding != null && !TextUtils.isEmpty(noticeText)) {
            mBinding.tvStateNotice.setText(noticeText);
        }
        this.noticeText = noticeText;
    }

    private void init(Context context) {
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_single_face, this, true);
        mBinding = LayoutSingleFaceBinding.bind(view);
        mBinding.ivAreaNotice.setVisibility(needIdentifyAreaCheck ? View.VISIBLE : View.GONE);
        //1.获取是否开启游客模式，如果未开启游客模式会倒计时推出
        visitorAble = MMKVUtil.INSTANCE.decodeBoolean(BusiBaseConstantsValue.VISITOR_ABLE, BusiBaseConstantsValue.VISITOR_OPEN);
        //2.获取身份识别的方式
        identity_type = MMKVUtil.INSTANCE.decodeInt(MMKVBusiKey.IDENTITY_TYPE, 0);
        //3.获取屏幕尺寸
        size = DisplayUtil.INSTANCE.getSize(context);
        //4.运动区域检查

        //首先根据骨骼图的大小获取到换算比例
        scW = (float) size.x / BusiBaseConstantsValue.HTTP_CAMERA_PREVIEW_WIDTH;
        scH = (float) size.y / BusiBaseConstantsValue.HTTP_CAMERA_PREVIEW_HEIGHT;
        setSportRect();
    }

    public void setSportRect() {
//        ivArea.post(() -> {
//            int[] location = new int[2];
//            ivArea.getLocationOnScreen(location);
//            int x = location[0];
//            int y = location[1];
//            int w = ivArea.getWidth();
//            int h = ivArea.getHeight();
//            sportRect = new Rect(x, y, x + w, y + h);
//        });
        sportRect = new Rect(744, 866, 1177, 1050);
    }

    /**
     * 扫码成功
     *
     * @param qrCode
     */
    @SuppressLint("CheckResult")
    private void qrOK(String qrCode) {
        RxJavaPlugins.setErrorHandler(throwable -> {
            LogUtil.INSTANCE.d("error throw " + throwable.toString(), false);
        });
//        AppViewModelFactory.getInstance(AppApplication.getInstance()).getRepository().getSportsQrcodeData(qrCode)
//                .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
//                .doOnSubscribe(disposable -> {
//                    mQrDisposable.add(disposable);
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new BaseDefaultObserver<BaseResponse<QrcodeEntity>>() {
//                    @Override
//                    public void onSuccess(BaseResponse<QrcodeEntity> response) {
//                        if (response.isOk()) {
//                            ZyLogger.d("请求结果》》》》》" + response.getData().getPlatform() + "_" + response.getData().getSportStudentId());
//                            //结果存储在NFC卡字段里面，复用刷卡的识别逻辑
//                            readCardBack(response.getData().getPlatform() + "_" + response.getData().getSportStudentId());
//                        } else {
//                            ToastUtil.getInstance(AppApplication.getInstance(), "二维码识别失败,请重新扫码").show();
//                        }
//                    }
//
//                    @Override
//                    public void onFail(ResponseThrowable throwable) {
//                        ZyLogger.d("请求失败》》》》》" + throwable.msg);
//                        ToastUtil.getInstance(AppApplication.getInstance(), "二维码识别失败,请重新扫码").show();
//                    }
//
//                });
    }


//    public void openMachine(IdentifyMachineListener listener) {
//        //这里做初始化
//        visitorAble = false;
//        openMachine(listener);
//    }

//    @Override
//    public void identify(AdlPoseSkeleton skeleton) {
//        LogUtils.d("游客模式开关:" + visitorAble + " identityOk:" + identityOk);
//        ZyMemoryYuvData data = sportEngineBase.findOriginalNv21(skeleton.getKey());
//        if (data == null) return;
//        doIdentity(skeleton, data.buffer, data.width, data.height);
//    }

    public boolean checkBodyIn(AdlPoseSkeleton skeleton) {
        if (!judgeBodyIn(skeleton, scW, scH)) {
            //getHandler可能为空，做空判断
            //java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.os.Handler.post(java.lang.Runnable)' on a null object reference
            //at com.zy.sportbs.sport.identify.SingleIdentityForZy.checkBodyIn(SingleIdentityForZy.java:241)
            if (getHandler() != null) {
                getHandler().post(() -> {
                    mBinding.ivAreaNotice.setImageResource(R.mipmap.bg_foot_area);
                });
            }
            return false;
        } else {
            if (getHandler() != null) {
                getHandler().post(() -> {
                    mBinding.ivAreaNotice.setImageResource(R.mipmap.rect_area);
                });
            }
            return true;
        }
    }


    @Override
    public void openMachine(IdentifyMachineListener listener) {
        identity_type = MMKVUtil.INSTANCE.decodeInt(MMKVBusiKey.IDENTITY_TYPE, BusiBaseConstantsValue.IDENTITY_TYPE_FACE);
        //这里做初始化
        this.identityListener = listener;
        isFirstDetect = true;
        cardOk = false;
        switch (identity_type) {
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.hand_up));
                selectFace();
                uiFace();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card));
                selectCard();
                uiCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_qr));
                selectQr();
                uiQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                selectFace();
                selectCard();
                uiFaceCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_qr));
                selectFace();
                selectQr();
                uiFaceQr();
                break;

            case BusiBaseConstantsValue.IDENTITY_TYPE_QR | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card_qr));
                selectQr();
                selectCard();
                uiCardQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_card_qr));
                selectFace();
                selectCard();
                selectQr();
                uiAll();
                break;
        }
        this.setVisibility(VISIBLE);
        identityOk = false;
        identityListener.onIdentityMachineInit(true);
        //设置识别超时任务，超时3秒后，关闭页面
        startTimeOutTask();
    }

    @Override
    public void openMachineByMatchModel(IdentifyMachineListener listener, AccountInfo accountInfo) {
        this.identityListener = listener;
        identityListener.onIdentityMachineInit(true);
        this.setVisibility(VISIBLE);
        matchModel(accountInfo);
        //设置识别超时任务，超时3秒后，关闭页面
        startTimeOutTask();
    }

    /**
     * 比赛模式，需要人到区域才开始
     *
     * @param accountInfo
     */
    public void matchModel(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
        identityOk = true;
        mBinding.ivAreaNotice.setVisibility(VISIBLE);
        setSportRect();
        mBinding.progressView.setVisibility(GONE);
        mBinding.rlIdentityNotice.setVisibility(GONE);
        mBinding.rlAreaNotice.setVisibility(VISIBLE);
        mBinding.tvAreaNotice.setText(getString(R.string.move_to_rect));
        SpeechWrapper.INSTANCE.speech(getString(R.string.move_to_rect));
        setNeedFinishAreaCheck(true);
    }

    @Override
    public void identify(AdlPoseSkeleton skeleton, byte[] data, int w, int h, int type) {
        currentSkeleton = skeleton;
        if (identityOk && !needFinishAreaCheck) {
            return;
        }

        //需要检测是否进入区域
        if (identityOk && judgeBodyIn(skeleton, scW, scH)) {
            getHandler().postDelayed(() -> identityOkCallback(accountInfo), 100);
            return;
        }

        //记录身份识别开始时间
        //完成刷卡
        if ((identity_type & BusiBaseConstantsValue.IDENTITY_TYPE_FACE) != BusiBaseConstantsValue.IDENTITY_TYPE_FACE) {
            return;
        }

        //判断有没有举手
        if (needIdentifyAreaCheck && !checkBodyIn(skeleton) || !judgeHandUp(skeleton)) {
            return;
        }

        if (identityStartTime == 0) {
            identityStartTime = System.currentTimeMillis();
        }

        if (skeleton == null) {
            LogUtil.INSTANCE.d("***未找到用户***", false);
            return;
        }

        long faceRecTime = System.currentTimeMillis() - identityStartTime;
        //当没有正在识别人脸和达到超时要求就判断为游客
        if (!FaceDetectorHelper.Companion.getInstance().isDetecting() && (faceRecTime >= VISITOR_TIME)) {
            if (visitorAble) {
                createVisitor();
            } else {
                finishWithFail();
            }
            return;
        }
//        bodyInfo = body;

        if (isFirstDetect) {
            isFirstDetect = false;
            SpeechWrapper.INSTANCE.stop();
            SpeechWrapper.INSTANCE.speech(getString(R.string.identifying_id));
            getHandler().post(() -> {
                mBinding.tvStateNotice.setText(getString(R.string.scan_notice));

            });
        }

        getHandler().post(() -> {
//            acProgressView.setProgress((System.currentTimeMillis() - identityStartTime) / 100 >= 99 ? 99 : (System.currentTimeMillis() - identityStartTime) / 100);
            float progress = (faceRecTime * 1.0f / VISITOR_TIME) * 100;
            mBinding.progressView.setProgress(progress);
        });
        if (!FaceDetectorHelper.Companion.getInstance().isDetecting()) {
            LogUtil.INSTANCE.d("face yuv 数据是否为空:" + (null == data) + " key is:" + skeleton.getKey(), false);
            if (data == null) return;
            faceDetect(System.currentTimeMillis() + "", data, w, h, skeleton, type);
        }
    }

    @Override
    public void identify(AdlPoseSkeleton aiResponse, byte[] data, int w, int h, boolean isLeft) {

    }

    public void reset() {
        identityStartTime = 0;
        isFirstDetect = true;
        cardOk = false;
        qrOk = false;
        switch (identity_type) {
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.hand_up));
                uiFace();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card));
                uiCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_qr));
                uiQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                uiFaceCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_qr));
                uiFaceQr();
                break;

            case BusiBaseConstantsValue.IDENTITY_TYPE_QR | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card_qr));
                uiCardQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_card_qr));
                uiAll();
                break;
        }

        this.setVisibility(VISIBLE);
        identityOk = false;
    }

    public void reStart(IdentifyMachineListener listener) {
        identityStartTime = 0;
        isFirstDetect = true;
        cardOk = false;
        identityOk = false;
        this.identityListener = listener;
        switch (identity_type) {
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.hand_up));
                uiFace();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card));
                uiCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_qr));
                uiQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                uiFaceCard();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_qr));
                uiFaceQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_QR | BusiBaseConstantsValue.IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card_qr));
                uiCardQr();
                break;
            case BusiBaseConstantsValue.IDENTITY_TYPE_FACE | BusiBaseConstantsValue.IDENTITY_TYPE_CARD | BusiBaseConstantsValue.IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_card_qr));
                uiAll();
                break;
        }
        this.setVisibility(VISIBLE);
        identityListener.onIdentityMachineInit(true);
        startTimeOutTask();
    }

    public void setVisitorAble(boolean visitor_able) {
        this.visitorAble = visitor_able;
    }

    /**
     * 刷卡
     */
    private void selectCard() {
//        UsbReader usbReader = UsbReader.getUsbReader();
//        usbReader.init();
//        usbReader.startRead(this);
    }

    /**
     * 人脸识别
     */
    private void selectFace() {
        FaceDetectorHelper.Companion.getInstance().setFaceCallback(new OnFaceCallback() {
            @Override
            public void onFindStudent(@Nullable StudentEntity user) {
                identity(user);
            }

            @Override
            public void onFindFail(@Nullable TyFaceInfo<?> info) {
            }

            @Override
            public void onFindTeacher(@Nullable TeacherEntity user) {
                identity(user);
            }
        });
    }


    /**
     * 扫码
     */
    private void selectQr() {
//        if (serialPort == null) {
//            serialPort = new AdlSerialPort();
//        }
//        serialPort.open("/dev/ttyS1", 9600, value -> {
//            qrOk = true;
//            qrOK(value);
//        }, 0);
    }

    /**
     * 识别失败
     */
    private void finishWithFail() {
        if (identityListener != null && !identityOk) {
            identityOk = true;
            if (mBinding.progressView.getVisibility() == View.VISIBLE) {

                SpeechWrapper.INSTANCE.speech2(getString(R.string.rec_face_fail_retry), () -> mBinding.progressView.post(() -> {
                    reset();
                }));

            }

            mBinding.progressView.post(() -> {
                mBinding.progressView.setVisibility(GONE);
            });

        }

    }

    /**
     * 刷卡的ui
     */
    private void uiCard() {
        setUi(false, R.string.card_notice, R.mipmap.card);
    }

    /**
     * 人脸识别的UI
     */
    private void uiFace() {
        setUi(true, R.string.hand_up, R.mipmap.hand_up_icon);
    }


    /**
     * 刷二维码
     */
    private void uiQr() {
        setUi(false, R.string.qr_notice, R.mipmap.icon_identity_qr);
    }

    /**
     * 人脸和刷卡
     */
    private void uiFaceCard() {
        setUi(true, R.string.card_face_notice, R.mipmap.hand_up_icon);
    }

    /**
     * 人脸和二维码
     */
    private void uiFaceQr() {
        setUi(true, R.string.face_qr_notice, R.mipmap.hand_up_icon);
    }

    /**
     * 刷卡和二维码
     */
    private void uiCardQr() {
        setUi(false, R.string.card_qr_notice, R.mipmap.card);
    }

    private void uiAll() {
        setUi(true, R.string.face_card_qr_notice, R.mipmap.hand_up_icon);
    }

    private void setUi(boolean hasFace, int textRes, int imgRes) {
        if (hasFace) {
            mBinding.progressView.setProgress(0);
            mBinding.progressView.setVisibility(VISIBLE);
            if (needIdentifyAreaCheck) {
                mBinding.ivAreaNotice.setImageResource(R.mipmap.bg_foot_area);
                mBinding.ivAreaNotice.setVisibility(VISIBLE);
            }

        } else {
            mBinding.ivAreaNotice.setVisibility(GONE);
            mBinding.progressView.setVisibility(GONE);
        }
        mBinding.ivState.setImageResource(imgRes);
        if (TextUtils.isEmpty(noticeText)) {
            mBinding.tvStateNotice.setText(textRes);
        } else {
            mBinding.tvStateNotice.setText(noticeText);
        }
    }

    private static boolean isFirstDetect;
    private static final int VISITOR_TIME = 1 * 1000;

    public void identityFinish() {
        if (identityListener != null && !identityOk) {
            identityOk = true;
            mBinding.tvStateNotice.setText(accountInfo.getStudentEntity().getAccountName());
            mBinding.ivState.setImageResource(R.mipmap.identity_ok);
            mBinding.progressView.setVisibility(GONE);
            if (speakResult) {
                SpeechWrapper.INSTANCE.stop();
                SpeechWrapper.INSTANCE.speech(getString(R.string.identified_someone, accountInfo.getStudentEntity().getAccountName()));
            }
            if (needFinishAreaCheck && !judgeBodyIn(currentSkeleton, scW, scH)) {
                mBinding.ivAreaNotice.setVisibility(VISIBLE);
                mBinding.rlIdentityNotice.setVisibility(GONE);
                mBinding.rlAreaNotice.setVisibility(VISIBLE);
                SpeechWrapper.INSTANCE.speech(getString(R.string.move_to_rect));
                return;
            }
            getHandler().post(() -> {
                identityOkCallback(accountInfo);
            });

        }
    }

    private void identityOkCallback(AccountInfo accountInfo) {
        cancelTimeOutTask();
        if (identityListener != null && accountInfo != null) {
            identityListener.onIdentityOk(accountInfo);
            identityListener = null;
        }
    }

    private void createVisitor() {
        accountInfo = new AccountInfo();
        StudentEntity user = new StudentEntity();
        user.setUserId(BusiBaseConstantsValue.VISITOR_USER_ID);
        user.setUserFaceImgUrl("");
        user.setAccountName(getString(R.string.visitor));
        accountInfo.setStudentEntity(user);
        getHandler().post(() -> {
            mBinding.progressView.setProgress(100);
            identityFinish();
        });


    }

    public void restUI() {

    }

    @Override
    public void find(String sportLocationLeft) {

    }

    /**
     * 判断有没有举手
     *
     * @param skeleton
     * @return
     */
    private boolean judgeHandUp(AdlPoseSkeleton skeleton) {
        if (skeleton == null || skeleton.getLWrist() == null || skeleton.getLShoulder() == null)
            return false;
        return skeleton.getLWrist().y < skeleton.getLShoulder().y;
    }

    /**
     * 判断当前人员是否在运动区域内
     *
     * @param skeleton
     * @param scW
     * @param scH
     * @return
     */
    private boolean judgeBodyIn(AdlPoseSkeleton skeleton, float scW, float scH) {
        if (skeleton == null) return false;
        //脚踝
        if (skeleton.getLAnkle() == null || !sportRect.contains((int) (skeleton.getLAnkle().getX() * scW), (int) (skeleton.getLAnkle().getY() * scH)))
            return false;
        if (skeleton.getRAnkle() == null || !sportRect.contains((int) (skeleton.getRAnkle().getX() * scW), (int) (skeleton.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    public boolean judgeBodyIn(AdlPoseSkeleton skeleton) {
        if (skeleton == null) return false;
        //脚踝
        if (skeleton.getLAnkle() == null || !sportRect.contains((int) (skeleton.getLAnkle().getX() * scW), (int) (skeleton.getLAnkle().getY() * scH)))
            return false;
        if (skeleton.getRAnkle() == null || !sportRect.contains((int) (skeleton.getRAnkle().getX() * scW), (int) (skeleton.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    private void faceDetect(String imageId, byte[] yuv, int w, int h, AdlPoseSkeleton skeleton, int type) {
        if (w > 0 && h > 0) {

            // YUV数据裁剪
            AdlPoseKeyPoint vlEar = skeleton.getLEar();
            AdlPoseKeyPoint vrEar = skeleton.getREar();
            AdlPoseKeyPoint vNose = skeleton.getNose();
            AdlPoseKeyPoint lShoulder = skeleton.getLShoulder();
            AdlPoseKeyPoint rShoulder = skeleton.getRShoulder();

            int displayWidth = getWidth();
            int displayHeight = getHeight();
            float sx = (float) displayWidth / w;
            float sy = (float) displayHeight / h;
            RectF rectF = null;
            float headerTop = 0;
            if (skeleton.getHeadTop() != null) {
                AdlPoseKeyPoint header = resetPoint(skeleton.getHeadTop(), sx, sy);
                headerTop = header.y;
            }
            if (vlEar != null && vrEar != null && vNose != null) {
                // 点映射
                AdlPoseKeyPoint lEar = resetPoint(vlEar, sx, sy);
                AdlPoseKeyPoint rEar = resetPoint(vrEar, sx, sy);
                AdlPoseKeyPoint nose = resetPoint(vNose, sx, sy);
                // 以鼻子为圆心，眼距2倍为半径
                int centerX = (int) nose.getX();
                int centerY = (int) nose.getY();
//                int radius = (int) (Math.abs(EuHelper.readX(lEar) - EuHelper.readX(rEar)) * 2f);
                int radius = (int) (Math.abs(lEar.x - rEar.x) * 2f);
                float l = Math.max(centerX - radius / 2, 0);
                float t = headerTop == 0 ? Math.max(centerY - radius / 2, 0) : headerTop;
                float fw = Math.min(l + radius, w);
                float fh = Math.min(t + radius, h);
                rectF = new RectF(l, t, fw, fh);
            } else if (lShoulder != null && rShoulder != null) {
                lShoulder = resetPoint(lShoulder, sx, sy);
                rShoulder = resetPoint(rShoulder, sx, sy);
                //头部裁剪框，以肩膀向上1.5倍肩宽
                float height = (float) (EuHelper.distance(lShoulder, rShoulder) * 1.5f);
                float l = Math.max(rShoulder.x, 0);
                float t = headerTop == 0 ? Math.max((lShoulder.y + rShoulder.y) / 2 - height, 0) : headerTop;
                float r = Math.min(lShoulder.x, w);
                float b = Math.min((lShoulder.y + rShoulder.y) / 2, h);
                rectF = new RectF(l, t, r, b);
            }
            if (rectF == null) return;
            FaceDetectorHelper.Companion.getInstance().detectFace(imageId, yuv, w, h, rectF, type);
        }
    }

    private AdlPoseKeyPoint resetPoint(AdlPoseKeyPoint p, float sx, float sy) {
        return new AdlPoseKeyPoint(p.x / sx, p.y / sy, p.score);
    }

    public void identity(StudentEntity user) {
        accountInfo = new AccountInfo();
        user.setUserFaceImgUrl(AdlService.getService().getFacePrefix() + user.getUserFaceImgUrl());
        accountInfo.setStudentEntity(user);
        getHandler().post(() -> {
            mBinding.progressView.setProgress(100);
            identityFinish();
        });

    }

    public void identity(TeacherEntity user) {
        accountInfo = new AccountInfo();
        user.setAccountAvatar(AdlService.getService().getFacePrefix() + user.getAccountAvatar());
        accountInfo.setTeacherEntity(user);
        getHandler().post(() -> {
            mBinding.progressView.setProgress(100);
            identityFinish();
        });
    }

    public void getAccountInfoWithCard(String card) {
        List<StudentEntity> studentList = UserCaller.instance().queryStudentByCarNum(card);
        for (StudentEntity baseForFace : studentList) {
            List<String> cardNumList = baseForFace.getCardNums();
            if (cardNumList == null || cardNumList.isEmpty()) continue;
            if (cardNumList.contains(card)) {
                identity(baseForFace);
                return;
            }
        }
        cardOk = false;
    }


    @Override
    public void closeMachine() {
        this.setVisibility(View.GONE);
        identityStartTime = 0;
        identityListener = null;
        accountInfo = null;
//        UsbReader.getUsbReader().stop();
        FaceDetectorHelper.Companion.getInstance().setFaceCallback(null);
        cancelTimeOutTask();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mQrDisposable.dispose();
        mQrDisposable.clear();
//        if (serialPort != null) {
//            serialPort.close();
//        }
    }

//    @Override
//    public void readCardBack(String cardStr) {
//        if (!TextUtils.isEmpty(cardStr) && !cardOk && !identityOk) {
//            cardOk = true;
//            getAccountInfoWithCard(cardStr);
//        }
//    }

    public Rect getSportRect() {
        return sportRect;
    }

    public boolean isSpeakResult() {
        return speakResult;
    }

    public void setSpeakResult(boolean speakResult) {
        this.speakResult = speakResult;
    }

    //开启任务超时
    public void startTimeOutTask() {
        timeOutHelper.start();
    }

    public void cancelTimeOutTask() {
        timeOutHelper.cancel();
    }
}
