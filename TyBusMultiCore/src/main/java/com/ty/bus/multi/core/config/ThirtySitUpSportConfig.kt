package com.ty.bus.multi.core.config

import com.ty.bus.multi.core.base.BaseConfigBuilder
import com.ty.bus.multi.core.base.BaseSportConfig

/**
 * 30人仰卧起坐配置：在十人仰卧起坐配置基础上，扩展三屏协同相关配置
 */
class ThirtySitUpSportConfig(
    override val isVoiceBroadcastSupported: Boolean,
    override val isSupportNextGroup: Boolean,
    override val countdownText: String,
    override val sportStartText: String,
    override val resultPageText: String,
    override val autoFinishTime: Long,
    val isServer: Boolean,
    val sportModel: SportModel
) : BaseSportConfig(
    isVoiceBroadcastSupported,
    isSupportNextGroup,
    countdownText,
    sportStartText,
    resultPageText,
    autoFinishTime,
) {
    class Builder : BaseConfigBuilder<ThirtySitUpSportConfig>() {
        private var isServer: Boolean = false
        private var sportModel: SportModel = SportModel.NORMAL

        fun setIsServer(server: Boolean): Builder {
            this.isServer = server
            return this
        }

        fun setSportModel(model: SportModel): Builder {
            this.sportModel = model
            return this
        }

        override fun build(): ThirtySitUpSportConfig {
            return ThirtySitUpSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                countdownText,
                sportStartText,
                resultPageText,
                autoFinishTime,
                isServer,
                sportModel
            )
        }
    }

    companion object {
        fun builder(block: Builder.() -> Unit): ThirtySitUpSportConfig {
            val build = Builder()
            build.block()
            return build.build()
        }
    }
}
