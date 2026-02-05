package com.example.fmr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.MedicalRecord
import com.example.fmr.data.network.ConnectionState
import com.example.fmr.data.network.NetworkManager
import com.example.fmr.data.network.model.HomeDashboardDto
import com.example.fmr.data.network.model.MemberProfileDto
import com.example.fmr.data.network.model.ProgressItemDto
import com.example.fmr.data.repository.DashboardResult
import com.example.fmr.data.repository.FamilyRepository
import com.example.fmr.data.repository.HomeRepository
import com.example.fmr.data.repository.MedicalRecordRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 首页ViewModel
 */
class HomeViewModel(
    private val familyRepository: FamilyRepository,
    private val medicalRecordRepository: MedicalRecordRepository,
    private val homeRepository: HomeRepository? = null,
    private val networkManager: NetworkManager? = null
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 家庭成员列表
    val familyMembers: StateFlow<List<FamilyMember>> = familyRepository
        .getAllActiveMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 网络连接状态
    val connectionState: StateFlow<ConnectionState> = networkManager?.connectionState
        ?: MutableStateFlow(ConnectionState.UNKNOWN)
    
    // 当前选中的成员ID（用于获取仪表盘数据）
    private val _currentMemberId = MutableStateFlow<Long?>(null)
    
    // 当前选中的成员
    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()
    
    // 选中成员的健康档案
    private val _memberProfile = MutableStateFlow<MemberProfileDto?>(null)
    val memberProfile: StateFlow<MemberProfileDto?> = _memberProfile.asStateFlow()
    
    init {
        loadDashboardData()
        
        // 监听成员列表变化，自动选中第一个成员
        viewModelScope.launch {
            familyMembers.collect { members ->
                if (members.isNotEmpty() && _selectedMember.value == null) {
                    selectMember(members.first())
                }
            }
        }
        
        // 监听网络状态变化
        viewModelScope.launch {
            networkManager?.connectionState?.collect { state ->
                _uiState.update { it.copy(isOnline = state == ConnectionState.CONNECTED) }
                if (state == ConnectionState.CONNECTED) {
                    // 网络恢复时刷新数据
                    loadDashboardData()
                }
            }
        }
    }
    
    /**
     * 加载首页数据
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 获取本地数据
                val recentRecords = medicalRecordRepository.getAllRecentRecords(5)
                val memberCount = familyRepository.getAllActiveMembersSync().size
                val recordCount = medicalRecordRepository.getAllRecordsSync().size
                
                // 尝试从服务器获取仪表盘数据
                val memberId = _currentMemberId.value ?: 1L
                val dashboardResult = homeRepository?.getDashboard(memberId)
                
                when (dashboardResult) {
                    is DashboardResult.Success -> {
                        val dashboard = dashboardResult.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                greeting = dashboard.greeting,
                                recentRecords = recentRecords,
                                memberCount = memberCount,
                                recordCount = recordCount,
                                todayProgress = dashboard.todayProgress?.let { progress ->
                                    TodayProgress(
                                        medicationTarget = progress.medication?.target ?: 0,
                                        medicationCurrent = progress.medication?.current ?: 0,
                                        exerciseTarget = progress.exercise?.target ?: 0,
                                        exerciseCurrent = progress.exercise?.current ?: 0
                                    )
                                },
                                isFromServer = dashboardResult.fromServer,
                                error = null
                            )
                        }
                    }
                    is DashboardResult.Error -> {
                        // 服务器获取失败，使用本地数据
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                greeting = getLocalGreeting(),
                                recentRecords = recentRecords,
                                memberCount = memberCount,
                                recordCount = recordCount,
                                isFromServer = false,
                                error = null
                            )
                        }
                    }
                    null -> {
                        // 没有配置远程数据源，使用本地数据
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                greeting = getLocalGreeting(),
                                recentRecords = recentRecords,
                                memberCount = memberCount,
                                recordCount = recordCount,
                                isFromServer = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        greeting = getLocalGreeting(),
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
    
    /**
     * 获取本地问候语
     */
    private fun getLocalGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 6 -> "凌晨好"
            hour < 9 -> "早上好"
            hour < 12 -> "上午好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            hour < 22 -> "晚上好"
            else -> "夜深了"
        }
    }
    
    /**
     * 设置当前成员ID
     */
    fun setCurrentMember(memberId: Long) {
        _currentMemberId.value = memberId
        loadDashboardData()
    }
    
    /**
     * 选择成员并加载其健康档案
     */
    fun selectMember(member: FamilyMember) {
        _selectedMember.value = member
        _currentMemberId.value = member.id
        loadMemberProfile(member.id)
    }
    
    /**
     * 加载成员健康档案
     */
    private fun loadMemberProfile(memberId: Long) {
        viewModelScope.launch {
            try {
                val profile = familyRepository.getMemberProfile(memberId)
                _memberProfile.value = profile
            } catch (e: Exception) {
                _memberProfile.value = null
            }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadDashboardData()
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 检查是否在线
     */
    fun isOnline(): Boolean {
        return networkManager?.checkNetworkAvailability() == true
    }
}

/**
 * 首页UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val isFromServer: Boolean = false,
    val greeting: String = "",
    val recentRecords: List<MedicalRecord> = emptyList(),
    val memberCount: Int = 0,
    val recordCount: Int = 0,
    val todayProgress: TodayProgress? = null,
    val error: String? = null
)

/**
 * 今日进度数据
 */
data class TodayProgress(
    val medicationTarget: Int = 0,
    val medicationCurrent: Int = 0,
    val exerciseTarget: Int = 0,
    val exerciseCurrent: Int = 0
) {
    val medicationProgress: Float
        get() = if (medicationTarget > 0) medicationCurrent.toFloat() / medicationTarget else 0f
    
    val exerciseProgress: Float
        get() = if (exerciseTarget > 0) exerciseCurrent.toFloat() / exerciseTarget else 0f
}

/**
 * 快捷入口数据
 */
data class ShortcutItem(
    val id: String,
    val name: String,
    val icon: Int
)

/**
 * HomeViewModel工厂
 */
class HomeViewModelFactory(
    private val familyRepository: FamilyRepository,
    private val medicalRecordRepository: MedicalRecordRepository,
    private val homeRepository: HomeRepository? = null,
    private val networkManager: NetworkManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                familyRepository,
                medicalRecordRepository,
                homeRepository,
                networkManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
