package com.ty.tybusbase.face

import android.content.Context
import android.graphics.RectF
import android.os.Handler
import android.os.HandlerThread
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.tybusbase.face.st.StFaceDetectorImp

class FaceDetectorHelper private constructor() {

    companion object {
        @Volatile
        private var instance: FaceDetectorHelper? = null
        fun getInstance(): FaceDetectorHelper {
            return instance ?: synchronized(this) {
                instance ?: FaceDetectorHelper().also { instance = it }
            }
        }
    }

    private var faceDetector: FaceDetectorInterface? = null

    private var faceHandlerThread: HandlerThread? = null
    private var faceHandler: Handler? = null

    private var isInitOk = false //是否初始化OK

    /**
     * 初始化
     * type: 1:商汤：STFaceDetector 2:百度
     */
    fun init(context: Context, type: Int = 1, finish: (Boolean) -> Unit?) {
        if (isInitOk) return
        if (faceHandlerThread == null) {
            faceHandlerThread = HandlerThread("St_FACE")
            faceHandlerThread!!.start()
            faceHandler = Handler(faceHandlerThread!!.looper)
        }
        faceDetector = when (type) {
            1 -> StFaceDetectorImp()
            2 -> StFaceDetectorImp()
            else -> StFaceDetectorImp()
        }
        faceHandler?.post {
            faceDetector?.init(context) {
                isInitOk = it
                finish.invoke(it)
            }
        }
    }

    fun isDetecting(): Boolean = faceDetector?.isDetecting() == true

    fun loadStudentFace(
        studentList: List<StudentEntity>?,
        replace: Boolean,
        finish: (() -> Unit)? = null
    ) {
        faceHandler?.post {
            faceDetector?.loadStudentFace(studentList, replace)
            finish?.invoke()
        }
    }

    fun removeStudentFace(studentList: List<StudentEntity>) {
        faceHandler?.post {
            faceDetector?.removeStudentFace(studentList)
        }
    }

    fun loadTeacherFace(
        teacherList: List<TeacherEntity>?,
        replace: Boolean,
        finish: (() -> Unit)? = null
    ) {
        faceHandler?.post {
            faceDetector?.loadTeacherFace(teacherList, replace)
            finish?.invoke()
        }
    }

    fun removeTeacherFace(teacherList: List<TeacherEntity>) {
        faceHandler?.post {
            faceDetector?.removeTeacherFace(teacherList)
        }
    }

    fun detectFace(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int
    ) {
        faceHandler?.post {
            faceDetector?.detectFace(key, yuv, width, height, rectF, type)
        }
    }

    fun setFaceCallback(callback: OnFaceCallback?) {
        faceDetector?.setFaceCallback(callback)
    }

    fun release() {
        faceDetector?.release()
        faceHandlerThread?.quitSafely()
        faceHandlerThread = null
        faceHandler = null
    }
}