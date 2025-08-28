package com.ty.tybusbase.view

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.IntDef
import com.adl.lib.pose.AdlPoseSkeleton
import com.ty.tybusbase.R
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.constants.SportConstant.SPORT_LOCATION_LEFT
import com.ty.tybusbase.constants.SportConstant.SPORT_LOCATION_RIGHT
import com.ty.tybusbase.constants.SportConstant.SportLocation
import com.ty.tybusbase.databinding.SportAreaViewBinding
import com.ty.tybusbase.utils.DisplayUtil
import java.nio.ByteBuffer

/**
 * @author pc
 */
class SportAreaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var scW = 0f
    private var scH = 0f
    private var sportAi: String? = null

    /**
     * 当前显示状态；0：隐藏；1：显示
     */
    private var currentAreaUi = -1
    private val inAreaUi = 0
    private val outAreaUi = 1

    @IntDef(AREA_TAPE_SINGLE, AREA_TAPE_DOUBLE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SportAreaType

    var buffer: ByteBuffer? = null
    private var sportRect: Rect? = null
    private var leftRect: Rect? = null
    private var rightRect: Rect? = null
    private var areaType = 0

    private lateinit var mBinding: SportAreaViewBinding

    private constructor(context: Context) : this(context, null)

    private fun init(context: Context) {
        mBinding = SportAreaViewBinding.inflate(LayoutInflater.from(context), this)
    }

    /**
     * 调整左右站立区域离中间的位置
     * @param distance
     */
    fun ajustRecAreaDistance(distance: Int) {
        val llparam = mBinding.viewLayoutLeft.layoutParams as LayoutParams
        llparam.rightMargin = -distance
        mBinding.viewLayoutLeft.layoutParams = llparam
        val rlparam = mBinding.viewLayoutRight.layoutParams as LayoutParams
        rlparam.leftMargin = -distance
        mBinding.viewLayoutRight.layoutParams = rlparam
    }

    fun configSportArea(areaType: Int, sportAi: String?) {
        val size = DisplayUtil.getSize(context)
        scW = size.x.toFloat() / BusiBaseConstantsValue.CAMERA_PREVIEW_WIDTH
        scH = size.y.toFloat() / BusiBaseConstantsValue.CAMERA_PREVIEW_HEIGHT
        this.sportAi = sportAi
        this.areaType = areaType
        when (areaType) {
            AREA_TAPE_SINGLE -> {
                mBinding.tvAreaNotice.visibility = VISIBLE
                mBinding.ivAreaNotice.visibility = VISIBLE
                mBinding.viewLayoutLeft.visibility = GONE
                mBinding.viewLayoutRight.visibility = GONE
                sportRect = Rect(
                    resources.getDimensionPixelOffset(R.dimen.dp_690),
                    resources.getDimensionPixelOffset(R.dimen.dp_820),
                    resources.getDimensionPixelOffset(R.dimen.dp_1230),
                    resources.getDimensionPixelOffset(R.dimen.dp_1000)
                )
            }

            AREA_TAPE_DOUBLE -> {
                mBinding.tvAreaNotice.visibility = GONE
                mBinding.ivAreaNotice.visibility = GONE
                mBinding.viewLayoutLeft.visibility = VISIBLE
                mBinding.viewLayoutRight.visibility = VISIBLE
                leftRect = Rect(
                    resources.getDimensionPixelOffset(R.dimen.dp_210),
                    resources.getDimensionPixelOffset(R.dimen.dp_820),
                    resources.getDimensionPixelOffset(R.dimen.dp_750),
                    resources.getDimensionPixelOffset(R.dimen.dp_1000)
                )
                rightRect = Rect(
                    resources.getDimensionPixelOffset(R.dimen.dp_1120),
                    resources.getDimensionPixelOffset(R.dimen.dp_820),
                    resources.getDimensionPixelOffset(R.dimen.dp_1710),
                    resources.getDimensionPixelOffset(R.dimen.dp_1000)
                )
            }
        }
    }

    /**
     * 单个区域检测
     * @param body
     * @return
     */
    fun checkSportAreaByNewWay(skeleton: AdlPoseSkeleton?, location: String): Boolean {
        return if (skeleton == null || TextUtils.isEmpty(sportAi)) {
            false
        } else when (sportAi) {
            SportConstant.SPORT_ONE_LEG_SKIP_ROPE, SportConstant.SPORT_FILL_BLANK_GAME, SportConstant.SPORT_FILL_BLANK_EN_GAME, SportConstant.SPORT_TROT, SportConstant.SPORT_HIGH_KNEE -> {
                //            case SportConstant.SPORT_HIGH_KNEE:
                val b = checkForOneLegByNewWay(skeleton, location)
                if (b) {
                    inAreaUi(location)
                } else {
                    outAreaUi(location)
                }
                b
            }

            else -> {
                val c = checkAnkleAllByNewWay(skeleton, location)
                if (c) {
                    inAreaUi(location)
                } else {
                    outAreaUi(location)
                }
                c
            }
        }
    }

    private fun checkAnkleAllByNewWay(skeleton: AdlPoseSkeleton, location: String): Boolean {
        if (!judegeNullAnkleBodyByNewWay(skeleton)) return false
        if (areaType == AREA_TAPE_SINGLE) {
            if (sportRect!!.contains(skeleton.lAnkle.x.toInt(), skeleton.lAnkle.y.toInt())
                && sportRect!!.contains(
                    skeleton.rAnkle.x.toInt(),
                    skeleton.rAnkle.y.toInt()
                )
            ) {
                return true
            }
        } else {
            if (location == SPORT_LOCATION_LEFT) {
                if (leftRect!!.contains(
                        (skeleton.lAnkle.x * scW).toInt(),
                        (skeleton.lAnkle.y * scH).toInt()
                    )
                    && leftRect!!.contains(
                        (skeleton.rAnkle.x * scW).toInt(),
                        (skeleton.rAnkle.y * scH).toInt()
                    )
                ) {
                    return true
                }
            } else if (location == SPORT_LOCATION_RIGHT) {
                if (rightRect!!.contains(
                        ((skeleton.lAnkle.x + 1920 / 2) * scW).toInt(),
                        (skeleton.lAnkle.y * scH).toInt()
                    )
                    && rightRect!!.contains(
                        ((skeleton.rAnkle.x + 1920 / 2) * scW).toInt(),
                        (skeleton.rAnkle.y * scH).toInt()
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }

    init {
        init(context)
    }

    fun forceSingleInAreaUI() {
        post {
            mBinding.ivAreaNotice.setImageResource(R.mipmap.rect_area)
            mBinding.tvAreaNotice.visibility = INVISIBLE
        }
    }

    private fun inAreaUi(@SportLocation location: String) {
        if (currentAreaUi == inAreaUi) return
        currentAreaUi = inAreaUi
        if (areaType == AREA_TAPE_SINGLE) {
            handler.post {
                mBinding.ivAreaNotice.setImageResource(R.mipmap.rect_area)
                mBinding.tvAreaNotice.visibility = INVISIBLE
            }
        } else {
            post {
                if (location === SPORT_LOCATION_LEFT) {
                    mBinding.ivAreaNoticeLeft.setImageResource(R.mipmap.rect_area)
                    mBinding.tvAreaNoticeLeft.visibility = INVISIBLE
                } else {
                    mBinding.ivAreaNoticeRight.setImageResource(R.mipmap.rect_area)
                    mBinding.tvAreaNoticeRight.visibility = INVISIBLE
                }
            }
        }
    }

    private fun outAreaUi(@SportLocation location: String) {
        if (currentAreaUi == outAreaUi) return
        currentAreaUi = outAreaUi
        if (areaType == AREA_TAPE_SINGLE) {
            handler.post {
                mBinding.ivAreaNotice.setImageResource(R.mipmap.bg_foot_area)
                mBinding.tvAreaNotice.visibility = VISIBLE
            }
        } else {
            handler.post {
                if (location === SPORT_LOCATION_LEFT) {
                    mBinding.ivAreaNoticeLeft.setImageResource(R.mipmap.bg_foot_area)
                    mBinding.tvAreaNoticeLeft.visibility = VISIBLE
                } else {
                    mBinding.ivAreaNoticeRight.setImageResource(R.mipmap.bg_foot_area)
                    mBinding.tvAreaNoticeRight.visibility = VISIBLE
                }
            }
        }
    }

    private fun checkForOneLegByNewWay(skeleton: AdlPoseSkeleton, location: String): Boolean {
        if (!judegeNullAnkleBodyByNewWay(skeleton)) return false
        if (areaType == AREA_TAPE_SINGLE) {
            if (sportRect!!.contains(
                    (skeleton.getLAnkle().x * scW).toInt(),
                    (skeleton.getLAnkle().y * scH).toInt()
                )
                || sportRect!!.contains(
                    (skeleton.getRAnkle().x * scW).toInt(),
                    (skeleton.getRAnkle().y * scH).toInt()
                )
            ) {
                return true
            }
        } else {
            if (location == SPORT_LOCATION_LEFT) {
                if (leftRect!!.contains(
                        (skeleton.getLAnkle().x * scW).toInt(),
                        (skeleton.getLAnkle().y * scH).toInt()
                    )
                    || leftRect!!.contains(
                        (skeleton.getRAnkle().x * scW).toInt(),
                        (skeleton.getRAnkle().y * scH).toInt()
                    )
                ) {
                    return true
                }
            } else if (location == SPORT_LOCATION_RIGHT) {
                if (rightRect!!.contains(
                        (skeleton.getLAnkle().x * scW + 1920 / 2).toInt(),
                        (skeleton.getLAnkle().y * scH).toInt()
                    )
                    || rightRect!!.contains(
                        (skeleton.rAnkle.x * scW + 1920 / 2).toInt(),
                        (skeleton.rAnkle.y * scH).toInt()
                    )
                ) {
                    inAreaUi(location)
                    return true
                }
            }
        }
        return false
    }

    private fun judegeNullAnkleBodyByNewWay(skeleton: AdlPoseSkeleton): Boolean {
        return !(skeleton.lAnkle == null || skeleton.rAnkle == null)
    }

    companion object {
        const val AREA_TAPE_SINGLE = 1
        const val AREA_TAPE_DOUBLE = 2
    }
}