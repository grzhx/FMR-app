package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 每日目标实体
 * 对应数据库表 t_daily_goal
 */
@Entity(
    tableName = "t_daily_goal",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("member_id"), Index(value = ["member_id", "goal_date", "type"], unique = true)]
)
data class DailyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "member_id")
    val memberId: Long,
    
    @ColumnInfo(name = "goal_date")
    val goalDate: String,
    
    @ColumnInfo(name = "type")
    val type: String,
    
    @ColumnInfo(name = "target_val")
    val targetVal: Int,
    
    @ColumnInfo(name = "current_val")
    val currentVal: Int = 0,
    
    @ColumnInfo(name = "unit")
    val unit: String? = null
) {
    companion object {
        const val TYPE_EXERCISE = "exercise"
        const val TYPE_WATER = "water"
        const val TYPE_SLEEP = "sleep"
        const val TYPE_STEPS = "steps"
    }
    
    fun getTypeText(): String {
        return when (type) {
            TYPE_EXERCISE -> "运动"
            TYPE_WATER -> "饮水"
            TYPE_SLEEP -> "睡眠"
            TYPE_STEPS -> "步数"
            else -> type
        }
    }
    
    fun getUnitText(): String {
        return unit ?: when (type) {
            TYPE_EXERCISE -> "分钟"
            TYPE_WATER -> "ml"
            TYPE_SLEEP -> "小时"
            TYPE_STEPS -> "步"
            else -> ""
        }
    }
    
    fun getProgress(): Float {
        return if (targetVal > 0) {
            (currentVal.toFloat() / targetVal).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    fun isCompleted(): Boolean {
        return currentVal >= targetVal
    }
}
