package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 服药时刻表实体
 * 对应数据库表 t_medication_schedule
 */
@Entity(
    tableName = "t_medication_schedule",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medication_id"), Index("schedule_date")]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "medication_id")
    val medicationId: Long,
    
    @ColumnInfo(name = "schedule_date")
    val scheduleDate: String,
    
    @ColumnInfo(name = "schedule_time")
    val scheduleTime: String,
    
    @ColumnInfo(name = "period")
    val period: Int,
    
    @ColumnInfo(name = "taken")
    val taken: Boolean = false,
    
    @ColumnInfo(name = "taken_time")
    val takenTime: Long? = null,
    
    @ColumnInfo(name = "notify_status")
    val notifyStatus: Int = NOTIFY_PENDING
) {
    companion object {
        const val PERIOD_MORNING = 1
        const val PERIOD_NOON = 2
        const val PERIOD_EVENING = 3
        const val PERIOD_BEDTIME = 4
        
        const val NOTIFY_PENDING = 0
        const val NOTIFY_SENT = 1
        const val NOTIFY_FAILED = 2
    }
    
    fun getPeriodText(): String {
        return when (period) {
            PERIOD_MORNING -> "早上"
            PERIOD_NOON -> "中午"
            PERIOD_EVENING -> "晚上"
            PERIOD_BEDTIME -> "睡前"
            else -> "其他"
        }
    }
}
