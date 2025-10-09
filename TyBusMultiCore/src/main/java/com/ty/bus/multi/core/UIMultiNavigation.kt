package com.ty.bus.multi.core

import android.content.Context
import android.content.Intent
import com.ty.bus.multi.core.ui.set.multiregionset.MultiRegionSettingActivity
import com.ty.bus.multi.core.ui.sport.tensport.TenSportActivity
import com.ty.bus.multi.core.ui.set.forwardflexionset.ForwardFlexionSettingActivity
import com.ty.tybusbase.config.UINavConfig

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
object UIMultiNavigation {
    @JvmStatic
    fun goTenSportRope(context: Context, config: UINavConfig?) {
        // TODO 导航跳转
        val intent = Intent(context, TenSportActivity::class.java)
        intent.putExtra("sport_info", config?.sportInfo)
        intent.putExtra("match_id", config?.matchId)
        intent.putExtra("scene_id", config?.sceneId)
        intent.putExtra("account_info", config?.accountInfo)
        intent.putExtra("match_id", config?.matchId)
        intent.putExtra("model_id", config?.modelId)
        intent.putExtra("match_name", config?.matchName)
        intent.putExtra("match_sport_id", config?.matchSportId)
        context.startActivity(intent)
    }

    @JvmStatic
    fun goFiveSkipRope(context: Context?, config: UINavConfig?) {
        // TODO 导航跳转
    }

    @JvmStatic
    fun goThreeSitReach(context: Context?, config: UINavConfig?) {
        // TODO 导航跳转
    }

    fun goMultiRegionSetting(context: Context, number: Int) {
        // TODO 导航跳转
        context.startActivity(Intent(context, MultiRegionSettingActivity::class.java).apply {
            putExtra("test_count", number)
        })
    }

    fun goForwardFelxionSetting(context: Context) {
        // TODO 跳转
        context.startActivity(Intent(context, ForwardFlexionSettingActivity::class.java))
    }
}
