package com.ty.tybusbase.view.whiteboard;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ty.tybusbase.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :WEI_CP
 * Time:2023/1/11 14:38
 * Description: this is PenSettingPopwindow
 */
public class PenSettingPopWindow extends PopupWindow {

    private Context mContext;

   // @BindView(R.id.rv_pen_color)
    RecyclerView mRvPenColor;

   // @BindView(R.id.btn_pen_small)
    View mBtnPenSmall;

   // @BindView(R.id.btn_pen_middle)
    View mBtnPenMiddle;

   // @BindView(R.id.btn_pen_large)
    View mBtnPenLarge;

    private OnPenSettingCallback mSettingCallback;

    public static final int SMALL = 2;
    public static final int MIDDLE = 4;
    public static final int LARGE = 8;
    private PenColorAdapter penColorAdapter;

    @IntDef({SMALL,MIDDLE,LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PenWidth{}

    public static final String RED = "#E5291B";
    public static final String GREEN = "#11B91D";
    public static final String YELLOW = "#ffe816";
    public static final String CYAN = "#18D0C5";
    public static final String BLUE = "#2977FF";
    public static final String PINK = "#B623FF";
    public static final String BLACK = "#000000";
    public static final String WHITE = "#FFFFFF";

    private List<String> penColors = new ArrayList();

    @StringDef({RED,GREEN,YELLOW,CYAN,BLUE,PINK,BLACK,WHITE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PenColor{}

    public PenSettingPopWindow(Context context) {
        this(context,null);
    }

    public PenSettingPopWindow(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PenSettingPopWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_pen, null);
        setContentView(view);
        mRvPenColor = view.findViewById(R.id.rv_pen_color);
        mBtnPenSmall = view.findViewById(R.id.btn_pen_small);
        mBtnPenMiddle = view.findViewById(R.id.btn_pen_middle);
        mBtnPenLarge = view.findViewById(R.id.btn_pen_large);
        setWidth(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_248));
        setHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_179));
        //聚焦
        setFocusable(true);
        //设置背景
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);
        this.setOutsideTouchable(true);

        GridLayoutManager manager = new GridLayoutManager(mContext,4,RecyclerView.VERTICAL,false);
        mRvPenColor.setLayoutManager(manager);
        mRvPenColor.setNestedScrollingEnabled(false);
        penColors.add(RED);
        penColors.add(GREEN);
        penColors.add(YELLOW);
        penColors.add(CYAN);
        penColors.add(BLUE);
        penColors.add(PINK);
        penColors.add(BLACK);
        penColors.add(WHITE);
        penColorAdapter = new PenColorAdapter(penColors);
        mRvPenColor.setAdapter(penColorAdapter);
        penColorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String s = penColors.get(position);
                if(mSettingCallback!=null)mSettingCallback.setPenColor(s);
                penColorAdapter.setSelectColor(s);
            }
        });

        mBtnPenSmall.setOnClickListener((v)->clickSmallPen());
        mBtnPenMiddle.setOnClickListener((v1)->clickMiddlePen());
        mBtnPenLarge.setOnClickListener((v2)->clickLargePen());

    }

    public void setSettingCallback(OnPenSettingCallback mSettingCallback) {
        this.mSettingCallback = mSettingCallback;
    }

    public void setDefaultWidth(@PenWidth int width){
        switch (width){
            case SMALL:
                selectSmallPen();
                break;
            case MIDDLE:
                selectMiddlePen();
                break;
            case LARGE:
                selectLargePen();
                break;
        }
    }
    public void setDefaultColor(@PenColor String penColor){
        if(TextUtils.isEmpty(penColor))return;
        if(penColors.contains(penColor)){
            penColorAdapter.setSelectColor(penColor);
        }
    }

    private void clickSmallPen(){
        if(mBtnPenSmall.isSelected())return;
        selectSmallPen();
        if(null!=mSettingCallback)mSettingCallback.setPenWidth(SMALL);
    }
    public void selectSmallPen(){
        mBtnPenSmall.setSelected(true);
        mBtnPenMiddle.setSelected(false);
        mBtnPenLarge.setSelected(false);
    }

    public void clickMiddlePen(){
        if(mBtnPenMiddle.isSelected())return;
        selectMiddlePen();
        if(null!=mSettingCallback)mSettingCallback.setPenWidth(MIDDLE);
    }
    private void selectMiddlePen(){
        mBtnPenSmall.setSelected(false);
        mBtnPenMiddle.setSelected(true);
        mBtnPenLarge.setSelected(false);
    }

    public void clickLargePen(){
        if(mBtnPenLarge.isSelected())return;
        selectLargePen();
        if(null!=mSettingCallback)mSettingCallback.setPenWidth(LARGE);
    }
    private void selectLargePen(){
        mBtnPenSmall.setSelected(false);
        mBtnPenMiddle.setSelected(false);
        mBtnPenLarge.setSelected(true);
    }


    public interface OnPenSettingCallback{

        void setPenWidth(@PenWidth int width);

        void setPenColor(@PenColor String color);
    }

}
