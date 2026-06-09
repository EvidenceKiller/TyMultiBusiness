package com.ty.bus.multi.core.base

import java.io.Serializable

// 定义公共基类
abstract class BaseSportConfig(
    open val isVoiceBroadcastSupported: Boolean,
    open val isSupportNextGroup: Boolean,
    open val countdownText: String,
    open val sportStartText: String,
    open val resultPageText: String,
    open val autoFinishTime: Long,
) : Serializable