package com.ty.bus.multi.core.config

import com.adl.service.entity.SceneSportInfoEntity
import java.io.Serializable

class BluetoothSportConfig private constructor(
    val isVoiceBroadcastSupported: Boolean,
    val isSupportNextGroup: Boolean,
    val isSupportChoose: Boolean,
    val externalSportList: List<SceneSportInfoEntity>?
) : Serializable {

    class Builder {
        private var isVoiceBroadcastSupported: Boolean = false
        private var isSupportNextGroup: Boolean = false
        private var isSupportChoose: Boolean = false
        private var externalSportList: List<SceneSportInfoEntity>? = null
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

        fun setExternalSportList(list: List<SceneSportInfoEntity>?): Builder {
            this.externalSportList = list
            return this
        }

        fun build(): BluetoothSportConfig {
            return BluetoothSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                isSupportChoose,
                externalSportList
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

}