package com.example.fmr.data.repository

import com.example.fmr.data.dao.LabReportDao
import com.example.fmr.data.entity.LabReport
import com.example.fmr.data.entity.LabResult
import kotlinx.coroutines.flow.Flow

/**
 * 检查报告数据仓库
 */
class LabReportRepository(private val labReportDao: LabReportDao) {
    
    // ==================== 报告操作 ====================
    
    suspend fun addReport(report: LabReport): Long {
        return labReportDao.insertReport(report)
    }
    
    suspend fun updateReport(report: LabReport) {
        labReportDao.updateReport(report)
    }
    
    suspend fun deleteReport(report: LabReport) {
        // 先删除关联的结果
        labReportDao.deleteResultsByReport(report.id)
        // 再删除报告
        labReportDao.deleteReport(report)
    }
    
    suspend fun getReportById(id: Long): LabReport? {
        return labReportDao.getReportById(id)
    }
    
    fun getReportsByMember(memberId: Long): Flow<List<LabReport>> {
        return labReportDao.getReportsByMember(memberId)
    }
    
    suspend fun getReportsByMemberSync(memberId: Long): List<LabReport> {
        return labReportDao.getReportsByMemberSync(memberId)
    }
    
    fun getAllReports(): Flow<List<LabReport>> {
        return labReportDao.getAllReports()
    }
    
    suspend fun getAllReportsSync(): List<LabReport> {
        return labReportDao.getAllReportsSync()
    }
    
    suspend fun getRecentReports(limit: Int = 5): List<LabReport> {
        return labReportDao.getRecentReports(limit)
    }
    
    fun getReportsByType(memberId: Long, reportType: String): Flow<List<LabReport>> {
        return labReportDao.getReportsByType(memberId, reportType)
    }
    
    suspend fun getReportCount(memberId: Long): Int {
        return labReportDao.getReportCount(memberId)
    }
    
    suspend fun getTotalReportCount(): Int {
        return labReportDao.getTotalReportCount()
    }
    
    // ==================== 检查结果操作 ====================
    
    suspend fun addResult(result: LabResult): Long {
        return labReportDao.insertResult(result)
    }
    
    suspend fun addResults(results: List<LabResult>) {
        labReportDao.insertResults(results)
    }
    
    fun getResultsByReport(reportId: Long): Flow<List<LabResult>> {
        return labReportDao.getResultsByReport(reportId)
    }
    
    suspend fun getResultsByReportSync(reportId: Long): List<LabResult> {
        return labReportDao.getResultsByReportSync(reportId)
    }
    
    fun getAbnormalResultsByReport(reportId: Long): Flow<List<LabResult>> {
        return labReportDao.getAbnormalResultsByReport(reportId)
    }
    
    suspend fun getAbnormalResultsByReportSync(reportId: Long): List<LabResult> {
        return labReportDao.getAbnormalResultsByReportSync(reportId)
    }
    
    // ==================== 趋势分析 ====================
    
    suspend fun getResultTrend(memberId: Long, itemName: String): List<LabResult> {
        return labReportDao.getResultTrend(memberId, itemName)
    }
    
    suspend fun getDistinctItemNames(memberId: Long): List<String> {
        return labReportDao.getDistinctItemNames(memberId)
    }
    
    // ==================== 添加报告及结果 ====================
    
    /**
     * 添加报告及其检查结果
     */
    suspend fun addReportWithResults(report: LabReport, results: List<LabResult>): Long {
        // 计算异常指标数量
        val abnormalCount = results.count { it.isAbnormal() }
        val reportWithCount = report.copy(abnormalCount = abnormalCount)
        
        // 插入报告
        val reportId = labReportDao.insertReport(reportWithCount)
        
        // 插入结果（关联报告ID）
        val resultsWithReportId = results.map { it.copy(reportId = reportId) }
        labReportDao.insertResults(resultsWithReportId)
        
        return reportId
    }
    
    /**
     * 获取报告详情（包含所有结果）
     */
    suspend fun getReportDetail(reportId: Long): Pair<LabReport?, List<LabResult>> {
        val report = labReportDao.getReportById(reportId)
        val results = labReportDao.getResultsByReportSync(reportId)
        return Pair(report, results)
    }
    
    /**
     * 获取成员的异常指标总数
     */
    suspend fun getTotalAbnormalCount(memberId: Long): Int {
        return labReportDao.getTotalAbnormalCount(memberId) ?: 0
    }
}
