package com.ty.bus.multi.core.view.doublesport;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.ty.bus.multi.R;
import com.ty.bus.multi.databinding.ViewDoubleSportResultBinding;
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

    public void setResult(SportResult rightResult, SportResult leftResult) {
        if (leftResult == null || rightResult == null) return;
        int leftAchievement = leftResult.getSportAchievement();
        int rightAchievement = rightResult.getSportAchievement();
        if (leftAchievement > rightAchievement) {
            leftResult.setRank(1);
            rightResult.setRank(2);
            leftWin(rightResult, leftResult);
        } else if (leftAchievement < rightAchievement) {
            leftResult.setRank(2);
            rightResult.setRank(1);
            rightWin(rightResult, leftResult);
        } else {
            leftResult.setRank(2);
            rightResult.setRank(2);
            draw(rightResult, leftResult);
        }
    }

    private void leftWin(SportResult rightResult, SportResult leftResult) {
        try {
            View view = vb.vsLeftWin.inflate();
            TextView tvNameLw = view.findViewById(R.id.tv_name_lw);
            TextView tvScoreLw = view.findViewById(R.id.tv_score_lw);
            TextView tvScoreRl = view.findViewById(R.id.tv_score_rl);
            TextView tvNameRl = view.findViewById(R.id.tv_name_rl);
            ImageView ivHeadLw = view.findViewById(R.id.iv_head_lw);
            ImageView ivHeadRl = view.findViewById(R.id.iv_head_rl);

            tvNameLw.setText(leftResult.userName != null ? leftResult.userName : "");
            tvNameRl.setText(rightResult.userName == null ? "" : rightResult.userName);
            tvScoreLw.setText(String.valueOf(leftResult.getSportAchievement()));
            tvScoreRl.setText(String.valueOf(rightResult.getSportAchievement()));
            Glide.with(getContext()).load(leftResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadLw);
            Glide.with(getContext()).load(rightResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadRl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rightWin(SportResult rightResult, SportResult leftResult) {
        try {
            View view = vb.vsRightWin.inflate();
            TextView tvNameRw = view.findViewById(R.id.tv_name_rw);
            TextView tvScoreRw = view.findViewById(R.id.tv_score_rw);
            TextView tvScoreLl = view.findViewById(R.id.tv_score_ll);
            TextView tvNameLl = view.findViewById(R.id.tv_name_ll);
            ImageView ivHeadRw = view.findViewById(R.id.iv_head_rw);
            ImageView ivHeadLl = view.findViewById(R.id.iv_head_ll);
            tvNameRw.setText(rightResult.userName != null ? rightResult.userName : "");
            tvNameLl.setText(leftResult.userName == null ? "" : leftResult.userName);
            tvScoreRw.setText(String.valueOf(rightResult.getSportAchievement()));
            tvScoreLl.setText(String.valueOf(leftResult.getSportAchievement()));
            Glide.with(getContext()).load(rightResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadRw);
            Glide.with(getContext()).load(leftResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadLl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void draw(SportResult rightResult, SportResult leftResult) {
        try {
            View view = vb.vsDrawWin.inflate();
            TextView tvNameR = view.findViewById(R.id.tv_name_r);
            TextView tvScoreR = view.findViewById(R.id.tv_score_r);
            TextView tvScoreL = view.findViewById(R.id.tv_score_l);
            TextView tvNameL = view.findViewById(R.id.tv_name_l);
            ImageView ivHeadR = view.findViewById(R.id.iv_head_r);
            ImageView ivHeadL = view.findViewById(R.id.iv_head_l);
            tvNameR.setText(rightResult.userName != null ? rightResult.userName : "");
            tvNameL.setText(leftResult.userName == null ? "" : leftResult.userName);
            tvScoreR.setText(String.valueOf(rightResult.getSportAchievement()));
            tvScoreL.setText(String.valueOf(leftResult.getSportAchievement()));
            Glide.with(getContext()).load(rightResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadR);
            Glide.with(getContext()).load(leftResult.getHeadUrl()).error(R.mipmap.ic_head_default).into(ivHeadL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
