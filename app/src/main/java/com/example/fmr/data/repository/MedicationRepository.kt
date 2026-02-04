package com.example.fmr.data.repository

import com.example.fmr.data.dao.MedicationDao
import com.example.fmr.data.entity.Medication
import com.example.fmr.data.entity.MedicationSchedule
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用药数据仓库
 */
class MedicationRepository(private val medicationDao: MedicationDao) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // ==================== 药品操作 ====================
    
    suspend fun addMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }
    
    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }
    
    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }
    
    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getMedicationById(id)
    }
    
    fun getActiveMedicationsByMember(memberId: Long): Flow<List<Medication>> {
        return medicationDao.getActiveMedicationsByMember(memberId)
    }
    
    fun getAllMedicationsByMember(memberId: Long): Flow<List<Medication>> {
        return medicationDao.getAllMedicationsByMember(memberId)
    }
    
    fun getAllActiveMedications(): Flow<List<Medication>> {
        return medicationDao.getAllActiveMedications()
    }
    
    suspend fun getAllActiveMedicationsSync(): List<Medication> {
        return medicationDao.getAllActiveMedicationsSync()
    }
    
    suspend fun stopMedication(id: Long) {
        medicationDao.updateMedicationStatus(id, Medication.STATUS_STOPPED)
    }
    
    suspend fun resumeMedication(id: Long) {
        medicationDao.updateMedicationStatus(id, Medication.STATUS_ACTIVE)
    }
    
    // ==================== 服药时刻表操作 ====================
    
    suspend fun addSchedule(schedule: MedicationSchedule): Long {
        return medicationDao.insertSchedule(schedule)
    }
    
    suspend fun addSchedules(schedules: List<MedicationSchedule>) {
        medicationDao.insertSchedules(schedules)
    }
    
    fun getSchedulesByDate(date: String): Flow<List<MedicationSchedule>> {
        return medicationDao.getSchedulesByDate(date)
    }
    
    suspend fun getSchedulesByDateSync(date: String): List<MedicationSchedule> {
        return medicationDao.getSchedulesByDateSync(date)
    }
    
    suspend fun getTodaySchedules(): List<MedicationSchedule> {
        val today = dateFormat.format(Date())
        return medicationDao.getSchedulesByDateSync(today)
    }
    
    suspend fun markAsTaken(scheduleId: Long) {
        medicationDao.markAsTaken(scheduleId)
    }
    
    suspend fun markAsNotTaken(scheduleId: Long) {
        medicationDao.markAsNotTaken(scheduleId)
    }
    
    suspend fun getTodayProgress(): Pair<Int, Int> {
        val today = dateFormat.format(Date())
        val total = medicationDao.getTotalScheduleCount(today)
        val completed = medicationDao.getCompletedScheduleCount(today)
        return Pair(completed, total)
    }
    
    // ==================== 生成服药计划 ====================
    
    /**
     * 为药品生成服药计划
     * @param medication 药品信息
     * @param times 服药时间列表 (如 ["08:00", "12:00", "18:00"])
     * @param days 生成天数
     */
    suspend fun generateSchedules(
        medication: Medication,
        times: List<String>,
        days: Int = 7
    ) {
        val schedules = mutableListOf<MedicationSchedule>()
        val calendar = Calendar.getInstance()
        
        for (day in 0 until days) {
            val date = dateFormat.format(calendar.time)
            
            times.forEachIndexed { index, time ->
                val period = when {
                    time < "10:00" -> MedicationSchedule.PERIOD_MORNING
                    time < "14:00" -> MedicationSchedule.PERIOD_NOON
                    time < "20:00" -> MedicationSchedule.PERIOD_EVENING
                    else -> MedicationSchedule.PERIOD_BEDTIME
                }
                
                schedules.add(
                    MedicationSchedule(
                        medicationId = medication.id,
                        scheduleDate = date,
                        scheduleTime = time,
                        period = period
                    )
                )
            }
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        medicationDao.insertSchedules(schedules)
    }
    
    /**
     * 删除药品的所有计划
     */
    suspend fun deleteSchedulesByMedication(medicationId: Long) {
        medicationDao.deleteSchedulesByMedication(medicationId)
    }
    
    /**
     * 清理过期的计划
     */
    suspend fun cleanupOldSchedules(daysToKeep: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -daysToKeep)
        val cutoffDate = dateFormat.format(calendar.time)
        medicationDao.deleteOldSchedules(cutoffDate)
    }
}
