package com.ty.bus.multi.core.view.doublesport;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.adl.lib.pose.AdlPoseSkeleton;
import com.adl.service.outer.AdlService;
import com.bumptech.glide.Glide;
import com.ty.bus.multi.R;
import com.ty.bus.multi.databinding.ViewDoubleSportSportBinding;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.constants.SportConstant;
import com.ty.tybusbase.entity.AccountInfo;
import com.ty.tybusbase.utils.ScreenAdapterUtils;

import java.text.SimpleDateFormat;

/**
 * @author :WEI_CP
 * Time:2022/3/2 16:55
 * Description: this is
 */
public class DoubleSportSportView extends ConstraintLayout {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
    private ViewDoubleSportSportBinding vb;
    private Rect leftRect;
    private Rect rightRect;
    private int scW;
    private int scH;
    private Point size;

    public DoubleSportSportView(Context context) {
        super(context);
        init(context);
    }

    public DoubleSportSportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DoubleSportSportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DoubleSportSportView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        vb = ViewDoubleSportSportBinding.inflate(LayoutInflater.from(context), this);
        size = ScreenAdapterUtils.getSize(context);
        scW = size.x / BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH;
        scH = size.y / BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT;
    }

    public void setSportName(String name) {
        vb.sportName.setText(name);
    }

    public void refreshTime(long millis) {
        vb.time.setText(timeFormat.format(millis));
    }

    public void warnTime() {
        vb.time.setTextColor(Color.RED);
    }

    public void setLeftValue(int value) {
        vb.tvScoreLeft.setText(value + "");
    }

    public void setRightValue(int value) {
        vb.tvScoreRight.setText(value + "");
    }

    public void setLeftPlayer(AccountInfo info) {
        Glide.with(this.getContext()).load(AdlService.getService().getFacePrefix() + info.getStudentEntity().getUserFaceImgUrl())
                .error(R.mipmap.ic_head_default)
                .into(vb.ivHeadLeft);
        vb.tvNameLeft.setText(info.getStudentEntity().getAccountName());
        if (info.getStudentEntity().getCardType().equals(BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE)) {
            vb.ivHeaderBgLeft.setVisibility(View.VISIBLE);
        }
    }

    public void setRightPlayer(AccountInfo info) {
        Glide.with(this.getContext()).load(AdlService.getService().getFacePrefix() + info.getStudentEntity().getUserFaceImgUrl())
                .error(R.mipmap.ic_head_default)
                .into(vb.ivHeadRight);
        vb.tvNameRight.setText(info.getStudentEntity().getAccountName());
        if (info.getStudentEntity().getCardType().equals(BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE)) {
            vb.ivHeaderBgRight.setVisibility(View.VISIBLE);
        }

    }

    public void setAreaVisibility(Boolean show) {
        vb.groupArea.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public boolean checkLeftArea(AdlPoseSkeleton skeleton, @SportConstant.SportAiCode String sportAi) {

        boolean b = skeleton != null && checkArea(skeleton, sportAi, true);
        this.post(() -> {
            if (b) {
                vb.ivAreaNoticeLeft.setImageResource(R.mipmap.rect_area);
                vb.tvAreaNoticeLeft.setVisibility(GONE);
            } else {
                vb.ivAreaNoticeLeft.setImageResource(R.mipmap.bg_foot_area);
                vb.tvAreaNoticeLeft.setVisibility(VISIBLE);
            }
        });
        return b;
    }

    public boolean checkRightArea(AdlPoseSkeleton skeleton, @SportConstant.SportAiCode String sportAi) {
        boolean b = skeleton != null && checkArea(skeleton, sportAi, false);
        this.post(() -> {
            if (b) {
                vb.ivAreaNoticeRight.setImageResource(R.mipmap.rect_area);
                vb.tvAreaNoticeRight.setVisibility(GONE);
            } else {
                vb.ivAreaNoticeRight.setImageResource(R.mipmap.bg_foot_area);
                vb.tvAreaNoticeRight.setVisibility(VISIBLE);
            }
        });
        return b;
    }

    private boolean checkArea(AdlPoseSkeleton skeleton, @SportConstant.SportAiCode String sportAiCode, boolean isLeft) {
        Rect areaRect = null;
        if (isLeft) {
            if (leftRect == null) {
                leftRect = new Rect(vb.ivAreaNoticeLeft.getLeft(), vb.ivAreaNoticeLeft.getTop(),
                        vb.ivAreaNoticeLeft.getRight(), vb.ivAreaNoticeLeft.getBottom());
            }
            areaRect = leftRect;
        } else {
            if (rightRect == null) {
                rightRect = new Rect(vb.ivAreaNoticeRight.getLeft(), vb.ivAreaNoticeRight.getTop(),
                        vb.ivAreaNoticeRight.getRight(), vb.ivAreaNoticeRight.getBottom());
            }
            areaRect = rightRect;
        }
        switch (sportAiCode) {
            case SportConstant.SPORT_ONE_LEG_SKIP_ROPE:
            case SportConstant.SPORT_FILL_BLANK_GAME:
            case SportConstant.SPORT_FILL_BLANK_EN_GAME:
            case SportConstant.SPORT_TROT:
            case SportConstant.SPORT_HIGH_KNEE:
                return judgeOneLegIn(skeleton, areaRect);
            default:
                return judgeBodyIn(skeleton, areaRect);
        }
    }

    private boolean judgeBodyIn(AdlPoseSkeleton body, Rect areaRect) {
        //脚踝
        if (body.getLAnkle() == null || !areaRect.contains((int) (body.getLAnkle().getX() * scW), (int) (body.getLAnkle().getY() * scH)))
            return false;
        if (body.getRAnkle() == null || !areaRect.contains((int) (body.getRAnkle().getX() * scW), (int) (body.getRAnkle().getY() * scH)))
            return false;
        return true;
    }

    private boolean judgeOneLegIn(AdlPoseSkeleton body, Rect areaRect) {
        //脚踝
        if (body.getLAnkle() != null && areaRect.contains((int) (body.getLAnkle().getX() * scW), (int) (body.getLAnkle().getY() * scH)))
            return true;
        if (body.getRAnkle() != null && areaRect.contains((int) (body.getRAnkle().getX() * scW), (int) (body.getRAnkle().getY() * scH)))
            return true;
        return false;
    }


}
