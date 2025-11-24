package com.ai.ui.bodytest

import android.graphics.PointF
import com.adl.lib.pose.AdlObject
import com.adl.mount.sport.TsSportRegion
import com.adl.mount.sport.TsUtil
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.tracker.SingleTracker
import com.ty.tybusbase.utils.LogUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PullUpTracker() : SingleTracker(null) {

    //多少帧后，检测不到，就按最初逻辑寻找人框
    private val checkFrameCount = 10

    //当前不合格帧数统计，找到需要清零
    private var countFrame = 0

    private val screenHeight = 1080

    private val maxDistanceRatio = 0.2 //两帧超过此距离认为非正常移动

    private val maxRatioArea = 0.3 //最大面积比不超过此比例

    //宽高比
    private val whRatio = 0.72

    //过滤最小身高为屏幕高的倍数
    private val minHeight = 0.3

    override fun onTracker(list: MutableList<AdlObject>?, region: TsSportRegion?): AdlObject? {
        var newList = if (bodyFilter != null) {
            bodyFilter.onFilter(list, region, score)
        } else {
            TsUtil.multiBodyFilter(list, region, score)
        }

        LogUtil.d("检测框体个数 ${newList.size}")
        //过滤框体比例不合格的框体
        newList = newList.filter {
            it.h > screenHeight * minHeight && (it.w.toFloat() / it.h.toFloat()) < whRatio
        }

        var min = Double.MAX_VALUE
        pList.clear()
        //有框体，但是距离未达到要求
        var hasBodyButInValid = true
        newList.forEach {
            pList.add(PointF(it.centerX().toFloat(), it.bottom().toFloat()))
            val distance = EuHelper.distanceByPoint(
                preCenterBottom.x,
                preCenterBottom.y,
                it.centerX().toFloat(),
                it.bottom().toFloat()
            )
            var areaRatio = 0.2f
            if (lastBody != null) {
                val preArea = lastBody.area().toFloat()
                val curArea = it.area().toFloat()
                areaRatio = abs(preArea / curArea - 1)
            }

            if ((sportStatus != SportStatusEnum.SPORT_START
                        || lastBody == null
                        || distance < maxDistanceRatio * screenHeight
                        || countFrame > checkFrameCount)
                && distance < min
                && areaRatio < maxRatioArea
            ) {
                countFrame = 0
                hasBodyButInValid = false
                min = distance
                lastBody = it
            }
            LogUtil.d("距离 $distance $areaRatio")
        }
        if (hasBodyButInValid) {
            LogUtil.d("距离过大，兼容丢失帧加加 $countFrame")
            countFrame++
            return null
        }
        pointCallback?.prePoint(preCenterBottom)
        if (sportStatus == SportStatusEnum.SPORT_START) {
            preCenterBottom.x = lastBody.centerX().toFloat()
            preCenterBottom.y = lastBody.bottom().toFloat()
        }
        pointCallback?.targetPoint(
            PointF(
                lastBody.centerX().toFloat(),
                lastBody.bottom().toFloat()
            )
        )
        pointCallback?.bodyBottomPoint(pList)
        return lastBody
    }
}
