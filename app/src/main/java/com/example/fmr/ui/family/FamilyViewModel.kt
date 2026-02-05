package com.example.fmr.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.network.ConnectionState
import com.example.fmr.data.network.NetworkManager
import com.example.fmr.data.network.model.MemberProfileDto
import com.example.fmr.data.network.model.UpdateHealthProfileRequest
import com.example.fmr.data.repository.FamilyRepository
import com.example.fmr.data.repository.SyncResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 家庭成员管理ViewModel
 */
class FamilyViewModel(
    private val familyRepository: FamilyRepository,
    private val networkManager: NetworkManager? = null
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()
    
    // 家庭成员列表
    val familyMembers: StateFlow<List<FamilyMember>> = familyRepository
        .getAllActiveMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 当前选中的成员
    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()
    
    // 成员详情（含健康档案）
    private val _memberProfile = MutableStateFlow<MemberProfileDto?>(null)
    val memberProfile: StateFlow<MemberProfileDto?> = _memberProfile.asStateFlow()
    
    // 添加/编辑成员表单状态
    private val _formState = MutableStateFlow(MemberFormState())
    val formState: StateFlow<MemberFormState> = _formState.asStateFlow()
    
    // 网络连接状态
    val connectionState: StateFlow<ConnectionState> = networkManager?.connectionState
        ?: MutableStateFlow(ConnectionState.UNKNOWN)
    
    init {
        // 监听网络状态变化，自动同步
        viewModelScope.launch {
            networkManager?.connectionState?.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    // 网络恢复时尝试同步
                    syncFromServer()
                }
            }
        }
    }
    
    /**
     * 添加成员
     */
    fun addMember(
        name: String,
        gender: Int,
        birthDate: String,
        relation: String,
        role: Int = FamilyMember.ROLE_MEMBER,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 验证输入
                if (name.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "请输入成员姓名") }
                    return@launch
                }
                if (name.length > 50) {
                    _uiState.update { it.copy(isLoading = false, error = "姓名长度不能超过50个字符") }
                    return@launch
                }
                if (birthDate.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "请选择出生日期") }
                    return@launch
                }
                if (relation.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "请选择与成员的关系") }
                    return@launch
                }
                
                val member = FamilyMember(
                    name = name,
                    gender = gender,
                    birthDate = birthDate,
                    relation = relation,
                    role = role,
                    avatarUrl = avatarUrl,
                    viewAll = role == FamilyMember.ROLE_ADMIN,
                    editAll = role == FamilyMember.ROLE_ADMIN
                )
                
                when (val result = familyRepository.addMember(member)) {
                    is SyncResult.Success -> {
                        val message = if (result.synced) {
                            "添加成功"
                        } else {
                            "添加成功（${result.syncError ?: "离线模式"}）"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = message,
                                isSynced = result.synced,
                                error = null
                            )
                        }
                        resetForm()
                    }
                    is SyncResult.LocalError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is SyncResult.SyncError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "添加成功（本地）",
                                isSynced = false,
                                error = null
                            )
                        }
                        resetForm()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "添加失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 更新成员
     */
    fun updateMember(member: FamilyMember) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when (val result = familyRepository.updateMember(member)) {
                    is SyncResult.Success -> {
                        val message = if (result.synced) {
                            "更新成功"
                        } else {
                            "更新成功（${result.syncError ?: "离线模式"}）"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = message,
                                isSynced = result.synced,
                                error = null
                            )
                        }
                    }
                    is SyncResult.LocalError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is SyncResult.SyncError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "更新成功（本地）",
                                isSynced = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "更新失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 删除成员
     */
    fun deleteMember(memberId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when (val result = familyRepository.deleteMember(memberId)) {
                    is SyncResult.Success -> {
                        val message = if (result.synced) {
                            "删除成功"
                        } else {
                            "删除成功（${result.syncError ?: "离线模式"}）"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = message,
                                isSynced = result.synced,
                                error = null
                            )
                        }
                        _selectedMember.value = null
                    }
                    is SyncResult.LocalError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is SyncResult.SyncError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "删除成功（本地）",
                                isSynced = false,
                                error = null
                            )
                        }
                        _selectedMember.value = null
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "删除失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 从服务器同步数据
     */
    fun syncFromServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                when (val result = familyRepository.syncFromServer()) {
                    is SyncResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                isSynced = result.synced,
                                syncMessage = if (result.synced) "同步完成" else result.syncError
                            )
                        }
                    }
                    is SyncResult.SyncError -> {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                isSynced = false,
                                syncMessage = result.message
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isSyncing = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = "同步失败：${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 选择成员
     */
    fun selectMember(member: FamilyMember?) {
        _selectedMember.value = member
    }
    
    /**
     * 加载成员详情
     */
    fun loadMemberDetail(memberId: Long) {
        viewModelScope.launch {
            try {
                val member = familyRepository.getMemberById(memberId)
                _selectedMember.value = member
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "加载失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 加载成员详情（含健康档案）
     */
    fun loadMemberProfile(memberId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val profile = familyRepository.getMemberProfile(memberId)
                _memberProfile.value = profile
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "加载失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 更新健康档案
     */
    fun updateHealthProfile(
        memberId: Long,
        height: Double?,
        weight: Double?,
        bloodType: String?,
        allergies: List<String>?,
        chronicDiseases: List<String>?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val request = UpdateHealthProfileRequest(
                    height = height,
                    weight = weight,
                    bloodType = bloodType,
                    allergies = allergies,
                    chronicDiseases = chronicDiseases
                )
                familyRepository.updateHealthProfile(memberId, request)
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "健康档案更新成功")
                }
                // 重新加载档案
                loadMemberProfile(memberId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "更新失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 更新表单状态
     */
    fun updateFormState(
        name: String? = null,
        gender: Int? = null,
        birthDate: String? = null,
        relation: String? = null,
        role: Int? = null
    ) {
        _formState.update {
            it.copy(
                name = name ?: it.name,
                gender = gender ?: it.gender,
                birthDate = birthDate ?: it.birthDate,
                relation = relation ?: it.relation,
                role = role ?: it.role
            )
        }
    }
    
    /**
     * 重置表单
     */
    fun resetForm() {
        _formState.value = MemberFormState()
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * 清除同步消息
     */
    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }
    
    /**
     * 检查是否在线
     */
    fun isOnline(): Boolean {
        return networkManager?.checkNetworkAvailability() == true
    }
}

/**
 * 家庭成员UI状态
 */
data class FamilyUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isSynced: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val syncMessage: String? = null
)

/**
 * 成员表单状态
 */
data class MemberFormState(
    val name: String = "",
    val gender: Int = FamilyMember.GENDER_MALE,
    val birthDate: String = "",
    val relation: String = "",
    val role: Int = FamilyMember.ROLE_MEMBER
)

/**
 * FamilyViewModel工厂
 */
class FamilyViewModelFactory(
    private val familyRepository: FamilyRepository,
    private val networkManager: NetworkManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FamilyViewModel(familyRepository, networkManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
