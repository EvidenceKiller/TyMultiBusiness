package com.ty.tybusbase.base

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.trello.rxlifecycle4.components.support.RxDialogFragment
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * @description:
 * @author: chenwei
 * @date :   2021/9/29 10:14
 */
abstract class BaseDialogFragment<VB : ViewBinding, VM : BaseViewModel> : RxDialogFragment() {
    lateinit var viewModel: VM
    lateinit var mBinding: VB
    private val mLifecycleRegistry = LifecycleRegistry(this)
    var TAG = "BaseDialogFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        initParam()
    }

    open fun initParam() {

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(cancelOnTouchOutside())
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = dialog!!.window
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        //注意此处
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.decorView.setPadding(0, 0, 0, 0)
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //消除边距
        //添加一层布局，让我们写的根布局下的尺寸生效；目前高不能铺满全屏，有全屏需求尽量不用Dialog方式显示
        val view = FrameLayout(requireContext())
        view.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        initViewDataBinding(view)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
        initViewObservable()
        mLifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    open fun initView() {}

    protected abstract fun initViewObservable()
    protected abstract fun initData()
    open fun cancelOnTouchOutside() = false
    override fun onStart() {
        super.onStart()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding(container: ViewGroup?) {
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
                            layoutInflater, initLayoutId(), container, true
                        )
                        (mBinding as ViewDataBinding).lifecycleOwner = this

                    }

                    ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                        cls.getDeclaredMethod(
                            "inflate",
                            LayoutInflater::class.java,
                            ViewGroup::class.java,
                            Boolean::class.java
                        ).let {
                            @Suppress("UNCHECKED_CAST") mBinding =
                                it.invoke(null, layoutInflater, container, true) as VB
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

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    open fun initLayoutId(): Int = 0

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
    </T> */
    @Suppress("UNCHECKED_CAST")
    private fun createViewModel(type: Type) {
        //让ViewModel拥有View的生命周期感应
        //注入RxLifecycle生命周期
        val modelClass = type as? Class<VM> ?: BaseViewModel::class.java
        viewModel =
            ViewModelProvider(viewModelStore, defaultViewModelProviderFactory)[modelClass] as VM
        lifecycle.addObserver(viewModel)
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    fun startActivity(clz: Class<*>?) {
        startActivity(Intent(context, clz))
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }
}
