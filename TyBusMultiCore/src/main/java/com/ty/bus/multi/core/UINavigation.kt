package com.ty.bus.multi.core

import android.content.Context
import android.content.Intent
import com.ty.bus.multi.core.ui.multiregionset.MultiRegionSettingActivity
import com.ty.bus.multi.core.ui.tensport.TenSportActivity

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
object UINavigation {
    @JvmStatic
    fun goTenSportRope(context: Context, config: UINavConfig?) {
        // TODO 导航跳转
        context.startActivity(Intent(context, TenSportActivity::class.java))
    }

    @JvmStatic
    fun goFiveSkipRope(context: Context?, config: UINavConfig?) {
        // TODO 导航跳转
    }

    @JvmStatic
    fun goThreeSitReach(context: Context?, config: UINavConfig?) {
        // TODO 导航跳转
    }

    fun goMultiRegionSetting(context: Context,number:Int) {
        // TODO 导航跳转
        context.startActivity(Intent(context, MultiRegionSettingActivity::class.java).apply {
            putExtra("test_count",number)
        })
    }
}
