package com.ty.tybusbase.identify

import com.adl.lib.AdlCameraFrameProcessor
import com.adl.lib.AdlCameraPreviewInfo
import com.adl.mount.sport.TsYuvFrame
import com.ty.tybusbase.utils.LogUtil

class OnceCacheProcessor : AdlCameraFrameProcessor {
    private val mFrameProcessors by lazy { ArrayList<AdlCameraFrameProcessor>() }
    private val yuvData by lazy { TsYuvFrame() }

    override fun onFrame(yuv: ByteArray, info: AdlCameraPreviewInfo) {
        mFrameProcessors.forEach {
            it.onFrame(yuv, info)
        }
        yuvData.copy(yuv, info.width, info.height, info.frameTimestamp)
    }

    fun addFrameProcessor(callback: AdlCameraFrameProcessor) {
        mFrameProcessors.add(callback)
    }

    fun onRelease() {
        mFrameProcessors.clear()
        yuvData.yuv = null
    }

    fun getCurrentYur() = yuvData.yuv

    fun getCurrentYuvData() = yuvData

}