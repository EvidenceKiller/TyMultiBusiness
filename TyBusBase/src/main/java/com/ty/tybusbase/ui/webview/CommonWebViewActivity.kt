package com.ty.tybusbase.ui.webview

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.BusiBaseActivityCommonWebviewBinding
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible

class CommonWebViewActivity : BaseActivity<BusiBaseActivityCommonWebviewBinding, BaseViewModel>() {

    private var urlStr: String? = null

    private var webView: WebView? = null

    private var classId: String? = null

    private var isFinish = false
    private val showWhiteBoard by lazy { intent.getBooleanExtra("show_white_board", false) }
    override fun initObserver() {
    }

    override fun initData() {
        urlStr = intent.getStringExtra("url")
        isFinish = intent.getBooleanExtra("finish", false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.ivBack.setOnClickListener {
            if (isFinish) {
                finish()
                return@setOnClickListener
            }
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                finish()
            }
        }
        webView = WebView(this)
        mBinding.flContent.addView(webView)
        initWebView(webView!!)
        urlStr?.let {
            webView?.loadUrl(it)
        }

        if (showWhiteBoard) {
            mBinding.layoutPen.visible()
            mBinding.layoutPen.setOnClickListener {
                mBinding.whiteBoard.visibility = View.VISIBLE
                mBinding.layoutPen.visibility = View.GONE
            }
            mBinding.whiteBoard.setCloseListener {
                mBinding.layoutPen.visibility = View.VISIBLE
            }
        }

    }

    private fun initWebView(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                showLoadingDialog()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoadingDialog()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        //  不使用缓存
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        //  支持JavaScript
        webView.settings.javaScriptEnabled = true
        //  设置允许JS弹窗
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

//        webView.addJavascriptInterface(ChinaMobileAppInterface(this), "ChinaMobileAppInterface")
//        webView.addJavascriptInterface(JavascriptInterface(jsCallback), "JSInterface")

        //  解决图片不显示
        webView.settings.loadsImagesAutomatically = true
        webView.settings.blockNetworkImage = false

        //  自适应屏幕
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true

        //  不支持缩放(如果要支持缩放，html页面本身也要支持缩放：不能加user-scalable=no)
        webView.settings.builtInZoomControls = false
        webView.settings.setSupportZoom(false)
        webView.settings.displayZoomControls = false

        //  设置WebView字体最小值，默认值8，取值1到72
        webView.settings.minimumFontSize = 1

        //  支持通过js打开新的窗口
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        //  设置默认文本编码
        webView.settings.defaultTextEncodingName = "UTF-8"

        //  使用file://加载文件
        webView.settings.allowFileAccess = true

        //  使用content://加载文件
        webView.settings.allowContentAccess = true

        webView.settings.domStorageEnabled = true

        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        //  设置默认颜色
        webView.setBackgroundColor(Color.parseColor("#130F23"))
        webView.setOnLongClickListener { true }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.flContent.removeAllViews()
    }
}