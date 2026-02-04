package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.MedicalRecord
import kotlinx.coroutines.flow.Flow

/**
 * 病历记录数据访问对象
 */
@Dao
interface MedicalRecordDao {
    
    /**
     * 插入病历记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecord): Long
    
    /**
     * 批量插入病历记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MedicalRecord>): List<Long>
    
    /**
     * 更新病历记录
     */
    @Update
    suspend fun update(record: MedicalRecord)
    
    /**
     * 删除病历记录
     */
    @Delete
    suspend fun delete(record: MedicalRecord)
    
    /**
     * 根据ID删除病历记录
     */
    @Query("DELETE FROM t_medical_record WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
    
    /**
     * 根据ID获取病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): MedicalRecord?
    
    /**
     * 根据ID获取病历记录（Flow）
     */
    @Query("SELECT * FROM t_medical_record WHERE id = :recordId")
    fun getRecordByIdFlow(recordId: Long): Flow<MedicalRecord?>
    
    /**
     * 获取成员的所有病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 ORDER BY visit_date DESC, create_time DESC")
    fun getRecordsByMemberId(memberId: Long): Flow<List<MedicalRecord>>
    
    /**
     * 获取成员的所有病历记录（非Flow）
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 ORDER BY visit_date DESC, create_time DESC")
    suspend fun getRecordsByMemberIdSync(memberId: Long): List<MedicalRecord>
    
    /**
     * 获取家庭的所有病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE family_id = :familyId AND status = 1 ORDER BY visit_date DESC, create_time DESC")
    fun getRecordsByFamilyId(familyId: Long): Flow<List<MedicalRecord>>
    
    /**
     * 获取所有病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE status = 1 ORDER BY visit_date DESC, create_time DESC")
    fun getAllRecords(): Flow<List<MedicalRecord>>
    
    /**
     * 获取所有病历记录（非Flow）
     */
    @Query("SELECT * FROM t_medical_record WHERE status = 1 ORDER BY visit_date DESC, create_time DESC")
    suspend fun getAllRecordsSync(): List<MedicalRecord>
    
    /**
     * 根据类型获取病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND record_type = :recordType AND status = 1 ORDER BY visit_date DESC")
    suspend fun getRecordsByType(memberId: Long, recordType: Int): List<MedicalRecord>
    
    /**
     * 根据医院名称搜索病历
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 AND hospital_name LIKE '%' || :keyword || '%' ORDER BY visit_date DESC")
    suspend fun searchByHospital(memberId: Long, keyword: String): List<MedicalRecord>
    
    /**
     * 根据诊断搜索病历
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 AND main_diagnosis LIKE '%' || :keyword || '%' ORDER BY visit_date DESC")
    suspend fun searchByDiagnosis(memberId: Long, keyword: String): List<MedicalRecord>
    
    /**
     * 综合搜索病历
     */
    @Query("""
        SELECT * FROM t_medical_record 
        WHERE member_id = :memberId AND status = 1 
        AND (hospital_name LIKE '%' || :keyword || '%' 
             OR department LIKE '%' || :keyword || '%' 
             OR main_diagnosis LIKE '%' || :keyword || '%')
        ORDER BY visit_date DESC
    """)
    suspend fun searchRecords(memberId: Long, keyword: String): List<MedicalRecord>
    
    /**
     * 获取指定日期范围内的病历
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 AND visit_date BETWEEN :startDate AND :endDate ORDER BY visit_date DESC")
    suspend fun getRecordsByDateRange(memberId: Long, startDate: String, endDate: String): List<MedicalRecord>
    
    /**
     * 统计成员病历数量
     */
    @Query("SELECT COUNT(*) FROM t_medical_record WHERE member_id = :memberId AND status = 1")
    suspend fun countRecordsByMemberId(memberId: Long): Int
    
    /**
     * 软删除病历（更新状态为已删除）
     */
    @Query("UPDATE t_medical_record SET status = 2, update_time = :updateTime WHERE id = :recordId")
    suspend fun softDelete(recordId: Long, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 获取最近的病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE member_id = :memberId AND status = 1 ORDER BY visit_date DESC, create_time DESC LIMIT :limit")
    suspend fun getRecentRecords(memberId: Long, limit: Int): List<MedicalRecord>
    
    /**
     * 获取所有成员的最近病历记录
     */
    @Query("SELECT * FROM t_medical_record WHERE status = 1 ORDER BY visit_date DESC, create_time DESC LIMIT :limit")
    suspend fun getAllRecentRecords(limit: Int): List<MedicalRecord>
}
