package com.ty.tybusbase.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountInfo(
    var id: String = "",
    var className: String? = "",
    var classId: String? = "",
    var grade: Int = 0,
    var gradeName: String? = null, //年级名字
    var period: Int = 0,//1为小学，2为初中，3为高中，4为大学
    var sex: Int = 0,//1 男 2女 9未知
    var userImg: String? = "",
    var userName: String = "",
    var type: Int = 0, // 0为学生，1为老师
) : Parcelable, Cloneable {
    override fun toString(): String {
        return "AccountInfo(id='$id', className=$className, classId=$classId, grade=$grade, gradeName=${gradeName}, period=$period, sex=$sex, userImg=$userImg, userName=$userName)"
    }
}