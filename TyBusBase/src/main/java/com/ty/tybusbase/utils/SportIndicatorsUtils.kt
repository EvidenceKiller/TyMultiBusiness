package com.ty.tybusbase.utils

import com.adl.service.entity.SportImageEntity
import com.adl.sport.general.EuExerciseFileGroup
import java.math.BigDecimal
import java.math.RoundingMode

object SportIndicatorsUtils {
    //  保留X位小数
    fun setValueScale(value: String, newScale: Int = 1): String {
        val result = BigDecimal(value).setScale(newScale, RoundingMode.HALF_UP).toString()
        return StringKt.subZeroAndDot(result)
    }

    //  转换（毫秒->秒）
    fun convertMsToS(ms: String?): String {
        if (ms.isNullOrEmpty()) return "0.0"
        return BigDecimal(ms).divide(BigDecimal(1000), 2, RoundingMode.HALF_UP).toString()
    }

    // 确保 List 长度为 targetSize，不足则用 defaultItem 填充
    fun <T> ensureListSize(list: List<T>?, targetSize: Int, defaultItem: T): List<T> {
        if (list == null || list.size >= targetSize) {
            return list?.toList() ?: List(targetSize) { defaultItem }
        }
        return list.toMutableList().apply {
            while (size < targetSize) {
                add(defaultItem)
            }
        }
    }

    //  变向时机系数列表
    //  turningTimeArr:绕杆区各个杆的变向时间 windingTimeArr:绕杆区各个杆的绕杆时间
    fun getTurningFactorList(
        poleNum: Int, turningTimeArr: List<Long>?, windingTimeArr: List<Long>?,
    ): List<String> {
        return arrayListOf<String>().also { temp ->
            repeat(poleNum) {
                try {
                    if (turningTimeArr.isNullOrEmpty() || windingTimeArr.isNullOrEmpty()) {
                        temp.add("")
                    } else {
                        val turningTime = turningTimeArr[it]
                        val windingTime = windingTimeArr[it]
                        if (turningTime == 0L || windingTime == 0L) {
                            temp.add("")
                        } else {
                            getTurningFactor(turningTime, windingTime).let { factor ->
                                temp.add(factor.toString())
                            }
                        }
                    }
                } catch (_: Exception) {
                    temp.add("")
                }
            }
        }
    }

    //   变向时机系数=(单次绕行变向区耗时/单次绕行总耗时-0.5)*10
    private fun getTurningFactor(turningTime: Long, totalTime: Long): Double {
        if (turningTime == 0L || totalTime == 0L) return 0.0

        val divide = BigDecimal(turningTime).divide(
            BigDecimal(totalTime.toDouble()), 2, RoundingMode.HALF_UP
        )
        val subtract = divide.subtract(BigDecimal(0.5))
        val multiply = subtract.multiply(BigDecimal(10))
        return multiply.setScale(1, RoundingMode.HALF_UP).toDouble()
    }

//    //  转换点
//    fun transformPoints(
//        points: List<com.zy.logic.Point>?,
//        height: Int
//    ): List<SerialPoint> {
//        return points?.map { SerialPoint(it.x, height - it.y) }?.toList() ?: listOf()
//    }
//
//    //  转换点
//    fun transformPoints2(
//        points: List<com.ty.sport.common.basic.bean.Point>?,
//        height: Int
//    ): List<SerialPoint> {
//        return points?.map { SerialPoint(it.x.toInt(), height - it.y.toInt()) }?.toList()
//            ?: listOf()
//    }
//
//    //  转换点
//    fun transformPoints3(
//        points: List<android.graphics.Point>?,
//        height: Int
//    ): List<SerialPoint> {
//        return points?.map { SerialPoint(it.x, height - it.y) }?.toList()
//            ?: listOf()
//    }

    fun addIndicatorsImages(groupList: List<EuExerciseFileGroup>?): MutableList<SportImageEntity> {
        val indicatorsImages = mutableListOf<SportImageEntity>()
        groupList?.forEach { group ->
            group.items?.forEach { item ->
                item.file?.let {
                    val sportImageEntity = SportImageEntity(
                        item.desc, it.absolutePath, group.index.toString()
                    )
                    indicatorsImages.add(sportImageEntity)
                }
            }
        }
        return indicatorsImages
    }

    fun addIndicatorsImages(path: String?, des: String): MutableList<SportImageEntity> {
        return if (path.isNullOrEmpty()) {
            mutableListOf()
        } else {
            mutableListOf(SportImageEntity(path, des))
        }
    }


}