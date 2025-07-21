package com.ty.bus.multi.core;

import android.app.Activity;
import android.content.Intent;

import com.ty.bus.multi.core.ui.rope.TenSportActivity;

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
public class UINavigation {

    public static void goTenSportRope(Activity activity, UINavConfig config) {
        // TODO 导航跳转
        activity.startActivity(new Intent(activity, TenSportActivity.class));
    }

    public static void goFiveSkipRope(Activity activity, UINavConfig config) {
        // TODO 导航跳转

    }

    public static void goThreeSitReach(Activity activity, UINavConfig config) {
        // TODO 导航跳转

    }
}
