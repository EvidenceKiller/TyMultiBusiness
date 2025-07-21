package com.ty.tybusbase.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.ty.tybusbase.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<DB : ViewBinding, VM : BaseViewModel> : Fragment(), IBaseView,
    CoroutineScope by MainScope() {

    protected lateinit var viewModel: VM

    protected lateinit var mBinding: DB
    fun isInitialized() = ::mBinding.isInitialized

    /**
     * Fragment是否创建
     */
    var mIsViewCreated = false

    /**
     * Fragment是否第一次可见
     */
    var mIsFirstVisible = true

    /**
     * 标记保存Fragment的可见状态，表示当前Fragment是否分发过  可见到不可见  不可见到可见才可以调用 disPatchVisibaleHint  防止重复调用
     */
    var mCurrentVisibleState = false

    var TAG = "BaseFragment"

    //是否第一次加载
    private var isFirst: Boolean = true

    private val loadingDialog by lazy {
        LoadingDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initBinding(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createViewModel()
        mIsViewCreated = true
        //2：对于默认Fragment的加载，可以在此分发一下，可见才分发
        //2：对于默认Fragment的加载，可以在此分发一下，可见才分发
        initData()
        initView(savedInstanceState)
        initBaseObserver()
        initObserver()
        if (userVisibleHint && !isHidden) {
            //可见
            disPatchVisibleHint(true)
        }
    }

    private fun initBaseObserver() {
        viewModel.mBaseUcLiveData.showEmptyCallBackEvent.observe(requireActivity()){
            onEmpty()
        }
        viewModel.mBaseUcLiveData.showLoadingCallBackEvent.observe(requireActivity()){
            showLoading()
        }
        viewModel.mBaseUcLiveData.disLoadingCallBackEvent.observe(requireActivity()){
            dismissLoading()
        }
    }

    override fun onResume() {
        //只有当前可见的Fragment
        if (!mCurrentVisibleState && userVisibleHint && !isHidden) {
            disPatchVisibleHint(true)
        }
        super.onResume()
    }

    override fun onPause() {
        if (mCurrentVisibleState && userVisibleHint) {
            disPatchVisibleHint(false)
        }
        super.onPause()
    }

    //与生命周期无关的函数
    /**
     * onCreateView在此方法之后  第一次时 mIsViewCreated=false
     *
     * @param isVisibleToUser
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        //用户是否可见
        Log.e(TAG, "setUserVisibleHint----$isVisibleToUser")
        super.setUserVisibleHint(isVisibleToUser)
        /**
         * 1：分发的前提是FragmentView已经创建，因为setUserVisibleHint在fragment 的声明周期之前执行，
         * 如果页面没创建就去分发容易造成空指针异常
         *
         * 2：因为此方法是由setUserVisibleHint方法调用，而setUserVisibleHint方法由系统的Viewpager调用多次，我们没办法控制它，
         * 但是我们可以控制disPatchVisibaleHint的分发
         */
        if (mIsViewCreated) {
            //只有Fragment创建了后才进行分发
            //防止重复调用disPatchVisibleHint !mCurrentVisibleState表示没有分发过时才分发
            if (isVisibleToUser && !mCurrentVisibleState) {
                //用户可见时进行分发事件
                disPatchVisibleHint(true)
            } else if (mCurrentVisibleState && !isVisibleToUser) {
                //用户不可见时不分发事件
                disPatchVisibleHint(false)
            }
        }
    }

    /**
     * 与生命周期无关的函数
     * 在使用`FragmentTransaction`的`hidden` 和`show`方法管理fragment时，是不会走Fragment正常的生命周期，
     * 而是走`onHiddenChanged`方法，此方法可以监听Fragment 的显示与隐藏。
     *
     * @param hidden
     */
    override fun onHiddenChanged(hidden: Boolean) {
        Log.e(TAG, "onHiddenChanged")
        super.onHiddenChanged(hidden)
        if (hidden) {
            disPatchVisibleHint(false)
        } else {
            disPatchVisibleHint(true)
        }
    }


    /**
     * 分发可见
     * 调用的前提：可见--》不可见    或者不可见--》可见
     *
     * @param isVisible
     */
    open fun disPatchVisibleHint(isVisible: Boolean) {
        //view没创建，则不处理分发逻辑，会出现先调用onFragmentLazyLoad()方法，再回调onViewCreated的情况
        if (!mIsViewCreated) return
        Log.e(TAG, "disPatchVisibleHint----$isVisible")
        if (mCurrentVisibleState == isVisible) {
            //防止调用两次更新，存在当 mCurrentVisibleState=isVisible;执行前setUserVisibleHint可能被调用两次
            return
        }
        //进行赋值操作
        mCurrentVisibleState = isVisible
        if (isVisible) {
            //可见
            if (mIsFirstVisible) {
                mIsFirstVisible = false
                onFragmentLazyLoad()
            }
            onFragmentResume()
            //处理子ViewPager嵌套使用时，子fragment懒加载的实现
            dispatchChildVisibleState(true)
        } else {
            onFragmentPause()
            dispatchChildVisibleState(false)
        }
    }

    /**
     *  当需要懒加载时，才需要复写这三个方法。
     */

    /**
     * 当需要懒加载时，才需要复写这三个方法。
     */
    /**
     * 没有特殊要求可在此进行数据加载
     */
    open fun onFragmentLazyLoad() {
        Log.e(TAG, "onFragmentLazyLoad")
    }

    /**
     * 不可见时处理相关动作  停止数据的加载
     */
    open fun onFragmentPause() {
        Log.e(TAG, "onFragmentPause")
    }

    /**
     * 表面可见时  加载数据（会多次调用）
     */
    open fun onFragmentResume() {
        Log.e(TAG, "onFragmentResume")
    }

    /**
     * ViewPage嵌套使用时处理子的Fragment懒加载逻辑
     * 通过getChildFragmentManager();获取子Fragment
     *
     * @param visible
     */
    open fun dispatchChildVisibleState(visible: Boolean) {
        val childFragmentManager = childFragmentManager
        val childFragmentManagerFragments = childFragmentManager.fragments
        for (fragment in childFragmentManagerFragments) {
            //进行类型校验，只有继承了LazyFragment才进行懒加载的处理
            if (fragment is BaseFragment<*, *> && !fragment.isHidden() && fragment.getUserVisibleHint()) {
                fragment.disPatchVisibleHint(visible)
            }
        }
    }


    /**
     * 使用 DataBinding时,要重写此方法返回相应的布局 id
     * 使用ViewBinding时，不用重写此方法
     */
    open fun layoutId(): Int = 0


    override fun initData() {}

    override fun initLayoutId(): Int = 0

    protected open fun onEmpty() {

    }

    protected open fun onError(){

    }

    private fun initBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        var type = javaClass.genericSuperclass
        if (type !is ParameterizedType) {//当多继承出现此处会崩溃，需要添加层级
            type = javaClass.superclass.genericSuperclass
        }
        if (type is ParameterizedType) {
            var vbPosition = -1
            for (index in type.actualTypeArguments.indices) {
                val cls = type.actualTypeArguments[index] as Class<*>
                if (ViewDataBinding::class.java.isAssignableFrom(cls) || ViewBinding::class.java.isAssignableFrom(
                        cls
                    )
                ) {
                    vbPosition = index
                }
            }
            val cls = type.actualTypeArguments[vbPosition] as Class<*>
            return when {
                ViewDataBinding::class.java.isAssignableFrom(cls) && cls != ViewDataBinding::class.java -> {
                    if (layoutId() == 0) throw IllegalArgumentException("Using DataBinding requires overriding method layoutId")
                    mBinding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
                    (mBinding as ViewDataBinding).lifecycleOwner = this
                    mBinding.root
                }

                ViewBinding::class.java.isAssignableFrom(cls) && cls != ViewBinding::class.java -> {
                    cls.getDeclaredMethod("inflate", LayoutInflater::class.java).let {
                        @Suppress("UNCHECKED_CAST")
                        mBinding = it.invoke(null, inflater) as DB
                        mBinding.root
                    }
                }

                else -> {
                    if (layoutId() == 0) throw IllegalArgumentException("If you don't use ViewBinding, you need to override method layoutId")
                    inflater.inflate(layoutId(), container, false)
                }
            }
        } else throw IllegalArgumentException("Generic error")
    }

    /**
     * 创建 ViewModel
     *
     * 共享 ViewModel的时候，重写  Fragmnt 的 getViewModelStore() 方法，
     * 返回 activity 的  ViewModelStore 或者 父 Fragmnt 的 ViewModelStore
     */
    @Suppress("UNCHECKED_CAST")
    private fun createViewModel() {

        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            var mdPosition = -1
            for (index in type.actualTypeArguments.indices) {
                val cls = type.actualTypeArguments[index] as Class<*>
                if (BaseViewModel::class.java.isAssignableFrom(cls)) {
                    mdPosition = index
                }
            }
            if (mdPosition < 0) {
                return
            }
            val cls = type.actualTypeArguments[mdPosition] as Class<*>
            if (BaseViewModel::class.java.isAssignableFrom(cls)) {
                val tp = type.actualTypeArguments[mdPosition]
                val tClass = tp as? Class<VM> ?: BaseViewModel::class.java
                viewModel = ViewModelProvider(viewModelStore, defaultViewModelProviderFactory)
                    .get(tClass) as VM
            }
        }
    }


    override fun showLoading() {
        loadingDialog.show()
    }

    override fun dismissLoading() {
        loadingDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }


}