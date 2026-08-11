package com.ty.tybusbase.identify

import android.graphics.Rect
import android.graphics.RectF
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyFaceInfo
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.entity.PipelineIdentifyInfo
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.face.OnFaceCallback
import com.ty.tybusbase.face.strategy.IVerifyStrategy
import com.ty.tybusbase.utils.LogUtil
import org.beetl.core.util.TextUtils

class MultiPipelineIdentityEngine {

    private var identityListener: MultiIdentifyMachineListener? = null

    /**
     * 开始识别时间
     */
    private var identityStartTime: Long = 0

    //当前正在识别的道次
    private var currentIndex = -1

    //最小多少帧开始判断人脸是否识别成功
    private val minFrameCount = 4

    //大于等于此值，直接识别成功
    private val successThresholds = 0.9f

    /**
     * 游客模式
     */
    var visitorAble = true

    private val identifyConfigMap by lazy { HashMap<Int, PipelineFaceIdentifyConfig>() } //每个位置单独设置的识别配置

    private val identifiedMap by lazy { HashMap<Int, PipelineIdentifyInfo>() } //记录每个位置已识别的信息

    private val verifyStrategy = FaceDetectorHelper.getInstance().getVerifyStrategy()


    private fun identityFinish(index: Int, user: StudentEntity) {
        identityListener?.onIdentityOk(index, AccountInfo().apply {
            studentEntity =
                user.apply { userFaceImgUrl = AdlService.getService().facePrefix + userFaceImgUrl }
        })
    }

    fun openMachine(listener: MultiIdentifyMachineListener? = null) {
        this.identityListener = listener
        initFaceDetect()
    }

    private fun initFaceDetect() {
        FaceDetectorHelper.getInstance()
            .setFaceFeatureThreshold(BusiBaseConstantsValue.DEFAULT_MULTI_FACE_THRESHOLD)
        FaceDetectorHelper.getInstance().setVerifyStrategy(object : IVerifyStrategy {
            override fun verifyUser(faceId: String, score: Float): Boolean {
                return false
            }

            override fun reset() {
            }
        })
        FaceDetectorHelper.getInstance().setFaceCallback(object : OnFaceCallback {
            override fun onFindStudent(user: StudentEntity?) {
            }

            override fun onFindFail(info: TyFaceInfo<*>?, type: FailType) {
                identityListener?.onIdentityTimeFail(currentIndex)
            }

            override fun onFindUserId(userId: String?, score: Float) {
                verifyUserId(currentIndex, userId, score)
            }

        })
    }

    /**
     * 判断人脸识别是否成功
     */
    private fun verifyUserId(index: Int, userId: String?, score: Float) {
//        if (TextUtils.isEmpty(userId)) return
        val identifyInfo = identifiedMap.getOrDefault(index, null)
            ?: PipelineIdentifyInfo().apply { identifiedMap.put(index, this) }
        //已识别成功
        if (identifyInfo.isOk) return
        //如果阈值大于设定的靠谱值，直接返回成功
        if (score >= successThresholds) {
            identifyInfo.isOk = true
            val studentEntity =
                AdlService.getService().studentDao.queryStudentByAccountId(userId)
            if (studentEntity != null) {
                identityFinish(index, studentEntity)
            }
            return
        }
        identifyInfo.identifiedList.add(userId to score)
        val config = identifyConfigMap.getOrDefault(index, PipelineFaceIdentifyConfig())
        //累计帧数达到，判断帧总数，计算是否识别成功
        if (identifyInfo.identifiedList.size >= minFrameCount) {
            val group = identifyInfo.identifiedList.groupBy { it.first }
            var matchUserId: String? = null
            for ((uid, list) in group) {
                // 同一userId数量超过4
                if (list.size >= 4) {
                    LogUtil.d("开始计算人脸识别结果,道次：$index")
                    // 分数累计2次大于等于config.minScoreThreshold
                    val highScoreCount = list.count { it.second >= config.minScoreThreshold }
                    if (highScoreCount >= 2) {
                        matchUserId = uid
                        break
                    }
                }
            }
            if (matchUserId != null) {
                identifyInfo.isOk = true
                val studentEntity =
                    AdlService.getService().studentDao.queryStudentByAccountId(userId)
                if (studentEntity != null) {
                    identityFinish(index, studentEntity)
                }
            }
        }
        // 没有识别到人，累计识别帧数超过阈值，判断为游客
        identifyInfo.frameCount++
        if (!identifyInfo.isOk && identifyInfo.frameCount >= config.visitorRoundCount * config.verifyRoundCount) {
            //如果支持游客
            if (visitorAble) {
                identifyInfo.isOk = true
                createVisitor(index)
            } else {
                identifyInfo.frameCount = 0
            }
        }
        //一轮次清零累计
        if (identifyInfo.identifiedList.size >= config.verifyRoundCount) {
            identifyInfo.identifiedList.clear()
        }
    }


    /**
     * 添加每个位置人脸识别配置
     */
    fun addIdentityConfig(number: Int, config: PipelineFaceIdentifyConfig) {
        identifyConfigMap[number] = config
    }

    /**
     * 设置每个位置人脸识别配置
     */
    fun setIdentityConfigMap(map: HashMap<Int, PipelineFaceIdentifyConfig>) {
        identifyConfigMap.clear()
        identifyConfigMap.putAll(map)
    }

    fun identify(rectF: Rect?, data: ByteArray?, w: Int, h: Int, index: Int = 0) {
        //已识别完成，不往下走了
        if (rectF == null) {
            LogUtil.d("裁剪框为空，人脸识别失败")
            return
        }
        if (identityStartTime == 0L) {
            identityStartTime = System.currentTimeMillis()
        }
        if (!FaceDetectorHelper.getInstance().isDetecting()) {
            if (data == null) return
            currentIndex = index
            LogUtil.d("开始识别人脸：索引：$currentIndex")
            val rect = RectF(
                rectF.left.toFloat(),
                rectF.top.toFloat(),
                rectF.right.toFloat(),
                rectF.bottom.toFloat()
            )
            FaceDetectorHelper.getInstance().detectFace(
                System.currentTimeMillis().toString(),
                data,
                w,
                h,
                rect
            )
        }
    }


    private fun finishWithFail() {
        identityListener?.onIdentityTimeFail(0)
    }

    private fun createVisitor(index: Int) {
        val studentEntity = StudentEntity().apply {
            accountId = BusiBaseConstantsValue.VISITOR_USER_ID
            accountName = getString(R.string.visitor)
        }
        identityFinish(index, studentEntity)
    }

    fun closeMachine() {
        identifiedMap.clear()
        identifyConfigMap.clear()
        identityListener = null
        FaceDetectorHelper.getInstance().setVerifyStrategy(verifyStrategy)
        FaceDetectorHelper.getInstance().reset()
    }

    /**
     * 是否正在识别
     */
    fun isIdentifying() = FaceDetectorHelper.getInstance().isDetecting()

    fun reset() {
        identifiedMap.clear()
    }

}