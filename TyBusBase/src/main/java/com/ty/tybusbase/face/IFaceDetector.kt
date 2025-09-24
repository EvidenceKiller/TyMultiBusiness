package com.ty.tybusbase.face

import android.graphics.RectF
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity

interface IFaceDetector {
    /**
     * 初始化人脸SDK
     */
    fun init(finish: () -> Unit)

    /**
     * 释放人脸SDK
     */
    fun release()

    /**
     * 加载学生人脸数据
     */
    fun loadStudentFace(
        userBaseForFaces: List<StudentEntity>,
        replace: Boolean,
        finish: (() -> Unit)? = null
    )
    /**
     * 移除学生人脸数据
     */
    fun removeFace(userBaseForFaces: List<StudentEntity>)


    /**
     * 加载教师人脸数据
     */
    fun loadTeacherFace(
        userBaseForFaces: List<TeacherEntity>,
        replace: Boolean = false,
        finish: (() -> Unit)? = null
    )

    /**
     * 移除教师人脸数据
     */
    fun removeTeacherFace(userBaseForFaces: List<TeacherEntity>)


    /**
     * 调用人脸识别
     */
    fun callFaceDetector(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int = 0,
        savePic: Boolean = false
    )

}