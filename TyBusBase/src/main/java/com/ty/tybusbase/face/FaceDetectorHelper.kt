package com.ty.tybusbase.face

import android.content.Context
import android.graphics.RectF
import android.os.Handler
import android.os.HandlerThread
import com.adl.lib.AdlYUVUtil
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyLivingModel
import com.ty.identifty.stface.SaveBitmapUtil
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.toEven
import com.ty.tybusbase.face.baidu.BaiduFaceDetectorImp
import com.ty.tybusbase.face.st.StFaceDetectorImp
import com.ty.tybusbase.face.strategy.DefaultVerifyStrategy
import com.ty.tybusbase.face.strategy.FindUserByDatabase
import com.ty.tybusbase.face.strategy.IFindUserStrategy
import com.ty.tybusbase.face.strategy.IVerifyStrategy
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.dformat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

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

    @Volatile
    private var isIsDetecting = false  //是否正在检测人脸

    private var isInitOk = false //是否初始化OK

    //获取用户信息的策略类
    private var findUerStrategy: IFindUserStrategy = FindUserByDatabase()

    //检验策略
    private var verifyStrategy: IVerifyStrategy = DefaultVerifyStrategy()

    /**
     * 记录帧数，识别人脸隔三针识别一次
     */
    private var detectCount = 0

    /**
     * 上次检测时间，超过1秒，则重置帧数
     */
    private var preDetectTime = 0L

    private val resetFrameCountTime = 60 * 1000 //1分钟重置帧数统计

    private val defaultFrameInterval = 4  //默认检测间隔

    private var frameInterval = defaultFrameInterval //几帧检测一帧

    private var defaultFaceFeatureThreshold = 0.4f //默认人脸阈值

    private var savePic = true

    /**
     * 初始化
     * type: 1:商汤：STFaceDetector 2:百度
     */
    fun init(
        context: Context,
        type: Int = 1,
        licenseKey: String? = null,
        finish: (Boolean) -> Unit?
    ) {
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
            faceDetector?.init(context, licenseKey) {
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
        type: Int = 0
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
        //如果在识别，则返回
        if (isIsDetecting) return
        isIsDetecting = true
        /***************************连续一次识别中，隔三帧检测一次*******************************/
        faceHandler?.post {
            val livingModel =
                faceDetector?.detectFace(key, yuv, width, height, rectF, type) ?: TyLivingModel()
            val userId = faceDetector?.getUserId(livingModel)
            savePic(yuv, width, height, rectF, userId, livingModel.faceInfo?.score ?: 0f)
            if (userId == null && livingModel.failType == FailType.NONE) {
                livingModel.failType = FailType.NO_SEARCH
            }
            val verify = verifyStrategy.verifyUser(
                userId ?: "",
                livingModel.faceInfo?.score ?: 0f
            )
            if (verify) {
                LogUtil.d("识别到用户ID：$userId")
                if (type == BusiBaseConstantsValue.FACE_DETECT_TYPE_STUDENT) {
                    val studentEntity = findUerStrategy.findStudent(userId)
                    if (studentEntity != null) {
                        mFaceCallback?.onFindStudent(studentEntity);
                        LogUtil.d("本地数据库中查询到学生：${studentEntity.accountName}")
                    } else {
                        mFaceCallback?.onFindFail(livingModel?.faceInfo, FailType.NO_MATCH)
                        LogUtil.d("本地数据库中未查询到学生：空")
                    }
                } else {
                    val teacherEntity = findUerStrategy.findTeacher(userId)
                    if (teacherEntity != null) {
                        mFaceCallback?.onFindTeacher(teacherEntity)
                        LogUtil.d("本地数据库中查询到老师：${teacherEntity.accountName}")
                    } else {
                        LogUtil.d("本地数据库中未查询到老师：空");
                        mFaceCallback?.onFindFail(livingModel.faceInfo, FailType.NO_MATCH)
                    }
                }
            } else {
                if (livingModel.failType == FailType.NONE){
                    livingModel.failType = FailType.NO_VERIFY
                }
                mFaceCallback?.onFindFail(livingModel.faceInfo,livingModel.failType)
            }
            isIsDetecting = false
        }
    }

    /**
     * 调试使用
     */
    private fun savePic(
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        userId: String?,
        score: Float
    ) {
        if (savePic.not()) return
        if (yuv == null || rectF == null || userId == null) return
        if (executor == null) {
            executor = Executors.newFixedThreadPool(2, Executors.defaultThreadFactory())
        }
        executor?.execute {

            try {
                if (rectF.width() < 40) {
                    val centerX = rectF.centerX()
                    rectF.left = max(centerX - 20, 0f)
                    rectF.right = min(centerX + 20, width.toFloat())
                }
                if (rectF.height() < 40) {
                    val centerY = rectF.centerY()
                    rectF.top = max(centerY - 20, 0f)
                    rectF.bottom = min(centerY + 20, height.toFloat())
                }
                var targetW = width
                var targetH = height
                targetW = rectF.width().toInt().toEven()
                targetH = rectF.height().toInt().toEven()
                val left = rectF.left.toInt().toEven()
                val top = rectF.top.toInt().toEven()
                val bytes = AdlYUVUtil.cropNV21(yuv, width, height, left, top, targetW, targetH)
                val studentEntity = findUerStrategy.findStudent(userId)
                saveBitmap(
                    bytes,
                    targetW,
                    targetH,
                    "${formatHms.format(Date(System.currentTimeMillis()))}_${studentEntity?.accountId}_${studentEntity?.accountName}_${score}.jpg"
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    private val bitmapDic = "/sdcard/zy.sport.cache/pic"
    private var executor: ExecutorService? = null
    val dformat = SimpleDateFormat("yyyy-MM-dd")
    val formatHms = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    private fun saveBitmap(yuv: ByteArray, width: Int, height: Int, name: String) {
        val date = dformat.format(Date())
        val path = "$bitmapDic/$date"
        SaveBitmapUtil.saveBitmap(
            yuv,
            width,
            height,
            path,
            name
        )
    }


    /**
     * 是否需要保存图片
     */
    fun setSavePic(save: Boolean) {
        savePic = save
    }

    /**
     * 设置默认人脸特征阈值
     * 用于reset使用
     */
    fun setDefaultFaceFeatureThreshold(th: Float) {
        defaultFaceFeatureThreshold = th
    }

    /**
     * 设置人脸特征阈值
     */
    fun setFaceFeatureThreshold(score: Float) {
        faceDetector?.setFaceFeatureThreshold(score)
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

    /**
     * 设置人脸检验策略类
     */
    fun setVerifyStrategy(strategy: IVerifyStrategy) {
        verifyStrategy = strategy
    }

    /**
     * 获取默认人脸检验策略类
     * 可以设置策略里的相应参数
     */
    fun getDefaultVerifyStrategy(): DefaultVerifyStrategy? {
        return verifyStrategy as? DefaultVerifyStrategy
    }

    /**
     * 设置检测的帧间隔
     */
    fun setFrameInterval(interval: Int) {
        frameInterval = interval
    }

    fun isInitOk(): Boolean {
        return isInitOk
    }

    fun isDetecting() = isIsDetecting

    fun reset() {
        isIsDetecting = false
        mFaceCallback = null
        frameInterval = defaultFrameInterval
        faceDetector?.setFaceFeatureThreshold(defaultFaceFeatureThreshold)
        verifyStrategy.reset()
    }

    fun release() {
        isIsDetecting = false
        faceDetector?.release()
        faceHandlerThread?.quitSafely()
        faceHandlerThread = null
        faceHandler = null
    }
}