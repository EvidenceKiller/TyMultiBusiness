package com.ty.bus.multi.core.base

import android.app.Application
import android.graphics.Rect
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.identify.MultiIdentifyMachineListener
import com.ty.tybusbase.identify.MultiPipelineIdentityEngine
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil

open class BaseMultiPipelineIdentifyViewModel(application: Application) :
    BaseMultiSportViewModel(application) {
    protected val multiIdentityEngine by lazy { MultiPipelineIdentityEngine() } //人脸识别引擎类

    /**
     * 初始化流水线人脸识别
     */
    open fun initFaceIdentify() {
        /**
         * 人脸识别回调
         */
        var visitor: Boolean =
            MMKVUtil.decodeBoolean(
                MMKVBusiKey.VISITOR_ABLE,
                BusiBaseConstantsValue.DEFAULT_VISITOR_MODULE
            ) && matchId == null
        multiIdentityEngine.visitorAble = visitor
        FaceDetectorHelper.getInstance().setFrameInterval(1)
        multiIdentityEngine.openMachine(object : MultiIdentifyMachineListener {
            override fun onIdentityMachineInit(initResult: Boolean) {

            }

            override fun onIdentityTimeOn() {

            }

            override fun onIdentityOk(
                index: Int,
                accountInfo: AccountInfo
            ) {
                identifyOk(index, accountInfo)
            }

            override fun onIdentityTimeFail(index: Int) {
                identifyFail(index)
            }

        })
    }

    fun identify(rectF: Rect?, data: ByteArray?, w: Int, h: Int, number: Int = 0) {
        multiIdentityEngine.identify(rectF, data, w, h, number)
    }

    fun isIdentifying() = multiIdentityEngine.isIdentifying()

    /**
     * 识别成功，共上层业务逻辑实现
     */
    open fun identifyOk(index: Int, accountInfo: AccountInfo) {
    }

    open fun identifyFail(index: Int) {
    }

    override fun onPause() {
        super.onPause()
        multiIdentityEngine.closeMachine()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}