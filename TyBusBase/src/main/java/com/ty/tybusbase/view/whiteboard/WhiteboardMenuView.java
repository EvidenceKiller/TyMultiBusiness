package com.ty.tybusbase.view.whiteboard;

import static com.ty.tybusbase.view.whiteboard.PenSettingPopWindow.RED;
import static com.ty.tybusbase.view.whiteboard.PenSettingPopWindow.SMALL;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ty.tybusbase.R;
import com.ty.tybusbase.databinding.ViewWihteboardBinding;

/**
 * @author :WEI_CP
 * Time:2023/1/10 15:36
 * 画板菜单
 */
public class WhiteboardMenuView extends FrameLayout implements PenSettingPopWindow.OnPenSettingCallback, EraserPopWindow.OnEraserSettingCallback {

    private Context mContext;
    private OnCloseListener mCloseListener;

    //画笔模式的设置弹框
    private PenSettingPopWindow penSettingPop;

    //橡皮模式的设置弹框
    private EraserPopWindow eraserPopWindow;
    private long timeDown;
    private float downX;
    private float downY;
    private boolean isLongPrese;
    private long moveTime;
    private ViewWihteboardBinding mBinding;

    public WhiteboardMenuView(@NonNull Context context) {
        this(context, null);
    }

    public WhiteboardMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteboardMenuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBinding = ViewWihteboardBinding.inflate(LayoutInflater.from(context), this, true);
      /*
      //增加拖动功能时 解决点击事件与长按拖动事件的冲突
      //目前产品未要求 暂时屏蔽，很有可能下个版本要增加
      mLayoutMenu.setOnTouchListener(((view, motionEvent) ->{
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    timeDown = System.currentTimeMillis();
                    isLongPrese = false;
                    downX = motionEvent.getRawX();
                    downY = motionEvent.getRawY();
                    break;
                    case MotionEvent.ACTION_MOVE:
                        moveTime = System.currentTimeMillis();
                        if(moveTime-timeDown>600){
                            isLongPrese=true;
                            float moveX = motionEvent.getRawX();
                            float moveY = motionEvent.getRawY();
                            //移动的距离
                            float dx = moveX - downX;
                            float dy = moveY - downY;
                            //重新设置控件的位置。移动
                            ViewGroup.MarginLayoutParams params = (FrameLayout.MarginLayoutParams) mLayoutMenu.getLayoutParams();
                            params.rightMargin -= dx;
                            params.bottomMargin -= dy;
                            mLayoutMenu.setLayoutParams(params);
                            //重置
                            downX = moveX;
                            downY = moveY;
                        }
                case MotionEvent.ACTION_UP:
                    if(!isLongPrese){
                        if(isTouchPointInView(mBinding.layoutPen,(int)downX,(int)downY)){
                            clickPen();
                        }else if(isTouchPointInView(mBinding.layoutEraser,(int)downX,(int)downY)){
                            clickEraser();
                        }else if(isTouchPointInView(mBinding.imgClose,(int)downX,(int)downY)){
                            clickClose();
                        }
                    }
            }
            return true;
        } ));*/
        
        mBinding.layoutPen.setOnClickListener(v -> {
            clickPen();
        });

