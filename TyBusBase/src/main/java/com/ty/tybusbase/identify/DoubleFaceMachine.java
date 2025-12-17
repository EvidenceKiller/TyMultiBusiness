package com.ty.tybusbase.identify;

import static com.ty.tybusbase.constants.BusiBaseConstantsValue.IDENTITY_TYPE_CARD;
import static com.ty.tybusbase.constants.BusiBaseConstantsValue.IDENTITY_TYPE_FACE;
import static com.ty.tybusbase.constants.BusiBaseConstantsValue.IDENTITY_TYPE_QR;
import static com.ty.tybusbase.constants.MMKVBusiKey.IDENTITY_TYPE;
import static com.ty.tybusbase.expand.BusiExpandKt.getString;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.adl.base.common.AdlHandlerThread;
import com.adl.lib.pose.AdlPoseKeyPoint;
import com.adl.lib.pose.AdlPoseSkeleton;
import com.adl.service.entity.StudentEntity;
import com.adl.service.entity.TeacherEntity;
import com.adl.service.outer.AdlService;
import com.adl.service.outer.UserCaller;
import com.ty.identifty.base.TyFaceInfo;
import com.ty.nfc.reader.base.NfcType;
import com.ty.nfc.reader.base.ReadCallback;
import com.ty.nfc.reader.manager.NfcReaderManager;
import com.ty.tybusbase.R;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.constants.MMKVBusiKey;
import com.ty.tybusbase.databinding.ViewDoubleFaceIdentifyBinding;
import com.ty.tybusbase.entity.AccountInfo;
import com.ty.tybusbase.face.FaceDetectorHelper;
import com.ty.tybusbase.face.OnFaceCallback;
import com.ty.tybusbase.helper.SpeechWrapper;
import com.ty.tybusbase.helper.TimeOutHelper;
import com.ty.tybusbase.utils.LogUtil;
import com.ty.tybusbase.utils.MMKVUtil;
import com.ty.tybusbase.utils.ScreenAdapterUtils;
import com.ty.tybusbase.utils.ToastUtil;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import kotlin.Unit;

/**
 * 测试新流程专用双人人脸识别
 */
public class DoubleFaceMachine extends RelativeLayout implements ReadCallback {

    private ViewDoubleFaceIdentifyBinding mBinding;
    /**
     * 检测区域
     */
    private Rect leftSportArea;
    private Rect rightSportArea;

    /**
     * 屏幕大小
     */
    private Point size;
    /**
     * 开始识别时间
     */
    private long identityStarLeftTime;
    private long identityStartRightTime;
    private DoubleIdentifyMachineListener identityListener;
    private boolean isLeftOk;
    private boolean isRightOk;
    private boolean isLeft;
    private boolean visitorAble;
    private Context mContext;
    private int identityType;
    private boolean identityOk;
    private boolean cardOkLeft;
    private boolean cardOkRight;

    private AccountInfo leftAccountInfo;
    private AccountInfo rightAccountInfo;

    private boolean footInLeft;
    private boolean footInRight;
    private float scW;
    private float scH;
    private boolean areaIn;
    private boolean isFirst;

    private static final int VISITOR_TIME = 1 * 1000;
    private static final int VISITOR_TIME_RIGHT = 1 * 1000;//因为延迟了2秒所以多+2秒
    private volatile boolean isFistDelay = true;//是否第一次延迟识别右侧，第一次才需要延迟

    private CompositeDisposable mQrDisposable = new CompositeDisposable();
    private boolean needAreaCheck; //是否需要进区域才开始

    private AdlPoseSkeleton currentLeftSkeleton; //左边骨骼
    private AdlPoseSkeleton currentRightSkeleton; //右边骨骼
    private Activity mActivity;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TimeOutHelper timeOutHelper = new TimeOutHelper(() -> {
        ToastUtil.getInstance(mBinding.getRoot().getContext(), mBinding.getRoot().getContext().getString(R.string.identify_time_out)).show();
        if (mActivity != null) {
            mHandler.postDelayed(() -> {
                mActivity.finish();
            }, BusiBaseConstantsValue.FINISH_TIME_OUT);
        }
        return Unit.INSTANCE;
    }, BusiBaseConstantsValue.IDENTITY_TIME_OUT);

