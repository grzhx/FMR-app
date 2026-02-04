package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 病历记录实体类
 * 对应数据库表 t_medical_record
 */
@Entity(
    tableName = "t_medical_record",
    foreignKeys = [
        ForeignKey(
            entity = FamilyMember::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["member_id"]),
        Index(value = ["family_id"]),
        Index(value = ["visit_date"])
    ]
)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "member_id")
    val memberId: Long,
    
    @ColumnInfo(name = "family_id")
    val familyId: Long = 1,
    
    @ColumnInfo(name = "record_type")
    val recordType: Int, // 1-门诊，2-住院，3-体检
    
    @ColumnInfo(name = "hospital_name")
    val hospitalName: String? = null,
    
    @ColumnInfo(name = "department")
    val department: String? = null,
    
    @ColumnInfo(name = "doctor_name")
    val doctorName: String? = null,
    
    @ColumnInfo(name = "visit_date")
    val visitDate: String? = null, // 格式：YYYY-MM-DD
    
    @ColumnInfo(name = "main_diagnosis")
    val mainDiagnosis: String? = null,
    
    @ColumnInfo(name = "completeness")
    val completeness: Int = 0, // 完整度百分比
    
    @ColumnInfo(name = "status")
    val status: Int = 1, // 1-正常，2-删除
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis()
) {
    companion object {
        // 记录类型常量
        const val TYPE_OUTPATIENT = 1  // 门诊
        const val TYPE_INPATIENT = 2   // 住院
        const val TYPE_CHECKUP = 3     // 体检
        
        // 状态常量
        const val STATUS_NORMAL = 1
        const val STATUS_DELETED = 2
    }
    
    /**
     * 获取记录类型显示文本
     */
    fun getRecordTypeText(): String = when (recordType) {
        TYPE_OUTPATIENT -> "门诊"
        TYPE_INPATIENT -> "住院"
        TYPE_CHECKUP -> "体检"
        else -> "未知"
    }
}
