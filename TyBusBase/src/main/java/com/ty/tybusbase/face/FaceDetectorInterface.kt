package com.ty.tybusbase.face

import android.content.Context
import android.graphics.RectF
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.tybusbase.face.strategy.IFindUserStrategy

interface FaceDetectorInterface {
    val faceFeatureThreshold: Float
        get() = 0.75f

    fun init(context: Context, finish: (Boolean) -> Unit?)

    fun isDetecting(): Boolean

    fun loadStudentFace(studentList: List<StudentEntity>?, replace: Boolean)

    fun removeStudentFace(studentList: List<StudentEntity>)

    fun loadTeacherFace(teacherList: List<TeacherEntity>?, replace: Boolean)

    fun removeTeacherFace(teacherList: List<TeacherEntity>)

    fun detectFace(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int = 0
    )

    fun setFindUserStrategy(strategy: IFindUserStrategy)

    fun setFaceCallback(callback: OnFaceCallback?)

    fun release()
}