package com.ty.tybusbase.base

import android.content.Context
import android.content.Intent
import com.ty.tybusbase.config.UINavConfig

open class BaseNavigation {

    /**
     * 设置传参，并跳转页面
     */
    fun startActivity(context: Context, intent: Intent, config: UINavConfig?) {
        intent.putExtra("sport_info", config?.sportInfo)
        intent.putExtra("match_id", config?.matchId)
        intent.putExtra("scene_id", config?.sceneId)
        intent.putExtra("account_info", config?.accountInfo)
        intent.putExtra("match_id", config?.matchId)
        intent.putExtra("model_id", config?.modelId)
        intent.putExtra("match_name", config?.matchName)
        intent.putExtra("match_sport_id", config?.matchSportId)
        intent.putExtra("pt_plan_id", config?.ptPlanId)
        intent.putExtra("pt_standard_id", config?.ptStandardId)
        intent.putExtra("energy_point", config?.energyPoint)
        context.startActivity(intent)
    }
}