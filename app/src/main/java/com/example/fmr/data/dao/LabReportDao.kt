package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.LabReport
import com.example.fmr.data.entity.LabResult
import kotlinx.coroutines.flow.Flow

/**
 * 检查报告数据访问对象
 */
@Dao
interface LabReportDao {
    
    // ==================== 报告操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: LabReport): Long
    
    @Update
    suspend fun updateReport(report: LabReport)
    
    @Delete
    suspend fun deleteReport(report: LabReport)
    
    @Query("SELECT * FROM t_lab_report WHERE id = :id")
    suspend fun getReportById(id: Long): LabReport?
    
    @Query("SELECT * FROM t_lab_report WHERE member_id = :memberId ORDER BY report_date DESC")
    fun getReportsByMember(memberId: Long): Flow<List<LabReport>>
    
    @Query("SELECT * FROM t_lab_report WHERE member_id = :memberId ORDER BY report_date DESC")
    suspend fun getReportsByMemberSync(memberId: Long): List<LabReport>
    
    @Query("SELECT * FROM t_lab_report ORDER BY report_date DESC")
    fun getAllReports(): Flow<List<LabReport>>
    
    @Query("SELECT * FROM t_lab_report ORDER BY report_date DESC")
    suspend fun getAllReportsSync(): List<LabReport>
    
    @Query("SELECT * FROM t_lab_report ORDER BY report_date DESC LIMIT :limit")
    suspend fun getRecentReports(limit: Int): List<LabReport>
    
    @Query("SELECT * FROM t_lab_report WHERE member_id = :memberId AND report_type = :reportType ORDER BY report_date DESC")
    fun getReportsByType(memberId: Long, reportType: String): Flow<List<LabReport>>
    
    @Query("SELECT COUNT(*) FROM t_lab_report WHERE member_id = :memberId")
    suspend fun getReportCount(memberId: Long): Int
    
    @Query("SELECT COUNT(*) FROM t_lab_report")
    suspend fun getTotalReportCount(): Int
    
    @Query("SELECT SUM(abnormal_count) FROM t_lab_report WHERE member_id = :memberId")
    suspend fun getTotalAbnormalCount(memberId: Long): Int?
    
    // ==================== 检查结果操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: LabResult): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<LabResult>)
    
    @Update
    suspend fun updateResult(result: LabResult)
    
    @Delete
    suspend fun deleteResult(result: LabResult)
    
    @Query("SELECT * FROM t_lab_result WHERE report_id = :reportId ORDER BY id")
    fun getResultsByReport(reportId: Long): Flow<List<LabResult>>
    
    @Query("SELECT * FROM t_lab_result WHERE report_id = :reportId ORDER BY id")
    suspend fun getResultsByReportSync(reportId: Long): List<LabResult>
    
    @Query("SELECT * FROM t_lab_result WHERE report_id = :reportId AND status != 1 ORDER BY status DESC")
    fun getAbnormalResultsByReport(reportId: Long): Flow<List<LabResult>>
    
    @Query("SELECT * FROM t_lab_result WHERE report_id = :reportId AND status != 1 ORDER BY status DESC")
    suspend fun getAbnormalResultsByReportSync(reportId: Long): List<LabResult>
    
    @Query("DELETE FROM t_lab_result WHERE report_id = :reportId")
    suspend fun deleteResultsByReport(reportId: Long)
    
    // ==================== 趋势分析查询 ====================
    
    @Query("""
        SELECT lr.* FROM t_lab_result lr
        INNER JOIN t_lab_report lrp ON lr.report_id = lrp.id
        WHERE lrp.member_id = :memberId AND lr.item_name = :itemName
        ORDER BY lrp.report_date ASC
    """)
    suspend fun getResultTrend(memberId: Long, itemName: String): List<LabResult>
    
    @Query("""
        SELECT DISTINCT lr.item_name FROM t_lab_result lr
        INNER JOIN t_lab_report lrp ON lr.report_id = lrp.id
        WHERE lrp.member_id = :memberId
    """)
    suspend fun getDistinctItemNames(memberId: Long): List<String>
}

/**
 * 报告与结果的联合查询结果
 */
data class ReportWithResults(
    @Embedded val report: LabReport,
    @Relation(
        parentColumn = "id",
        entityColumn = "report_id"
    )
    val results: List<LabResult>
)
