package com.ty.bus.multi.core.config

import java.io.Serializable

class DoubleSportConfig private constructor(
    val autoFinishTime: Long,
) : Serializable {

    class Builder {
        private var autoFinishTime: Long = 0

        fun setAutoFinishTime(time: Long): Builder {
            this.autoFinishTime = time
            return this
        }

        fun build(): DoubleSportConfig {
            return DoubleSportConfig(
                autoFinishTime
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

}