package com.ty.tybusbase.ui.report

import adl.ssp.common.report.model.IndicatorsResultDto
import adl.ssp.common.report.util.PrescriptionUtil
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.coroutineScope
import com.adl.base.log.AdlLogger
import com.alibaba.fastjson.JSON
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.databinding.ActivitySportReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SportReportActivity : BaseActivity<ActivitySportReportBinding, BaseViewModel>() {

    private var classId: String? = null
    override fun initObserver() {

    }

    override fun initData() {
        classId = intent.getStringExtra("class_id")
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.ivClose.setOnClickListener { finish() }
        mBinding.layoutPen.setOnClickListener {
            mBinding.whiteBoard.visibility = View.VISIBLE
            mBinding.layoutPen.visibility = View.GONE
        }
        mBinding.whiteBoard.setCloseListener {
            mBinding.layoutPen.visibility = View.VISIBLE
        }
        initWebView()
        loadData()
    }

    private fun loadData() {
        val reportIndicator =
            intent.getSerializableExtra("report_indicator") as? IndicatorsResultDto
        //运动报告类型
        val type = intent.getIntExtra("type", 0)
        //数据报告类型，0，原始数据，1.接口获取的处理过的数据
        val dataType = intent.getIntExtra("data_type", 0)
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            val file = File("/sdcard/report.json")
            file.writeText(JSON.toJSONString(reportIndicator))
        }
        if (reportIndicator != null) {
            val time = System.currentTimeMillis()
            val generateHtml = if (dataType == 1) {
                PrescriptionUtil.generateResultHtml(reportIndicator)
            } else {
                PrescriptionUtil.generateHtml(reportIndicator)
            }
            AdlLogger.d("加载立定跳远报告str时间:time：${System.currentTimeMillis() - time}")
            generateHtml?.let {
                lifecycle.coroutineScope.launch(Dispatchers.IO) {
                    val file = File("/sdcard/report.html")
                    file.writeText(generateHtml)
                }
            }
            when (type) {
                1 -> {
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/ropeSkipping/",
                        generateHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }

                2 -> {
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/jump/", generateHtml, "text/html", "UTF-8", null
                    )
                }

                3 ->{
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/pullup/", generateHtml, "text/html", "UTF-8", null
                    )
                }

                4 ->{
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/situp/", generateHtml, "text/html", "UTF-8", null
                    )
                }

                5 ->{
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/pingPongHc/", generateHtml, "text/html", "UTF-8", null
                    )
                }

                6 ->{
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/medicineBall/", generateHtml, "text/html", "UTF-8", null
                    )
                }

                else -> {
                    mBinding.webView.loadDataWithBaseURL(
                        "file:///android_asset/", generateHtml, "text/html", "UTF-8", null
                    )
                }
            }
        }
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {
        mBinding.webView.webViewClient = object : WebViewClient() {
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
        mBinding.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        //  支持JavaScript
        mBinding.webView.settings.javaScriptEnabled = true
        //  设置允许JS弹窗
        mBinding.webView.settings.javaScriptCanOpenWindowsAutomatically = true

        //  解决图片不显示
        mBinding.webView.settings.loadsImagesAutomatically = true
        mBinding.webView.settings.blockNetworkImage = false

        //  自适应屏幕
        mBinding.webView.settings.useWideViewPort = true
        mBinding.webView.settings.loadWithOverviewMode = true

        //  不支持缩放(如果要支持缩放，html页面本身也要支持缩放：不能加user-scalable=no)
        mBinding.webView.settings.builtInZoomControls = false
        mBinding.webView.settings.setSupportZoom(false)
        mBinding.webView.settings.displayZoomControls = false

        //  设置WebView字体最小值，默认值8，取值1到72
        mBinding.webView.settings.minimumFontSize = 1

        //  支持通过js打开新的窗口
        mBinding.webView.settings.javaScriptCanOpenWindowsAutomatically = true

        //  设置默认文本编码
        mBinding.webView.settings.defaultTextEncodingName = "UTF-8"

        //  使用file://加载文件
        mBinding.webView.settings.allowFileAccess = true

        //  使用content://加载文件
        mBinding.webView.settings.allowContentAccess = true

        mBinding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        //  通过addJavascriptInterface()将Java对象映射到JS对象
        mBinding.webView.addJavascriptInterface(JavascriptInterface(), "JSInterface")

        //  设置默认颜色
        mBinding.webView.setBackgroundColor(Color.parseColor("#130F23"))
        mBinding.webView.setOnLongClickListener { true }
    }


}