package com.ai.ui.facetest2

import android.graphics.Rect
import com.adl.base.bean.ViewTransform
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.service.entity.StudentEntity
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyFaceInfo
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.face.OnFaceCallback
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.ToastUtil
import com.yuv.tool.YuvTool
import com.zy.sport.core.SportCameraYuv
import com.zy.sport.core.SportConfig
import com.zy.sport.core.SportLifecycle

//  人脸识别
class SportFaceProcessor : SportCameraYuv("人脸识别"), SportLifecycle {

    // 显示窗口大小
    private var viewWidth = 1920
    private var viewHeight = 1080

    //每隔多少帧识别一次人脸
    private var frameInterval = 3

    //帧统计
    private var frameCount = frameInterval - 1

    private var mFaceWidth = (viewWidth * 0.25f).toInt()
    private var mFaceHeight = (viewHeight * 0.25f).toInt()
    private var mFaceRect: Rect? = null

    private var mInit = false

    private val mTransform: ViewTransform by lazy {
        ViewTransform(viewWidth, viewHeight, mPreviewWidth, mPreviewHeight)
    }

    fun setDisplaySize(
        viewWidth: Int, viewHeight: Int, faceWidth: Int? = null, faceHeight: Int? = null
    ): SportFaceProcessor {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight
        mFaceWidth = faceWidth ?: (viewWidth * 0.25f).toInt()
        mFaceHeight = faceHeight ?: (viewHeight * 0.25f).toInt()
        return this
    }

    fun centerAlign(cx: Int, cy: Int): SportFaceProcessor {
        var left = cx - mFaceWidth / 2
        var right = cx + mFaceWidth / 2
        var top = cy - mFaceHeight / 2
        var bottom = cy + mFaceHeight / 2

        left = left.coerceAtLeast(0)
        right = right.coerceAtMost(viewWidth)

        top = top.coerceAtLeast(0)
        bottom = bottom.coerceAtMost(viewHeight)

        mFaceRect = Rect(left, top, right, bottom)
        return this
    }

    override fun onYuv(yuv: ByteArray?, info: AdlCameraPreviewInfo?) {
        if (!mInit) return
        if (yuv == null || info == null || info.width <= 0 || info.height <= 0) return

        if (frameCount == Int.MAX_VALUE) {
            frameCount = 0
        }

        frameCount++
        if (frameCount % frameInterval != 0) {
            return
        }

        val imageId = info.imageId
        if (mFaceRect != null) {
            val originalRect = mTransform.toOriginalRect(mFaceRect) ?: return

            val cropWidth = valueTwo(originalRect.width())
            val cropHeight = valueTwo(originalRect.height())
            val cropX = valueTwo(originalRect.left)
            val cropY = valueTwo(originalRect.top)

            // 裁剪区域
            val i420New = YuvTool.convertToI420(
                yuv, mPreviewWidth, mPreviewHeight,
                cropX, cropY, cropWidth, cropHeight, 0, charArrayOf('N', 'V', '2', '1')
            )
            if (i420New == null) {
                d("裁剪失败")
                return
            }
            // i420 to nv21
            val outNv21 = YuvTool.I420ToNV21(i420New, cropWidth, cropHeight)
            // 人脸识别
            FaceDetectorHelper.getInstance().detectFace(
                imageId, outNv21, cropWidth, cropHeight, null, 0
            )
        } else {
            FaceDetectorHelper.getInstance().detectFace(
                imageId, yuv, mPreviewWidth, mPreviewHeight, null, 0
            )
        }
    }

    fun open() {
        FaceDetectorHelper.getInstance().setFaceCallback(object : OnFaceCallback {
            override fun onFindStudent(user: StudentEntity?) {
                LogUtil.d("识别学生：${user?.accountName}")
            }

            override fun onFindUserId(userId: String?, score: Float) {
            }

            override fun onFindFail(info: TyFaceInfo<*>?, type: FailType) {
            }

        })
        mInit = true
    }

    override fun setConfig(config: SportConfig?) {
    }

    override fun reset() {
    }

    @Synchronized
    override fun close() {
        FaceDetectorHelper.getInstance().setFaceCallback(null)
        mFaceRect = null
        mInit = false
    }
}