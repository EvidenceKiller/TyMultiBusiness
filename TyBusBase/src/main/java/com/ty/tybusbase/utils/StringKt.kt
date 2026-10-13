package com.ty.tybusbase.utils

import java.math.BigDecimal
import java.math.RoundingMode


object StringKt {

    fun subZeroAndDot(value: String): String {
        if (value.isEmpty() || !value.contains('.')) return value
        var result = value
        while (result.endsWith('0')) result = result.dropLast(1)
        if (result.endsWith('.')) result = result.dropLast(1)
        return result
    }


    fun bigDecimalDivide(
        dividend: Any,
        divisor: Number,
        scale: Int,
        roundingMode: RoundingMode
    ): BigDecimal {
        val bigDividend = when (dividend) {
            is BigDecimal -> dividend
            is Number -> BigDecimal(dividend.toString())   // 避免 Double 精度问题
            is String -> BigDecimal(dividend)
            else -> throw IllegalArgumentException("Unsupported dividend type: ${dividend::class.java}")
        }
        val bigDivisor = BigDecimal(divisor.toString())    // 同理
        return bigDividend.divide(bigDivisor, scale, roundingMode)
    }

    fun bigDecimalDivideToString(
        dividend: Any,
        divisor: Number,
        scale: Int,
        roundingMode: RoundingMode,
        nullValue: String = ""
    ): String {
        return try {
            bigDecimalDivide(dividend, divisor, scale, roundingMode).toString()
        } catch (e: ArithmeticException) {
            nullValue
        }
    }
}