package com.ty.bus.multi.core.base

interface IMultiSportView {
    fun onStatusPrepare() {}
    fun onStatusIdentify()
    fun onStatusIdentifySuccess()
    fun onStatusSport()
    fun onStatusEnd()
}