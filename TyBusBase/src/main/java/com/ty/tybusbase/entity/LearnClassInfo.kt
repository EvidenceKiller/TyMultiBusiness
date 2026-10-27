package com.ty.tybusbase.entity

import com.google.protobuf.Timestamp

data class LearnClassInfo(
    val lessonId: String,
    val teacherId: String,
    val classId: String,
    val timestamp: Long
)