package com.example.fmr.data.dao

import androidx.room.*
import com.example.fmr.data.entity.FamilyMember
import kotlinx.coroutines.flow.Flow

/**
 * 家庭成员数据访问对象
 */
@Dao
interface FamilyMemberDao {
    
    /**
     * 插入家庭成员
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember): Long
    
    /**
     * 批量插入家庭成员
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>): List<Long>
    
    /**
     * 更新家庭成员
     */
    @Update
    suspend fun update(member: FamilyMember)
    
    /**
     * 删除家庭成员
     */
    @Delete
    suspend fun delete(member: FamilyMember)
    
    /**
     * 根据ID删除家庭成员
     */
    @Query("DELETE FROM t_family_member WHERE id = :memberId")
    suspend fun deleteById(memberId: Long)
    
    /**
     * 根据ID获取家庭成员
     */
    @Query("SELECT * FROM t_family_member WHERE id = :memberId")
    suspend fun getMemberById(memberId: Long): FamilyMember?
    
    /**
     * 根据ID获取家庭成员（Flow）
     */
    @Query("SELECT * FROM t_family_member WHERE id = :memberId")
    fun getMemberByIdFlow(memberId: Long): Flow<FamilyMember?>
    
    /**
     * 获取家庭所有成员
     */
    @Query("SELECT * FROM t_family_member WHERE family_id = :familyId AND status = 1 ORDER BY relation ASC, create_time ASC")
    fun getMembersByFamilyId(familyId: Long): Flow<List<FamilyMember>>
    
    /**
     * 获取家庭所有成员（非Flow）
     */
    @Query("SELECT * FROM t_family_member WHERE family_id = :familyId AND status = 1 ORDER BY relation ASC, create_time ASC")
    suspend fun getMembersByFamilyIdSync(familyId: Long): List<FamilyMember>
    
    /**
     * 获取所有活跃成员
     */
    @Query("SELECT * FROM t_family_member WHERE status = 1 ORDER BY relation ASC, create_time ASC")
    fun getAllActiveMembers(): Flow<List<FamilyMember>>
    
    /**
     * 获取所有活跃成员（非Flow）
     */
    @Query("SELECT * FROM t_family_member WHERE status = 1 ORDER BY relation ASC, create_time ASC")
    suspend fun getAllActiveMembersSync(): List<FamilyMember>
    
    /**
     * 统计家庭成员数量
     */
    @Query("SELECT COUNT(*) FROM t_family_member WHERE family_id = :familyId AND status = 1")
    suspend fun countMembersByFamilyId(familyId: Long): Int
    
    /**
     * 根据关系类型获取成员
     */
    @Query("SELECT * FROM t_family_member WHERE family_id = :familyId AND relation = :relation AND status = 1")
    suspend fun getMembersByRelation(familyId: Long, relation: String): List<FamilyMember>
    
    /**
     * 获取管理员成员
     */
    @Query("SELECT * FROM t_family_member WHERE family_id = :familyId AND role = 1 AND status = 1")
    suspend fun getAdminMembers(familyId: Long): List<FamilyMember>
    
    /**
     * 软删除成员（更新状态为已删除）
     */
    @Query("UPDATE t_family_member SET status = 3, update_time = :updateTime WHERE id = :memberId")
    suspend fun softDelete(memberId: Long, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 搜索成员
     */
    @Query("SELECT * FROM t_family_member WHERE family_id = :familyId AND status = 1 AND name LIKE '%' || :keyword || '%'")
    suspend fun searchMembers(familyId: Long, keyword: String): List<FamilyMember>
}
