package com.example.tvapp.js

import android.util.Log
import android.webkit.JavascriptInterface
import com.example.tvapp.data.AppDatabase
import com.example.tvapp.data.SearchHistory
import com.example.tvapp.data.WatchHistory

/**
 * JavaScript ↔ 原生通信桥
 * 通过 @JavascriptInterface 暴露给 WebView 中的 JS 调用
 */
class JsBridge(
    private val database: AppDatabase
) {

    companion object {
        private const val TAG = "JsBridge"
        const val NAME = "AndroidBridge"
    }

    // ====== 断点续播 ======

    /**
     * JS 调用：保存播放进度
     */
    @JavascriptInterface
    fun saveProgress(videoUrl: String, title: String, coverUrl: String, position: Int, duration: Int) {
        Log.d(TAG, "saveProgress: $title $position/$duration")
        val percent = if (duration > 0) (position * 100 / duration) else 0
        val history = WatchHistory(
            videoUrl = videoUrl,
            title = title,
            coverUrl = coverUrl,
            position = position.toLong(),
            duration = duration.toLong(),
            progressPercent = percent,
            updatedAt = System.currentTimeMillis()
        )
        Thread {
            database.historyDao().saveProgress(history)
        }.start()
    }

    /**
     * JS 调用：获取视频进度
     */
    @JavascriptInterface
    fun getProgress(videoUrl: String): String? {
        Log.d(TAG, "getProgress: $videoUrl")
        // 同步查询（Room 不支持在非协程直接返回，这里简化处理）
        // 实际项目中应使用 LiveData/callback 模式
        try {
            val result = database.historyDao().getProgress(videoUrl)
            if (result != null && result.position > 0) {
                return """{"title":"${result.title}","position":${result.position},"duration":${result.duration}}"""
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProgress error", e)
        }
        return null
    }

    /**
     * JS 调用：显示恢复播放提示
     */
    @JavascriptInterface
    fun showResumeTip(title: String, position: Int) {
        Log.d(TAG, "showResumeTip: $title 已播放到 $position 秒")
        // 原生 Toast 或 Snackbar 提示
        // 通过回调交给 UI 层处理
        onResumeTipCallback?.invoke(title, position)
    }

    private var onResumeTipCallback: ((String, Int) -> Unit)? = null

    fun setOnResumeTipCallback(callback: (String, Int) -> Unit) {
        onResumeTipCallback = callback
    }

    // ====== 搜索历史 ======

    /**
     * JS 调用：保存搜索关键词
     */
    @JavascriptInterface
    fun saveSearchKeyword(keyword: String) {
        val search = SearchHistory(
            keyword = keyword,
            searchedAt = System.currentTimeMillis()
        )
        Thread {
            database.historyDao().saveSearchKeyword(search)
        }.start()
    }

    // ====== 页面信息 ======

    /**
     * JS 调用：获取 TV 配置信息
     */
    @JavascriptInterface
    fun getTvConfig(): String {
        return """{"platform":"android_tv","focusBorderColor":"#FF6600"}"""
    }
}
