package com.example.tvapp.js

import android.util.Log
import android.webkit.JavascriptInterface
import com.example.tvapp.data.AppDatabase
import com.example.tvapp.data.SearchHistory
import com.example.tvapp.data.WatchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class JsBridge(
    private val database: AppDatabase
) {

    companion object {
        private const val TAG = "JsBridge"
        const val NAME = "AndroidBridge"
    }

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
        GlobalScope.launch(Dispatchers.IO) {
            database.historyDao().saveProgress(history)
        }
    }

    @JavascriptInterface
    fun getProgress(videoUrl: String): String? {
        Log.d(TAG, "getProgress: $videoUrl")
        return try {
            val result = runBlocking(Dispatchers.IO) {
                database.historyDao().getProgress(videoUrl)
            }
            if (result != null && result.position > 0) {
                """{"title":"${result.title}","position":${result.position},"duration":${result.duration}}"""
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "getProgress error", e)
            null
        }
    }

    @JavascriptInterface
    fun showResumeTip(title: String, position: Int) {
        Log.d(TAG, "showResumeTip: $title 已播放到 $position 秒")
        onResumeTipCallback?.invoke(title, position)
    }

    private var onResumeTipCallback: ((String, Int) -> Unit)? = null
    fun setOnResumeTipCallback(callback: (String, Int) -> Unit) {
        onResumeTipCallback = callback
    }

    @JavascriptInterface
    fun saveSearchKeyword(keyword: String) {
        val search = SearchHistory(
            keyword = keyword,
            searchedAt = System.currentTimeMillis()
        )
        GlobalScope.launch(Dispatchers.IO) {
            database.historyDao().saveSearchKeyword(search)
        }
    }

    @JavascriptInterface
    fun getTvConfig(): String {
        return """{"platform":"android_tv","focusBorderColor":"#FF6600"}"""
    }
}
