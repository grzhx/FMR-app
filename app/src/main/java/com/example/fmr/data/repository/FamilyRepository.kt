package com.example.fmr.data.repository

import com.example.fmr.data.dao.FamilyMemberDao
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.network.NetworkManager
import com.example.fmr.data.network.RemoteDataSource
import com.example.fmr.data.network.model.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * 家庭成员仓库
 * 负责家庭成员数据的管理，支持本地+远程数据源
 * 
 * 策略：
 * - 读取：优先从本地读取，联网时同步远程数据
 * - 写入：联网时同步到远程，离线时仅保存本地
 */
class FamilyRepository(
    private val familyMemberDao: FamilyMemberDao,
    private val remoteDataSource: RemoteDataSource? = null,
    private val networkManager: NetworkManager? = null
) {
    
    /**
     * 获取所有活跃成员
     */
    fun getAllActiveMembers(): Flow<List<FamilyMember>> {
        return familyMemberDao.getAllActiveMembers()
    }
    
    /**
     * 获取所有活跃成员（同步）
     */
    suspend fun getAllActiveMembersSync(): List<FamilyMember> {
        return familyMemberDao.getAllActiveMembersSync()
    }
    
    /**
     * 根据家庭ID获取成员列表
     */
    fun getMembersByFamilyId(familyId: Long): Flow<List<FamilyMember>> {
        return familyMemberDao.getMembersByFamilyId(familyId)
    }
    
    /**
     * 根据ID获取成员
     */
    suspend fun getMemberById(memberId: Long): FamilyMember? {
        return familyMemberDao.getMemberById(memberId)
    }
    
    /**
     * 根据ID获取成员（Flow）
     */
    fun getMemberByIdFlow(memberId: Long): Flow<FamilyMember?> {
        return familyMemberDao.getMemberByIdFlow(memberId)
    }
    
    /**
     * 添加成员
     * 联网时同步到服务器，离线时仅保存本地
     * @return SyncResult 包含操作结果和同步状态
     */
    suspend fun addMember(member: FamilyMember): SyncResult<Long> {
        // 检查成员数量限制
        val count = familyMemberDao.countMembersByFamilyId(member.familyId)
        if (count >= MAX_FAMILY_MEMBERS) {
            return SyncResult.LocalError("家庭成员已达上限（${MAX_FAMILY_MEMBERS}人），无法继续添加")
        }
        
        // 先保存到本地
        val localId = familyMemberDao.insert(member)
        
        // 尝试同步到服务器
        if (isOnline()) {
            return when (val result = remoteDataSource?.addMember(member.copy(id = localId))) {
                is NetworkResult.Success -> {
                    // 如果服务器返回的ID与本地不同，更新本地记录
                    if (result.data != localId) {
                        val updatedMember = member.copy(id = result.data)
                        familyMemberDao.deleteById(localId)
                        familyMemberDao.insert(updatedMember)
                        SyncResult.Success(result.data, synced = true)
                    } else {
                        SyncResult.Success(localId, synced = true)
                    }
                }
                is NetworkResult.Error -> {
                    SyncResult.Success(localId, synced = false, syncError = result.message)
                }
                is NetworkResult.Exception -> {
                    SyncResult.Success(localId, synced = false, syncError = result.throwable.message)
                }
                else -> SyncResult.Success(localId, synced = false)
            }
        }
        
        return SyncResult.Success(localId, synced = false, syncError = "离线模式，数据已保存到本地")
    }
    
    /**
     * 更新成员
     * 联网时同步到服务器，离线时仅更新本地
     */
    suspend fun updateMember(member: FamilyMember): SyncResult<Unit> {
        // 先更新本地
        familyMemberDao.update(member.copy(updateTime = System.currentTimeMillis()))
        
        // 尝试同步到服务器
        if (isOnline()) {
            return when (val result = remoteDataSource?.updateMember(member)) {
                is NetworkResult.Success -> {
                    SyncResult.Success(Unit, synced = true)
                }
                is NetworkResult.Error -> {
                    SyncResult.Success(Unit, synced = false, syncError = result.message)
                }
                is NetworkResult.Exception -> {
                    SyncResult.Success(Unit, synced = false, syncError = result.throwable.message)
                }
                else -> SyncResult.Success(Unit, synced = false)
            }
        }
        
        return SyncResult.Success(Unit, synced = false, syncError = "离线模式，数据已保存到本地")
    }
    
    /**
     * 删除成员（软删除）
     * 联网时同步到服务器，离线时仅删除本地
     */
    suspend fun deleteMember(memberId: Long): SyncResult<Unit> {
        // 先软删除本地
        familyMemberDao.softDelete(memberId)
        
        // 尝试同步到服务器
        if (isOnline()) {
            return when (val result = remoteDataSource?.deleteMember(memberId)) {
                is NetworkResult.Success -> {
                    SyncResult.Success(Unit, synced = true)
                }
                is NetworkResult.Error -> {
                    SyncResult.Success(Unit, synced = false, syncError = result.message)
                }
                is NetworkResult.Exception -> {
                    SyncResult.Success(Unit, synced = false, syncError = result.throwable.message)
                }
                else -> SyncResult.Success(Unit, synced = false)
            }
        }
        
        return SyncResult.Success(Unit, synced = false, syncError = "离线模式，数据已保存到本地")
    }
    
    /**
     * 硬删除成员
     */
    suspend fun hardDeleteMember(memberId: Long) {
        familyMemberDao.deleteById(memberId)
    }
    
    /**
     * 统计家庭成员数量
     */
    suspend fun countMembers(familyId: Long): Int {
        return familyMemberDao.countMembersByFamilyId(familyId)
    }
    
    /**
     * 搜索成员
     */
    suspend fun searchMembers(familyId: Long, keyword: String): List<FamilyMember> {
        return familyMemberDao.searchMembers(familyId, keyword)
    }
    
    /**
     * 获取管理员成员
     */
    suspend fun getAdminMembers(familyId: Long): List<FamilyMember> {
        return familyMemberDao.getAdminMembers(familyId)
    }
    
    /**
     * 从服务器同步成员列表到本地
     * @return 同步结果
     */
    suspend fun syncFromServer(familyId: Long? = null): SyncResult<Int> {
        if (!isOnline()) {
            return SyncResult.Success(0, synced = false, syncError = "离线模式，无法同步")
        }
        
        return when (val result = remoteDataSource?.getMembers(familyId)) {
            is NetworkResult.Success -> {
                val remoteMemberDtos = result.data
                var syncCount = 0
                
                remoteMemberDtos.forEach { dto ->
                    val localMember = familyMemberDao.getMemberById(dto.memberId)
                    val remoteMember = dto.toEntity()
                    
                    if (localMember == null) {
                        // 本地不存在，插入
                        familyMemberDao.insert(remoteMember)
                        syncCount++
                    } else {
                        // 本地存在，更新（以服务器数据为准）
                        familyMemberDao.update(remoteMember.copy(
                            updateTime = System.currentTimeMillis()
                        ))
                        syncCount++
                    }
                }
                
                SyncResult.Success(syncCount, synced = true)
            }
            is NetworkResult.Error -> {
                SyncResult.SyncError(result.message)
            }
            is NetworkResult.Exception -> {
                SyncResult.SyncError(result.throwable.message ?: "同步失败")
            }
            else -> SyncResult.SyncError("未知错误")
        }
    }
    
    /**
     * 检查是否在线
     */
    private fun isOnline(): Boolean {
        return networkManager?.checkNetworkAvailability() == true && remoteDataSource != null
    }
    
    /**
     * 检查服务器是否可达
     */
    suspend fun checkServerConnection(): Boolean {
        return remoteDataSource?.checkServerHealth() == true
    }
    
    companion object {
        const val MAX_FAMILY_MEMBERS = 10
    }
}

/**
 * 同步结果封装类
 */
sealed class SyncResult<out T> {
    /**
     * 操作成功
     * @param data 返回数据
     * @param synced 是否已同步到服务器
     * @param syncError 同步错误信息（如果有）
     */
    data class Success<T>(
        val data: T,
        val synced: Boolean,
        val syncError: String? = null
    ) : SyncResult<T>()
    
    /**
     * 本地操作失败
     */
    data class LocalError(val message: String) : SyncResult<Nothing>()
    
    /**
     * 同步失败（本地操作成功但同步失败）
     */
    data class SyncError(val message: String) : SyncResult<Nothing>()
}
