package com.ty.tybusbase.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mc wu on 2018/1/16.
 */

public class DateToStringUtils {

    public static final String YMD="yyyy年MM月dd日";
    public static final String YMDHM="yyyy-MM-dd HH:mm";
    public static final String HM="HH:mm";
    public static final String MD = "MM-dd";
    public static final String CH_H = "HH";
    public static final String CH_M = "mm";


    public static String dateToString(String type, Date date){
        if(date==null){
            return "";
        }else {
            SimpleDateFormat sdf = new SimpleDateFormat(type);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));//可能会导致ANR
            String str = sdf.format(date);
            return str;
        }
    }
    public static String formatDateTime(long mss) {
        String DateTimes = null;
        long minutes = (mss % (60 * 60)) / 60;
        long seconds = mss % 60;
        if (minutes > 0) {
            if (minutes < 10) {
                if(seconds<10){
                    DateTimes = "0" + minutes + ":"
                            + "0"+seconds ;
                }else {
                    DateTimes = "0" + minutes + ":"
                            + seconds + "";
                }

            } else {
                if(seconds<10){
                    DateTimes = minutes + ":"
                            + "0"+seconds ;
                }else {
                    DateTimes = minutes + ":"
                            + seconds + "";
                }

            }

        } else {
            if (seconds < 10) {
                DateTimes = "00:" + "0" + seconds;
            } else {
                DateTimes = "00:" + seconds;
            }

        }

        return DateTimes;
    }

    public static String sToHour(int s){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        double hour = Math.max(s/60.0/60.0,0.1);
        return decimalFormat.format(hour);
    }

    public static String sToHour(long s){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        double hour = Math.max(s/60.0/60.0,0.1);
        return decimalFormat.format(hour);
    }

}
