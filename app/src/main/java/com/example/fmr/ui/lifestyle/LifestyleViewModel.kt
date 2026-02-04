package com.example.fmr.ui.lifestyle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.DailyGoal
import com.example.fmr.data.entity.FollowUp
import com.example.fmr.data.repository.LifestyleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 生活管理ViewModel
 */
class LifestyleViewModel(
    private val lifestyleRepository: LifestyleRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(LifestyleUiState())
    val uiState: StateFlow<LifestyleUiState> = _uiState.asStateFlow()
    
    // 今日目标
    private val _todayGoals = MutableStateFlow<List<DailyGoal>>(emptyList())
    val todayGoals: StateFlow<List<DailyGoal>> = _todayGoals.asStateFlow()
    
    // 待复诊列表
    val pendingFollowUps: StateFlow<List<FollowUp>> = lifestyleRepository
        .getPendingFollowUps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        loadTodayGoals()
        loadNextFollowUp()
    }
    
    /**
     * 加载今日目标
     */
    fun loadTodayGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val goals = lifestyleRepository.getTodayGoals()
                _todayGoals.value = goals
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
    
    /**
     * 加载下次复诊信息
     */
    private fun loadNextFollowUp() {
        viewModelScope.launch {
            try {
                val nextFollowUp = lifestyleRepository.getNextFollowUp()
                val daysLeft = lifestyleRepository.getDaysUntilNextFollowUp()
                
                _uiState.update {
                    it.copy(
                        nextFollowUp = nextFollowUp,
                        daysUntilNextFollowUp = daysLeft
                    )
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
    
    /**
     * 更新目标进度
     */
    fun updateGoalProgress(goalId: Long, currentVal: Int) {
        viewModelScope.launch {
            try {
                lifestyleRepository.updateGoalProgress(goalId, currentVal)
                loadTodayGoals()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "更新失败")
                }
            }
        }
    }
    
    /**
     * 增加目标进度
     */
    fun incrementGoalProgress(goalId: Long, delta: Int) {
        viewModelScope.launch {
            try {
                lifestyleRepository.incrementGoalProgress(goalId, delta)
                loadTodayGoals()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "更新失败")
                }
            }
        }
    }
    
    /**
     * 添加复诊计划
     */
    fun addFollowUp(
        memberId: Long,
        hospitalName: String?,
        department: String?,
        doctorName: String?,
        appointmentDate: String,
        appointmentTime: String?,
        reason: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val followUp = FollowUp(
                    memberId = memberId,
                    hospitalName = hospitalName,
                    department = department,
                    doctorName = doctorName,
                    appointmentDate = appointmentDate,
                    appointmentTime = appointmentTime,
                    reason = reason,
                    source = FollowUp.SOURCE_SELF
                )
                
                lifestyleRepository.addFollowUp(followUp)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "复诊计划添加成功",
                        error = null
                    )
                }
                
                loadNextFollowUp()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "添加失败"
                    )
                }
            }
        }
    }
    
    /**
     * 完成复诊
     */
    fun completeFollowUp(followUpId: Long) {
        viewModelScope.launch {
            try {
                lifestyleRepository.completeFollowUp(followUpId)
                loadNextFollowUp()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "操作失败")
                }
            }
        }
    }
    
    /**
     * 取消复诊
     */
    fun cancelFollowUp(followUpId: Long) {
        viewModelScope.launch {
            try {
                lifestyleRepository.cancelFollowUp(followUpId)
                loadNextFollowUp()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "操作失败")
                }
            }
        }
    }
    
    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}

/**
 * 生活管理UI状态
 */
data class LifestyleUiState(
    val isLoading: Boolean = false,
    val nextFollowUp: FollowUp? = null,
    val daysUntilNextFollowUp: Int? = null,
    val successMessage: String? = null,
    val error: String? = null
)

/**
 * LifestyleViewModel工厂
 */
class LifestyleViewModelFactory(
    private val lifestyleRepository: LifestyleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifestyleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LifestyleViewModel(lifestyleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
