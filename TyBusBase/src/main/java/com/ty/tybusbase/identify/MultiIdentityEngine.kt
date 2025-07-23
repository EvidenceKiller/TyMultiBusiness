package com.ty.tybusbase.identify

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import com.adl.base.log.AdlLogger
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.adl.service.outer.AdlService
import com.adl.service.outer.UserCaller
import com.adl.sport.general.EuHelper
import com.aipower.modulecommon.expand.getString
import com.ty.identifty.stface.TyFaceInfo
import com.ty.tybusbase.BuildConfig
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.ConstantsValue
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.face.StFaceHelper
import com.ty.tybusbase.helper.SportHelper
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors


class MultiIdentityEngine {

    private var identityListener: IdentifyMachineListener? = null

    /**
     * 开始识别时间
     */
    private var identityStartTime: Long = 0

    /**
     * 游客模式
     */
    var visitorAble = true

    private val VISITOR_TIME = 1 * 1000

    private val VISITOR_RETRY_COUNT = 2

    private val identifyMap by lazy { HashMap<Int, Int>() }

    private var isOK = false
    fun identity(user: StudentEntity, accountId: String?) {
        val accountInfo = AccountInfo()
        accountInfo.classId = user.classId
        accountInfo.className = user.className
        accountInfo.grade = user.gradeNum.toInt()
//        accountInfo.period = user.period
        accountInfo.sex = user.userSex.toInt()
        accountInfo.id = accountId!!
        accountInfo.userImg = AdlService.getService().facePrefix + user.userFaceImgUrl
        accountInfo.userName = user.accountName
        identityFinish(accountInfo)

    }

    private fun identityFinish(accountInfo: AccountInfo) {
        isOK = true
        identityListener?.onIdentityOk(accountInfo)
    }

    fun openMachine(listener: IdentifyMachineListener? = null) {
        this.identityListener = listener
        initFaceDetect()
    }

    private fun initFaceDetect() {
        StFaceHelper.setFaceCallback(object : StFaceHelper.OnFaceCallback {
            override fun onFindUser(user: StudentEntity?) {
                user?.let {
                    identity(it, it.accountId)
                }
            }

            override fun onFindTeacher(user: TeacherEntity?) {

            }

            override fun onFaceFeature(info: TyFaceInfo<*>?) {
            }
        })
    }

    /**
     * 查询用户基本信息
     */
    fun getAccountInfoWithAccount(accountId: String?) {
        val studentEntity = UserCaller.instance().queryStudentByAccountId(accountId)
        studentEntity?.let { identity(it, accountId) }
    }


    fun identify(poseSkeleton: AdlPoseSkeleton, yuv: ByteArray, w: Int, h: Int) {
        doIdentity(poseSkeleton, yuv, w, h)
    }

    private val executor = Executors.newFixedThreadPool(2, Executors.defaultThreadFactory())
    fun identify(rectF: Rect, data: ByteArray?, w: Int, h: Int, number: Int = 0) {
        //已识别完成，不往下走了
        if (isOK) return
        if (identityStartTime == 0L) {
            identityStartTime = System.currentTimeMillis()
        }
        val faceRecTime: Long = System.currentTimeMillis() - identityStartTime


        //当没有正在识别人脸和达到超时要求就判断为游客
        if (!StFaceHelper.isIsDetecting && faceRecTime >= VISITOR_TIME) {
            identifyMap[number] = identifyMap.getOrDefault(number, 0) + 1
            if (visitorAble && identifyMap.getOrDefault(number, 0) >= VISITOR_RETRY_COUNT) {
                createVisitor()
            } else {
                finishWithFail()
            }
            return
        }
        //        bodyInfo = body;
        if (!StFaceHelper.isIsDetecting) {
            if (data == null) return
            val rect = RectF(
                rectF.left.toFloat(),
                rectF.top.toFloat(),
                rectF.right.toFloat(),
                rectF.bottom.toFloat()
            )
            StFaceHelper.callFaceDetector(
                System.currentTimeMillis().toString(),
                data,
                w,
                h,
                rect,
                savePic = BuildConfig.BUILD_TYPE != "release"
            )
//            executor.execute {
//                val bitmap = AdlYUVUtil.yuvToBitmap(data, w, h)
//                if (bitmap != null) {
//                    val createBitmap = Bitmap.createBitmap(
//                        bitmap,
//                        rect.left.toInt(),
//                        rect.top.toInt(),
//                        rect.width().toInt(),
//                        rect.height().toInt()
//                    )
//                    saveBitmap(createBitmap, "${System.currentTimeMillis()}-${number}.jpg")
//                }
//
//            }
        }
    }

