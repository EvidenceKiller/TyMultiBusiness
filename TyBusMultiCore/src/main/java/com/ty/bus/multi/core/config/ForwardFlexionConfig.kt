package com.ty.bus.multi.core.config

import java.io.Serializable

class ForwardFlexionConfig private constructor(
    val isVoiceBroadcastSupported: Boolean,
    val isSupportNextGroup: Boolean,
) : Serializable {

    class Builder {
        private var isVoiceBroadcastSupported: Boolean = false
        private var isSupportNextGroup: Boolean = false

        /**
         * 是否支持语音播报
         */
        fun setVoiceBroadcastSupported(supported: Boolean): Builder {
            this.isVoiceBroadcastSupported = supported
            return this
        }

        /**
         * 是否支持下一组
         */
        fun setSupportNextGroup(support: Boolean): Builder {
            this.isSupportNextGroup = support
            return this
        }

        fun build(): ForwardFlexionConfig {
            return ForwardFlexionConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

}