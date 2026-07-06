package com.ty.tybusbase.identify

import com.adl.lib.pose.AdlPoseSkeleton

interface IStartGestureDetection {
    fun startGestureDetection(skeleton: AdlPoseSkeleton?, handType: Int): Boolean
    fun reset()
}