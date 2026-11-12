package com.ty.bus.multi.core.ui.sport.tensport

import android.app.Application
import android.graphics.Rect
import androidx.core.util.forEach
import com.adl.mount.sport.TsSportChannel
import com.adl.service.entity.SportStartRecordEntity.SportStartAccountDetail
import com.ty.bus.multi.core.base.BaseCollabSportViewModel
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.entity.PipelineFaceIdentifyConfig
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 10人跳绳：复用 [BaseCollabSportViewModel] 协同流程，纯单屏运动（无副屏参与）。
 */
class TenSportViewModel(application: Application) : BaseCollabSportViewModel(application) {

    var tenSportConfig: TenSportConfig? = null

    //十人页为纯单屏运动：不支持协同，不向客户端发送任何Grpc消息，协同层自动退化
    override fun isSupportCollaboration(): Boolean = false

    //获取运动模式
    override fun getSportModel(): SportModel {
        return tenSportConfig?.sportModel ?: SportModel.NORMAL
    }

    override fun isSupportReport(): Boolean {
        return tenSportConfig?.isSupportReport != false
    }

    override fun initFaceIdentify() {
        super.initFaceIdentify()
        //0~9索引对应1~10道
        multiIdentityEngine.addIdentityConfig(0, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(1, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(2, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(3, PipelineFaceIdentifyConfig(2, 0.7f))
        multiIdentityEngine.addIdentityConfig(4, PipelineFaceIdentifyConfig(6))
        multiIdentityEngine.addIdentityConfig(5, PipelineFaceIdentifyConfig(4))
        multiIdentityEngine.addIdentityConfig(6, PipelineFaceIdentifyConfig(3, 0.6f))
        multiIdentityEngine.addIdentityConfig(7, PipelineFaceIdentifyConfig(3, 0.6f))
        multiIdentityEngine.addIdentityConfig(8, PipelineFaceIdentifyConfig(4))
        multiIdentityEngine.addIdentityConfig(9, PipelineFaceIdentifyConfig(6))
    }
    /**
     * 重置流程重建相机并重新登录预览（保持十人页原行为）
     */
    fun reinitCameraAndResume() {
        initCamera()
        onResume()
    }

    /**
     * 十人页保持原行为：运动开始时上传运动开始记录
     */
    override fun onSportStartRecordUpload() {
        val sportDetail = mutableListOf<SportStartAccountDetail>()
        skipPersonArray.forEach { _, multiResult ->
            currentCardState[multiResult.number] = MultiSportCardViewState.SPORTING
            sportDetail.add(SportStartAccountDetail().apply {
                this.accountId = multiResult.accountInfo.studentEntity?.accountId
                this.sportStartTime = startSportTime
            })
        }
        uploadSportStartRecord(sportDetail)
    }

    /**
     * 十人页人脸识别区域：下巴定位优先，边缘道次做水平偏移修正
     */
    override fun getIdentityRect(
        it: TsSportChannel?,
        width: Int,
        height: Int,
        currentIdentifyNumber: Int
    ): Rect {
        if (it?.body == null || it.skeleton == null) {
            LogUtil.d("it.bodyrect  is Null null null null ------", true)
            return Rect(0, 0, 50, 50)
        }
        var rect = Rect(it.body.x, it.body.y, it.body.right(), it.body.bottom())
        val chin = it.skeleton.chin
        val headTop = it.skeleton.headTop
        if (chin != null && headTop != null) {
            val height = (chin.y - headTop.y) * 1.2
            rect.top = headTop.y.toInt()
            rect.bottom = (chin.y + height / 8).toInt()
            rect.left = (headTop.x - height / 2).toInt()
            rect.right = (headTop.x + height / 2).toInt()
            when (currentIdentifyNumber) {
                4 -> {
                    rect.left += (height / 8).toInt()
                    rect.right += (height / 8).toInt()
                }

                9 -> {
                    rect.left -= (height / 8).toInt()
                    rect.right -= (height / 8).toInt()
                }

                0, 5 -> {
                    rect.left += (height / 10).toInt()
                    rect.right += (height / 10).toInt()
                }

                3, 6 -> {
                    rect.left -= (height / 10).toInt()
                    rect.right -= (height / 10).toInt()
                }

            }
        } else {
            if (it.skeleton != null && it.skeleton.LShoulder != null && it.skeleton.RShoulder != null) {
                val shoulderWidth = abs(it.skeleton.LShoulder.x - it.skeleton.RShoulder.x)
                rect.bottom = (if (it.skeleton.chin != null) {
                    it.skeleton.chin.y.toInt()
                } else {
                    max(it.skeleton.LShoulder.y, it.skeleton.RShoulder.y) + shoulderWidth / 8
                }) as Int
                rect.left =
                    max(
                        min(
                            it.skeleton.LShoulder.x,
                            it.skeleton.RShoulder.x
                        ).toInt(), 0
                    )
                rect.right = min(rect.left + (shoulderWidth).toInt(), width)
                rect.top = if (it.skeleton.headTop != null) {
                    it.skeleton.headTop.y.toInt()
                } else {
                    max((rect.bottom - 1.8 * shoulderWidth).toInt(), 0)
                }
            }
        }

        //框体纠正
        if (rect.width() < 40 || rect.height() < 40) {
            rect.left = max(rect.left - 20, 0)
            rect.right = min(rect.left + 40, width)
            rect.top = max(rect.top - 20, 0)
            rect.bottom = min(rect.top + 40, height)
        }
        return rect
    }
}
