package com.ty.identifty.stface;

import static com.ty.identifty.stface.StContact.LOG_TAG;

import android.util.Log;

/**
 * @author :WEI_CP
 * Time:2022/12/29 13:32
 * Description: this is ST
 */
public class StHelper {

    public static void setDebugAble(boolean debugAble) {
        StHelper.debugAble = debugAble;
    }

    private static boolean debugAble = true;

    public static void logI(String msg) {
        if (debugAble) {
            Log.d(LOG_TAG,msg);
        }

    }

    public static void logE(String msg) {
        if(debugAble){
            Log.e(LOG_TAG,msg);
        }
    }

    public static void parseSdkErrorCode(int ret, String msg) {
        if (ret == 0) {
            return;
        }
        String errMsg = msg + "错误码: " + ret + "! ";
        if (ret == -13) {
            Log.d(LOG_TAG,errMsg + "please check if the license file exists!");
        } else if (ret == -17) {
            Log.d(LOG_TAG,errMsg + "please check if you are connected to the network!");
        } else if (ret == -29) {
            Log.d(LOG_TAG,errMsg + "please try turn on you wifi!");
        } else if (ret == -32) {
            Log.d(LOG_TAG,errMsg + "please check if the I2C communication is normal!");
        } else if (ret == -33) {
            Log.d(LOG_TAG,errMsg + "please check if the license key is correct!");
        } else if (ret == -1) {
            Log.d(LOG_TAG,errMsg + "please check if the plugin path is correct!");
        } else if (ret == -4) {
            Log.d(LOG_TAG,errMsg + "please check if the model path is correct!");
        } else {
            Log.d(LOG_TAG,errMsg);
        }
        //System.exit(0);
    }
}
