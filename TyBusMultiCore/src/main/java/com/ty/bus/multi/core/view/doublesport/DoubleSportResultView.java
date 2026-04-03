package com.ty.bus.multi.core.view.doublesport;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.adl.service.outer.AdlService;
import com.bumptech.glide.Glide;
import com.ty.bus.multi.R;
import com.ty.bus.multi.databinding.ViewDoubleSportResultBinding;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.databinding.BusiBaseSlidingTabLayoutBinding;
import com.ty.tybusbase.entity.SportResult;


/**
 * @author pc
 */
public class DoubleSportResultView extends ConstraintLayout {

    private ViewDoubleSportResultBinding vb;

    public DoubleSportResultView(Context context) {
        this(context, null);
    }

    public DoubleSportResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleSportResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        vb = ViewDoubleSportResultBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setResultBtnListener(OnClickListener listener) {
        vb.ivResultExit.setOnClickListener(listener);
    }

    public void setResult(SportResult rightResult, SportResult leftResult, int energyPoint) {
        if (leftResult == null || rightResult == null) return;
        int leftAchievement = leftResult.getSportAchievement();
        int rightAchievement = rightResult.getSportAchievement();
        if (leftAchievement > rightAchievement) {
            leftResult.setRank(1);
            rightResult.setRank(2);
            loadResult(rightResult, leftResult, 1, energyPoint);
        } else if (leftAchievement < rightAchievement) {
            leftResult.setRank(2);
            rightResult.setRank(1);
            loadResult(rightResult, leftResult, 2, energyPoint);
        } else {
            leftResult.setRank(2);
            rightResult.setRank(2);
            loadResult(rightResult, leftResult, 3, energyPoint);
        }
    }

    /**
     * 加载结果数据
     *
     * @param rightResult
     * @param leftResult
     * @param type：1左赢，2：右赢，3：平局
     */
    private void loadResult(SportResult rightResult, SportResult leftResult, int type, int energyPoint) {
        View view = null;
        switch (type) {
            case 1:
                view = vb.vsLeftWin.inflate();
                break;
            case 2:
                view = vb.vsRightWin.inflate();
                break;
            case 3:
                view = vb.vsDrawWin.inflate();
        }
        TextView tvNameLw = view.findViewById(R.id.tv_name_l);
        TextView tvScoreLw = view.findViewById(R.id.tv_score_l);
        TextView tvScoreRl = view.findViewById(R.id.tv_score_r);
        TextView tvNameRl = view.findViewById(R.id.tv_name_r);
        ImageView ivHeadLw = view.findViewById(R.id.iv_head_l);
        ImageView ivHeadRl = view.findViewById(R.id.iv_head_r);
        ImageView ivHeaderBgL = view.findViewById(R.id.iv_header_bg_l);
        ImageView ivHeaderBgR = view.findViewById(R.id.iv_header_bg_r);
        ivHeaderBgL.setVisibility(leftResult.getCardType().equals(BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) ? View.VISIBLE : View.GONE);
        ivHeaderBgR.setVisibility(rightResult.getCardType().equals(BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) ? View.VISIBLE : View.GONE);
        if (energyPoint > 0) {
            if (!leftResult.userId.equals(BusiBaseConstantsValue.VISITOR_USER_ID)) {
                View viewById = view.findViewById(R.id.layout_energy_l);
                viewById.setVisibility(View.VISIBLE);
                TextView energyTvL = viewById.findViewById(com.ty.tybusbase.R.id.tv_energy_number);
                energyTvL.setText("+" + energyPoint);
            }
            if (!rightResult.userId.equals(BusiBaseConstantsValue.VISITOR_USER_ID)) {
                View viewById1 = view.findViewById(R.id.layout_energy_r);
                viewById1.setVisibility(View.VISIBLE);
                TextView energyTvR = viewById1.findViewById(com.ty.tybusbase.R.id.tv_energy_number);
                energyTvR.setText("+" + energyPoint);
            }
        }
        tvNameLw.setText(leftResult.userName != null ? leftResult.userName : "");
        tvNameRl.setText(rightResult.userName == null ? "" : rightResult.userName);
        tvScoreLw.setText(String.valueOf(leftResult.getSportAchievement()));
        tvScoreRl.setText(String.valueOf(rightResult.getSportAchievement()));
        Glide.with(getContext()).load(AdlService.getService().getFacePrefix() + leftResult.getHeadUrl()).error(com.ty.tybusbase.R.mipmap.ic_head_default).into(ivHeadLw);
        Glide.with(getContext()).load(AdlService.getService().getFacePrefix() + rightResult.getHeadUrl()).error(com.ty.tybusbase.R.mipmap.ic_head_default).into(ivHeadRl);
    }


}
