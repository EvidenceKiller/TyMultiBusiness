package com.ty.tybusbase.view.whiteboard;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import androidx.annotation.IntDef;

import com.ty.tybusbase.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author :WEI_CP
 * Time:2023/1/11 14:38
 * Description: this is PenSettingPopwindow
 */
public class EraserPopWindow extends PopupWindow {

    private Context mContext;

    View mBtnEraserSmall;

    View mBtnEraserMiddle;

    View mBtnEraserLarge;

    private OnEraserSettingCallback mSettingCallback;

    public static final int SMALL = 50;
    public static final int MIDDLE = 100;
    public static final int LARGE = 200;

    @IntDef({SMALL,MIDDLE,LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EraserWidth{}

    public EraserPopWindow(Context context) {
        this(context,null);
    }

    public EraserPopWindow(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EraserPopWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_eraser, null);
        setContentView(view);
        mBtnEraserSmall = view.findViewById(R.id.btn_eraser_small);
        mBtnEraserMiddle = view.findViewById(R.id.btn_eraser_middle);
        mBtnEraserLarge = view.findViewById(R.id.btn_eraser_large);
        setWidth(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_243));
        setHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.dp_103));
        //聚焦
        setFocusable(true);
        //设置背景
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);
        this.setOutsideTouchable(true);

        mBtnEraserSmall.setOnClickListener((v)->clickSmall());
        mBtnEraserMiddle.setOnClickListener((v)->clickMiddle());
        mBtnEraserLarge.setOnClickListener((v)->clickLargePen());
    }

    public void setSettingCallback(OnEraserSettingCallback mSettingCallback) {
        this.mSettingCallback = mSettingCallback;

    }

    public void setDefaultWidth(@EraserWidth int width){
        switch (width){
            case SMALL:
                selectSmall();
                break;
            case MIDDLE:
                selectMiddle();
                break;
            case LARGE:
                selectLarge();
                break;
        }
    }

    public void clickSmall(){
        if(mBtnEraserSmall.isSelected())return;
        selectSmall();
        if(null!=mSettingCallback)mSettingCallback.setEraserWidth(SMALL);
    }
    public void selectSmall(){
        mBtnEraserSmall.setSelected(true);
        mBtnEraserMiddle.setSelected(false);
        mBtnEraserLarge.setSelected(false);
    }

    public void clickMiddle(){
        if(mBtnEraserMiddle.isSelected())return;
        selectMiddle();
        if(null!=mSettingCallback)mSettingCallback.setEraserWidth(MIDDLE);
    }
    private void selectMiddle(){
        mBtnEraserSmall.setSelected(false);
        mBtnEraserMiddle.setSelected(true);
        mBtnEraserLarge.setSelected(false);
    }

    public void clickLargePen(){
        if(mBtnEraserLarge.isSelected())return;
        selectLarge();
        if(null!=mSettingCallback)mSettingCallback.setEraserWidth(LARGE);
    }
    private void selectLarge(){
        mBtnEraserSmall.setSelected(false);
        mBtnEraserMiddle.setSelected(false);
        mBtnEraserLarge.setSelected(true);
    }


    public interface OnEraserSettingCallback{

        void setEraserWidth(@EraserWidth int width);

    }

}
