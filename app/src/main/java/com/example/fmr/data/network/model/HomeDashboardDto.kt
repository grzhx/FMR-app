package com.example.fmr.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * 首页仪表盘响应DTO
 */
data class HomeDashboardDto(
    @SerializedName("greeting")
    val greeting: String,
    
    @SerializedName("shortcuts")
    val shortcuts: List<ShortcutDto>?,
    
    @SerializedName("todayProgress")
    val todayProgress: TodayProgressDto?,
    
    @SerializedName("recentUpdates")
    val recentUpdates: List<RecentUpdateDto>?
)

/**
 * 快捷入口DTO
 */
data class ShortcutDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("icon")
    val icon: String?
)

/**
 * 今日进度DTO
 */
data class TodayProgressDto(
    @SerializedName("medication")
    val medication: ProgressItemDto?,
    
    @SerializedName("exercise")
    val exercise: ProgressItemDto?
)

/**
 * 进度项DTO
 */
data class ProgressItemDto(
    @SerializedName("target")
    val target: Int,
    
    @SerializedName("current")
    val current: Int,
    
    @SerializedName("unit")
    val unit: String
)

/**
 * 最近动态DTO
 */
data class RecentUpdateDto(
    @SerializedName("id")
    val id: Long?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("title")
    val title: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("time")
    val time: String?
)
