package com.example.fmr.data.repository

import android.util.Log
import com.example.fmr.data.dao.DocumentDao
import com.example.fmr.data.dao.MedicalRecordDao
import com.example.fmr.data.entity.Document
import com.example.fmr.data.entity.MedicalRecord
import com.example.fmr.data.network.RemoteDataSource
import com.example.fmr.data.network.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 病历记录仓库
 * 负责病历记录和文书的数据管理
 */
class MedicalRecordRepository(
    private val medicalRecordDao: MedicalRecordDao,
    private val documentDao: DocumentDao,
    private val remoteDataSource: RemoteDataSource? = null
) {
    
    // ==================== 病历记录相关 ====================
    
    /**
     * 获取所有病历记录
     */
    fun getAllRecords(): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getAllRecords()
    }
    
    /**
     * 获取所有病历记录（同步）
     */
    suspend fun getAllRecordsSync(): List<MedicalRecord> {
        return medicalRecordDao.getAllRecordsSync()
    }
    
    /**
     * 根据成员ID获取病历记录
     */
    fun getRecordsByMemberId(memberId: Long): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getRecordsByMemberId(memberId)
    }
    
    /**
     * 根据成员ID获取病历记录（同步）
     */
    suspend fun getRecordsByMemberIdSync(memberId: Long): List<MedicalRecord> {
        return medicalRecordDao.getRecordsByMemberIdSync(memberId)
    }
    
    /**
     * 根据ID获取病历记录
     */
    suspend fun getRecordById(recordId: Long): MedicalRecord? {
        return medicalRecordDao.getRecordById(recordId)
    }
    
    /**
     * 根据ID获取病历记录（Flow）
     */
    fun getRecordByIdFlow(recordId: Long): Flow<MedicalRecord?> {
        return medicalRecordDao.getRecordByIdFlow(recordId)
    }
    
    /**
     * 添加病历记录
     */
    suspend fun addRecord(record: MedicalRecord): Long {
        return medicalRecordDao.insert(record)
    }
    
    /**
     * 更新病历记录
     */
    suspend fun updateRecord(record: MedicalRecord) {
        medicalRecordDao.update(record.copy(updateTime = System.currentTimeMillis()))
    }
    
    /**
     * 删除病历记录（软删除）
     */
    suspend fun deleteRecord(recordId: Long) {
        medicalRecordDao.softDelete(recordId)
    }
    
    /**
     * 搜索病历记录
     */
    suspend fun searchRecords(memberId: Long, keyword: String): List<MedicalRecord> {
        return medicalRecordDao.searchRecords(memberId, keyword)
    }
    
    /**
     * 获取最近的病历记录
     */
    suspend fun getRecentRecords(memberId: Long, limit: Int = 5): List<MedicalRecord> {
        return medicalRecordDao.getRecentRecords(memberId, limit)
    }
    
    /**
     * 获取所有成员的最近病历记录
     */
    suspend fun getAllRecentRecords(limit: Int = 10): List<MedicalRecord> {
        return medicalRecordDao.getAllRecentRecords(limit)
    }
    
    /**
     * 统计成员病历数量
     */
    suspend fun countRecordsByMemberId(memberId: Long): Int {
        return medicalRecordDao.countRecordsByMemberId(memberId)
    }
    
    // ==================== 文书相关 ====================
    
    /**
     * 获取病历记录的所有文书
     */
    fun getDocumentsByRecordId(recordId: Long): Flow<List<Document>> {
        return documentDao.getDocumentsByRecordId(recordId)
    }
    
    /**
     * 获取病历记录的所有文书（同步）
     */
    suspend fun getDocumentsByRecordIdSync(recordId: Long): List<Document> {
        return documentDao.getDocumentsByRecordIdSync(recordId)
    }
    
    /**
     * 添加文书
     */
    suspend fun addDocument(document: Document): Long {
        return documentDao.insert(document)
    }
    
    /**
     * 批量添加文书
     */
    suspend fun addDocuments(documents: List<Document>): List<Long> {
        return documentDao.insertAll(documents)
    }
    
    /**
     * 更新文书
     */
    suspend fun updateDocument(document: Document) {
        documentDao.update(document)
    }
    
    /**
     * 删除文书
     */
    suspend fun deleteDocument(documentId: Long) {
        documentDao.deleteById(documentId)
    }
    
    /**
     * 统计病历记录的文书数量
     */
    suspend fun countDocumentsByRecordId(recordId: Long): Int {
        return documentDao.countDocumentsByRecordId(recordId)
    }
    
    /**
     * 更新OCR状态
     */
    suspend fun updateOcrStatus(documentId: Long, status: Int, ocrText: String?) {
        documentDao.updateOcrStatus(documentId, status, ocrText)
    }
    
    /**
     * 更新抽取状态
     */
    suspend fun updateExtractStatus(documentId: Long, status: Int) {
        documentDao.updateExtractStatus(documentId, status)
    }
    
    /**
     * 创建病历记录并添加文书
     */
    suspend fun createRecordWithDocuments(record: MedicalRecord, documents: List<Document>): Long {
        val recordId = medicalRecordDao.insert(record)
        val documentsWithRecordId = documents.map { it.copy(recordId = recordId) }
        documentDao.insertAll(documentsWithRecordId)
        return recordId
    }
    
    // ==================== 远程API调用 ====================
    
    companion object {
        private const val TAG = "FMR_RecordRepo"
    }
    
    /**
     * 创建导入任务
     */
    suspend fun importRecords(request: ImportRecordRequest): Result<ImportTaskDto> {
        Log.d(TAG, "importRecords 调用, remoteDataSource=${remoteDataSource != null}")
        Log.d(TAG, "请求: memberId=${request.memberId}, files=${request.files.size}")
        
        if (remoteDataSource == null) {
            Log.w(TAG, "❌ remoteDataSource 为 null")
            return Result.failure(Exception("远程服务不可用"))
        }
        
        val result = remoteDataSource.importRecords(request)
        Log.d(TAG, "importRecords 结果: isSuccess=${result.isSuccess}, isFailure=${result.isFailure}")
        if (result.isFailure) {
            Log.e(TAG, "❌ 失败原因: ${result.exceptionOrNull()?.message}")
        } else {
            Log.d(TAG, "✅ 成功: ${result.getOrNull()}")
        }
        return result
    }
    
    /**
     * 查询任务状态
     */
    suspend fun getTaskStatus(taskId: String): Result<TaskStatusDto> {
        Log.d(TAG, "getTaskStatus 调用, taskId=$taskId")
        
        if (remoteDataSource == null) {
            Log.w(TAG, "❌ remoteDataSource 为 null")
            return Result.failure(Exception("远程服务不可用"))
        }
        
        val result = remoteDataSource.getTaskStatus(taskId)
        Log.d(TAG, "getTaskStatus 结果: isSuccess=${result.isSuccess}")
        return result
    }
    
    /**
     * 从远程获取病历列表
     */
    suspend fun getRecordListFromRemote(memberId: Long, page: Int = 1): Result<RecordListDto> {
        Log.d(TAG, "getRecordListFromRemote 调用, memberId=$memberId, page=$page")
        
        if (remoteDataSource == null) {
            Log.w(TAG, "❌ remoteDataSource 为 null")
            return Result.failure(Exception("远程服务不可用"))
        }
        
        return remoteDataSource.getRecordList(memberId, page)
    }
}
