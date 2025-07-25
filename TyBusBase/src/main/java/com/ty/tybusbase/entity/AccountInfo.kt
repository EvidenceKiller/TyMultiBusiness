package com.ty.tybusbase.entity

import android.os.Parcelable
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountInfo(
    var studentEntity: StudentEntity? = null,
    var teacherEntity: TeacherEntity? = null
) : Parcelable, Cloneable {
    override fun toString(): String {
        return studentEntity.toString() + teacherEntity.toString()
    }
}