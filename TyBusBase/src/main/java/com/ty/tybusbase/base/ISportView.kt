package com.ty.tybusbase.base

import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.sport.TsSportRegion

interface ISportView {
    fun onStatusPrepare() {}
    fun onStatusIdentify()
    fun onStatusIdentifySuccess()
    fun onStatusSport()
    fun onStatusEnd()
    fun onCheckArea()
    fun onStatusIdentifyMat() {}
    fun createEuBase():TsSingleProcessor
    fun getRegion():TsSportRegion
    fun initSportEngine()
}