package com.ty.tybusbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ty.tybusbase.R;
import com.ty.tybusbase.databinding.DirectionPadViewBinding;

/**
 * 上下左右方向控制按钮 View
 * <p>
 * 结构：标题(可选) + 中间设备图 + 上/下/左/右四个方向按钮，按钮点击通过 {@link OnDirectionClickListener} 回调
 * <p>
 * 自定义属性：
 * <ul>
 * <li>app:dp_title 标题文字（默认“落点标定”）</li>
 * <li>app:dp_show_title 是否显示标题（默认 true）</li>
 * <li>app:dp_center_image 中间设备图（默认 img_machine_ping）</li>
 * </ul>
 * <p>
 * 用法：
 * <pre>
 *     DirectionPadView directionPadView = findViewById(R.id.direction_pad);
 *     directionPadView.setOnDirectionClickListener(new DirectionPadView.OnDirectionClickListener() {
 *         &#64;Override public void onMoveUp()   { ... }
 *         &#64;Override public void onMoveDown() { ... }
 *         &#64;Override public void onMoveLeft() { ... }
 *         &#64;Override public void onMoveRight(){ ... }
 *     });
 * </pre>
 */
public class DirectionPadView extends ConstraintLayout {

    /** 方向按钮点击回调 */
    public interface OnDirectionClickListener {
        void onMoveUp();

        void onMoveDown();

        void onMoveLeft();

        void onMoveRight();
    }

    private final DirectionPadViewBinding mBinding;
    private OnDirectionClickListener mDirectionClickListener;
    private int mButtonSize;
    private int mCenterSize;
    private int mTitleSize;
    private int mGap;

    public DirectionPadView(Context context) {
        this(context, null);
    }

    public DirectionPadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DirectionPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBinding = DirectionPadViewBinding.inflate(LayoutInflater.from(context), this, true);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        String title = null;
        boolean showTitle = true;
        int centerImageRes = 0;
        int buttonSize = 0;
        int centerSize = 0;
        int titleSize = 0;
        int buttonMarginTop = 0;
        int buttonGap = 0;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DirectionPadView);
            title = ta.getString(R.styleable.DirectionPadView_dp_title);
            showTitle = ta.getBoolean(R.styleable.DirectionPadView_dp_show_title, true);
            centerImageRes = ta.getResourceId(R.styleable.DirectionPadView_dp_center_image, 0);
            buttonSize = ta.getDimensionPixelSize(R.styleable.DirectionPadView_dp_button_size, 0);
            titleSize = ta.getDimensionPixelSize(R.styleable.DirectionPadView_dp_title_size, 0);
            buttonMarginTop = ta.getDimensionPixelSize(R.styleable.DirectionPadView_dp_button_margin_top, 0);
            buttonGap = ta.getDimensionPixelSize(R.styleable.DirectionPadView_dp_button_gap, 0);
            ta.recycle();
        }
        if (title != null && !title.isEmpty()) {
            mBinding.tvTitle.setText(title);
        }
        mBinding.tvTitle.setVisibility(showTitle ? VISIBLE : GONE);
        mButtonSize = buttonSize;
        mCenterSize = centerSize;
        mTitleSize = titleSize != 0 ? titleSize : getResources().getDimensionPixelSize(R.dimen.dp_24);
        mGap = buttonGap != 0 ? buttonGap
                : (buttonMarginTop != 0 ? buttonMarginTop : dp2px(20));
        applySize();

        mBinding.bntMoveU.setOnClickListener(v -> {
            if (mDirectionClickListener != null) {
                mDirectionClickListener.onMoveUp();
            }
        });
        mBinding.bntMoveD.setOnClickListener(v -> {
            if (mDirectionClickListener != null) {
                mDirectionClickListener.onMoveDown();
            }
        });
        mBinding.bntMoveL.setOnClickListener(v -> {
            if (mDirectionClickListener != null) {
                mDirectionClickListener.onMoveLeft();
            }
        });
        mBinding.bntMoveR.setOnClickListener(v -> {
            if (mDirectionClickListener != null) {
                mDirectionClickListener.onMoveRight();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            applySize();
        }
    }

    /**
     * 应用尺寸。未显式指定按钮/中间图大小时，根据整体布局大小自适应计算：
     * 水平方向：左按钮 + 间距 + 中间图 + 间距 + 右按钮 = 总宽，其中中间图 = 2 × 按钮；
     * 垂直方向：标题 + 间距 + 上按钮 + 间距 + 中间图 + 间距 + 下按钮 = 总高，
     * 取宽/高两个方向计算结果的较小值，保证在任意尺寸下完整显示。
     */
    private void applySize() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0) {
            width = getResources().getDimensionPixelSize(R.dimen.dp_408);
        }
        if (height <= 0) {
            height = width;
        }
        int gap = mGap;
        int buttonByWidth = (width - 2 * gap) / 4;
        int titleHeight = mBinding.tvTitle.getVisibility() == VISIBLE
                ? Math.max(mBinding.tvTitle.getMeasuredHeight(), mTitleSize) : 0;
        int buttonByHeight = (height - titleHeight - 3 * gap) / 4;
        int buttonSize = mButtonSize > 0 ? mButtonSize
                : Math.max(Math.min(buttonByWidth, buttonByHeight), 1);
        int centerSize = mCenterSize > 0 ? mCenterSize : Math.max(buttonSize * 2, 1);

