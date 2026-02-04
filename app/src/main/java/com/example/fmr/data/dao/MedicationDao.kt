package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.Medication
import com.example.fmr.data.entity.MedicationSchedule
import kotlinx.coroutines.flow.Flow

/**
 * 药品数据访问对象
 */
@Dao
interface MedicationDao {
    
    // ==================== 药品操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long
    
    @Update
    suspend fun updateMedication(medication: Medication)
    
    @Delete
    suspend fun deleteMedication(medication: Medication)
    
    @Query("SELECT * FROM t_medication WHERE id = :id")
    suspend fun getMedicationById(id: Long): Medication?
    
    @Query("SELECT * FROM t_medication WHERE member_id = :memberId AND status = 1 ORDER BY create_time DESC")
    fun getActiveMedicationsByMember(memberId: Long): Flow<List<Medication>>
    
    @Query("SELECT * FROM t_medication WHERE member_id = :memberId ORDER BY create_time DESC")
    fun getAllMedicationsByMember(memberId: Long): Flow<List<Medication>>
    
    @Query("SELECT * FROM t_medication WHERE status = 1 ORDER BY create_time DESC")
    fun getAllActiveMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM t_medication WHERE status = 1 ORDER BY create_time DESC")
    suspend fun getAllActiveMedicationsSync(): List<Medication>
    
    @Query("UPDATE t_medication SET status = :status, update_time = :updateTime WHERE id = :id")
    suspend fun updateMedicationStatus(id: Long, status: Int, updateTime: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM t_medication WHERE member_id = :memberId AND status = 1")
    suspend fun getActiveMedicationCount(memberId: Long): Int
    
    // ==================== 服药时刻表操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MedicationSchedule): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MedicationSchedule>)
    
    @Update
    suspend fun updateSchedule(schedule: MedicationSchedule)
    
    @Delete
    suspend fun deleteSchedule(schedule: MedicationSchedule)
    
    @Query("SELECT * FROM t_medication_schedule WHERE id = :id")
    suspend fun getScheduleById(id: Long): MedicationSchedule?
    
    @Query("SELECT * FROM t_medication_schedule WHERE medication_id = :medicationId ORDER BY schedule_date, schedule_time")
    fun getSchedulesByMedication(medicationId: Long): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM t_medication_schedule WHERE schedule_date = :date ORDER BY schedule_time")
    fun getSchedulesByDate(date: String): Flow<List<MedicationSchedule>>
    
    @Query("SELECT * FROM t_medication_schedule WHERE schedule_date = :date ORDER BY schedule_time")
    suspend fun getSchedulesByDateSync(date: String): List<MedicationSchedule>
    
    @Query("UPDATE t_medication_schedule SET taken = 1, taken_time = :takenTime WHERE id = :id")
    suspend fun markAsTaken(id: Long, takenTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE t_medication_schedule SET taken = 0, taken_time = NULL WHERE id = :id")
    suspend fun markAsNotTaken(id: Long)
    
    @Query("SELECT COUNT(*) FROM t_medication_schedule WHERE schedule_date = :date")
    suspend fun getTotalScheduleCount(date: String): Int
    
    @Query("SELECT COUNT(*) FROM t_medication_schedule WHERE schedule_date = :date AND taken = 1")
    suspend fun getCompletedScheduleCount(date: String): Int
    
    @Query("DELETE FROM t_medication_schedule WHERE medication_id = :medicationId")
    suspend fun deleteSchedulesByMedication(medicationId: Long)
    
    @Query("DELETE FROM t_medication_schedule WHERE schedule_date < :date")
    suspend fun deleteOldSchedules(date: String)
}

/**
 * 药品与时刻表的联合查询结果
 */
data class MedicationWithSchedule(
    @Embedded val medication: Medication,
    @Relation(
        parentColumn = "id",
        entityColumn = "medication_id"
    )
    val schedules: List<MedicationSchedule>
)
