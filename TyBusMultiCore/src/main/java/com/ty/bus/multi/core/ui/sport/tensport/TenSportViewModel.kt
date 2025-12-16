package com.ty.bus.multi.core.ui.sport.tensport

import android.app.Application
import android.graphics.Rect
import android.net.Uri
import com.adl.mount.core.TsTaskBodyDefaultFilter
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseIPCMultiSportViewModel
import com.ty.tybusbase.utils.ApplicationUtil
import androidx.core.net.toUri
import com.adl.base.log.AdlLogger
import com.adl.mount.sport.TsSportChannel
import com.adl.sport.general.RegionFilter
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TenSportViewModel(application: Application) : BaseIPCMultiSportViewModel(application) {

    var tenSportConfig: TenSportConfig? = null
    override fun getDefaultCountDownTime(): Long = 15

    override fun getCountDownBgmUri(): Uri =
        ("android.resource://" + ApplicationUtil.getContext().packageName + "/" + R.raw.bgm_15_prepare).toUri()

    override fun getSportResolution(): Byte = 70

    override fun getCameraSize(): Pair<Int, Int> = 2560 to 1440

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
                when (currentIdentifyNumber) {
                    0, 1, 2, 3, 6, 7 -> {

                    }

                    5 -> {

                    }

                    8 -> {

                    }

                    4 -> {

                    }

                    9 -> {

                    }

                    else -> {
                        0
                    }
                } as Int
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

    override fun initFaceIdentify() {
        super.initFaceIdentify()
        multiIdentityEngine.addIdentityCount(4, 6)
        multiIdentityEngine.addIdentityCount(9, 6)
        multiIdentityEngine.addIdentityCount(5, 4)
        multiIdentityEngine.addIdentityCount(8, 4)
    }
}