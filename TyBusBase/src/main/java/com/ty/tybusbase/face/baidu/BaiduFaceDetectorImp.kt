package com.ty.tybusbase.face.baidu

import android.content.Context
import android.graphics.RectF
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import com.adl.base.log.AdlLogger
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyFaceAuthCallback
import com.ty.identifty.base.TyFaceFeature
import com.ty.identifty.base.TyFaceInfo
import com.ty.identifty.base.TyLivingModel
import com.ty.identifty.bdface.BDFaceDetector
import com.ty.identifty.bdface.BDFaceFactory
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.face.FaceDetectorInterface
import com.ty.tybusbase.utils.MMKVUtil
import kotlin.math.abs
import kotlin.math.pow
import kotlin.text.isEmpty

class BaiduFaceDetectorImp : FaceDetectorInterface {

    private var faceBdDetector: BDFaceDetector? = null

    private val faceIdMap = hashMapOf<Int, String>()

    @Volatile
    private var isInitOk = false

    @Volatile
    private var isIsDetecting = false

    private var faceFeatureThreshold: Float = 0.75f
    override fun init(context: Context, licenseKey: String?, finish: (Boolean) -> Unit?) {
        if (isInitOk) return
        BDFaceFactory.auth(
            context, licenseKey,
            object : TyFaceAuthCallback {
                override fun initLicenseSuccess() {
                    AdlLogger.d("百度人脸授权成功,初始化百度人脸算法")
                    isInitOk = true
                    // 人脸识别对象
                    faceBdDetector = BDFaceDetector()
                    faceBdDetector?.enableDebug(
                        MMKVUtil.decodeBoolean(
                            MMKVBusiKey.MODEL_DEBUG,
                            BusiBaseConstantsValue.DEFAULT_DEBUG_MODULE
                        )
                    )
                    faceBdDetector?.setFaceFeatureThreshold(faceFeatureThreshold)
                    faceBdDetector?.init(context, null)
                    finish.invoke(true)
                }

                override fun initLicenseFail(errorCode: Int, msg: String) {
                    finish.invoke(false)
                    AdlLogger.e("百度人脸初始化失败, code=$errorCode, msg=$msg")
                }
            })
    }

    override fun setFaceFeatureThreshold(thres: Float) {
        faceFeatureThreshold = thres
        faceBdDetector?.setFaceFeatureThreshold(thres)
    }

    override fun loadStudentFace(
        studentList: List<StudentEntity>?,
        replace: Boolean
    ) {
        faceBdDetector ?: return
        if (replace) {
            faceIdMap.clear()
        }
        //  百度加载人脸
        val bdFeatures: MutableList<TyFaceFeature<Any>> = ArrayList()
        studentList?.forEachIndexed { index, userBase ->
            var feature = userBase.faceDataList?.find { it.type == "1" }?.faceData
            if (feature == null || feature.isEmpty()) return@forEachIndexed
            val jfFeature = TyFaceFeature<Any>()
            jfFeature.userId = userBase.accountId
            jfFeature.feature = Base64.decode(feature, Base64.DEFAULT)
            jfFeature.id = generateUUid(8)
            bdFeatures.add(jfFeature)
            faceIdMap[jfFeature.id] = userBase.accountId
        }
        faceBdDetector?.loadFace(bdFeatures)
    }

    override fun removeStudentFace(studentList: List<StudentEntity>) {
    }

    override fun loadTeacherFace(
        teacherList: List<TeacherEntity>?,
        replace: Boolean
    ) {
        faceBdDetector ?: return
        if (replace) {
            faceIdMap.clear()
        }
        //  百度加载人脸
        val bdFeatures: MutableList<TyFaceFeature<Any>> = ArrayList()

        teacherList?.forEachIndexed { index, userBase ->
            var feature = userBase.faceDataList?.find { it.type == "1" }?.faceData
            if (feature == null || feature.isEmpty()) return@forEachIndexed
            val jfFeature = TyFaceFeature<Any>()
            jfFeature.userId = userBase.accountId
            jfFeature.feature = Base64.decode(feature, Base64.DEFAULT)
            jfFeature.id = generateUUid(8)
            bdFeatures.add(jfFeature)
            faceIdMap[jfFeature.id] = userBase.accountId
        }
        faceBdDetector?.loadFace(bdFeatures)
    }

    override fun removeTeacherFace(teacherList: List<TeacherEntity>) {
    }

    override fun getUserId(faceInfo: TyLivingModel?): String? {
        val faceInfo: TyFaceInfo<*> = faceInfo?.faceInfo ?: return null
        return faceIdMap[faceInfo.id]
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
            AdlLogger.d("ERROR: 人脸未初始化")
            return TyLivingModel().apply {
                failType = FailType.NOT_INIT
                msg = "人脸未初始化"
            }
        }
        if (yuv == null) {
            AdlLogger.d("ERROR: yuv is null")
            return TyLivingModel().apply {
                failType = FailType.DATA_ERROR
                msg = "yuv is null"
            }
        }
        if (isIsDetecting) return TyLivingModel().apply {
            failType = FailType.DETECTING
            msg = "人脸正在检测中..."
        }
        isIsDetecting = true
        val startDetectFace = System.currentTimeMillis()
        var tyLivingModel: TyLivingModel? = null
        try {
            tyLivingModel =
                faceBdDetector?.detectByYuv(
                    key,
                    yuv,
                    width,
                    height,
                    null,
                    1.0f,
                    3
                )
        } catch (e: Exception) {
            e.printStackTrace()
            tyLivingModel = TyLivingModel().apply {
                failType = FailType.EXCEPTION
                msg = e.message
            }
        } finally {
            isIsDetecting = false
            val endDetect = System.currentTimeMillis()
            Log.d("BDFace", "百度人脸识别耗时:" + (endDetect - startDetectFace))
        }
        return tyLivingModel
    }

    override fun release() {
        isIsDetecting = false
    }

    private fun generateUUid(len: Int): Int {
        //延时1毫秒，确保每次的时间戳在毫秒数值上不同
        SystemClock.sleep(1)
        val uuid = (System.currentTimeMillis() % 10.0.pow(len.toDouble())).toInt()
        return abs(uuid)
    }
}