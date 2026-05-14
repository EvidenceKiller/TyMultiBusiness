package com.ty.bus.multi.core.config

import java.io.Serializable

class TenSportConfig private constructor(
    val isVoiceBroadcastSupported: Boolean,
    val isSupportNextGroup: Boolean,
    val countdownText: String,
    val sportStartText: String,
    val resultPageText: String,
    val isSupportReport: Boolean,
    val autoFinishTime: Long,
    val sportModel: SportModel
) : Serializable {

    class Builder {
        private var isVoiceBroadcastSupported: Boolean = false
        private var isSupportNextGroup: Boolean = false
        private var countdownText: String = ""
        private var gameStartText: String = ""
        private var resultPageText: String = ""
        private var isOpenReport: Boolean = false
        private var autoFinishTime: Long = 0
        private var sportModel: SportModel = SportModel.NORMAL

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
         * 人脸识别后倒计时文案,前缀
         */
        fun setCountdownText(text: String): Builder {
            this.countdownText = text
            return this
        }

        /**
         * 人脸识别后开始比赛文案
         */
        fun setSportStartedText(text: String): Builder {
            this.gameStartText = text
            return this
        }

        /**
         * 成绩页文案
         */
        fun setResultPageText(text: String): Builder {
            this.resultPageText = text
            return this
        }

        /**
         * 是否开启报告
         */
        fun setSupportReport(open: Boolean): Builder {
            this.isOpenReport = open
            return this
        }

        fun setAutoFinishTime(time: Long): Builder {
            this.autoFinishTime = time
            return this
        }

        fun setSportModel(model: SportModel): Builder {
            this.sportModel = model
            return this
        }

        fun build(): TenSportConfig {
            return TenSportConfig(
                isVoiceBroadcastSupported,
                isSupportNextGroup,
                countdownText,
                gameStartText,
                resultPageText,
                isOpenReport,
                autoFinishTime,
                sportModel
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

}

