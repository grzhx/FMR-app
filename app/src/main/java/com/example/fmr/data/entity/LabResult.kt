package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 检查指标明细实体
 * 对应数据库表 t_lab_result
 */
@Entity(
    tableName = "t_lab_result",
    foreignKeys = [
        ForeignKey(
            entity = LabReport::class,
            parentColumns = ["id"],
            childColumns = ["report_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("report_id"), Index("item_name")]
)
data class LabResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "report_id")
    val reportId: Long,
    
    @ColumnInfo(name = "item_name")
    val itemName: String,
    
    @ColumnInfo(name = "item_value")
    val itemValue: String,
    
    @ColumnInfo(name = "item_unit")
    val itemUnit: String? = null,
    
    @ColumnInfo(name = "reference_range")
    val referenceRange: String? = null,
    
    @ColumnInfo(name = "status")
    val status: Int = STATUS_NORMAL,
    
    @ColumnInfo(name = "trend")
    val trend: Int? = null
) {
    companion object {
        const val STATUS_NORMAL = 1
        const val STATUS_HIGH = 2
        const val STATUS_LOW = 3
        const val STATUS_CRITICAL = 4
        
        const val TREND_UP = 1
        const val TREND_DOWN = 2
        const val TREND_STABLE = 3
    }
    
    fun getStatusText(): String {
        return when (status) {
            STATUS_NORMAL -> "正常"
            STATUS_HIGH -> "偏高"
            STATUS_LOW -> "偏低"
            STATUS_CRITICAL -> "危急"
            else -> "未知"
        }
    }
    
    fun getTrendText(): String? {
        return when (trend) {
            TREND_UP -> "↑ 上升"
            TREND_DOWN -> "↓ 下降"
            TREND_STABLE -> "→ 稳定"
            else -> null
        }
    }
    
    fun isAbnormal(): Boolean {
        return status != STATUS_NORMAL
    }
}
