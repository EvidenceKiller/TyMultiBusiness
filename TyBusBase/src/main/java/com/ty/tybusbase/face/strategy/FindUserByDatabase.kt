package com.ty.tybusbase.face.strategy

import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.adl.service.outer.AdlService

class FindUserByDatabase : IFindUserStrategy {
    override fun findStudent(userId: String?): StudentEntity? = AdlService.getService().studentDao.queryStudentByAccountId(userId)

    override fun findTeacher(userId: String?): TeacherEntity? = AdlService.getService().teacherDao.queryTeacherByAccountId(userId)
}