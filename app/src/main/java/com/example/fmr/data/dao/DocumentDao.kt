package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.Document
import kotlinx.coroutines.flow.Flow

/**
 * 病历文书数据访问对象
 */
@Dao
interface DocumentDao {
    
    /**
     * 插入文书
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: Document): Long
    
    /**
     * 批量插入文书
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<Document>): List<Long>
    
    /**
     * 更新文书
     */
    @Update
    suspend fun update(document: Document)
    
    /**
     * 删除文书
     */
    @Delete
    suspend fun delete(document: Document)
    
    /**
     * 根据ID删除文书
     */
    @Query("DELETE FROM t_document WHERE id = :documentId")
    suspend fun deleteById(documentId: Long)
    
    /**
     * 根据ID获取文书
     */
    @Query("SELECT * FROM t_document WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Long): Document?
    
    /**
     * 根据ID获取文书（Flow）
     */
    @Query("SELECT * FROM t_document WHERE id = :documentId")
    fun getDocumentByIdFlow(documentId: Long): Flow<Document?>
    
    /**
     * 获取病历记录的所有文书
     */
    @Query("SELECT * FROM t_document WHERE record_id = :recordId ORDER BY upload_time DESC")
    fun getDocumentsByRecordId(recordId: Long): Flow<List<Document>>
    
    /**
     * 获取病历记录的所有文书（非Flow）
     */
    @Query("SELECT * FROM t_document WHERE record_id = :recordId ORDER BY upload_time DESC")
    suspend fun getDocumentsByRecordIdSync(recordId: Long): List<Document>
    
    /**
     * 统计病历记录的文书数量
     */
    @Query("SELECT COUNT(*) FROM t_document WHERE record_id = :recordId")
    suspend fun countDocumentsByRecordId(recordId: Long): Int
    
    /**
     * 根据文书类型获取文书
     */
    @Query("SELECT * FROM t_document WHERE record_id = :recordId AND doc_type = :docType ORDER BY upload_time DESC")
    suspend fun getDocumentsByType(recordId: Long, docType: String): List<Document>
    
    /**
     * 获取待OCR处理的文书
     */
    @Query("SELECT * FROM t_document WHERE ocr_status = 0 ORDER BY upload_time ASC")
    suspend fun getPendingOcrDocuments(): List<Document>
    
    /**
     * 获取待抽取的文书
     */
    @Query("SELECT * FROM t_document WHERE ocr_status = 1 AND extract_status = 0 ORDER BY upload_time ASC")
    suspend fun getPendingExtractDocuments(): List<Document>
    
    /**
     * 更新OCR状态
     */
    @Query("UPDATE t_document SET ocr_status = :status, ocr_text = :ocrText WHERE id = :documentId")
    suspend fun updateOcrStatus(documentId: Long, status: Int, ocrText: String?)
    
    /**
     * 更新抽取状态
     */
    @Query("UPDATE t_document SET extract_status = :status WHERE id = :documentId")
    suspend fun updateExtractStatus(documentId: Long, status: Int)
    
    /**
     * 获取所有文书
     */
    @Query("SELECT * FROM t_document ORDER BY upload_time DESC")
    fun getAllDocuments(): Flow<List<Document>>
    
    /**
     * 获取所有文书（非Flow）
     */
    @Query("SELECT * FROM t_document ORDER BY upload_time DESC")
    suspend fun getAllDocumentsSync(): List<Document>
    
    /**
     * 根据文件格式获取文书
     */
    @Query("SELECT * FROM t_document WHERE file_format = :format ORDER BY upload_time DESC")
    suspend fun getDocumentsByFormat(format: String): List<Document>
    
    /**
     * 删除病历记录的所有文书
     */
    @Query("DELETE FROM t_document WHERE record_id = :recordId")
    suspend fun deleteByRecordId(recordId: Long)
}
