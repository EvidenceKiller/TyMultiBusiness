package com.ty.bus.multi.core.factory

import com.ty.tybusbase.constants.SportConstant

object MultiSportProduceFactory {

    fun getFactory(itemCode: String?, testCount: String? = null): AbstractMultiSportFactory {
        return when (itemCode) {

            SportConstant.SPORT_SIT_UP -> {
                if (testCount == "30" || testCount == "10") {
                    TenSiteUpFactory()
                } else if (testCount == "6") {
                    SixSiteUpFactory()
                } else {
                    ThreeSiteUpFactory()
                }
            }

            SportConstant.SPORT_PUSH_UP -> {
                ThreePushUpFactory()
            }

            SportConstant.SPORT_SKIP_ROPE -> {
                if (testCount == "5") {
                    FiveSkipRopeFactory()
                } else {
                    TenSkipRopeFactory()
                }
            }

            SportConstant.SPORT_OPEN_CLOSE_JUMP -> {
                if (testCount == "5") {
                    FiveOpenCloseJumpFactory()
                } else {
                    TenOpenCloseJumpFactory()
                }
            }

            SportConstant.SPORT_SQUATS -> {
                if (testCount == "5") {
                    FiveSportSquatsFactory()
                } else {
                    TenSportSquatsFactory()
                }
            }

            SportConstant.SPORT_HIGH_KNEE -> {
                TenHighKneesFactory()
            }

            SportConstant.SPORT_LEFT_RIGHT_ROPE -> {
                TenSideToSideJumpFactory()
            }

            else -> FiveSportSquatsFactory()

        }
    }

}