package com.ty.bus.multi.core.config

import com.ty.bus.multi.core.base.BaseConfigBuilder
import com.ty.bus.multi.core.base.BaseSportConfig

class EtTenSportConfig(
    override val isVoiceBroadcastSupported: Boolean,
    override val isSupportNextGroup: Boolean,
    override val countdownText: String,
    override val sportStartText: String,
    override val resultPageText: String,
    override val autoFinishTime: Long,
    val isSupportReport: Boolean,
    val sportModel: SportModel
): BaseSportConfig(
    isVoiceBroadcastSupported,
    isSupportNextGroup,
    countdownText,
    sportStartText,
    resultPageText,
    autoFinishTime,
) {
    class Builder: BaseConfigBuilder<EtTenSportConfig>() {
        private var isSupportReport: Boolean = false
        private var sportModel: SportModel = SportModel.NORMAL

        fun setSupportReport(support: Boolean): Builder {
            this.isSupportReport = support
            return this
        }

        fun setSportModel(model: SportModel): Builder {
            this.sportModel = model
            return this
        }

        override fun build(): EtTenSportConfig {
            return EtTenSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                countdownText,
                sportStartText,
                resultPageText,
                autoFinishTime,
                isSupportReport,
                sportModel
            )
        }
    }

    companion object {
        fun builder(block:Builder.() -> Unit): EtTenSportConfig {
            val build = Builder()
            build.block()
            return build.build()
        }
    }
}
