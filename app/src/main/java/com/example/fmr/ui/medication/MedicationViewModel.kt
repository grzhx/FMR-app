package com.example.fmr.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.Medication
import com.example.fmr.data.entity.MedicationSchedule
import com.example.fmr.data.repository.MedicationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用药助手ViewModel
 */
class MedicationViewModel(
    private val medicationRepository: MedicationRepository
) : ViewModel() {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // UI状态
    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()
    
    // 所有药品
    val allMedications: StateFlow<List<Medication>> = medicationRepository
        .getAllActiveMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 今日服药计划
    private val _todaySchedules = MutableStateFlow<List<MedicationScheduleWithMedication>>(emptyList())
    val todaySchedules: StateFlow<List<MedicationScheduleWithMedication>> = _todaySchedules.asStateFlow()
    
    init {
        loadTodaySchedules()
    }
    
    /**
     * 加载今日服药计划
     */
    fun loadTodaySchedules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val today = dateFormat.format(Date())
                val schedules = medicationRepository.getSchedulesByDateSync(today)
                val medications = medicationRepository.getAllActiveMedicationsSync()
                
                val schedulesWithMedication = schedules.mapNotNull { schedule ->
                    val medication = medications.find { it.id == schedule.medicationId }
                    medication?.let {
                        MedicationScheduleWithMedication(schedule, it)
                    }
                }
                
                _todaySchedules.value = schedulesWithMedication
                
                // 计算进度
                val total = schedules.size
                val completed = schedules.count { it.taken }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayTotal = total,
                        todayCompleted = completed,
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
     * 添加药品
     */
    fun addMedication(
        memberId: Long,
        name: String,
        dosage: String,
        frequency: String,
        times: List<String>,
        startDate: String,
        instructions: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val medication = Medication(
                    memberId = memberId,
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    startDate = startDate,
                    instructions = instructions
                )
                
                val medicationId = medicationRepository.addMedication(medication)
                
                // 生成服药计划
                val savedMedication = medication.copy(id = medicationId)
                medicationRepository.generateSchedules(savedMedication, times)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "药品添加成功",
                        error = null
                    )
                }
                
                // 刷新今日计划
                loadTodaySchedules()
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
     * 标记服药
     */
    fun markAsTaken(scheduleId: Long) {
        viewModelScope.launch {
            try {
                medicationRepository.markAsTaken(scheduleId)
                loadTodaySchedules()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "操作失败")
                }
            }
        }
    }
    
    /**
     * 取消服药标记
     */
    fun markAsNotTaken(scheduleId: Long) {
        viewModelScope.launch {
            try {
                medicationRepository.markAsNotTaken(scheduleId)
                loadTodaySchedules()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "操作失败")
                }
            }
        }
    }
    
    /**
     * 停用药品
     */
    fun stopMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                medicationRepository.stopMedication(medicationId)
                loadTodaySchedules()
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
 * 用药助手UI状态
 */
data class MedicationUiState(
    val isLoading: Boolean = false,
    val todayTotal: Int = 0,
    val todayCompleted: Int = 0,
    val successMessage: String? = null,
    val error: String? = null
) {
    val completionRate: Float
        get() = if (todayTotal > 0) todayCompleted.toFloat() / todayTotal else 0f
}

/**
 * 服药计划与药品信息
 */
data class MedicationScheduleWithMedication(
    val schedule: MedicationSchedule,
    val medication: Medication
)

/**
 * MedicationViewModel工厂
 */
class MedicationViewModelFactory(
    private val medicationRepository: MedicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(medicationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
