package com.ty.tybusbase.tracker

import android.graphics.PointF
import com.adl.lib.pose.AdlObject
import com.adl.mount.core.TsBodyTracker
import com.adl.mount.sport.TsSportRegion
import com.adl.mount.sport.TsUtil
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.SportStatusEnum
import com.ty.tybusbase.utils.ApplicationUtil

open class SingleTracker(val pointCallback: PointCallback? = null) : TsBodyTracker() {
    //        private var preRect: Rect = Rect(
//        ContextUtil.getContext()!!.resources.getDimensionPixelOffset(R.dimen.dp_690),
//        ContextUtil.getContext()!!.resources.getDimensionPixelOffset(R.dimen.dp_820),
//        ContextUtil.getContext()!!.resources.getDimensionPixelOffset(R.dimen.dp_1230),
//        ContextUtil.getContext()!!.resources.getDimensionPixelOffset(R.dimen.dp_1000)
//    )
    private var enableDynamicId = true
    protected var sportStatus = SportStatusEnum.IDENTIFY_FACE
    private var initCenterBottom = PointF(
        ApplicationUtil.getContext().resources.getDimensionPixelOffset(R.dimen.dp_960).toFloat(),
        ApplicationUtil.getContext().resources.getDimensionPixelOffset(R.dimen.dp_910).toFloat()
    )
    protected var preCenterBottom = PointF(initCenterBottom.x, initCenterBottom.y)

    val pList = mutableListOf<PointF>()
    override fun onTracker(list: MutableList<AdlObject>?, region: TsSportRegion?): AdlObject? {
        val newList = if (bodyFilter != null) {
            bodyMultiFilter.onFilter(list, region, score)
        } else {
            TsUtil.multiBodyFilter(list, region, score)
        }
        if (newList.isNullOrEmpty()) return null
        var min = Double.MAX_VALUE
        pList.clear()
        newList.forEach {
            pList.add(PointF(it.centerX().toFloat(), it.bottom().toFloat()))
            val distance = EuHelper.distanceByPoint(
                preCenterBottom.x,
                preCenterBottom.y,
                it.centerX().toFloat(),
                it.bottom().toFloat()
            )
            if (distance < min) {
                val smoothFlag = if (lastBody == null) 0 else 1
                min = distance
                lastBody = it
                if (enableSmooth) {
                    lastBody.smoothId = getPersonId()
                    lastBody.smoothFlag = smoothFlag
                } else {
                    lastBody.smoothId = -1
                    lastBody.smoothFlag = -1
                }
            }
        }
        if (sportStatus == SportStatusEnum.SPORT_START) {
            preCenterBottom.x = lastBody.centerX().toFloat()
            preCenterBottom.y = lastBody.bottom().toFloat()
        }
        pointCallback?.prePoint(preCenterBottom)
        pointCallback?.targetPoint(
            PointF(
                lastBody.centerX().toFloat(),
                lastBody.bottom().toFloat()
            )
        )
        pointCallback?.bodyBottomPoint(pList)
        return lastBody
    }

    fun changeStatus(status: SportStatusEnum) {
        sportStatus = status
    }

    /**
     * 设置初始化框框的中心点
     */
    fun setInitPointF(p: PointF) {
        preCenterBottom.x = p.x
        preCenterBottom.y = p.y
    }

    /**
     * 设置初始化框框的中心点
     */
    fun setInitPointF(x: Float, y: Float) {
        preCenterBottom.x = x
        preCenterBottom.y = y
    }

    override fun setEnableDynamicId(enable: Boolean): TsBodyTracker? {
        enableDynamicId = enable
        return super.setEnableDynamicId(enable)
    }

    private fun getPersonId(): Int {
        return if (this.enableDynamicId) ++this.personId else this.personId
    }
}

interface PointCallback {
    fun prePoint(p: PointF)

    fun bodyBottomPoint(pList: MutableList<PointF>)

    fun targetPoint(p: PointF)
}