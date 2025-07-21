package com.ty.tybusbase.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseDialog<VB : ViewBinding>(context: Context, val gravity: Int = Gravity.CENTER) :
    Dialog(context) {

    lateinit var mBinding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window?.setGravity(gravity)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(getDialogView())
        setCanceledOnTouchOutside(canceledOnTouchOutside())
        setOnKeyListener { _, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
        val lp = getWindow()?.attributes
        if (needMatch()) {
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            lp?.width = WindowManager.LayoutParams.WRAP_CONTENT
        }
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        getWindow()?.attributes = lp
        initView()
    }

    abstract fun initView()

    open fun getDialogView(): View {
        val vbClass: Class<VB> =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        mBinding =
            inflate.invoke(null, LayoutInflater.from(context), null, false) as VB
        return mBinding.root
    }

    open fun needMatch() = false

    open fun canceledOnTouchOutside() = false

    override fun show() {
        window?.let {
            super.show()
        }
    }
}