    private NfcReaderManager nfcReaderManager;

    public DoubleFaceMachine(Context context) {
        this(context, null);
    }

    public DoubleFaceMachine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public DoubleFaceMachine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setNeedAreaCheck(boolean value) {
        this.needAreaCheck = value;
    }

    private void init(Context context) {
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        mContext = context;
        mBinding = ViewDoubleFaceIdentifyBinding.inflate(LayoutInflater.from(context), this);

        //1.获取是否开启游客模式，如果未开启游客模式会倒计时推出
        visitorAble = MMKVUtil.INSTANCE.decodeBoolean(MMKVBusiKey.VISITOR_ABLE, BusiBaseConstantsValue.VISITOR_OPEN);
        //2.获取身份识别的方式
        identityType = MMKVUtil.INSTANCE.decodeInt(IDENTITY_TYPE, IDENTITY_TYPE_FACE);
        //3.获取屏幕尺寸
        size = ScreenAdapterUtils.getSize(context);
        //4.检测区域
        leftSportArea = new Rect(getResources().getDimensionPixelOffset(R.dimen.dp_210), getResources().getDimensionPixelOffset(R.dimen.dp_820), getResources().getDimensionPixelOffset(R.dimen.dp_750), getResources().getDimensionPixelOffset(R.dimen.dp_1000));
        rightSportArea = new Rect(getResources().getDimensionPixelOffset(R.dimen.dp_1170), getResources().getDimensionPixelOffset(R.dimen.dp_820), getResources().getDimensionPixelOffset(R.dimen.dp_1710), getResources().getDimensionPixelOffset(R.dimen.dp_1000));
    }

    /**
     * 调整左右站立区域离中间的位置
     *
     * @param distance
     */
    public void ajustRecAreaDistance(int distance) {

        LayoutParams llparam = (LayoutParams) mBinding.layoutLeft.getLayoutParams();
        llparam.rightMargin = -distance;
        mBinding.layoutLeft.setLayoutParams(llparam);

        LayoutParams rlparam = (LayoutParams) mBinding.layoutRight.getLayoutParams();
        rlparam.leftMargin = -distance;
        mBinding.layoutRight.setLayoutParams(rlparam);

    }

    public void openMachine(DoubleIdentifyMachineListener listener) {
        openMachine();
        this.identityListener = listener;
        identityListener.onIdentityMachineInit(true);
    }


    public void openMachineByMatchModel(IdentifyMachineListener listener, AccountInfo accountInfo) {

    }


    public void identify(AdlPoseSkeleton aiResponse, byte[] data, int w, int h, int type) {

    }

