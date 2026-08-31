package com.ty.tybusbase.dialog

import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.DialogCommonTipBinding

class CommonTipDialog : BaseDialogFragment<DialogCommonTipBinding, BaseViewModel>() {

    private var content: String? = null
    private var action: Action? = null
    private var cancelAction: Action? = null

    fun setCallback(ac: Action): CommonTipDialog {
        this.action = ac
        return this
    }

    fun setCancelCallback(ac: Action): CommonTipDialog {
        this.cancelAction = ac
        return this
    }

    fun setContent(content: String): CommonTipDialog {
        this.content = content
        return this
    }

    /** 对话框已显示时更新内容（如协同已识别人数实时同步） */
    fun updateContent(content: String): CommonTipDialog {
        if (isVisible) {
            mBinding.tvContent.text = content
        }
        return this
    }

    override fun initViewObservable() {
        mBinding.tvCancel.setOnClickListener {
            cancelAction?.invoke()
            dismiss()
        }
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
