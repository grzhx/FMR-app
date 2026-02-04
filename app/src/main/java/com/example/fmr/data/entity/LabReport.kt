package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 检查报告实体
 * 对应数据库表 t_lab_report
 */
@Entity(
    tableName = "t_lab_report",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("member_id"), Index(value = ["member_id", "report_date"])]
)
data class LabReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "member_id")
    val memberId: Long,
    
    @ColumnInfo(name = "report_type")
    val reportType: String,
    
    @ColumnInfo(name = "hospital_name")
    val hospitalName: String? = null,
    
    @ColumnInfo(name = "report_date")
    val reportDate: String,
    
    @ColumnInfo(name = "interpretation")
    val interpretation: String? = null,
    
    @ColumnInfo(name = "abnormal_count")
    val abnormalCount: Int = 0,
    
    @ColumnInfo(name = "file_url")
    val fileUrl: String? = null,
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_BLOOD_ROUTINE = "血常规"
        const val TYPE_URINE_ROUTINE = "尿常规"
        const val TYPE_LIVER_FUNCTION = "肝功能"
        const val TYPE_KIDNEY_FUNCTION = "肾功能"
        const val TYPE_BLOOD_LIPID = "血脂"
        const val TYPE_BLOOD_SUGAR = "血糖"
        const val TYPE_THYROID = "甲状腺功能"
        const val TYPE_TUMOR_MARKER = "肿瘤标志物"
        const val TYPE_OTHER = "其他"
    }
}
