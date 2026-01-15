package com.ty.bus.multi.core.entity

import com.adl.service.entity.ClassInfoEntity

data class PlanGradeInfo(
    var gradeName: String,
    var gradeNum: Int,
    var classList: MutableList<ClassInfoEntity>
)