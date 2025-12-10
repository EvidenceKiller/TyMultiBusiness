package com.ty.tybusbase.face

import android.content.Context
import android.graphics.RectF
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.identifty.base.TyLivingModel

interface FaceDetectorInterface {
    fun setFaceFeatureThreshold(thres: Float)

    fun init(context: Context, licenseKey: String? = null, finish: (Boolean) -> Unit?)

    fun loadStudentFace(studentList: List<StudentEntity>?, replace: Boolean)

    fun removeStudentFace(studentList: List<StudentEntity>)

    fun loadTeacherFace(teacherList: List<TeacherEntity>?, replace: Boolean)

    fun removeTeacherFace(teacherList: List<TeacherEntity>)

    fun getUserId(faceInfo: TyLivingModel?): String?

    fun isDetecting(): Boolean

    fun detectFace(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int = 0
    ): TyLivingModel?

    fun release()
}