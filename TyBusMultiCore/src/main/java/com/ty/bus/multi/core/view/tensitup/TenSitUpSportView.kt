package com.ty.bus.multi.core.view.tensitup

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.databinding.TenSitUpSportViewBinding
import com.ty.tybusbase.expand.Action

class TenSitUpSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, TenSitUpSportView>()
    private var mBinding = TenSitUpSportViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false
    private var startListener: Action? = null

    var listener: ResultViewBtnClickListener? = null

    init {
        //顶部居中提示：背景为左右渐隐的蓝色渐变
        mBinding.tvTopHint.background = createTopHintGradient()
    }

    /**
     * 显示顶部居中提示
     */
    fun showTopHint(text: CharSequence) {
        mBinding.tvTopHint.text = text
        mBinding.tvTopHint.visibility = View.VISIBLE
    }

    /**
     * 隐藏顶部居中提示
     */
    fun hideTopHint() {
        mBinding.tvTopHint.visibility = View.GONE
    }

    /**
     * 顶部提示背景渐变：90deg（左→右），两端透明、中间蓝色
     * 色标较多（含 12%~88% 的平台段），XML 的 <gradient> 只支持三个色标，故用着色器还原
     */
    private fun createTopHintGradient(): Drawable = PaintDrawable().apply {
        shape = RectShape()
        shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                0f,
                TOP_HINT_GRADIENT_COLORS,
                TOP_HINT_GRADIENT_POSITIONS,
                Shader.TileMode.CLAMP
            )
        }
    }

    companion object {
        private val TOP_HINT_GRADIENT_COLORS = intArrayOf(
            Color.argb(0, 0, 92, 162),      //0%   rgba(0,92,162,0)
            Color.argb(82, 0, 96, 180),     //4%   rgba(0,96,180,0.32)
            Color.argb(204, 0, 99, 198),    //12%  rgba(0,99,198,0.8)
            Color.argb(242, 0, 99, 198),    //50%  rgba(0,99,198,0.95)
            Color.argb(204, 0, 99, 198),    //88%  rgba(0,99,198,0.8)
            Color.argb(82, 0, 97, 189),     //96%  rgba(0,97,189,0.32)
            Color.argb(0, 0, 92, 162)       //100% rgba(0,92,162,0)
        )

        private val TOP_HINT_GRADIENT_POSITIONS =
            floatArrayOf(0f, 0.04f, 0.12f, 0.5f, 0.88f, 0.96f, 1f)
    }

}
