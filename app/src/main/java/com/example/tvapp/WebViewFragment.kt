package com.example.tvapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.tvapp.js.JsBridge
import com.example.tvapp.tv.TvWebChromeClient
import com.example.tvapp.tv.TvWebViewClient

/**
 * WebView 内容展示 Fragment
 * 用于加载央视网列表页和播放页
 */
class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var currentUrl: String = ""
    private var category: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUrl = it.getString("url", "https://tv.cctv.com/")
            category = it.getString("category", "")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        webView = view.findViewById(R.id.web_view)

        // WebView 配置
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            @Suppress("DEPRECATION")
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.userAgentString = settings.userAgentString + " CCTV-TV-App/1.0"

            // 电视专用 WebViewClient
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    progressBar.visibility = View.VISIBLE
                    currentUrl = url ?: currentUrl
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    injectTvScripts()
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    // 拦截播放页 URL，注入播放监听
                    view?.loadUrl(request?.url.toString())
                    return true
                }
            }

            // ChromeClient 处理视频全屏
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                    if (newProgress >= 100) {
                        progressBar.visibility = View.GONE
                    }
                }
            }

            // 添加 JS Bridge
            val activity = requireActivity()
            if (activity is MainActivity) {
                val bridge = JsBridge(activity.database)
                addJavascriptInterface(bridge, JsBridge.NAME)
            }

            // 加载央视页面
            loadUrl(currentUrl)
        }

        return view
    }

    /**
     * 注入 TV 优化脚本
     */
    private fun injectTvScripts() {
        try {
            // 注入焦点优化
            val focusJs = readAssetFile("tv_focus.js")
            if (focusJs != null) {
                webView.evaluateJavascript(focusJs, null)
            }
            // 注入播放监听
            val playerJs = readAssetFile("tv_player.js")
            if (playerJs != null) {
                webView.evaluateJavascript(playerJs, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readAssetFile(fileName: String): String? {
        return try {
            requireContext().assets.open(fileName)
                .bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 处理返回键 - WebView 页面回退
     */
    fun onBackPressed(): Boolean {
        return if (webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }
}
