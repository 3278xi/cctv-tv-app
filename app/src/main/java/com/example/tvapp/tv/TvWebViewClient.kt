package com.example.tvapp.tv

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 电视端 WebView 配置
 * 优化遥控器操作和视频播放
 */
class TvWebViewClient : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        // 页面加载完成后注入焦点优化脚本
        view?.let { injectTvScripts(it) }
    }

    /**
     * 注入 TV 优化脚本到 WebView
     */
    private fun injectTvScripts(webView: WebView) {
        try {
            // 1. 注入焦点优化
            val focusJs = loadAssetScript(webView, "tv_focus.js")
            if (focusJs != null) {
                webView.evaluateJavascript(focusJs, null)
            }
            // 2. 注入播放器监听
            val playerJs = loadAssetScript(webView, "tv_player.js")
            if (playerJs != null) {
                webView.evaluateJavascript(playerJs, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAssetScript(webView: WebView, fileName: String): String? {
        return try {
            val stream = webView.context.assets.open(fileName)
            stream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 电视端 WebChromeClient
 * 处理视频全屏和弹窗
 */
class TvWebChromeClient : WebChromeClient() {

    private var mCustomView: android.view.View? = null
    private var mCustomViewCallback: CustomViewCallback? = null

    override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
        // 视频全屏
        if (mCustomView != null) {
            callback?.onCustomViewHidden()
            return
        }
        mCustomView = view
        mCustomViewCallback = callback
        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        mCustomView?.let {
            mCustomViewCallback?.onCustomViewHidden()
            mCustomView = null
            mCustomViewCallback = null
        }
        super.onHideCustomView()
    }
}
