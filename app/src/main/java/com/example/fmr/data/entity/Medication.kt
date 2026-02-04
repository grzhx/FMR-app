package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 药品实体
 * 对应数据库表 t_medication
 */
@Entity(
    tableName = "t_medication",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("member_id")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "member_id")
    val memberId: Long,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "generic_name")
    val genericName: String? = null,
    
    @ColumnInfo(name = "dosage")
    val dosage: String,
    
    @ColumnInfo(name = "frequency")
    val frequency: String,
    
    @ColumnInfo(name = "start_date")
    val startDate: String,
    
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,
    
    @ColumnInfo(name = "instructions")
    val instructions: String? = null,
    
    @ColumnInfo(name = "status")
    val status: Int = STATUS_ACTIVE,
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_ACTIVE = 1
        const val STATUS_STOPPED = 2
        const val STATUS_DELETED = 3
        
        const val FREQUENCY_DAILY_ONCE = "DAILY_ONCE"
        const val FREQUENCY_DAILY_TWICE = "DAILY_TWICE"
        const val FREQUENCY_DAILY_THREE = "DAILY_THREE"
        const val FREQUENCY_WEEKLY = "WEEKLY"
        const val FREQUENCY_AS_NEEDED = "AS_NEEDED"
    }
    
    fun getFrequencyText(): String {
        return when (frequency) {
            FREQUENCY_DAILY_ONCE -> "每日1次"
            FREQUENCY_DAILY_TWICE -> "每日2次"
            FREQUENCY_DAILY_THREE -> "每日3次"
            FREQUENCY_WEEKLY -> "每周1次"
            FREQUENCY_AS_NEEDED -> "按需服用"
            else -> frequency
        }
    }
    
    fun getStatusText(): String {
        return when (status) {
            STATUS_ACTIVE -> "使用中"
            STATUS_STOPPED -> "已停用"
            STATUS_DELETED -> "已删除"
            else -> "未知"
        }
    }
}
