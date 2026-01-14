package com.ty.bus.multi.core.config

import java.io.Serializable

class BluetoothSportConfig private constructor(
    val isVoiceBroadcastSupported: Boolean,
    val isSupportNextGroup: Boolean,
    val isSupportChoose: Boolean
) : Serializable {

    class Builder {
        private var isVoiceBroadcastSupported: Boolean = false
        private var isSupportNextGroup: Boolean = false
        private var isSupportChoose: Boolean = false

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

        /**
         * 是否支持手动选择
         */
        fun setSupportChoose(support: Boolean): Builder {
            this.isSupportChoose = support
            return this
        }

        fun build(): BluetoothSportConfig {
            return BluetoothSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                isSupportChoose
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

}