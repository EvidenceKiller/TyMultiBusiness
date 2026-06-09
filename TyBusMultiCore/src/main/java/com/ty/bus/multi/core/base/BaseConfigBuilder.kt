package com.ty.bus.multi.core.base

import com.ty.bus.multi.core.config.DoubleSportConfig.Builder

// 公共的Builder基类
abstract class BaseConfigBuilder<T : BaseSportConfig> {
    protected var isVoiceBroadcastSupported: Boolean = false
    protected var isSupportNextGroup: Boolean = false
    protected var countdownText: String = ""
    protected var sportStartText: String = ""
    protected var resultPageText: String = ""
    protected var autoFinishTime: Long = 0

    fun setVoiceBroadcastSupported(supported: Boolean): BaseConfigBuilder<T> {
        this.isVoiceBroadcastSupported = supported
        return this
    }

    fun setSupportNextGroup(support: Boolean): BaseConfigBuilder<T> {
        this.isSupportNextGroup = support
        return this
    }

    fun setCountdownText(text: String): BaseConfigBuilder<T> {
        this.countdownText = text
        return this
    }

    fun setSportStartText(text: String): BaseConfigBuilder<T> {
        this.sportStartText = text
        return this
    }

    fun setResultPageText(text: String): BaseConfigBuilder<T> {
        this.resultPageText = text
        return this
    }

    fun setAutoFinishTime(time: Long): BaseConfigBuilder<T> {
        this.autoFinishTime = time
        return this
    }

    abstract fun build(): T
}