    public void identify(AdlPoseSkeleton skeleton, byte[] data, int w, int h, boolean isLeftIdentity) {
        if (identityOk && !needAreaCheck) {
            return;
        }

        if (isLeftIdentity) {
            currentLeftSkeleton = skeleton;
        } else {
            currentRightSkeleton = skeleton;
        }

        //识别OK，检测双方是否在区域内
        if (identityOk) {
            if (isLeftIdentity) {
                footInLeft = judgeBodyIn(skeleton, scW, scH);
            } else {
                footInRight = judgeBodyIn(skeleton, scW, scH);
            }

            //两人同时在框里
            if (footInLeft && footInRight) {
                getHandler().postDelayed(() -> {
                    SpeechWrapper.INSTANCE.stop();
                    timeOutHelper.cancel();
                    if (null != identityListener) {
                        identityListener.onIdentityOk(leftAccountInfo, rightAccountInfo);
                    }
                }, 100);
            }
            return;
        }

        if (isLeft) {
            // 刷卡模式或者已经完成刷卡就不用进行下面的逻辑了
            if ((identityType & IDENTITY_TYPE_FACE) != IDENTITY_TYPE_FACE || isLeftOk) {
                return;
            }
        } else {
            //刷卡模式或者已经完成刷卡就不用进行下面的逻辑了
            if ((identityType & IDENTITY_TYPE_FACE) != IDENTITY_TYPE_FACE || isRightOk) {
                return;
            }
        }

        // 脚是否在区域内
        boolean footIn = judgeBodyIn(skeleton, scW, scH);
        changeAreaState(footIn);
        if (!footIn) {
            return;
        }
        // 判断有没有举手
        if (!judgeHandUp(skeleton, isLeftIdentity)) {
            return;
        }

        // 修改开始时间计算, add by wei.zhou
        if (isLeft && !isLeftOk) {
            if (identityStarLeftTime == 0) {
                identityStarLeftTime = System.currentTimeMillis();
            }
        } else {
            if (identityStartRightTime == 0) {
                identityStartRightTime = System.currentTimeMillis();
            }
        }

        if (isLeft && !isLeftOk) {
            isFistDelay = true; // 识别左边的的时候重置一下
            if (((System.currentTimeMillis() - identityStarLeftTime) > VISITOR_TIME)) {

                if (visitorAble) {
                    createVisitor();
                } else {

                    if (mBinding.progressView.getVisibility() == View.VISIBLE) {
                        SpeechWrapper.INSTANCE.speech2(getContext().getString(R.string.rec_face_fail_retry), () -> getHandler().postDelayed(() -> {

                            if (identityListener != null) {
                                identityListener.onIdentityTimeFail();
//                                identityListener = null;
                            }

                        }, 1000));
                    }

                    post(() -> {
                        mBinding.progressView.setVisibility(View.GONE);
                    });
                }

                return;
            }

            if (isFirst) {
                isFirst = false;
                isFistDelay = true;
                getHandler().post(() -> {
                    mBinding.tvStateNotice.setText(R.string.scan_notice);
                    int progress = (int) ((double) (System.currentTimeMillis() - identityStarLeftTime) / VISITOR_TIME * 100);
                    mBinding.progressView.setProgress(progress);
                });
            }

            getHandler().post(() -> {
                int progress = (int) ((double) (System.currentTimeMillis() - identityStarLeftTime) / VISITOR_TIME * 100);
                mBinding.progressView.setProgress(Math.min(progress, 99));
            });

        } else {
            if (((System.currentTimeMillis() - identityStartRightTime) > VISITOR_TIME)) {
                if (visitorAble) {
                    createVisitor();
                } else {
                    if (mBinding.progressViewRight.getVisibility() == View.VISIBLE) {
                        SpeechWrapper.INSTANCE.speech2(getContext().getString(R.string.rec_face_fail_retry), () -> getHandler().postDelayed(() -> {
                            if (identityListener != null) {
                                identityListener.onIdentityTimeFail();
//                                identityListener = null;
                            }
                        }, 1000));
                    }
                    post(() -> {
                        mBinding.progressViewRight.setVisibility(View.GONE);
                    });
                }

                return;
            }

            if (isFirst) {
                isFirst = false;
                getHandler().post(() -> {
                    mBinding.tvStateNoticeRight.setText(R.string.scan_notice);
                    int progress = (int) ((double) (System.currentTimeMillis() - identityStartRightTime) / VISITOR_TIME * 100);
                    mBinding.progressViewRight.setProgress(progress);
                });
            }

            getHandler().post(() -> {
                int progress = (int) ((double) (System.currentTimeMillis() - identityStartRightTime) / VISITOR_TIME * 100);
                mBinding.progressViewRight.setProgress(Math.min(progress, 99));
            });
        }

        if (isLeftIdentity) {
            isFistDelay = true;//识别左边的的时候重置一下
            if (data == null) return;
            faceDetect(System.currentTimeMillis() + "", data, w, h, skeleton, true);
        } else {
            if (isLeftOk) {
                if (data == null) return;

//                if (isFistDelay) {//只有第一次才需要延迟
                getHandler().postDelayed(() -> {
//                    LogUtils.e("延迟两秒识别右边");
//                    identityStartRightTime = 0;//重置时间
                    faceDetect(System.currentTimeMillis() + "", data, w, h, skeleton, false);
                    isFistDelay = false;
                }, 0);

            }
        }
    }

