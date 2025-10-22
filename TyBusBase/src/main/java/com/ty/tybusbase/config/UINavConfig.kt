package com.ty.tybusbase.config

import com.adl.service.entity.SceneSportInfoEntity
import com.ty.tybusbase.entity.AccountInfo
import java.io.Serializable

/**
 * ProjectName: TylMultiBusiness
 * Author     : 南山
 * Date       : 2025/7/18
 * Describe   : 类描述
 */
data class UINavConfig(
    var sportInfo: SceneSportInfoEntity? = null,
    var matchId: String? = null,
    var sceneId: String? = null,
    var accountInfo: AccountInfo? = null,
    var modelId: String? = null,
    var matchName: String? = null,
    var matchSportId: String? = null,
    var ptPlanId: String? = null,
    var ptStandardId: String? = null
) : Serializable