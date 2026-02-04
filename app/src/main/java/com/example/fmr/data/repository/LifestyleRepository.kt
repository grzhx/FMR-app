package com.example.fmr.data.repository

import com.example.fmr.data.dao.LifestyleDao
import com.example.fmr.data.entity.DailyGoal
import com.example.fmr.data.entity.FollowUp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 生活管理数据仓库
 */
class LifestyleRepository(private val lifestyleDao: LifestyleDao) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // ==================== 每日目标操作 ====================
    
    suspend fun addGoal(goal: DailyGoal): Long {
        return lifestyleDao.insertGoal(goal)
    }
    
    suspend fun addGoals(goals: List<DailyGoal>) {
        lifestyleDao.insertGoals(goals)
    }
    
    suspend fun updateGoal(goal: DailyGoal) {
        lifestyleDao.updateGoal(goal)
    }
    
    suspend fun deleteGoal(goal: DailyGoal) {
        lifestyleDao.deleteGoal(goal)
    }
    
    fun getGoalsByMemberAndDate(memberId: Long, date: String): Flow<List<DailyGoal>> {
        return lifestyleDao.getGoalsByMemberAndDate(memberId, date)
    }
    
    suspend fun getGoalsByMemberAndDateSync(memberId: Long, date: String): List<DailyGoal> {
        return lifestyleDao.getGoalsByMemberAndDateSync(memberId, date)
    }
    
    fun getGoalsByDate(date: String): Flow<List<DailyGoal>> {
        return lifestyleDao.getGoalsByDate(date)
    }
    
    suspend fun getGoalsByDateSync(date: String): List<DailyGoal> {
        return lifestyleDao.getGoalsByDateSync(date)
    }
    
    suspend fun getTodayGoals(): List<DailyGoal> {
        val today = dateFormat.format(Date())
        return lifestyleDao.getGoalsByDateSync(today)
    }
    
    suspend fun updateGoalProgress(goalId: Long, currentVal: Int) {
        lifestyleDao.updateGoalProgress(goalId, currentVal)
    }
    
    suspend fun incrementGoalProgress(goalId: Long, delta: Int) {
        lifestyleDao.incrementGoalProgress(goalId, delta)
    }
    
    /**
     * 为成员创建今日默认目标
     */
    suspend fun createDefaultGoalsForMember(memberId: Long) {
        val today = dateFormat.format(Date())
        val existingGoals = lifestyleDao.getGoalsByMemberAndDateSync(memberId, today)
        
        if (existingGoals.isEmpty()) {
            val defaultGoals = listOf(
                DailyGoal(
                    memberId = memberId,
                    goalDate = today,
                    type = DailyGoal.TYPE_STEPS,
                    targetVal = 6000,
                    unit = "步"
                ),
                DailyGoal(
                    memberId = memberId,
                    goalDate = today,
                    type = DailyGoal.TYPE_WATER,
                    targetVal = 2000,
                    unit = "ml"
                ),
                DailyGoal(
                    memberId = memberId,
                    goalDate = today,
                    type = DailyGoal.TYPE_EXERCISE,
                    targetVal = 30,
                    unit = "分钟"
                ),
                DailyGoal(
                    memberId = memberId,
                    goalDate = today,
                    type = DailyGoal.TYPE_SLEEP,
                    targetVal = 8,
                    unit = "小时"
                )
            )
            lifestyleDao.insertGoals(defaultGoals)
        }
    }
    
    // ==================== 复诊计划操作 ====================
    
    suspend fun addFollowUp(followUp: FollowUp): Long {
        return lifestyleDao.insertFollowUp(followUp)
    }
    
    suspend fun updateFollowUp(followUp: FollowUp) {
        lifestyleDao.updateFollowUp(followUp)
    }
    
    suspend fun deleteFollowUp(followUp: FollowUp) {
        lifestyleDao.deleteFollowUp(followUp)
    }
    
    suspend fun getFollowUpById(id: Long): FollowUp? {
        return lifestyleDao.getFollowUpById(id)
    }
    
    fun getFollowUpsByMember(memberId: Long): Flow<List<FollowUp>> {
        return lifestyleDao.getFollowUpsByMember(memberId)
    }
    
    suspend fun getFollowUpsByMemberSync(memberId: Long): List<FollowUp> {
        return lifestyleDao.getFollowUpsByMemberSync(memberId)
    }
    
    fun getAllFollowUps(): Flow<List<FollowUp>> {
        return lifestyleDao.getAllFollowUps()
    }
    
    suspend fun getAllFollowUpsSync(): List<FollowUp> {
        return lifestyleDao.getAllFollowUpsSync()
    }
    
    fun getPendingFollowUps(): Flow<List<FollowUp>> {
        return lifestyleDao.getPendingFollowUps()
    }
    
    suspend fun getPendingFollowUpsSync(): List<FollowUp> {
        return lifestyleDao.getPendingFollowUpsSync()
    }
    
    suspend fun getNextFollowUp(): FollowUp? {
        val today = dateFormat.format(Date())
        return lifestyleDao.getNextFollowUp(today)
    }
    
    suspend fun getUpcomingFollowUps(limit: Int = 5): List<FollowUp> {
        return lifestyleDao.getUpcomingFollowUps(limit)
    }
    
    suspend fun completeFollowUp(id: Long) {
        lifestyleDao.updateFollowUpStatus(id, FollowUp.STATUS_COMPLETED)
    }
    
    suspend fun cancelFollowUp(id: Long) {
        lifestyleDao.updateFollowUpStatus(id, FollowUp.STATUS_CANCELLED)
    }
    
    suspend fun getPendingFollowUpCount(memberId: Long): Int {
        return lifestyleDao.getPendingFollowUpCount(memberId)
    }
    
    suspend fun getTotalPendingFollowUpCount(): Int {
        return lifestyleDao.getTotalPendingFollowUpCount()
    }
    
    /**
     * 计算距离下次复诊的天数
     */
    suspend fun getDaysUntilNextFollowUp(): Int? {
        val nextFollowUp = getNextFollowUp() ?: return null
        
        return try {
            val appointmentDate = dateFormat.parse(nextFollowUp.appointmentDate)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            val diff = appointmentDate!!.time - today.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }
}