//        ConstraintLayout.LayoutParams lpU = (ConstraintLayout.LayoutParams) mBinding.bntMoveU.getLayoutParams();
//        lpU.width = buttonSize;
//        lpU.height = buttonSize;
//        lpU.topMargin = gap;
//        mBinding.bntMoveU.setLayoutParams(lpU);
//
//        ConstraintLayout.LayoutParams lpD = (ConstraintLayout.LayoutParams) mBinding.bntMoveD.getLayoutParams();
//        lpD.width = buttonSize;
//        lpD.height = buttonSize;
//        lpD.topMargin = gap;
//        mBinding.bntMoveD.setLayoutParams(lpD);
//
//        ConstraintLayout.LayoutParams lpL = (ConstraintLayout.LayoutParams) mBinding.bntMoveL.getLayoutParams();
//        lpL.width = buttonSize;
//        lpL.height = buttonSize;
//        lpL.setMarginEnd(gap);
//        mBinding.bntMoveL.setLayoutParams(lpL);
//
//        ConstraintLayout.LayoutParams lpR = (ConstraintLayout.LayoutParams) mBinding.bntMoveR.getLayoutParams();
//        lpR.width = buttonSize;
//        lpR.height = buttonSize;
//        lpR.setMarginStart(gap);
//        mBinding.bntMoveR.setLayoutParams(lpR);


        mBinding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
    }

    private int dp2px(float dpValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getResources().getDisplayMetrics()) + 0.5f);
    }

    /** 设置方向按钮点击回调 */
    public void setOnDirectionClickListener(OnDirectionClickListener listener) {
        this.mDirectionClickListener = listener;
    }

    /** 设置方向按钮边长（px），传 0 恢复为按总布局大小自适应 */
    public void setButtonSize(int sizePx) {
        mButtonSize = sizePx;
        applySize();
    }

    /** 设置中间设备图边长（px），传 0 恢复为按总布局大小自适应（默认 = 2 × 按钮） */
    public void setCenterSize(int sizePx) {
        mCenterSize = sizePx;
        applySize();
    }

    /** 设置标题文字大小（px） */
    public void setTitleSize(int sizePx) {
        mTitleSize = sizePx;
        applySize();
    }

    /** 设置按钮间距（按钮与按钮、按钮与标题/中间图的间距，px） */
    public void setButtonGap(int gapPx) {
        mGap = gapPx;
        applySize();
    }

    /** 设置标题文字 */
    public void setTitle(String title) {
        mBinding.tvTitle.setText(title);
    }

    /** 是否显示标题 */
    public void setTitleVisible(boolean visible) {
        mBinding.tvTitle.setVisibility(visible ? VISIBLE : GONE);
    }

    /** 获取标题 TextView */
    public TextView getTitleView() {
        return mBinding.tvTitle;
    }

    /** 获取上按钮，便于扩展长按/连击等操作 */
    public ImageView getBtnUp() {
        return mBinding.bntMoveU;
    }

    /** 获取下按钮 */
    public ImageView getBtnDown() {
        return mBinding.bntMoveD;
    }

    /** 获取左按钮 */
    public ImageView getBtnLeft() {
        return mBinding.bntMoveL;
    }

    /** 获取右按钮 */
    public ImageView getBtnRight() {
        return mBinding.bntMoveR;
    }
}
