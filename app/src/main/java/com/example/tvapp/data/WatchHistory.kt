package com.example.tvapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 观看历史记录实体 — 用于断点续播和继续观看列表
 */
@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey
    val videoUrl: String,           // 视频页面 URL（唯一标识）
    val title: String,              // 视频标题
    val coverUrl: String,           // 封面图 URL
    val position: Long,             // 当前播放位置（秒）
    val duration: Long,             // 视频总时长（秒）
    val progressPercent: Int,       // 进度百分比 0-100
    val updatedAt: Long,            // 最后更新时间（时间戳）
    val category: String = ""       // 分类（动画片/电视剧/纪录片）
)
