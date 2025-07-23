package com.ty.bus.multi.core.ui.tensport

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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TenSportViewModel(application: Application) : BaseIPCMultiSportViewModel(application) {
    override fun getDefaultCountDownTime(): Long = 15

    override fun getCountDownBgmUri(): Uri =
        ("android.resource://" + ApplicationUtil.getContext().packageName + "/" + R.raw.bgm_15_prepare).toUri()

    override fun getBodyFilter() = TsTaskBodyDefaultFilter(0.1f)

    override fun getSportResolution(): Byte = 70

    override fun getIdentityRect(
        it: TsSportChannel?,
        width: Int,
        height: Int,
        currentIdentifyNumber: Int
    ): Rect {
        if (it?.body == null || it.skeleton == null) {
            AdlLogger.d("it.bodyrect  is Null null null null ------")
            AdlLogger.flushNow()
            return Rect(0, 0, 50, 50)
        }
        var rect = Rect(it.body.x, it.body.y, it.body.right(), it.body.bottom())
        if (it.skeleton != null && it.skeleton.LShoulder != null && it.skeleton.RShoulder != null) {
            val shoulderWidth = abs(it.skeleton.LShoulder.x - it.skeleton.RShoulder.x)
            rect.bottom = max(it.skeleton.LShoulder.y, it.skeleton.RShoulder.y).toInt()
            rect.left =
                max(
                    min(
                        it.skeleton.LShoulder.x,
                        it.skeleton.RShoulder.x
                    ).toInt() - shoulderWidth.div(2).toInt(), 0
                )
            rect.right = min(rect.left + shoulderWidth.times(2).toInt(), width)
            rect.top = max((rect.bottom - 1.8 * shoulderWidth).toInt(), 0)
        }
        if (rect.width() < 40 || rect.height() < 40) {
            rect.left = max(rect.left - 20, 0)
            rect.right = min(rect.left + 40, width)
            rect.top = max(rect.top - 20, 0)
            rect.bottom = min(rect.top + 40, height)
        }
        return rect
    }
}