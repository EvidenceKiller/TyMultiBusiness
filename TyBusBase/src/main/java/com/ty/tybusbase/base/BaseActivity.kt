package com.ty.tybusbase.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.ty.tybusbase.R
import com.ty.tybusbase.gesture.GestureCommand
import com.ty.tybusbase.gesture.GestureListener
import com.ty.tybusbase.gesture.GestureProcessor
import com.ty.tybusbase.gesture.GestureType
import com.ty.tybusbase.helper.GlobalTouchHelper
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseActivity<DB : ViewBinding, VM : BaseViewModel> : RxAppCompatActivity(),
    LifecycleOwner {

    protected lateinit var viewModel: VM

    protected lateinit var mBinding: DB

    //    private SweetAlertDialog pDialog;
    private var loadingView: View? = null
    private var imgLoading: ImageView? = null

    private val gestureListener: GestureListener = object :
        GestureListener {
        override fun onGesture(command: GestureCommand, type: GestureType) {
            runOnUiThread {
                this@BaseActivity.onGesture(command, type)
                when (command) {
                    GestureCommand.LEFT -> {
                        onGestureLeft()
                    }

                    GestureCommand.RIGHT -> {
                        onGestureRight()
                    }

                    GestureCommand.HANDS_UP -> {
                        if (type == GestureType.TYPE_LEFT) {
                            onGestureLeftHandsUp()
                        } else {
                            onGestureRightHandsUp()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewDataBinding()
        initData()
        initView(savedInstanceState)
        initObserver()
        registerLive()
    }

    private fun registerLive() {
        viewModel.mBaseUcLiveData.showEmptyCallBackEvent
            .observe(this) { onEmpty() }
        viewModel.mBaseUcLiveData.showLoadingCallBackEvent.observe(this) { showLoadingDialog(it) }
        viewModel.mBaseUcLiveData.disLoadingCallBackEvent.observe(this) { hideLoadingDialog() }
    }

    protected open fun onEmpty() {

    }

    protected open fun onError() {

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        GlobalTouchHelper.onTouch()
        return super.dispatchTouchEvent(ev)
    }

    abstract fun initObserver()

    abstract fun initData()

    abstract fun initView(savedInstanceState: Bundle?)

    protected open fun onGesture(command: GestureCommand, type: GestureType){}

    protected open fun onGestureLeft() {}
    protected open fun onGestureRight() {}

    protected open fun onGestureLeftHandsUp() {
        finish()
    }
    protected open fun onGestureRightHandsUp() {}
    private fun addGestureListener() {
        GestureProcessor.getInstance().addGestureListener(gestureListener)
    }

    private fun removeGestureListener() {
        GestureProcessor.getInstance().removeGestureListener(gestureListener)
    }

    override fun onResume() {
        super.onResume()
        addGestureListener()
    }

    override fun onPause() {
        super.onPause()
        removeGestureListener()
    }
    open fun initViewDataBinding() {
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
                        setContentView(mBinding.root)
                        (mBinding as ViewDataBinding).lifecycleOwner = this

                    }

                    ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                        cls.getDeclaredMethod("inflate", LayoutInflater::class.java).let {
                            @Suppress("UNCHECKED_CAST") mBinding =
                                it.invoke(null, layoutInflater) as DB
                            setContentView(mBinding.root)
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

    @Suppress("UNCHECKED_CAST")
    protected fun createViewModel(type: Type) {
        val tClass = type as? Class<VM> ?: BaseViewModel::class.java
        viewModel =
            ViewModelProvider(viewModelStore, getProviderFactory())[tClass] as VM
        lifecycle.addObserver(viewModel)
    }


    open fun showLoadingDialog(notice: String? = "") {
        runOnUiThread {
            if (null == loadingView) {
                loadingView =
                    View.inflate(this@BaseActivity, R.layout.base_activity_dialog_loading, null)
                val vlpp: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addContentView(loadingView, vlpp)
            }
            loadingView?.visibility = View.VISIBLE
            imgLoading = loadingView?.findViewById(R.id.img_loading)
            loadingView?.findViewById<TextView>(R.id.tv_notice)?.text = notice ?: ""
            val rotate = RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotate.interpolator = LinearInterpolator()
            rotate.duration = 1000 //设置动画持续周期
            rotate.repeatCount = -1 //设置重复次数
            rotate.fillAfter = true //动画执行完后是否停留在执行完的状态
            imgLoading?.startAnimation(rotate)
        }
    }

    open fun hideLoadingDialog() {
        runOnUiThread {
            loadingView?.visibility = View.GONE
            imgLoading?.clearAnimation()
        }
    }


    open fun getProviderFactory(): ViewModelProvider.Factory = defaultViewModelProviderFactory

    open fun initLayoutId(): Int = 0

}