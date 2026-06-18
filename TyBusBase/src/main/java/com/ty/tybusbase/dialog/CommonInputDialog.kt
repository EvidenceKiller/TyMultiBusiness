package com.ty.tybusbase.dialog

import android.text.Editable
import android.text.TextWatcher
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.DialogCommonInputBinding
import com.ty.tybusbase.expand.Action1

class CommonInputDialog : BaseDialogFragment<DialogCommonInputBinding, BaseViewModel>() {

    private var title: String? = null
    private var label: String? = null
    private var hint: String? = null
    private var cancelText: String? = null
    private var confirmText: String? = null
    private var content: String? = null
    private var callback: Action1<String>? = null

    fun setTitle(title: String): CommonInputDialog {
        this.title = title
        return this
    }

    fun setLabel(label: String): CommonInputDialog {
        this.label = label
        return this
    }

    fun setHint(hint: String): CommonInputDialog {
        this.hint = hint
        return this
    }

    fun setCancelText(text: String): CommonInputDialog {
        this.cancelText = text
        return this
    }

    fun setConfirmText(text: String): CommonInputDialog {
        this.confirmText = text
        return this
    }

    fun setContent(content: String): CommonInputDialog {
        this.content = content
        return this
    }

    fun setCallback(callback: Action1<String>): CommonInputDialog {
        this.callback = callback
        return this
    }

    override fun initViewObservable() {
        mBinding.tvClose.setOnClickListener { dismiss() }
        mBinding.tvCancel.setOnClickListener { dismiss() }
        mBinding.tvConfirm.setOnClickListener {
            val value = mBinding.etInput.text?.toString()?.trim() ?: ""
            if (value.isNotEmpty()) {
                callback?.invoke(value)
                dismiss()
            }
        }

        mBinding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasContent = !s.isNullOrBlank()
                mBinding.tvConfirm.isEnabled = hasContent
                mBinding.tvConfirm.setBackgroundResource(
                    if (hasContent) R.drawable.bg_round_r50_gradient_red else R.drawable.bg_round_r50_gray
                )
            }
        })
    }

    override fun initData() {
        title?.let { mBinding.tvTitle.text = it }
        label?.let { mBinding.tvLabel.text = it }
        hint?.let { mBinding.etInput.hint = it }
        cancelText?.let { mBinding.tvCancel.text = it }
        confirmText?.let { mBinding.tvConfirm.text = it }
        content?.let { mBinding.etInput.setText(it) }
    }
}