package com.ty.tybusbase.callback;

import android.graphics.Canvas;

/**
 * @author :WEI_CP
 * Time:2023/4/19 13:46
 * Description: this is SurfaceAnimation
 */
public interface OnRefreshObserver {
    boolean onRefreshTick(Canvas canvas);
}
