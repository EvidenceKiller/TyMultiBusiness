package com.ty.tybusbase.face.baidu

import android.content.Context
import android.graphics.RectF
import android.util.Base64
import com.adl.base.log.AdlLogger
import com.adl.lib.AdlYUVUtil
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.ty.identifty.base.TyFaceAuthCallback
import com.ty.identifty.base.TyFaceFeature
import com.ty.identifty.base.TyFaceInfo
import com.ty.identifty.base.TyLivingModel
import com.ty.identifty.bdface.BDFaceDetector
import com.ty.identifty.bdface.BDFaceFactory
import com.ty.identifty.stface.StHelper
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.expand.toEven
import com.ty.tybusbase.face.FaceDetectorInterface
import com.ty.tybusbase.utils.MMKVUtil
import kotlin.math.max
import kotlin.math.min
import kotlin.text.isEmpty

class BaiduFaceDetectorImp : FaceDetectorInterface {

    private var faceBdDetector: BDFaceDetector? = null

    @Volatile
    private var isInitOk = false

    @Volatile
    private var isIsDetecting = false

    override fun init(context: Context, finish: (Boolean) -> Unit?) {
        if (isInitOk) return
        BDFaceFactory.auth(
            context, BusiBaseConstantsValue.FACE_LICENSE_KEY,
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

    override fun loadStudentFace(
        studentList: List<StudentEntity>?,
        replace: Boolean
    ) {
        faceBdDetector ?: return
        //  百度加载人脸
        val bdFeatures: MutableList<TyFaceFeature<Any>> = ArrayList()

        studentList?.forEachIndexed { index, userBase ->
            var feature = userBase.faceDataList?.find { it.type == "1" }?.faceData
            if (feature == null || feature.isEmpty()) return@forEachIndexed
            val jfFeature = TyFaceFeature<Any>()
            jfFeature.userId = userBase.userId
            jfFeature.feature = Base64.decode(feature, Base64.DEFAULT)
            bdFeatures.add(jfFeature)
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
        //  百度加载人脸
        val bdFeatures: MutableList<TyFaceFeature<Any>> = ArrayList()

        teacherList?.forEachIndexed { index, userBase ->
            var feature = userBase.faceDataList?.find { it.type == "1" }?.faceData
            if (feature == null || feature.isEmpty()) return@forEachIndexed
            val jfFeature = TyFaceFeature<Any>()
            jfFeature.userId = userBase.userId
            jfFeature.feature = Base64.decode(feature, Base64.DEFAULT)
            bdFeatures.add(jfFeature)
        }
        faceBdDetector?.loadFace(bdFeatures)
    }

    override fun removeTeacherFace(teacherList: List<TeacherEntity>) {
    }

    override fun getUserId(faceInfo: TyLivingModel?): String? {
        val faceInfo: TyFaceInfo<*> = faceInfo?.faceInfo ?: return null
        var userId: String? = null
        if (faceInfo.extendObject is TyFaceFeature<*>) {
            userId = (faceInfo.extendObject as TyFaceFeature<*>).userId
        }
        return userId
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
            return null
        }
        if (yuv == null) {
            AdlLogger.d("ERROR: yuv is null")
            return null
        }
        // 检测裁剪矩形
        if (rectF == null || (rectF.left < 0 || rectF.top < 0 || rectF.right > width || rectF.bottom > height)) {
            AdlLogger.d("ERROR : 人脸裁剪区域错误")
            return null
        }
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
        if (isIsDetecting) return null
        isIsDetecting = true
        val startDetectFace = System.currentTimeMillis()
        var jfLivingModel: TyLivingModel? = null
        try {
            var targetW = width
            var targetH = height
            targetW = rectF.width().toInt().toEven()
            targetH = rectF.height().toInt().toEven()
            val left = rectF.left.toInt().toEven()
            val top = rectF.top.toInt().toEven()
            var bytes = AdlYUVUtil.cropNV21(yuv, width, height, left, top, targetW, targetH)

            if (bytes == null) {
                StHelper.logE("警告： 人脸数据为null")
                return null
            }
            jfLivingModel =
                faceBdDetector?.detectByYuv(
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
        } finally {
            isIsDetecting = false
            val endDetect = System.currentTimeMillis()
            StHelper.logI("百度人脸识别耗时:" + (endDetect - startDetectFace))
        }
        return jfLivingModel
    }

    override fun release() {
        isIsDetecting = false
    }

}