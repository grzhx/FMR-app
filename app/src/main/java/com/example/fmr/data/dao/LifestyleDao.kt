package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.DailyGoal
import com.example.fmr.data.entity.FollowUp
import kotlinx.coroutines.flow.Flow

/**
 * 生活管理数据访问对象
 */
@Dao
interface LifestyleDao {
    
    // ==================== 每日目标操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoal): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoal>)
    
    @Update
    suspend fun updateGoal(goal: DailyGoal)
    
    @Delete
    suspend fun deleteGoal(goal: DailyGoal)
    
    @Query("SELECT * FROM t_daily_goal WHERE id = :id")
    suspend fun getGoalById(id: Long): DailyGoal?
    
    @Query("SELECT * FROM t_daily_goal WHERE member_id = :memberId AND goal_date = :date")
    fun getGoalsByMemberAndDate(memberId: Long, date: String): Flow<List<DailyGoal>>
    
    @Query("SELECT * FROM t_daily_goal WHERE member_id = :memberId AND goal_date = :date")
    suspend fun getGoalsByMemberAndDateSync(memberId: Long, date: String): List<DailyGoal>
    
    @Query("SELECT * FROM t_daily_goal WHERE goal_date = :date")
    fun getGoalsByDate(date: String): Flow<List<DailyGoal>>
    
    @Query("SELECT * FROM t_daily_goal WHERE goal_date = :date")
    suspend fun getGoalsByDateSync(date: String): List<DailyGoal>
    
    @Query("UPDATE t_daily_goal SET current_val = :currentVal WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, currentVal: Int)
    
    @Query("UPDATE t_daily_goal SET current_val = current_val + :delta WHERE id = :id")
    suspend fun incrementGoalProgress(id: Long, delta: Int)
    
    @Query("DELETE FROM t_daily_goal WHERE goal_date < :date")
    suspend fun deleteOldGoals(date: String)
    
    // ==================== 复诊计划操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long
    
    @Update
    suspend fun updateFollowUp(followUp: FollowUp)
    
    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)
    
    @Query("SELECT * FROM t_follow_up WHERE id = :id")
    suspend fun getFollowUpById(id: Long): FollowUp?
    
    @Query("SELECT * FROM t_follow_up WHERE member_id = :memberId ORDER BY appointment_date ASC")
    fun getFollowUpsByMember(memberId: Long): Flow<List<FollowUp>>
    
    @Query("SELECT * FROM t_follow_up WHERE member_id = :memberId ORDER BY appointment_date ASC")
    suspend fun getFollowUpsByMemberSync(memberId: Long): List<FollowUp>
    
    @Query("SELECT * FROM t_follow_up ORDER BY appointment_date ASC")
    fun getAllFollowUps(): Flow<List<FollowUp>>
    
    @Query("SELECT * FROM t_follow_up ORDER BY appointment_date ASC")
    suspend fun getAllFollowUpsSync(): List<FollowUp>
    
    @Query("SELECT * FROM t_follow_up WHERE status = 0 ORDER BY appointment_date ASC")
    fun getPendingFollowUps(): Flow<List<FollowUp>>
    
    @Query("SELECT * FROM t_follow_up WHERE status = 0 ORDER BY appointment_date ASC")
    suspend fun getPendingFollowUpsSync(): List<FollowUp>
    
    @Query("SELECT * FROM t_follow_up WHERE status = 0 AND appointment_date >= :date ORDER BY appointment_date ASC LIMIT 1")
    suspend fun getNextFollowUp(date: String): FollowUp?
    
    @Query("SELECT * FROM t_follow_up WHERE status = 0 ORDER BY appointment_date ASC LIMIT :limit")
    suspend fun getUpcomingFollowUps(limit: Int): List<FollowUp>
    
    @Query("UPDATE t_follow_up SET status = :status WHERE id = :id")
    suspend fun updateFollowUpStatus(id: Long, status: Int)
    
    @Query("SELECT COUNT(*) FROM t_follow_up WHERE member_id = :memberId AND status = 0")
    suspend fun getPendingFollowUpCount(memberId: Long): Int
    
    @Query("SELECT COUNT(*) FROM t_follow_up WHERE status = 0")
    suspend fun getTotalPendingFollowUpCount(): Int
}
