package com.ty.tybusbase.face.st

import android.content.Context
import android.graphics.RectF
import android.os.SystemClock
import com.adl.base.file.AdlFileHelper
import com.adl.base.log.AdlLogger
import com.adl.lib.AdlYUVUtil
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.alibaba.fastjson.JSON
import com.sensetime.sdk.video.module.sdk.common.StVerifyResult
import com.ty.identifty.stface.BeforeSenseActiveCallBack
import com.ty.identifty.stface.STFaceDetector
import com.ty.identifty.stface.STFaceFactory
import com.ty.identifty.stface.StHelper
import com.ty.identifty.stface.StParameter
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.toEven
import com.ty.tybusbase.face.FaceDetectorInterface
import com.ty.tybusbase.face.OnFaceCallback
import com.ty.tybusbase.face.strategy.FindUserByDatabase
import com.ty.tybusbase.face.strategy.IFindUserStrategy
import com.ty.tybusbase.utils.ApplicationUtil.getContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class StFaceDetectorImp : FaceDetectorInterface {
    @Volatile
    var isInitOk = false

    @Volatile
    var isIsDetecting = false

    private var mFaceCallback: OnFaceCallback? = null

    private var faceDetector: STFaceDetector? = null

    private val faceIdMap = hashMapOf<Int, String>()

    private var failCount: Long = 0

    private var secCount: Long = 0

    private val tempIdentityMap by lazy { HashMap<Int, Pair<Int, Float>>() }

    private var findUerStrategy: IFindUserStrategy = FindUserByDatabase()

    /**
     * 记录帧数，识别人脸隔三针识别一次
     */
    private var detectCount = 0

    /**
     * 上次检测时间，超过1秒，则重置帧数
     */
    private var preDetectTime = 0L

    override fun init(context: Context, finish: (Boolean) -> Unit?) {
        if (isInitOk) {
            finish.invoke(true)
            return
        }
        StHelper.setDebugAble(true)
        faceDetector = STFaceFactory.getInstance()
            .detectorCreate((getContext()), object : BeforeSenseActiveCallBack {
                override fun beforeDo(params: StParameter.Params) {
                    val basePath = params.common.basePath
                    AdlLogger.d("license basePath:$basePath")
                    val licenseSignCode = params.common.licenseSignCode
                    val licenseSignCodeFile = "$basePath/$licenseSignCode"
                    val file = File(licenseSignCodeFile)
                    if (file.exists()) {
                        try {
                            val licenseIns = getContext()
                                .assets.open("sense_license.lic")
                            val fos: OutputStream =
                                FileOutputStream(basePath + File.separator + params.common.licenseName)
                            AdlFileHelper.copyStream(licenseIns, fos)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            val licenseIns = getContext()
                                .assets.open("sense_license_new.lic")
                            val fos: OutputStream =
                                FileOutputStream(basePath + File.separator + params.common.newLicenseName)
                            AdlFileHelper.copyStream(licenseIns, fos)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        if (faceDetector == null) {
            AdlLogger.e("商汤人脸创建失败")
            finish.invoke(false)
            return
        }
        faceDetector!!.init(getContext(), null)
        isInitOk = faceDetector!!.isOk
        finish.invoke(faceDetector!!.isOk)
    }

    override fun isDetecting(): Boolean = isIsDetecting
    override fun loadStudentFace(studentList: List<StudentEntity>?, replace: Boolean) {
        if (studentList == null) return
        //如果是替换所有，则先清除原数据
        if (replace) {
            faceDetector!!.clearDb()
            faceIdMap.clear()
        }
        for (user: StudentEntity in studentList) {
            val student = user.faceDataList?.find { it.type == "2" }
            if (student != null) {
                try {
                    val stVerifyResult =
                        StVerifyResult(
                            JSON.parseObject(
                                student.faceData,
                                FloatArray::class.java
                            )
                        )

                    if (addFace(replace, user.accountId, stVerifyResult)) {
                        StHelper.logI("${user.accountName} 特征入库ok")
                    } else {
                        StHelper.logI("${user.accountName} 特征入库失败")
                    }
                } catch (e: Exception) {
                    failCount++
                    AdlLogger.e("${user.accountName} 特征入库失败 $e")
                }
            } else {
                failCount++
                AdlLogger.e("${user.accountName} 特征入库失败,特征空")
            }
        }
        StHelper.logI("学生特征入库完成**成功:$secCount***失败：$failCount")
        failCount = 0
        secCount = 0
    }


    override fun removeStudentFace(userBaseForFaces: List<StudentEntity>) {
        var count = 0
        userBaseForFaces.forEach { stu ->
            faceIdMap.entries.find { it.value == stu.accountId }?.key?.let {
                if (faceDetector?.deleteDb(it) == true) {
                    count++
                }
                faceIdMap.remove(it)

            }
        }
        AdlLogger.d("人脸库移除成功：$count-----失败：${userBaseForFaces.size - count}")
    }

    override fun loadTeacherFace(teacherList: List<TeacherEntity>?, replace: Boolean) {
        if (teacherList == null) return
        if (replace) {
            faceDetector!!.clearDb()
            faceIdMap.clear()
        }
        for (user: TeacherEntity in teacherList) {
            val student = user.faceDataList?.find { it.type == "2" }
            if (student != null) {
                try {
                    val stVerifyResult =
                        StVerifyResult(
                            JSON.parseObject(
                                student.faceData,
                                FloatArray::class.java
                            )
                        )

                    if (addFace(replace, user.accountId, stVerifyResult)) {
                        StHelper.logI("${user.accountName} 特征入库ok")
                    } else {
                        StHelper.logI("${user.accountName} 特征入库失败")
                    }
                } catch (e: Exception) {
                    failCount++
                    AdlLogger.e("${user.accountName} 特征入库失败 $e")
                }
            } else {
                failCount++
                AdlLogger.e("${user.accountName} 特征入库失败,特征空")
            }
        }
        StHelper.logI("老师特征入库完成**成功:${secCount}***失败：${failCount}")
        failCount = 0
        secCount = 0
    }

    override fun removeTeacherFace(userBaseForFaces: List<TeacherEntity>) {
        var count = 0
        userBaseForFaces.forEach { stu ->
            faceIdMap.entries.find { it.value == stu.accountId }?.key?.let {
                if (faceDetector?.deleteDb(it) == true) {
                    count++
                }
                faceIdMap.remove(it)

            }
        }
        AdlLogger.d("人脸库移除成功：$count-----失败：${userBaseForFaces.size - count}")
    }

    override fun detectFace(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int
    ) {
        if (!isInitOk) {
            AdlLogger.d("ERROR: 人脸未初始化")
            return
        }
        if (yuv == null) {
            AdlLogger.d("ERROR: yuv is null")
            return
        }
        // 检测裁剪矩形
        if (rectF == null || (rectF.left < 0 || rectF.top < 0 || rectF.right > width || rectF.bottom > height)) {
            AdlLogger.d("ERROR : 人脸裁剪区域错误")
            return
        }

        /***************************连续一次识别中，隔三帧检测一次*******************************/
        //重置检测帧数和
        if (System.currentTimeMillis() - preDetectTime > 1000) {
            detectCount = 0
        }

        //帧数检测，连续识别隔3帧检测一次
        if (detectCount % 4 != 0) {
            return
        }
        detectCount++
        preDetectTime = System.currentTimeMillis()
        /***************************连续一次识别中，隔三帧检测一次*******************************/

        //裁剪区矫正,rect太小会导致人脸识别程序崩溃
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
        if (isIsDetecting) return
        isIsDetecting = true
        val startDetectFace = System.currentTimeMillis()
        var bytes = yuv
        try {
            var targetW = width
            var targetH = height
            targetW = rectF.width().toInt().toEven()
            targetH = rectF.height().toInt().toEven()
            val left = rectF.left.toInt().toEven()
            val top = rectF.top.toInt().toEven()
            bytes = AdlYUVUtil.cropNV21(yuv, width, height, left, top, targetW, targetH)

            if (bytes == null) {
                StHelper.logE("警告： 人脸数据为null")
                return
            }
            val jfLivingModel =
                faceDetector!!.detectByYuv(
                    key,
                    bytes,
                    targetW,
                    targetH,
                    null,
                    1.0f,
                    3
                )
            if (jfLivingModel != null) {
                val faceInfo = jfLivingModel.faceInfo
                if (verifyUser(faceInfo.id, jfLivingModel.faceInfo.score).not()) {
                    return
                }
                val id = faceIdMap[faceInfo.id]
                StHelper.logI("识别到底库的FeatureId：$id")
                if (type == BusiBaseConstantsValue.FACE_DETECT_TYPE_STUDENT) {
                    val studentEntity = findUerStrategy.findStudent(id)
                    if (studentEntity != null) {
                        mFaceCallback?.onFindStudent(studentEntity);
                        StHelper.logI("本地数据库中查询到学生：${studentEntity.accountName}")
                    } else {
                        mFaceCallback?.onFindFail(faceInfo)
                        StHelper.logI("本地数据库中未查询到学生：空")
                    }
                } else {
                    val teacherEntity = findUerStrategy.findTeacher(id)
                    if (teacherEntity != null) {
                        mFaceCallback?.onFindTeacher(teacherEntity)
                        StHelper.logI("本地数据库中查询到老师：${teacherEntity.accountName}")
                    } else {
                        StHelper.logI("本地数据库中未查询到老师：空");
                        mFaceCallback?.onFindFail(faceInfo)
                    }
                }
            } else {
                StHelper.logI("jfLivingModel == null null null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            StHelper.logI("人脸识别异常：" + e.message)
        } finally {
            isIsDetecting = false
            val endDetect = System.currentTimeMillis()
            StHelper.logI("人脸识别耗时:" + (endDetect - startDetectFace))
        }
    }

    override fun setFindUserStrategy(strategy: IFindUserStrategy) {
        this.findUerStrategy = strategy
    }

    override fun setFaceCallback(callback: OnFaceCallback?) {
        mFaceCallback = callback
    }

    override fun release() {
        mFaceCallback = null
        isIsDetecting = false
    }

    private fun addFace(
        replace: Boolean,
        accountId: String,
        stVerifyResult: StVerifyResult
    ): Boolean {
        //modify by zhb 已经有featureID，就不再重新生成；在跟新数据的时候，覆盖了已加载的人脸ID，识别时还是给的已加载的ID，导致已加载的人脸，无法识别
        //如果是替换，则直接生成id，否则先判断是否已经有了，不存在才生成id
        var id: Int =
            if (replace) {
                generateUUid(8)
            } else {
                val idKey =
                    faceIdMap.entries.find { it.value == accountId }?.key
                if (idKey != null) {
                    faceDetector?.deleteDb(idKey)
                    idKey
                } else {
                    generateUUid(8)
                }
            }

        if (!faceDetector!!.addDb(id, stVerifyResult)) {
            failCount++
            return false
        }
        //添加特征值库中id和学生库id的映射关系
        faceIdMap[id] = accountId
        secCount++
        return true
    }

    private fun generateUUid(len: Int): Int {
        //延时1毫秒，确保每次的时间戳在毫秒数值上不同
        SystemClock.sleep(1)
        val uuid = (System.currentTimeMillis() % 10.0.pow(len.toDouble())).toInt()
        return abs(uuid)
    }

    /**
     * 校验人员，如果，分数低于0.6的，需要三次连续识别是同一个人，且其中一帧的分要达到一个阈值（如0.5），才认为是识别成功
     */
    private fun verifyUser(faceId: Int, score: Float): Boolean {
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

}