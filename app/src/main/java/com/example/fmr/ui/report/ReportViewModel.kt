package com.example.fmr.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.LabReport
import com.example.fmr.data.entity.LabResult
import com.example.fmr.data.repository.LabReportRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 报告解读ViewModel
 */
class ReportViewModel(
    private val labReportRepository: LabReportRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    // 所有报告
    val allReports: StateFlow<List<LabReport>> = labReportRepository
        .getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 当前报告详情
    private val _currentReport = MutableStateFlow<LabReport?>(null)
    val currentReport: StateFlow<LabReport?> = _currentReport.asStateFlow()
    
    // 当前报告结果
    private val _currentResults = MutableStateFlow<List<LabResult>>(emptyList())
    val currentResults: StateFlow<List<LabResult>> = _currentResults.asStateFlow()
    
    /**
     * 添加报告
     */
    fun addReport(
        memberId: Long,
        reportType: String,
        hospitalName: String?,
        reportDate: String,
        results: List<LabResultInput>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val report = LabReport(
                    memberId = memberId,
                    reportType = reportType,
                    hospitalName = hospitalName,
                    reportDate = reportDate
                )
                
                val labResults = results.map { input ->
                    LabResult(
                        reportId = 0, // 会在Repository中设置
                        itemName = input.itemName,
                        itemValue = input.itemValue,
                        itemUnit = input.itemUnit,
                        referenceRange = input.referenceRange,
                        status = input.status
                    )
                }
                
                labReportRepository.addReportWithResults(report, labResults)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "报告添加成功",
                        error = null
                    )
                }
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
     * 加载报告详情
     */
    fun loadReportDetail(reportId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (report, results) = labReportRepository.getReportDetail(reportId)
                _currentReport.value = report
                _currentResults.value = results
                
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
     * 删除报告
     */
    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            try {
                val report = labReportRepository.getReportById(reportId)
                report?.let {
                    labReportRepository.deleteReport(it)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }
    
    /**
     * 获取趋势数据
     */
    fun loadTrendData(memberId: Long, itemName: String) {
        viewModelScope.launch {
            try {
                val trendData = labReportRepository.getResultTrend(memberId, itemName)
                _uiState.update {
                    it.copy(trendData = trendData)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "加载趋势失败")
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
 * 报告解读UI状态
 */
data class ReportUiState(
    val isLoading: Boolean = false,
    val trendData: List<LabResult> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null
)

/**
 * 检查结果输入
 */
data class LabResultInput(
    val itemName: String,
    val itemValue: String,
    val itemUnit: String? = null,
    val referenceRange: String? = null,
    val status: Int = LabResult.STATUS_NORMAL
)

/**
 * ReportViewModel工厂
 */
class ReportViewModelFactory(
    private val labReportRepository: LabReportRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(labReportRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
