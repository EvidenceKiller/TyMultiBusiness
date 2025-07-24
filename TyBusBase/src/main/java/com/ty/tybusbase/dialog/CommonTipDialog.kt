package com.ty.tybusbase.dialog

import com.aipower.modulecommon.expand.Action
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.DialogCommonTipBinding

class CommonTipDialog : BaseDialogFragment<DialogCommonTipBinding, BaseViewModel>() {

    private var content: String? = null
    private var action: Action? = null

    fun setCallback(ac: Action): CommonTipDialog {
        this.action = ac
        return this
    }

    fun setContent(content: String): CommonTipDialog {
        this.content = content
        return this
    }

    override fun initViewObservable() {
        mBinding.tvCancel.setOnClickListener { dismiss() }
        mBinding.tvConfirm.setOnClickListener {
            action?.invoke()
            dismiss()
        }
    }

    override fun initData() {
        content?.let {
            mBinding.tvContent.text = it
        }
    }


}