package com.ty.bus.multi.core.utils

import com.ty.tybusbase.utils.NumberUtil
import java.math.BigDecimal
import java.math.RoundingMode

object BmiUtil {
    fun computeBMI(hResult: Double, wResult: Double): String {
        val multiply = BigDecimal(hResult).multiply(BigDecimal(hResult))
        val bmi: Double =
            BigDecimal(wResult).divide(multiply, 1, RoundingMode.DOWN).toDouble()
        //  结果，保留一位小数
        return NumberUtil.format1<Double>(bmi)
    }
}