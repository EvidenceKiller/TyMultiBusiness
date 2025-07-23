package com.ty.bus.multi.core.factory

import com.adl.sport.general.groupcrunch.GroupCrunchParser
import com.adl.sport.general.groupjumpingjack.GroupJumpingJackParser
import com.adl.sport.general.grouppushup.GroupPushUpParser
import com.adl.sport.general.groupskippingremix.GroupSkippingRemixParser
import com.adl.sport.general.groupsquat.GroupSquatParser
import com.adl.sport.general.multi.MultiSportParser
import com.ty.tybusbase.constants.SportConstant

object MultiSportParserFactory {
    fun createParser(itemCode:String?): MultiSportParser<*> {
        return when(itemCode){
            SportConstant.SPORT_SKIP_ROPE ->{
                GroupSkippingRemixParser()
            }

            SportConstant.SPORT_OPEN_CLOSE_JUMP ->{
                GroupJumpingJackParser()
            }

            SportConstant.SPORT_SQUATS ->{
                GroupSquatParser()
            }

            SportConstant.SPORT_SIT_UP ->{
                GroupCrunchParser()
            }

            SportConstant.SPORT_PUSH_UP ->{
                GroupPushUpParser()
            }



            else -> GroupSkippingRemixParser()
        }
    }
}