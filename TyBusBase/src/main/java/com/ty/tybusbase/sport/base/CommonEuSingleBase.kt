package com.ty.tybusbase.sport.base

import android.os.Handler
import android.os.HandlerThread
import com.adl.sport.general.EuSingleBase
import com.ty.tybusbase.sport.bean.DetectorConfig
import com.ty.tybusbase.sport.bean.SportDetectResult
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack

/**
 * @ProjectName: SportBS
 * @Date: 2022/8/10
 * @Desc:
 */
abstract class CommonEuSingleBase(
    var singleEuBaseCallBack: SingleEuBaseCallBack?,
    var config: DetectorConfig? = null
) : EuSingleBase(null) {
    private var handlerThread: HandlerThread? = null

    protected val handler: Handler by lazy { Handler(handlerThread!!.looper) }

    protected val result: SportDetectResult by lazy { SportDetectResult() }

    /**
     * 运动错误
     */
    val SPORT_STATE_ERROR = -1

    /**
     * 运动初始态
     */
    val SPORT_STATE_IDLE = 0

    /**
     * 运动初始态
     */
    val SPORT_STATE_SPORTING = 1


    /**
     * 运动完成态
     */
    val SPORT_STATE_COMPLETE = 100

    /**
     * 运动状态
     */
    protected var state = 0

    private val END = "CP----------------------------------------"

    /**
     * 第一个状态完成时间
     * 如果第一个状态与最后一个状态完成时间过短，超过了正常情况那么就有可能是抖动或者误识别
     */
    protected var oneStateTime: Long = 0

    /**
     * 上一个完成时间
     * 如果本次计数距离上次计数时间过短，远远低于正常水平，那么就可能是错误记录
     */
    protected var upCountTime: Long = 0

    init {
        //初始运动后台处理器
        handlerThread = HandlerThread("detect")
        handlerThread!!.start()
    }

    open fun reset() {
        state = SPORT_STATE_IDLE
        handler.removeCallbacksAndMessages(null)
    }

    open fun stop() {
        handlerThread!!.quit()
    }

    open fun logD(msg: String) {
        // LogUtils.d("CP---->   "+msg);
        d("CP---->$msg")
    }

    override fun version(): String = "V2.0.3.0"

}