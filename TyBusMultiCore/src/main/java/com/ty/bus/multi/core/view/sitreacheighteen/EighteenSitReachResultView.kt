package com.ty.bus.multi.core.view.sitreacheighteen

import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.ThirtySportResultItem
import com.ty.bus.multi.databinding.ItemThirtySportResultGridBinding
import com.ty.bus.multi.databinding.ThirtySportResultViewBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.GridSpaceDecoration
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.utils.MMKVUtil

/**
 * 18人坐位体前屈结果页：展示主屏 + 左副屏 + 右副屏合并后的18人成绩。
 *
 * 复用三十人跳绳结果页的布局（网格列表），区别：
 * - 成绩按坐位体前屈规则展示：count/10.0 厘米，违规/未完成单独展示文案
 * - 坐位体前屈无运动报告，隐藏报告按钮
 */
class EighteenSitReachResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    /** 结果单位 */
    private val measureUnit = "cm"

    private val mBinding: ThirtySportResultViewBinding

    private var resultList: List<ThirtySportResultItem>? = null

    //自动下一组倒计时
    private var timer: CountDownTimer? = null
    private val COUNTDOWN_TIME = 10
    private var countDownCount = COUNTDOWN_TIME
    private var isDetached = false
    private var isSpeech = true
    private var currentSpeechIndex = 0

    init {
        mBinding = ThirtySportResultViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    private fun initView() {
        mBinding.rvResult.layoutManager = GridLayoutManager(context, 2)
        mBinding.rvResult.addItemDecoration(GridSpaceDecoration(12, 56))
        mBinding.rvResult.adapter =
            object : BaseBindingAdapter<ItemThirtySportResultGridBinding, ThirtySportResultItem>() {
                override fun convert(
                    holder: VBViewHolder<ItemThirtySportResultGridBinding>,
                    item: ThirtySportResultItem
                ) {
                    holder.vb.tvRank.text = item.rank.toString()
                    holder.vb.tvName.text = item.name
                    holder.vb.tvResult.text = formatResult(item)
                    holder.vb.btnReport.gone()
                    holder.vb.ivRank.visibility = GONE
                    holder.vb.tvRank.visibility = GONE
                    when (item.rank) {
                        1 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no1)
                            holder.vb.ivRank.visibility = VISIBLE
                        }

                        2 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no2)
                            holder.vb.ivRank.visibility = VISIBLE
                        }

                        3 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no3)
                            holder.vb.ivRank.visibility = VISIBLE
                        }

                        else -> {
                            holder.vb.tvRank.visibility = VISIBLE
                        }
                    }
                    Glide.with(context)
                        .load(item.faceUrl)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.ivHeader)
                    if (item.result.accountInfo.studentEntity?.cardType == BusiBaseConstantsValue.CHINA_MOBILE_CARD_TYPE) {
                        holder.vb.ivHeaderBg.visible()
                    }
                    if (item.isVisitor) {
                        holder.vb.tvVisitorNumber.text =
                            getString(
                                R.string.visitor_number_with_area,
                                item.areaName,
                                item.trackNumber + 1
                            )
                        holder.vb.tvVisitorNumber.visibility = VISIBLE
                    } else {
                        holder.vb.tvVisitorNumber.visibility = GONE
                    }
                }
            }

        mBinding.cbAutoNext.setOnCheckedChangeListener { _, isChecked ->
            MMKVUtil.encode(MultiCoreConstant.MULTI_SKIP_AUTO_NEXT, isChecked)
        }

        mBinding.llSpeech.setOnClickListener {
            if (isSpeech) {
                SpeechWrapper.stop()
                mBinding.ivPlayerIcon.setBackgroundResource(R.mipmap.video_ic_play_default)
                mBinding.tvSpeech.text = getString(R.string.result_speech)
                isSpeech = false
            } else {
                resultList?.let { resultSpeech(it) }
            }
        }
    }

    /**
     * 坐位体前屈成绩展示：0正常成绩（cm），1违规，2未完成
     */
    private fun formatResult(item: ThirtySportResultItem): String = when (item.result.status) {
        0 -> "${item.count / 10.0}$measureUnit"
        1 -> getString(R.string.violations)
        else -> getString(R.string.un_do)
    }

    fun setConfirmOnclickListener(listener: (view: View) -> Unit) {
        mBinding.tvConfirm.setOnClickListener {
            listener.invoke(it)
        }
    }

    /**
     * 设置下一组按钮的回调，不设置就不会自动下一组
     */
    fun setNextOnclickListener(listener: () -> Unit) {
        val autoNext = MMKVUtil.decodeBoolean(MultiCoreConstant.MULTI_SKIP_AUTO_NEXT, true)
        mBinding.cbAutoNext.isChecked = autoNext
        if (autoNext) {
            startCountTimer(listener)
        } else {
            mBinding.tvNext.text = getString(R.string.next_group)
        }
        mBinding.tvNext.visibility = VISIBLE
        mBinding.groupAutoNext.visibility = VISIBLE
        mBinding.tvNext.setOnClickListener {
            listener.invoke()
        }
    }

    private fun startCountTimer(function: () -> Unit) {
        countDownCount = COUNTDOWN_TIME
        mBinding.tvNext.text = getString(R.string.next_count_down, countDownCount)
        timer = object : CountDownTimer(COUNTDOWN_TIME * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvNext.text = getString(R.string.next_count_down, --countDownCount)
            }

            override fun onFinish() {
                function.invoke()
            }
        }
        timer?.start()
    }

    /**
     * 设置结束标题
     */
    fun setResultTitleText(text: String?) {
        if (TextUtils.isEmpty(text).not()) {
            mBinding.mtvFinish.text = text
        }
    }

    /**
     * 设置结果，传入主屏+副屏合并排序后的列表
     */
    fun setResult(
        resultList: List<ThirtySportResultItem>,
        speech: Boolean = false
    ) {
        this.resultList = resultList
        (mBinding.rvResult.adapter as BaseBindingAdapter<*, ThirtySportResultItem>).setNewInstance(
            resultList.toMutableList()
        )
        if (speech) {
            mBinding.llSpeech.visibility = VISIBLE
            resultSpeech(resultList)
        }
    }

    private fun resultSpeech(resultList: List<ThirtySportResultItem>) {
        isSpeech = true
        currentSpeechIndex = 0
        mBinding.llSpeech.visibility = VISIBLE
        mBinding.ivPlayerIcon.setBackgroundResource(R.mipmap.video_ic_stop_default)
        mBinding.tvSpeech.text = getString(R.string.pause_result_speech)
        speakNext(resultList)
    }

    private fun speakNext(resultList: List<ThirtySportResultItem>) {
        if (!isSpeech || isDetached) return
        if (currentSpeechIndex >= resultList.size) {
            speechEnd()
            return
        }
        val value = resultList[currentSpeechIndex]
        val isLast = currentSpeechIndex == resultList.size - 1
        val endCallback = Runnable {
            if (isDetached) return@Runnable
            if (!isSpeech) return@Runnable
            if (isLast) {
                speechEnd()
            } else {
                currentSpeechIndex++
                speakNext(resultList)
            }
        }
        SpeechWrapper.speech2(
            "${value.name}，${formatResult(value)}",
            endCallback
        )
    }

    private fun speechEnd() {
        isSpeech = false
        if (isDetached) return
        mBinding.ivPlayerIcon.setBackgroundResource(R.mipmap.video_ic_play_default)
        mBinding.tvSpeech.text = getString(R.string.result_speech)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SpeechWrapper.stop()
        timer?.cancel()
        isDetached = true
    }
}