    private val bitmapDic = "/sdcard/zy.sport.cache/pic"
    fun saveBitmap(bm: Bitmap, name: String): Boolean {
        try {
            val fileDic = File(bitmapDic)
            if (!fileDic.exists()) {
                fileDic.mkdirs()
            }
            val out = FileOutputStream(File(fileDic, name))
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
        }
        return false
    }

    private fun doIdentity(skeleton: AdlPoseSkeleton, data: ByteArray?, w: Int, h: Int) {
        //已识别完成，不往下走了
        if (isOK) return

        if (identityStartTime == 0L) {
            identityStartTime = System.currentTimeMillis()
        }
        if (skeleton == null) {
            AdlLogger.d("MultiIdentityEngine skeleton = null")
            return
        }
        val faceRecTime: Long = System.currentTimeMillis() - identityStartTime
        //当没有正在识别人脸和达到超时要求就判断为游客
        if (!StFaceHelper.isIsDetecting && faceRecTime >= VISITOR_TIME) {
            if (visitorAble) {
                createVisitor()
            } else {
                finishWithFail()
            }
            return
        }
        //        bodyInfo = body;
        if (!StFaceHelper.isIsDetecting) {
            if (data == null) return
            faceDetect(System.currentTimeMillis().toString() + "", data, w, h, skeleton)
        }
    }


    private fun faceDetect(
        imageId: String,
        yuv: ByteArray,
        w: Int,
        h: Int,
        skeleton: AdlPoseSkeleton
    ) {
        if (w > 0 && h > 0) {

            // YUV数据裁剪
            val vlEar = skeleton.lEar
            val vrEar = skeleton.rEar
            val vNose = skeleton.nose
            var lShoulder = skeleton.lShoulder
            var rShoulder = skeleton.rShoulder
            var rectF: RectF? = null
            if (vlEar != null && vrEar != null && vNose != null) {
                // 以鼻子为圆心，眼距2倍为半径
                val centerX = vNose.getX().toInt()
                val centerY = vNose.getY().toInt()
                val radius = (Math.abs(EuHelper.readX(vlEar) - EuHelper.readX(vrEar)) * 2f).toInt()
                val l = Math.max(centerX - radius / 2, 0).toFloat()
                val t = Math.max(centerY - radius / 2, 0).toFloat()
                val fw = Math.min(l + radius, w.toFloat())
                val fh = Math.min(t + radius, h.toFloat())
                rectF = RectF(l, t, fw, fh)
            } else if (lShoulder != null && rShoulder != null) {
                //头部裁剪框，以肩膀向上1.5倍肩宽
                val height = (SportHelper.getDistance(lShoulder, rShoulder) * 1.5f).toFloat()
                val l = Math.max(rShoulder.x, 0f)
                val t = Math.max((lShoulder.y + rShoulder.y) / 2 - height, 0f)
                val r = Math.min(lShoulder.x, w.toFloat())
                val b = Math.min((lShoulder.y + rShoulder.y) / 2, h.toFloat())
                rectF = RectF(l, t, r, b)
            }
            if (rectF == null) return
            StFaceHelper.callFaceDetector(imageId, yuv, w, h, rectF)
        }
    }

    private fun finishWithFail() {
//        if (identityListener != null && !identityOk) {
//            identityOk = true
//            if (acProgressView.getVisibility() == View.VISIBLE) {
//                SpeechWrapper.speech2(
//                    getContext().getString(R.string.rec_face_fail)
//                ) { acProgressView.post(Runnable { reset() }) }
//            }
//            acProgressView.post(Runnable { acProgressView.setVisibility(View.GONE) })
//        }
        isOK = true
        identityListener?.onIdentityTimeFail()
    }

    private fun createVisitor() {
        val accountInfo = AccountInfo()
        accountInfo.id = ConstantsValue.VISITOR_USER_ID
        accountInfo.userImg = ""
        accountInfo.userName = getString(R.string.visitor)
        identityFinish(accountInfo)
        isOK = true
    }

    fun closeMachine() {
        identityListener = null
        StFaceHelper.setFaceCallback(null)
    }

    /**
     * 是否正在识别
     */
    fun isIdentifying() = StFaceHelper.isIsDetecting

    fun reset() {
        isOK = false
        identityStartTime = 0
    }
}