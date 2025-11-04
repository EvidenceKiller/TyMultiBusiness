package com.ty.tybusbase.face.strategy

import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity

/**
 * 查找用户策略类接口
 */
interface IFindUserStrategy {
    fun findStudent(userId: String?): StudentEntity?

    fun findTeacher(userId: String?): TeacherEntity?
}