package com.ty.bus.multi.core

import android.content.Context
import android.content.Intent
import com.ty.bus.multi.core.config.BluetoothSportConfig
import com.ty.bus.multi.core.config.DoubleSportConfig
import com.ty.bus.multi.core.config.EtTenSportConfig
import com.ty.bus.multi.core.config.FiveSportConfig
import com.ty.bus.multi.core.config.ForwardFlexionConfig
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.config.SkipEightSportConfig
import com.ty.bus.multi.core.config.TenSitUpSportConfig
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.config.ThirtySportConfig
import com.ty.bus.multi.core.config.ThreeSportConfig
import com.ty.bus.multi.core.ui.set.bmi.BmiSettingActivity
import com.ty.bus.multi.core.ui.set.multiregionset.MultiRegionSettingActivity
import com.ty.bus.multi.core.ui.set.thirtysportset.ThirtySportSettingActivity
import com.ty.bus.multi.core.ui.sport.tensport.TenSportActivity
import com.ty.bus.multi.core.ui.set.forwardflexionset.ForwardFlexionSettingActivity
import com.ty.bus.multi.core.ui.set.forwardflexionsixset.ForwardFlexionSixSettingActivity
import com.ty.bus.multi.core.ui.set.lung.LungSettingActivity
import com.ty.bus.multi.core.ui.set.threesportsetting.ThreeSportSettingActivity
import com.ty.bus.multi.core.ui.sport.bluetoothsport.BluetoothSportActivity
import com.ty.bus.multi.core.ui.sport.doublegbsport.DoubleGbSportActivity
import com.ty.bus.multi.core.ui.sport.doublesport.DoubleSportActivity
import com.ty.bus.multi.core.ui.sport.ettensport.EtTenSportActivity
import com.ty.bus.multi.core.ui.sport.fivesport.FiveSportActivity
import com.ty.bus.multi.core.ui.sport.forwardfelxion.ForwardFlexionActivity
import com.ty.bus.multi.core.ui.sport.forwardflexionsix.ForwardFlexionSixActivity
import com.ty.bus.multi.core.ui.sport.sixsport.SixSportActivity
import com.ty.bus.multi.core.ui.sport.skiplikeeight.SkipLikeEightSportActivity
import com.ty.bus.multi.core.ui.sport.tensitup.TenSitUpSportActivity
import com.ty.bus.multi.core.ui.sport.thirtysport.ThirtySportActivity
import com.ty.bus.multi.core.ui.sport.threesport.ThreeSportActivity
import com.ty.tybusbase.base.BaseNavigation
import com.ty.tybusbase.config.UINavConfig
import com.ty.tybusbase.expand.Action1

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
object UIMultiNavigation : BaseNavigation() {

    @JvmStatic
    fun goEtTenSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: EtTenSportConfig? = null,
        preCallback: Action1<Intent>? = null
    ) {
        val intent = Intent(context, EtTenSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        preCallback?.invoke(intent)
        startActivity(context, intent, config)
    }

    @JvmStatic
    fun goThirtySport(
        context: Context,
        config: UINavConfig?,
        sportConfig: ThirtySportConfig? = null,
        preCallback: Action1<Intent>? = null
    ) {
        val intent = Intent(context, ThirtySportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        preCallback?.invoke(intent)
        startActivity(context, intent, config)
    }

    @JvmStatic
    fun goTenSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: TenSportConfig? = null,
        preCallback: Action1<Intent>? = null
    ) {
        val intent = Intent(context, TenSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        preCallback?.invoke(intent)
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

    /**
     * 主屏 + 左右副屏 10 人运动框体设置
     */
    fun goThirtySportSetting(context: Context) {
        context.startActivity(Intent(context, ThirtySportSettingActivity::class.java))
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
    fun goDoubleSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: DoubleSportConfig? = null
    ) {
        val intent = Intent(context, DoubleSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        startActivity(context, intent, config)
    }

    /**
     * 跳转到双人运动
     */
    fun goDoubleGbSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: DoubleSportConfig? = null
    ) {
        val intent = Intent(context, DoubleGbSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
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

    fun goSkipEightSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: SkipEightSportConfig? = null,
        preCallback: Action1<Intent>? = null
    ) {
        val intent = Intent(context, SkipLikeEightSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        preCallback?.invoke(intent)
        startActivity(context, intent, config)
    }

    /**
     * 十人仰卧起坐
     */
    fun goTenSitUpSport(
        context: Context,
        config: UINavConfig?,
        sportConfig: TenSitUpSportConfig? = null,
        preCallback: Action1<Intent>? = null
    ) {
        val intent = Intent(context, TenSitUpSportActivity::class.java)
        intent.putExtra("sport_config", sportConfig)
        preCallback?.invoke(intent)
        startActivity(context, intent, config)
    }


}