        mBinding.layoutEraser.setOnClickListener(v -> {
            clickEraser();
        });

        
        mBinding.imgClose.setOnClickListener(v -> {
            clickClose();
        });


    }

    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }


    public void setCloseListener(OnCloseListener mCloseListener) {
        this.mCloseListener = mCloseListener;
    }

    public void clickPen() {
        
        if (mBinding.imgPen.isSelected()) return;

        mBinding.whiteBoardSurface.setModel(WhiteboardView.PEN);
        mBinding.imgPen.setSelected(true);
        mBinding.layoutPen.setBackgroundResource(R.drawable.rect_whiteboard_click_bg);
        
        mBinding.imgEraser.setSelected(false);
        mBinding.layoutEraser.setBackground(null);
        if (eraserPopWindow != null && eraserPopWindow.isShowing()) {
            eraserPopWindow.dismiss();
        }
        //显示画笔设置弹框
        if (penSettingPop == null) penSettingPop = new PenSettingPopWindow(mContext);
        penSettingPop.setOnDismissListener(() -> {
            mBinding.imgPen.setSelected(false);
            mBinding.layoutPen.setBackground(null);
        });
        penSettingPop.setSettingCallback(this);
        penSettingPop.setDefaultColor(mBinding.whiteBoardSurface.getPaintColor());
        penSettingPop.setDefaultWidth(mBinding.whiteBoardSurface.getPenWidth());
        penSettingPop.showAtLocation(mBinding.getRoot(), 0,
                getResources().getDimensionPixelOffset(R.dimen.dp_1610),
                getResources().getDimensionPixelOffset(R.dimen.dp_790));
    }

    public void clickEraser() {
        if (mBinding.imgEraser.isSelected()) return;
        mBinding.whiteBoardSurface.setModel(WhiteboardView.ERASER);
        if (penSettingPop != null && penSettingPop.isShowing()) {
            penSettingPop.dismiss();
        }
        mBinding.imgPen.setSelected(false);
        mBinding.layoutPen.setBackground(null);
        mBinding.imgEraser.setSelected(true);
        mBinding.layoutEraser.setBackgroundResource(R.drawable.rect_whiteboard_click_bg);
        //显示画笔设置弹框
        if (eraserPopWindow == null) eraserPopWindow = new EraserPopWindow(mContext);
        eraserPopWindow.setOnDismissListener(() -> {
            mBinding.imgEraser.setSelected(false);
            mBinding.layoutEraser.setBackground(null);
        });
        eraserPopWindow.setSettingCallback(this);
        eraserPopWindow.setDefaultWidth(mBinding.whiteBoardSurface.getEraserWidth());
        eraserPopWindow.showAtLocation(mBinding.getRoot(), 0,
                getResources().getDimensionPixelOffset(R.dimen.dp_1637),
                getResources().getDimensionPixelOffset(R.dimen.dp_866));
    }

    public void clickClose() {
        if (mBinding.imgEraser.isSelected()) return;
        if (penSettingPop != null && penSettingPop.isShowing()) {
            penSettingPop.dismiss();
        }
        if (eraserPopWindow != null && eraserPopWindow.isShowing()) {
            eraserPopWindow.dismiss();
        }
        mBinding.imgPen.setSelected(false);
        mBinding.layoutPen.setBackground(null);
        mBinding.imgEraser.setSelected(false);
        mBinding.layoutEraser.setBackground(null);
        //关闭弹框
        this.setVisibility(GONE);
        if (mCloseListener != null) mCloseListener.onClose();
        //恢复默认防止下次打开出现历史设置
        mBinding.whiteBoardSurface.clearWhiteBoard();
        mBinding.whiteBoardSurface.setEraserWidth(EraserPopWindow.SMALL);
        mBinding.whiteBoardSurface.setPaintColor(RED);
        mBinding.whiteBoardSurface.setModel(WhiteboardView.PEN);
        mBinding.whiteBoardSurface.setPenWidth(SMALL);
    }

    public void hideCloseBtn(){
        mBinding.imgClose.setVisibility(GONE);
    }

    @Override
    public void setPenWidth(@PenSettingPopWindow.PenWidth int width) {
        mBinding.whiteBoardSurface.setPenWidth(width);
    }

    @Override
    public void setPenColor(@PenSettingPopWindow.PenColor String color) {
        mBinding.whiteBoardSurface.setPaintColor(color);
    }


    @Override
    public void setEraserWidth(@EraserPopWindow.EraserWidth int width) {
        mBinding.whiteBoardSurface.setEraserWidth(width);
    }

    public interface OnCloseListener {
        void onClose();
    }
}
