package com.ty.tybusbase.utils;

import android.icu.text.DecimalFormat;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * @ProjectName: SportBS
 * @Date: 2022/11/7
 * @Desc:
 */
public class NumberUtil {

    /**
     * 四舍五入
     *
     * @param d
     * @return
     */
    public static double formatDouble2(double d) {
        BigDecimal bigDecimal = new BigDecimal(d);
        double bg = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return bg;
    }


    static DecimalFormat df = new DecimalFormat("#.##");

    public static <T> String format2(T value) {
        return df.format(value);
    }

    static DecimalFormat df1 = new DecimalFormat("#.#");
    static DecimalFormat df2 = new DecimalFormat("#.#");
    static {
        df2.setRoundingMode(BigDecimal.ROUND_HALF_UP);
    }

    public static <T> String format1(T value) {
        return df1.format(value);
    }

    public static <T> String formatHalfUp(T value) {
        return df2.format(value);
    }

    static Pattern pattern = Pattern.compile("^[-+]?[d]*$");

    public static boolean isInteger(String str) {

        try {
            int x = Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }


        try {
            return pattern.matcher(str).matches();
        } catch (Exception e) {
            return false;
        }

    }


    public static float str2Float(String str) {
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
        }
        return 0;
    }

    public static int str2Int(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return 0;
    }

    public static long str2long(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
        }
        return 0;
    }


    public static double str2Double(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
        }
        return 0;
    }


    private static BigDecimal bigDecimal = new BigDecimal("0");

    public static String getBestShowValue(String unit, String poseValue) {
        try {
            float fsc = 0;
            if ("°".equals(unit)) {
                return poseValue + unit;
            } else if ("ms".equals(unit)) {
                return poseValue + unit;
            }else if ("s".equals(unit)) {
                return poseValue + unit;
            } else if ("%".equals(unit)) {
                bigDecimal = new BigDecimal(poseValue);
                fsc = bigDecimal.floatValue();
                if (fsc == 100) {
                    return "100%";
                } else if (fsc == 0) {
                    return "0%";
                } else if (NumberUtil.isInteger(String.valueOf(fsc))) {
                    return (int) fsc + "%";
                } else if ((null != poseValue && !poseValue.contains(".")) || String.valueOf(fsc).endsWith(".0")) {
                    return (int) fsc + "%";
                } else {
                    return NumberUtil.formatDouble2(fsc) + unit;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }

    private static final String[] DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    public static String toChinessNum(int num) {
        if (num >= 0 && num < DIGITS.length) {
            return DIGITS[num];
        } else {
            return String.valueOf(num);
        }
    }
}
