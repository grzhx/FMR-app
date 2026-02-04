package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 复诊计划实体
 * 对应数据库表 t_follow_up
 */
@Entity(
    tableName = "t_follow_up",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("member_id"), Index(value = ["member_id", "appointment_date"])]
)
data class FollowUp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "member_id")
    val memberId: Long,
    
    @ColumnInfo(name = "hospital_name")
    val hospitalName: String? = null,
    
    @ColumnInfo(name = "department")
    val department: String? = null,
    
    @ColumnInfo(name = "doctor_name")
    val doctorName: String? = null,
    
    @ColumnInfo(name = "appointment_date")
    val appointmentDate: String,
    
    @ColumnInfo(name = "appointment_time")
    val appointmentTime: String? = null,
    
    @ColumnInfo(name = "reason")
    val reason: String? = null,
    
    @ColumnInfo(name = "source")
    val source: String? = null,
    
    @ColumnInfo(name = "status")
    val status: Int = STATUS_PENDING,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_COMPLETED = 1
        const val STATUS_CANCELLED = 2
        
        const val SOURCE_DOCTOR = "医嘱"
        const val SOURCE_SELF = "自行安排"
        const val SOURCE_SYSTEM = "系统建议"
    }
    
    fun getStatusText(): String {
        return when (status) {
            STATUS_PENDING -> "待复诊"
            STATUS_COMPLETED -> "已完成"
            STATUS_CANCELLED -> "已取消"
            else -> "未知"
        }
    }
}
