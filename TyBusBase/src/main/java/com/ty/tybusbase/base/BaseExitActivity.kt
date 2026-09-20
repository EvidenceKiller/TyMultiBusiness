package com.ty.tybusbase.base

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.ty.tybusbase.R
import com.ty.tybusbase.databinding.BaseActivitySportExitBinding
import java.lang.reflect.ParameterizedType

abstract class BaseExitActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB, VM>() {

    private lateinit var exitPopDialog: PopupWindow

    private lateinit var mBaseBinding: BaseActivitySportExitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExitPop()
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (::mBaseBinding.isInitialized) {
            mBaseBinding.ivExit.setOnClickListener { showExitDialog() }
        }
    }

    open fun initExitPop() {
        //查找布局
        val inflate: View = LayoutInflater.from(this).inflate(R.layout.layout_exit, null)
        //初始化弹框
        exitPopDialog = PopupWindow(
            inflate,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        exitPopDialog.setBackgroundDrawable(BitmapDrawable())
        //退出
        inflate.findViewById<View>(R.id.btn_sure).setOnClickListener { _: View? ->
            exitPopDialog.dismiss()
            onExitDialogClose()
            finish()
        }
        //继续
        inflate.findViewById<View>(R.id.btn_cancel)
            .setOnClickListener { _: View? ->
                exitPopDialog.dismiss()
                onExitDialogContinue()
            }
        //点击外面不消失
        exitPopDialog.isOutsideTouchable = false
    }

    protected fun showExitDialog() {
        exitPopDialog.showAtLocation(mBinding.root, Gravity.NO_GRAVITY, 0, 0)
        onExitDialogShow()
    }

    protected fun hideBackBtn() {
        mBaseBinding.ivExit.visibility = View.GONE
    }

    open fun showBackBtn() {
        mBaseBinding.ivExit.visibility = View.VISIBLE
        mBaseBinding.ivExit.bringToFront()
    }

    fun showExitView(show: Boolean) {
        if (!::mBaseBinding.isInitialized) return
        mBaseBinding.ivExit.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * 退出对话框显示
     */
    open fun onExitDialogShow() {}

    /**
     * 退出对话框点击继续
     */
    open fun onExitDialogContinue() {}

    /**
     * 退出对话框点击关闭，页面退出
     */
    open fun onExitDialogClose() {}

    fun getBaseBinding(): BaseActivitySportExitBinding {
        return mBaseBinding
    }

    override fun initViewDataBinding() {
        mBaseBinding = BaseActivitySportExitBinding.inflate(layoutInflater)
        initContentBinding()
    }

    open fun initContentBinding() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            var mdPosition = -1
            var vbPosition = -1
            for (index in type.actualTypeArguments.indices) {
                val cls = type.actualTypeArguments[index] as Class<*>
                if (ViewDataBinding::class.java.isAssignableFrom(cls) || ViewBinding::class.java.isAssignableFrom(
                        cls
                    )
                ) {
                    vbPosition = index
                } else if (BaseViewModel::class.java.isAssignableFrom(cls)) {
                    mdPosition = index
                }
            }

            if (vbPosition >= 0) {
                val cls = type.actualTypeArguments[vbPosition] as Class<*>
                when {
                    ViewDataBinding::class.java.isAssignableFrom(cls) && cls != ViewDataBinding::class.java -> {
                        if (initLayoutId() == 0) throw IllegalArgumentException("Using DataBinding requires overriding method initLayoutId")
                        mBinding = DataBindingUtil.inflate(
                            layoutInflater, initLayoutId(), null, false
                        )
                        setContentView(mBaseBinding.root)
                        mBaseBinding.sportContent.addView(
                            mBinding.root, FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        (mBinding as ViewDataBinding).lifecycleOwner = this

                    }

                    ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                        cls.getDeclaredMethod("inflate", LayoutInflater::class.java).let {
                            @Suppress("UNCHECKED_CAST") mBinding =
                                it.invoke(null, layoutInflater) as VB
                            setContentView(mBaseBinding.root)
                            mBaseBinding.sportContent.addView(
                                mBinding.root, FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }
                    }

                    else -> {
                        if (initLayoutId() == 0) throw IllegalArgumentException("If you don't use ViewBinding, you need to override method initLayoutId")
                        layoutInflater.inflate(initLayoutId(), null, false)
                    }
                }
            }
            // val cls = type.actualTypeArguments[mdPosition] as Class<*>
            if (mdPosition >= 0) {
                createViewModel(type.actualTypeArguments[mdPosition])
            }

        } else throw IllegalArgumentException("Generic error")
    }

}