    public void openMachine() {
        identityStarLeftTime = identityStartRightTime = 0;
        //记录身份识别开始时间
        scW = (float) size.x / BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH;
        scH = (float) size.y / BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT;

        isFirst = true;
        //这里做初始化
        isLeft = true;
        isLeftOk = false;
        isRightOk = false;
        identityOk = false;
        cardOkLeft = false;
        cardOkRight = false;
        leftAccountInfo = new AccountInfo();
        rightAccountInfo = new AccountInfo();
        switch (identityType) {
            case IDENTITY_TYPE_FACE:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.hand_up));
                selectFace();
                uiFace();
                break;
            case IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card));
                selectCard();
                uiCard();
                break;
            case IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_qr));
                selectQr();
                uiQr();
                break;
            case IDENTITY_TYPE_FACE | IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                selectFace();
                selectCard();
                uiFaceCard();
                break;
            case IDENTITY_TYPE_FACE | IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_qr));
                selectFace();
                selectQr();
                uiFaceQr();
                break;

            case IDENTITY_TYPE_QR | IDENTITY_TYPE_CARD:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card_qr));
                selectQr();
                selectCard();
                uiCardQr();
                break;
            case IDENTITY_TYPE_FACE | IDENTITY_TYPE_CARD | IDENTITY_TYPE_QR:
                SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_card_qr));
                selectFace();
                selectCard();
                selectQr();
                uiAll();
                break;
        }

        this.setVisibility(VISIBLE);
        identityOk = false;
        timeOutHelper.start();
    }

    /**
     * 扫码
     */
    private void selectQr() {
    }

    private void selectCard() {
        if (nfcReaderManager == null) {
            nfcReaderManager = new NfcReaderManager();
        }
        int readerType = MMKVUtil.INSTANCE.decodeInt(MMKVBusiKey.NFC_READER_TYPE, BusiBaseConstantsValue.DEFAULT_NFC_READER_TYPE);
        NfcType type = NfcType.NFC_A;
        if (readerType == BusiBaseConstantsValue.NFC_READER_TYPE_B) {
            type = NfcType.NFC_B;
        }
        nfcReaderManager.init(getContext(), type);
        nfcReaderManager.startRead(this);
    }

    private void selectFace() {
        FaceDetectorHelper.Companion.getInstance().setFaceFeatureThreshold(BusiBaseConstantsValue.DEFAULT_SINGLE_FACE_THRESHOLD);
        FaceDetectorHelper.Companion.getInstance().setFaceCallback(new OnFaceCallback() {
            @Override
            public void onFindStudent(@Nullable StudentEntity studentEntity) {
                LogUtil.INSTANCE.d("onFindUser------->" + studentEntity.getAccountName(), false);
                if (!identityOk) {
                    identity(studentEntity);
                }
            }

            @Override
            public void onFindTeacher(@Nullable TeacherEntity teacherEntity) {

            }

            @Override
            public void onFindFail(@Nullable TyFaceInfo<?> tyFaceInfo) {

            }
        });
    }

    /**
     * 人脸识别的UI
     */
    private void uiFace() {
        setUi(true, R.string.hand_up, R.mipmap.hand_up_icon);
    }

    /**
     * 刷卡的ui
     */
    private void uiCard() {
        setUi(false, R.string.card_notice, R.mipmap.card);
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
        if (isLeft) {
            mBinding.layoutIdentityNotice.setVisibility(VISIBLE);
            mBinding.viewMarkLeft.setVisibility(GONE);
            mBinding.tvNotice.setVisibility(GONE);
            mBinding.ivState.setImageResource(imgRes);
            mBinding.tvStateNotice.setText(textRes);
            if (hasFace) {
                mBinding.progressView.setProgress(0);
                mBinding.progressView.setVisibility(VISIBLE);
                mBinding.ivAreaNoticeLeft.setVisibility(VISIBLE);
                mBinding.ivAreaNoticeLeft.setImageResource(R.mipmap.bg_foot_area);
            } else {
                mBinding.ivAreaNoticeLeft.setVisibility(GONE);
                mBinding.progressView.setVisibility(GONE);
            }

            if (!isRightOk) {
                mBinding.layoutIdentityNoticeRight.setVisibility(GONE);
                mBinding.tvNoticeRight.setVisibility(VISIBLE);
            }
            mBinding.viewMarkRight.setVisibility(VISIBLE);
            mBinding.ivAreaNoticeRight.setVisibility(GONE);
        } else {
            mBinding.layoutIdentityNoticeRight.setVisibility(VISIBLE);
            mBinding.viewMarkRight.setVisibility(GONE);
            mBinding.tvNoticeRight.setVisibility(GONE);
            mBinding.ivStateRight.setImageResource(imgRes);
            mBinding.tvStateNoticeRight.setText(textRes);

            if (hasFace) {
                mBinding.progressViewRight.setProgress(0);
                mBinding.progressViewRight.setVisibility(VISIBLE);
                mBinding.ivAreaNoticeRight.setVisibility(VISIBLE);
                mBinding.ivAreaNoticeRight.setImageResource(R.mipmap.bg_foot_area);
            } else {
                mBinding.ivAreaNoticeRight.setVisibility(GONE);
                mBinding.progressViewRight.setVisibility(GONE);
            }

            if (!isLeftOk) {
                mBinding.layoutIdentityNotice.setVisibility(GONE);
                mBinding.tvNotice.setVisibility(VISIBLE);
            }
            mBinding.viewMarkLeft.setVisibility(VISIBLE);
            mBinding.ivAreaNoticeLeft.setVisibility(GONE);
        }
    }

    private boolean isLeftBodyIn(AdlPoseSkeleton skeleton) {
        if (skeleton == null) return false;
        Rect sportRect = leftSportArea;
        //脚踝
        if (skeleton.getLAnkle() == null || !sportRect.contains((int) (skeleton.getLAnkle().getX() * scW), (int) (skeleton.getLAnkle().getY() * scH)))
            return false;
        if (skeleton.getRAnkle() == null || !sportRect.contains((int) (skeleton.getRAnkle().getX() * scW), (int) (skeleton.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    private boolean isRightBodyIn(AdlPoseSkeleton skeleton) {
        if (skeleton == null) return false;
        Rect sportRect = rightSportArea;
        //脚踝
        if (skeleton.getLAnkle() == null || !sportRect.contains((int) ((skeleton.getLAnkle().getX() + 1920 / 2) * scW), (int) (skeleton.getLAnkle().getY() * scH)))
            return false;
        if (skeleton.getRAnkle() == null || !sportRect.contains((int) ((skeleton.getRAnkle().getX() + 1920 / 2) * scW), (int) (skeleton.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    /**
     * 判断有没有举手
     *
     * @param body
     * @return
     */
    private boolean judgeHandUp(AdlPoseSkeleton body, boolean isLeftIdentity) {

        //因为骨骼点左右是对调的所以要判断右手需要获取左边的骨骼
        if (body == null || body.getLWrist() == null || body.getLShoulder() == null) {
            return false;
        }
        float wristY = body.getLWrist().y;
        float shoulderY = body.getLShoulder().y;
        return wristY < shoulderY;
    }

    /**
     * 判断当前人员是否在运动区域内
     *
     * @param body
     * @param scW
     * @param scH
     * @return
     */
    private boolean judgeBodyIn(AdlPoseSkeleton body, float scW, float scH) {
        Rect sportRect;
        if (isLeft) {
            sportRect = leftSportArea;
        } else {
            sportRect = rightSportArea;
        }
        //脚踝
        if (body.getLAnkle() == null || !sportRect.contains((int) (body.getLAnkle().getX() * scW), (int) (body.getLAnkle().getY() * scH)))
            return false;
        if (body.getRAnkle() == null || !sportRect.contains((int) (body.getRAnkle().getX() * scW), (int) (body.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    private void faceDetect(String imageId, byte[] yuv, int w, int h, AdlPoseSkeleton body, boolean isLeftIndentify) {
        RectF rect = new RectF(0f, 0f, 50f, 50f);
        if (body != null && body.LShoulder != null && body.RShoulder != null) {
            float shoulderWidth = Math.abs(body.LShoulder.x - body.RShoulder.x);
            rect.bottom = Math.max(body.LShoulder.y, body.RShoulder.y);
            rect.left = Math.min(body.LShoulder.x, body.RShoulder.x);
            rect.right = rect.left + shoulderWidth;
            rect.top = rect.bottom - 1.2f * shoulderWidth;
        }
        if (rect.width() < 40 || rect.height() < 40) {
            rect.left = rect.left - 20;
            rect.right = rect.left + 40;
            rect.top = rect.top - 20;
            rect.bottom = rect.top + 40;
        }
        FaceDetectorHelper.Companion.getInstance().detectFace(imageId, yuv, w, h, rect, 0);
    }

    private AdlPoseKeyPoint resetPoint(AdlPoseKeyPoint p, float sx, float sy) {
        return new AdlPoseKeyPoint(p.x / sx, p.y / sy, p.score);
    }

    private AdlPoseKeyPoint resetRightPoint(AdlPoseKeyPoint p, float sx, float sy) {
        return new AdlPoseKeyPoint((p.x + (float) (1920 / 2)) / sx, p.y / sy, p.score);
    }

    private void changeAreaState(boolean isIn) {
        if (isLeft && isIn != areaIn) {
            areaIn = isIn;
            if (isIn) {
                getHandler().post(() -> {
                    mBinding.ivAreaNoticeLeft.setImageResource(R.mipmap.rect_area);
                });
            } else {
                getHandler().post(() -> {
                    mBinding.ivAreaNoticeLeft.setImageResource(R.mipmap.bg_foot_area);
                });
            }
        } else if (isIn != areaIn) {
            areaIn = isIn;
            if (isIn) {
                getHandler().post(() -> {
                    mBinding.ivAreaNoticeRight.setImageResource(R.mipmap.rect_area);
                });

            } else {
                getHandler().post(() -> {
                    mBinding.ivAreaNoticeRight.setImageResource(R.mipmap.bg_foot_area);
                });
            }
        }
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
        SpeechWrapper.INSTANCE.speech(mBinding.getRoot().getContext().getString(R.string.rec_face_fail));
        ToastUtil.getInstance(mBinding.getRoot().getContext(), mBinding.getRoot().getContext().getString(R.string.rec_face_fail));
    }

    public void identity(StudentEntity user) {

        AccountInfo identifyResult = null;
        if (!isLeftOk && isLeft) {
            getHandler().post(() -> {
                mBinding.progressView.setProgress(100);
            });
            isLeftOk = true;
            identifyResult = leftAccountInfo;
            isFistDelay = true;
        } else if (!isLeft && !isRightOk) {
            getHandler().post(() -> {
                mBinding.progressViewRight.setProgress(100);
            });
            isRightOk = true;
            identifyResult = rightAccountInfo;
        } else {
            return;
        }
        identifyResult.setStudentEntity(user);
        identityFinish();
    }

    public void createVisitor() {
        post(() -> {
            LogUtil.INSTANCE.d("识别为游客", false);
            AccountInfo accountInfo;
            if (!isLeftOk && isLeft) {
                getHandler().post(() -> {
                    mBinding.progressView.setProgress(100);
                });
                isLeftOk = true;
                accountInfo = leftAccountInfo;
            } else if (!isLeft && !isRightOk) {
                getHandler().post(() -> {
                    mBinding.progressViewRight.setProgress(100);
                });
                isRightOk = true;
                accountInfo = rightAccountInfo;
            } else {
                return;
            }

            if (accountInfo == null) return;
            StudentEntity studentEntity = new StudentEntity();
            studentEntity.setAccountId(BusiBaseConstantsValue.VISITOR_USER_ID);
            studentEntity.setAccountName(getString(R.string.visitor));
            studentEntity.setUserFaceImgUrl("");
            accountInfo.setStudentEntity(studentEntity);
            identityFinish();

        });

    }


    public Handler getHandler() {
        return AdlHandlerThread.getUIHandler();
    }

    public void identityFinish() {
        identityOkUi();
        if (!isLeftOk || !isRightOk) {
            isFirst = true;
            isLeft = !isLeft;
            //原逻辑是判断了类型，再延时2秒执行，每个分支都调用postDelayed函数，更改为2秒延时判断再执行，只写一个postDelayed
            getHandler().postDelayed(() -> {
                switch (identityType) {
                    case IDENTITY_TYPE_FACE:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.hand_up));
                        uiFace();
                        break;
                    case IDENTITY_TYPE_CARD:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card));
                        uiCard();
                        break;
                    case IDENTITY_TYPE_QR:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_qr));
                        uiQr();
                        break;
                    case IDENTITY_TYPE_FACE | IDENTITY_TYPE_CARD:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                        uiFaceCard();
                        break;
                    case IDENTITY_TYPE_FACE | IDENTITY_TYPE_QR:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_face_qr));
                        uiFaceQr();
                        break;

                    case IDENTITY_TYPE_QR | IDENTITY_TYPE_CARD:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.please_card_qr));
                        uiCardQr();
                        break;
                    case IDENTITY_TYPE_FACE | IDENTITY_TYPE_CARD | IDENTITY_TYPE_QR:
                        SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.card_face_notice));
                        uiAll();
                        break;
                }
            }, 2000);

        } else if (isLeftOk && isRightOk && !identityOk && identityListener != null) {
            identityOk = true;
            if (needAreaCheck && (!isLeftBodyIn(currentLeftSkeleton) || !isRightBodyIn(currentRightSkeleton))) {
                getHandler().removeCallbacksAndMessages(null);
                getHandler().post(() -> {
                    mBinding.layoutIdentityNoticeRight.setVisibility(VISIBLE);
                    mBinding.viewMarkRight.setVisibility(GONE);
                    mBinding.tvNoticeRight.setVisibility(GONE);
                    mBinding.viewMarkLeft.setVisibility(GONE);
                    mBinding.ivStateRight.setVisibility(GONE);
                    mBinding.ivState.setVisibility(GONE);
                    mBinding.progressViewRight.setVisibility(GONE);
                    mBinding.ivAreaNoticeLeft.setVisibility(VISIBLE);
                    mBinding.ivAreaNoticeRight.setVisibility(VISIBLE);
                    mBinding.tvStateNotice.setText(getContext().getString(R.string.move_to_rect));
                    mBinding.tvStateNoticeRight.setText(getContext().getString(R.string.move_to_rect));
                    SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.move_to_rect));
                });
            } else {
                timeOutHelper.cancel();
                getHandler().postDelayed(() -> {
                    if (null != identityListener) {
                        identityListener.onIdentityOk(leftAccountInfo, rightAccountInfo);
                    }
                }, 2000);
            }
        }
    }

    private void identityOkUi() {

        if (isLeft) {
            getHandler().post(() -> {
                mBinding.tvStateNotice.setText(leftAccountInfo.getStudentEntity().getAccountName());
                mBinding.progressView.setVisibility(GONE);
                mBinding.ivState.setImageResource(R.mipmap.identity_ok);
            });
            SpeechWrapper.INSTANCE.stop();
            SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.identified_someone, leftAccountInfo.getStudentEntity().getAccountName()));
        } else {
            getHandler().post(() -> {
                mBinding.tvStateNoticeRight.setText(rightAccountInfo.getStudentEntity().getAccountName());
                mBinding.progressViewRight.setVisibility(GONE);
                mBinding.ivStateRight.setImageResource(R.mipmap.identity_ok);
            });
            SpeechWrapper.INSTANCE.stop();
            SpeechWrapper.INSTANCE.speech(getContext().getString(R.string.identified_someone, rightAccountInfo.getStudentEntity().getAccountName()));
        }
    }

    public void closeMachine() {
        //TODO 关闭
        getHandler().post(() -> {
            DoubleFaceMachine.this.setVisibility(GONE);
        });
        isLeft = true;
        isLeftOk = false;
        isRightOk = false;
        identityOk = false;
        cardOkLeft = false;
        cardOkRight = false;
        identityStarLeftTime = identityStartRightTime = 0;
        leftAccountInfo = null;
        rightAccountInfo = null;
        identityListener = null;
        FaceDetectorHelper.Companion.getInstance().reset();
        mQrDisposable.dispose();
        if (nfcReaderManager != null) {
            nfcReaderManager.stopRead();
            nfcReaderManager.release();
        }
        mQrDisposable.clear();
        mHandler.removeCallbacksAndMessages(null);
        timeOutHelper.cancel();
    }


    @Override
    public void readCardBack(String cardStr) {
        LogUtil.INSTANCE.d("usbReader-->读卡结果：" + cardStr, false);
        if (!TextUtils.isEmpty(cardStr) && !identityOk) {
            getAccountInfoWithCard(cardStr);
        }
    }
}
