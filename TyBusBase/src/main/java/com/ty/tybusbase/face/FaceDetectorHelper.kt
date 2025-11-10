package com.ty.tybusbase.face

import android.content.Context
import android.graphics.RectF
import android.os.Handler
import android.os.HandlerThread
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.face.baidu.BaiduFaceDetectorImp
import com.ty.tybusbase.face.st.StFaceDetectorImp
import com.ty.tybusbase.face.strategy.FindUserByDatabase
import com.ty.tybusbase.face.strategy.IFindUserStrategy
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.max

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

    private var mFaceCallback: OnFaceCallback? = null

    private var faceDetector: FaceDetectorInterface? = null

    private var faceHandlerThread: HandlerThread? = null
    private var faceHandler: Handler? = null

    private var isInitOk = false //是否初始化OK

    //获取用户信息的策略类
    private var findUerStrategy: IFindUserStrategy = FindUserByDatabase()

    private val tempIdentityMap by lazy { HashMap<String, Pair<Int, Float>>() }

    /**
     * 记录帧数，识别人脸隔三针识别一次
     */
    private var detectCount = 0

    /**
     * 上次检测时间，超过1秒，则重置帧数
     */
    private var preDetectTime = 0L

    private val frameInterval = 4 //几帧检测一帧

    private val resetFrameCountTime = 60 * 1000 //1分钟重置帧数统计

    /**
     * 初始化
     * type: 1:商汤：STFaceDetector 2:百度
     */
    fun init(context: Context, type: Int = 1, finish: (Boolean) -> Unit?) {
        if (isInitOk) {
            finish.invoke(true)
            return
        }
        if (faceHandlerThread == null) {
            faceHandlerThread = HandlerThread("FACE")
            faceHandlerThread!!.start()
            faceHandler = Handler(faceHandlerThread!!.looper)
        }
        faceDetector = when (type) {
            1 -> StFaceDetectorImp()
            2 -> BaiduFaceDetectorImp()
            else -> StFaceDetectorImp()
        }
        faceHandler?.post {
            faceDetector?.init(context) {
                isInitOk = it
                finish.invoke(it)
            }
        }
    }

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
        /***************************连续一次识别中，隔三帧检测一次*******************************/
        //重置检测帧数和
        if (System.currentTimeMillis() - preDetectTime > resetFrameCountTime) {
            detectCount = 0
        }
        preDetectTime = System.currentTimeMillis()
        //帧数检测，连续识别隔3帧检测一次
        detectCount++
        if (detectCount % frameInterval != 0) {
            return
        }

        /***************************连续一次识别中，隔三帧检测一次*******************************/
        faceHandler?.post {
            val livingModel = faceDetector?.detectFace(key, yuv, width, height, rectF, type)
            val userId = faceDetector?.getUserId(livingModel)
            if (userId != null && verifyUser(userId, livingModel?.faceInfo?.score ?: 0f)) {
                LogUtil.d("识别到用户ID：$userId")
                if (type == BusiBaseConstantsValue.FACE_DETECT_TYPE_STUDENT) {
                    val studentEntity = findUerStrategy.findStudent(userId)
                    if (studentEntity != null) {
                        mFaceCallback?.onFindStudent(studentEntity);
                        LogUtil.d("本地数据库中查询到学生：${studentEntity.accountName}")
                    } else {
                        mFaceCallback?.onFindFail(livingModel?.faceInfo)
                        LogUtil.d("本地数据库中未查询到学生：空")
                    }
                } else {
                    val teacherEntity = findUerStrategy.findTeacher(userId)
                    if (teacherEntity != null) {
                        mFaceCallback?.onFindTeacher(teacherEntity)
                        LogUtil.d("本地数据库中查询到老师：${teacherEntity.accountName}")
                    } else {
                        LogUtil.d("本地数据库中未查询到老师：空");
                        mFaceCallback?.onFindFail(livingModel?.faceInfo)
                    }
                }
            } else {
                mFaceCallback?.onFindFail(livingModel?.faceInfo)
            }
        }
    }

    /**
     * 设置人脸回调
     */
    fun setFaceCallback(callback: OnFaceCallback?) {
        mFaceCallback = callback
    }

    /**
     * 设置获取用户信息的策略类
     */
    fun setFindUserStrategy(strategy: IFindUserStrategy) {
        this.findUerStrategy = strategy
    }

    fun isInitOk(): Boolean {
        return isInitOk
    }

    fun isDetecting() = faceDetector?.isDetecting() == true

    /**
     * 校验人员，如果，分数低于0.6的，需要三次连续识别是同一个人，且其中一帧的分要达到一个阈值（如0.5），才认为是识别成功
     */
    private fun verifyUser(faceId: String, score: Float): Boolean {
        if (score >= 0.8f) {
            tempIdentityMap.clear()
            return true
        }
        // 低分时，累计识别次数
        val pair = tempIdentityMap.getOrDefault(faceId, Pair(0, 0f))
        val count = pair.first + 1
        val maxScore = max(score, pair.second)
        tempIdentityMap.clear()
        return if (count >= 3 && maxScore >= 0.5f) {
            true
        } else {
            tempIdentityMap[faceId] = Pair(count, maxScore)
            false
        }
    }

    fun release() {
        faceDetector?.release()
        faceHandlerThread?.quitSafely()
        faceHandlerThread = null
        faceHandler = null
    }
}