package com.ty.bus.multi.core.view.tensport

import android.content.Context
import android.icu.util.TimeUnit
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.expand.getString
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.databinding.ItemTenSportResult2Binding
import com.ty.bus.multi.databinding.ItemTenSportResultBinding
import com.ty.bus.multi.databinding.TenSportResultViewBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.utils.MMKVUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TenSportResultView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var measureUnit: String = getString(R.string.ge)

    private val mBinding: TenSportResultViewBinding
    var top1Count: Int = Int.MAX_VALUE

    protected var reportClickListener: Action1<Int>? = null
    private var resultList: MutableList<MultiSportResult>? = null

    //倒计时
    private var timer: CountDownTimer? = null
    private var COUNTDOWN_TIME = 10
    private var countDownCount = 10
    private var isDetached = false
    private var isSpeech = true
    private var currentSpeechIndex = 0
    private var isSupportReport = false

    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        mBinding = TenSportResultViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    fun setReportOnclickListener(listener: (view: Int) -> Unit) {
        reportClickListener = listener
    }

    private fun initView() {
        mBinding.rvResult1.layoutManager = LinearLayoutManager(context)
        mBinding.rvResult1.adapter =
            object : BaseBindingAdapter<ItemTenSportResultBinding, MultiSportResult>() {
                override fun convert(
                    holder: VBViewHolder<ItemTenSportResultBinding>,
                    item: MultiSportResult
                ) {
                    holder.vb.tvVisitorNumber.text =
                        getString(
                            R.string.visitor_number,
                            item.number + 1
                        )
                    holder.vb.tvVisitorNumber.visibility = VISIBLE

                    when (item.rank) {
                        1 -> {
                            if (top1Count < item.count) {
                                holder.vb.groupBest.visibility = VISIBLE
                            }
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no1)
                        }

                        2 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no2)
                        }

                        3 -> {
                            holder.vb.ivRank.setBackgroundResource(R.mipmap.ic_result_list_no3)
                        }
                    }
                    holder.vb.tvResult.text = "${item.count}$measureUnit"
                    holder.vb.tvRank.text = "${item.rank}"
                    holder.vb.tvName.text = item.accountInfo.studentEntity?.accountName
                    Glide.with(context)
                        .load(item.accountInfo.studentEntity?.userFaceImgUrl)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.ivHeader)
                    if (isSupportReport){
                        holder.vb.btnReport.visibility = VISIBLE
                        holder.vb.btnReport.setOnClickListener { reportClickListener?.invoke(item.number) }
                    }
                }
            }
        mBinding.rvResult2.layoutManager = LinearLayoutManager(context)
        mBinding.rvResult2.adapter =
            object : BaseBindingAdapter<ItemTenSportResult2Binding, MultiSportResult>() {
                override fun convert(
                    holder: VBViewHolder<ItemTenSportResult2Binding>,
                    item: MultiSportResult
                ) {
                    holder.vb.tvVisitorNumber.text =
                        getString(
                            R.string.visitor_number,
                            item.number + 1
                        )
                    holder.vb.tvVisitorNumber.visibility = VISIBLE
                    holder.vb.tvResult.text = "${item.count}$measureUnit"
                    holder.vb.tvRank.text = "${item.rank}"
                    holder.vb.tvName.text = item.accountInfo.studentEntity?.accountName
                    Glide.with(context)
                        .load(item.accountInfo.studentEntity?.userFaceImgUrl)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.ivHeader)
                    if (isSupportReport){
                        holder.vb.btnReport.visibility = VISIBLE
                        holder.vb.btnReport.setOnClickListener { reportClickListener?.invoke(item.number) }
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
                resultList?.let {
                    // 继续从当前索引播报
                    resultSpeech(it)
                }
            }
        }
    }

    fun setConfirmOnclickListener(listener: (view: View) -> Unit) {
        mBinding.tvConfirm.setOnClickListener {
            listener.invoke(it)
        }
    }

    fun setSupportReport(support: Boolean) {
        isSupportReport = support
    }
    /**
     * 设置下一组按钮的回调，不设置就不会自动下一组
     */
    fun setNextOnclickListener(listener: () -> Unit) {
        val autoNext = MMKVUtil.decodeBoolean(MultiCoreConstant.MULTI_SKIP_AUTO_NEXT, true)
        mBinding.cbAutoNext.isChecked = autoNext
        if (autoNext) {
            //自动下一组开启
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
     * 设置结果，需要先取出结果进行成绩排序，再设置卡片结果值
     */
    fun setResult(
        resultList: MutableList<MultiSportResult>,
        top1Count: Int,
        speech: Boolean = false,
        isLargeScreen: Boolean = true
    ) {
        this.resultList = resultList
        this.top1Count = top1Count
        if (resultList.size > 5&&isLargeScreen) {
            val list = resultList.subList(0, 5)
            (mBinding.rvResult1.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                list
            )
            val list2 = resultList.subList(5, resultList.size)
            (mBinding.rvResult2.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                list2
            )
            mBinding.rvResult2.visibility = VISIBLE
        } else {
            (mBinding.rvResult1.adapter as BaseBindingAdapter<*, MultiSportResult>).setNewInstance(
                resultList
            )
        }

        //播报语音
        if (speech) {
            coroutineScope.launch {
                runCatching {
                    delay(3000)
                    resultSpeech(resultList)
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun resultSpeech(resultList: MutableList<MultiSportResult>) {
        isSpeech = true
        currentSpeechIndex = 0
        mBinding.llSpeech.visibility = VISIBLE
        mBinding.ivPlayerIcon.setBackgroundResource(R.mipmap.video_ic_stop_default)
        mBinding.tvSpeech.text = getString(R.string.pause_result_speech)
        speakNext(resultList)
    }

    private fun speakNext(resultList: MutableList<MultiSportResult>) {
        if (!isSpeech || isDetached) return
        if (currentSpeechIndex >= resultList.size) {
            speechEnd()
            return
        }
        val value = resultList[currentSpeechIndex]
        val isLast = currentSpeechIndex == resultList.size - 1
        val endCallback = object : Runnable {
            override fun run() {
                if (isDetached) return
                if (!isSpeech) return
                if (isLast) {
                    speechEnd()
                } else {
                    currentSpeechIndex++
                    speakNext(resultList)
                }
            }
        }
        SpeechWrapper.speech2(
            "${value.accountInfo.studentEntity?.accountName}，${value.count}$measureUnit}",
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
        coroutineScope.cancel()
    }
}
