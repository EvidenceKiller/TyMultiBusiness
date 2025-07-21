package com.ty.tybusbase.utils;

import java.util.List;

/**
 * @ProjectName: SportBS
 * @Date: 2022/10/14
 * @Desc:
 */
public class ListUtils {


    public static boolean isEmpty(List datas) {
        if (null == datas || datas.isEmpty()) {
            return true;
        }
        return false;
    }

    public static int length(List datas) {
        if (null == datas || datas.isEmpty()) {
            return 0;
        } else {
            return datas.size();
        }
    }
}