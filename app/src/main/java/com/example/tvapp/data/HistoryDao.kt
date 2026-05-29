package com.example.tvapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    // ====== 观看历史 ======

    /** 保存/更新观看进度 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(history: WatchHistory)

    /** 获取单个视频的进度 */
    @Query("SELECT * FROM watch_history WHERE videoUrl = :videoUrl LIMIT 1")
    suspend fun getProgress(videoUrl: String): WatchHistory?

    /** 获取所有观看记录（按更新时间倒序） */
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC")
    suspend fun getAllHistory(): List<WatchHistory>

    /** 获取有进度的记录（用于继续观看列表） */
    @Query("SELECT * FROM watch_history WHERE position > 0 AND position < duration ORDER BY updatedAt DESC")
    suspend fun getContinueWatching(): List<WatchHistory>

    /** 删除单条记录 */
    @Query("DELETE FROM watch_history WHERE videoUrl = :videoUrl")
    suspend fun deleteProgress(videoUrl: String)

    /** 清空所有观看记录 */
    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    // ====== 搜索历史 ======

    /** 保存搜索关键词 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSearchKeyword(search: SearchHistory)

    /** 获取搜索历史（按时间倒序） */
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    suspend fun getSearchHistory(): List<SearchHistory>

    /** 清空搜索历史 */
    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
