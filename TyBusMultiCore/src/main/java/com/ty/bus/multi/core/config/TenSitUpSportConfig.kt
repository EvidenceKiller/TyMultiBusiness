package com.ty.bus.multi.core.config

import com.ty.bus.multi.core.base.BaseConfigBuilder
import com.ty.bus.multi.core.base.BaseSportConfig

class TenSitUpSportConfig private constructor(
    override val isVoiceBroadcastSupported: Boolean,
    override val isSupportNextGroup: Boolean,
    override val autoFinishTime: Long,
) : BaseSportConfig(
    isVoiceBroadcastSupported,
    isSupportNextGroup,
    "",
    "",
    "",
    autoFinishTime,
) {
    class Builder : BaseConfigBuilder<TenSitUpSportConfig>() {
        private var isSupportReport: Boolean = false
        fun setSupportReport(support: Boolean): Builder {
            this.isSupportReport = support
            return this
        }

        override fun build(): TenSitUpSportConfig {
            return TenSitUpSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                autoFinishTime,
            )
        }
    }

    companion object {
        fun builder(block: Builder.() -> Unit): TenSitUpSportConfig {
            val build = Builder()
            build.block()
            return build.build()
        }
    }
}