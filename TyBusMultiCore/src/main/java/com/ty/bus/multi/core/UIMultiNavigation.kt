package com.ty.bus.multi.core

import android.content.Context
import android.content.Intent
import com.ty.bus.multi.core.config.BluetoothSportConfig
import com.ty.bus.multi.core.config.FiveSportConfig
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.config.ThreeSportConfig
import com.ty.bus.multi.core.ui.set.bmi.BmiSettingActivity
import com.ty.bus.multi.core.ui.set.multiregionset.MultiRegionSettingActivity
import com.ty.bus.multi.core.ui.sport.tensport.TenSportActivity
import com.ty.bus.multi.core.ui.set.forwardflexionset.ForwardFlexionSettingActivity
import com.ty.bus.multi.core.ui.set.forwardflexionsixset.ForwardFlexionSixSettingActivity
import com.ty.bus.multi.core.ui.set.lung.LungSettingActivity
import com.ty.bus.multi.core.ui.set.threesportsetting.ThreeSportSettingActivity
import com.ty.bus.multi.core.ui.sport.bluetoothsport.BluetoothSportActivity
import com.ty.bus.multi.core.ui.sport.doublesport.DoubleSportActivity
import com.ty.bus.multi.core.ui.sport.fivesport.FiveSportActivity
import com.ty.bus.multi.core.ui.sport.forwardfelxion.ForwardFlexionActivity
import com.ty.bus.multi.core.ui.sport.forwardflexionsix.ForwardFlexionSixActivity
import com.ty.bus.multi.core.ui.sport.sixsport.SixSportActivity
import com.ty.bus.multi.core.ui.sport.threesport.ThreeSportActivity
import com.ty.tybusbase.base.BaseNavigation
import com.ty.tybusbase.config.UINavConfig

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
object UIMultiNavigation : BaseNavigation() {
    @JvmStatic
    fun goTenSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: TenSportConfig? = null
    ) {
        val intent = Intent(context, TenSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        startActivity(context, intent, config)
    }

    /**
     * 坐位体前屈
     */
    @JvmStatic
    fun goForwardFlexion(
        context: Context,
        config: UINavConfig?,
        ffConfig: ForwardFlexionConfig? = null
    ) {
        val intent = Intent(context, ForwardFlexionActivity::class.java)
        intent.putExtra("sport_config", ffConfig)
        startActivity(context, intent, config)
    }

    @JvmStatic
    fun goForwardFlexionSix(
        context: Context,
        config: UINavConfig?,
        ffConfig: ForwardFlexionConfig? = null
    ) {
        val intent = Intent(context, ForwardFlexionSixActivity::class.java)
        intent.putExtra("sport_config", ffConfig)
        startActivity(context, intent, config)
    }

    @JvmStatic
    fun goFiveSport(context: Context, config: UINavConfig?, sportConfig: FiveSportConfig? = null) {
        val intent = Intent(context, FiveSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        startActivity(context, intent, config)
    }

    @JvmStatic
    fun goThreeSport(context: Context, config: UINavConfig?, tsConfig: ThreeSportConfig? = null) {
        val intent = Intent(context, ThreeSportActivity::class.java)
        intent.putExtra("sport_config", tsConfig)
        startActivity(context, intent, config)
    }

    fun goThreeSportSetting(context: Context) {
        context.startActivity(Intent(context, ThreeSportSettingActivity::class.java))
    }

    fun goMultiRegionSetting(context: Context, number: Int, resolutionType: Int = 0) {
        context.startActivity(Intent(context, MultiRegionSettingActivity::class.java).apply {
            putExtra("test_count", number)
            putExtra("res_type", resolutionType)
        })
    }

    fun goForwardFlexionSetting(context: Context) {
        context.startActivity(Intent(context, ForwardFlexionSettingActivity::class.java))
    }

    /**
     * 6个位置体前屈
     */
    fun goForwardFlexionSixSetting(context: Context) {
        context.startActivity(Intent(context, ForwardFlexionSixSettingActivity::class.java))
    }

    /**
     * 跳转6人运动
     */
    fun goSixSport(context: Context, config: UINavConfig?, tsConfig: SixSportConfig? = null) {
        val intent = Intent(context, SixSportActivity::class.java)
        intent.putExtra("sport_config", tsConfig)
        startActivity(context, intent, config)
    }

    /**
     * 跳转到双人运动
     */
    fun goDoubleSport(context: Context, config: UINavConfig?) {
        val intent = Intent(context, DoubleSportActivity::class.java)
        startActivity(context, intent, config)
    }

    /**
     * 跳转到BMI设置页面
     */
    fun goBmiSetting(context: Context) {
        context.startActivity(Intent(context, BmiSettingActivity::class.java))
    }

    /**
     * 跳转到BMI运动
     */
    fun goBluetoothSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: BluetoothSportConfig?
    ) {
        val intent = Intent(context, BluetoothSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        startActivity(context, intent, config)
    }


    /**
     * 跳转到BMI设置页面
     */
    fun goLungSetting(context: Context) {
        context.startActivity(Intent(context, LungSettingActivity::class.java))
    }

}
