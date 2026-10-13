package com.ty.tybusbase.face.st

import android.content.Context
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import com.adl.base.file.AdlFileHelper
import com.adl.lib.AdlYUVUtil
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.alibaba.fastjson.JSON
import com.sensetime.sdk.video.module.sdk.common.StVerifyResult
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyLivingModel
import com.ty.identifty.stface.BeforeSenseActiveCallBack
import com.ty.identifty.stface.STFaceDetector
import com.ty.identifty.stface.STFaceFactory
import com.ty.identifty.stface.StContact
import com.ty.identifty.stface.StHelper
import com.ty.identifty.stface.StParameter
import com.ty.tybusbase.expand.toEven
import com.ty.tybusbase.face.FaceDetectorInterface
import com.ty.tybusbase.utils.ApplicationUtil.getContext
import com.ty.tybusbase.utils.LogUtil
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

    private var faceDetector: STFaceDetector? = null

    private val faceIdMap = hashMapOf<Int, String>()

    private var failCount: Long = 0

    private var secCount: Long = 0

    private var faceFeatureThreshold: Float = 0.4f
    override fun setFaceFeatureThreshold(thres: Float) {
        faceFeatureThreshold = thres
        faceDetector?.setFaceFeatureThreshold(faceFeatureThreshold)
    }

    override fun init(context: Context, licenseKey: String?, finish: (Boolean) -> Unit?) {
        if (isInitOk) {
            finish.invoke(true)
            return
        }
        StHelper.setDebugAble(true)
        faceDetector = STFaceFactory.getInstance()
            .detectorCreate((getContext()), object : BeforeSenseActiveCallBack {
                override fun beforeDo(params: StParameter.Params) {
                    val basePath = params.common.basePath
                    LogUtil.d("license basePath:$basePath")
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
            LogUtil.e("商汤人脸创建失败")
            finish.invoke(false)
            return
        }
        faceDetector!!.init(getContext(), null)
        faceDetector!!.setFaceFeatureThreshold(faceFeatureThreshold)
        isInitOk = faceDetector!!.isOk
        finish.invoke(faceDetector!!.isOk)
    }

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
                        Log.d(StContact.LOG_TAG, "${user.accountName} -- aId:${user.accountId} -- uId:${user.userId} 特征入库ok")
                    } else {
                        Log.d(StContact.LOG_TAG, "${user.accountName} -- aId:${user.accountId} -- uId:${user.userId} 特征入库失败")
                    }
                } catch (e: Exception) {
                    failCount++
                    LogUtil.e("${user.accountName} 特征入库失败 $e")
                }
            } else {
                failCount++
                LogUtil.e("${user.accountName} 特征入库失败,特征空")
            }
        }
        LogUtil.d("商汤学生特征入库完成**成功:$secCount***失败：$failCount")
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
        LogUtil.d("人脸库移除成功：$count-----失败：${userBaseForFaces.size - count}")
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
                        Log.d(StContact.LOG_TAG, "${user.accountName} 特征入库ok")
                    } else {
                        Log.d(StContact.LOG_TAG, "${user.accountName} 特征入库失败")
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.d(StContact.LOG_TAG, "${user.accountName} 特征入库失败 $e")
                }
            } else {
                failCount++
                Log.d(StContact.LOG_TAG, "${user.accountName} 特征入库失败,特征空")
            }
        }
        LogUtil.d("商汤老师特征入库完成**成功:${secCount}***失败：${failCount}")
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
        LogUtil.d("人脸库移除成功：$count-----失败：${userBaseForFaces.size - count}")
    }

    override fun getUserId(livingModel: TyLivingModel?): String? {
        if (livingModel == null || livingModel.faceInfo == null) return null
        return faceIdMap[livingModel.faceInfo.id]
    }

    override fun isDetecting(): Boolean = isIsDetecting

    override fun detectFace(
        key: String?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rectF: RectF?,
        type: Int
    ): TyLivingModel? {
        if (!isInitOk) {
            LogUtil.d("ERROR: 人脸未初始化")
            return TyLivingModel().apply {
                failType = FailType.NOT_INIT
                msg = "人脸未初始化"
            }
        }
        if (yuv == null) {
            LogUtil.d("ERROR: yuv is null")
            return TyLivingModel().apply {
                failType = FailType.DATA_ERROR
                msg = "yuv is null"
            }
        }
        // 检测裁剪矩形
        if (rectF == null || (rectF.left < 0 || rectF.top < 0 || rectF.right > width || rectF.bottom > height)) {
            LogUtil.d("ERROR : 人脸裁剪区域错误")
            return TyLivingModel().apply {
                failType = FailType.AREA_ERROR
                msg = "人脸裁剪区域错误"
            }
        }
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
        //校正完，再校验下宽高
        if (rectF.width() > width || rectF.height() > height) {
            return TyLivingModel().apply {
                failType = FailType.AREA_ERROR
                msg = "人脸裁剪区域错误"
            }
        }
        if (isIsDetecting) return TyLivingModel().apply {
            failType = FailType.DETECTING
            msg = "人脸正在检测中..."
        }
        isIsDetecting = true
        LogUtil.d("商汤开始检测人脸")
        val startDetectFace = System.currentTimeMillis()
        var bytes = yuv
        var tyLivingModel: TyLivingModel? = null
        try {
            var targetW = width
            var targetH = height
            targetW = rectF.width().toInt().toEven()
            targetH = rectF.height().toInt().toEven()
            val left = rectF.left.toInt().toEven()
            val top = rectF.top.toInt().toEven()
            bytes = AdlYUVUtil.cropNV21(yuv, width, height, left, top, targetW, targetH)

            if (bytes == null || bytes.size != targetW * targetH * 3 / 2) {
                LogUtil.d("警告： 人脸数据为null")
                return TyLivingModel().apply {
                    failType = FailType.DATA_ERROR
                    msg = "裁剪出的数据为空"
                }
            }
            tyLivingModel =
                faceDetector!!.detectByYuv(
                    key,
                    bytes,
                    targetW,
                    targetH,
                    null,
                    1.0f,
                    3
                )
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.e("人脸识别异常：" + e.message)
            tyLivingModel = TyLivingModel().apply {
                failType = FailType.EXCEPTION
                msg = e.message
            }
        } finally {
            isIsDetecting = false
            val endDetect = System.currentTimeMillis()
            LogUtil.d("商汤结束人脸检测，人脸识别耗时:" + (endDetect - startDetectFace))
        }
        return tyLivingModel
    }

    override fun release() {
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

}