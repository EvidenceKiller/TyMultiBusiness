package com.ty.identifty.stface;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author :WEI_CP
 * Time:2022/12/29 13:59
 * Description: this is StConfig
 */
public class StContact {
    public final static String LOG_TAG = "StFace>> ";

    //region----------------商湯授權-----------------
    public static final String AUTH_ON_LING = "sdk_video_online";
    public static final String AUTH_ESAM = "sdk_video_esam";
    public static final String AUTH_ACTIVE = "sdk_video_active";

    @StringDef({AUTH_ON_LING, AUTH_ESAM, AUTH_ACTIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SdkVideoMode {
    }
    // endregion----------------商湯授權-----------------
}
