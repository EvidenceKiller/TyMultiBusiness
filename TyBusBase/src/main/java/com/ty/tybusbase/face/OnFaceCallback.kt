package com.ty.tybusbase.face

import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyFaceInfo

interface OnFaceCallback {
    fun onFindStudent(user: StudentEntity?)
    fun onFindTeacher(user: TeacherEntity?) {}
    fun onFindUserId(userId:String?,score:Float)
    fun onFindFail(info: TyFaceInfo<*>?,type: FailType)
}