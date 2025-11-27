package com.ty.tybusbase.base

import com.adl.mount.impl.TsDoubleProcessor

interface IDoubleSportView {
    fun onStatusPrepare() {}
    fun onStatusIdentify()
    fun onStatusIdentifySuccess()
    fun onStatusSport()
    fun onStatusEnd()

    fun createTsDoubleProcessor(): TsDoubleProcessor
    fun initSportEngine()
}