package com.example.tvapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索历史记录实体
 */
@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,            // 搜索关键词
    val searchedAt: Long            // 搜索时间戳
)
