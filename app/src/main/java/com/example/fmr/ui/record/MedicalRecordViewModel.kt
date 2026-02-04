package com.example.fmr.ui.record

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.entity.Document
import com.example.fmr.data.entity.MedicalRecord
import com.example.fmr.data.repository.MedicalRecordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 导入任务状态枚举
 */
enum class ImportTaskStatus {
    IDLE,           // 空闲
    UPLOADING,      // 上传中
    OCR_PROCESSING, // OCR识别中
    EXTRACTING,     // 信息抽取中
    COMPLETED,      // 完成
    FAILED          // 失败
}

/**
 * 已选文件信息
 */
data class SelectedFile(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long,
    val fileType: String // PDF, JPG, PNG, JPEG
)

/**
 * 病历记录管理ViewModel
 */
class MedicalRecordViewModel(
    private val medicalRecordRepository: MedicalRecordRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "FMR_RecordVM"
    }
    
    // UI状态
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    
    // 所有病历记录
    val allRecords: StateFlow<List<MedicalRecord>> = medicalRecordRepository
        .getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 当前选中成员的病历记录
    private val _currentMemberId = MutableStateFlow<Long?>(null)
    val memberRecords: StateFlow<List<MedicalRecord>> = _currentMemberId
        .filterNotNull()
        .flatMapLatest { memberId ->
            medicalRecordRepository.getRecordsByMemberId(memberId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 当前选中的病历记录
    private val _selectedRecord = MutableStateFlow<MedicalRecord?>(null)
    val selectedRecord: StateFlow<MedicalRecord?> = _selectedRecord.asStateFlow()
    
    // 当前病历的文书列表
    private val _currentDocuments = MutableStateFlow<List<Document>>(emptyList())
    val currentDocuments: StateFlow<List<Document>> = _currentDocuments.asStateFlow()
    
    // 已选择的文件列表
    private val _selectedFiles = MutableStateFlow<List<SelectedFile>>(emptyList())
    val selectedFiles: StateFlow<List<SelectedFile>> = _selectedFiles.asStateFlow()
    
    // 导入任务状态
    private val _importTaskStatus = MutableStateFlow(ImportTaskStatus.IDLE)
    val importTaskStatus: StateFlow<ImportTaskStatus> = _importTaskStatus.asStateFlow()
    
    // 导入进度 (0-100)
    private val _importProgress = MutableStateFlow(0)
    val importProgress: StateFlow<Int> = _importProgress.asStateFlow()
    
    /**
     * 设置当前成员ID
     */
    fun setCurrentMemberId(memberId: Long) {
        _currentMemberId.value = memberId
    }
    
    /**
     * 添加选择的文件
     */
    fun addSelectedFile(file: SelectedFile) {
        val currentFiles = _selectedFiles.value.toMutableList()
        // 检查文件数量限制（最多10个）
        if (currentFiles.size >= 10) {
            _uiState.update { it.copy(error = "单次最多上传10个文件") }
            return
        }
        // 检查文件大小限制（单个不超过20MB）
        if (file.fileSize > 20 * 1024 * 1024) {
            _uiState.update { it.copy(error = "文件大小不能超过20MB") }
            return
        }
        // 检查文件格式
        val validTypes = listOf("PDF", "JPG", "PNG", "JPEG")
        if (file.fileType.uppercase() !in validTypes) {
            _uiState.update { it.copy(error = "仅支持PDF、JPG、PNG格式") }
            return
        }
        currentFiles.add(file)
        _selectedFiles.value = currentFiles
    }
    
    /**
     * 移除选择的文件
     */
    fun removeSelectedFile(file: SelectedFile) {
        _selectedFiles.value = _selectedFiles.value.filter { it.uri != file.uri }
    }
    
    /**
     * 清空选择的文件
     */
    fun clearSelectedFiles() {
        _selectedFiles.value = emptyList()
    }
    
    /**
     * 开始导入任务
     */
    fun startImportTask(memberId: Long) {
        Log.d(TAG, "========== startImportTask 开始 ==========")
        Log.d(TAG, "memberId: $memberId, 已选文件数: ${_selectedFiles.value.size}")
        
        if (_selectedFiles.value.isEmpty()) {
            Log.w(TAG, "❌ 没有选择文件")
            _uiState.update { it.copy(error = "请先选择文件") }
            return
        }
        
        viewModelScope.launch {
            _importTaskStatus.value = ImportTaskStatus.UPLOADING
            _importProgress.value = 0
            
            try {
                // 构建导入请求
                val files = _selectedFiles.value.map { file ->
                    Log.d(TAG, "文件: ${file.fileName}, 大小: ${file.fileSize}, 类型: ${file.fileType}")
                    com.example.fmr.data.network.model.ImportFileDto(
                        fileName = file.fileName,
                        fileUrl = file.uri.toString(),
                        fileSize = file.fileSize,
                        fileType = file.fileType
                    )
                }
                val request = com.example.fmr.data.network.model.ImportRecordRequest(
                    memberId = memberId,
                    files = files
                )
                Log.d(TAG, "构建请求完成: $request")
                
                // 调用后端创建导入任务
                Log.d(TAG, "正在调用后端 importRecords API...")
                val importResult = medicalRecordRepository.importRecords(request)
                Log.d(TAG, "importRecords 返回: isSuccess=${importResult.isSuccess}, isFailure=${importResult.isFailure}")
                
                if (importResult.isFailure) {
                    val error = importResult.exceptionOrNull()
                    Log.w(TAG, "❌ 后端调用失败: ${error?.message}", error)
                    Log.d(TAG, "切换到本地模拟模式...")
                    simulateImportProcess(memberId)
                    return@launch
                }
                
                val taskDto = importResult.getOrThrow()
                val taskId = taskDto.taskId
                Log.d(TAG, "✅ 任务创建成功, taskId: $taskId, status: ${taskDto.status}")
                
                // 轮询任务状态
                var completed = false
                var pollCount = 0
                while (!completed) {
                    delay(1000)
                    pollCount++
                    Log.d(TAG, "轮询任务状态 #$pollCount, taskId: $taskId")
                    val statusResult = medicalRecordRepository.getTaskStatus(taskId)
                    Log.d(TAG, "getTaskStatus 返回: isSuccess=${statusResult.isSuccess}")
                    
                    if (statusResult.isSuccess) {
                        val status = statusResult.getOrThrow()
                        Log.d(TAG, "任务状态: status=${status.status}, progress=${status.progress}, step=${status.currentStep}")
                        _importProgress.value = status.progress
                        
                        when (status.currentStep) {
                            "UPLOADING" -> _importTaskStatus.value = ImportTaskStatus.UPLOADING
                            "OCR_RECOGNITION" -> _importTaskStatus.value = ImportTaskStatus.OCR_PROCESSING
                            "EXTRACTING" -> _importTaskStatus.value = ImportTaskStatus.EXTRACTING
                            "DONE" -> {
                                _importTaskStatus.value = ImportTaskStatus.COMPLETED
                                completed = true
                            }
                        }
                        
                        if (status.status == "COMPLETED" || status.status == "FAILED") {
                            completed = true
                            if (status.status == "FAILED") {
                                Log.e(TAG, "❌ 任务失败")
                                _importTaskStatus.value = ImportTaskStatus.FAILED
                                _uiState.update { it.copy(error = "导入失败") }
                                return@launch
                            }
                        }
                    } else {
                        val error = statusResult.exceptionOrNull()
                        Log.w(TAG, "❌ 轮询失败: ${error?.message}", error)
                        simulateImportProcess(memberId)
                        return@launch
                    }
                }
                
                Log.d(TAG, "✅ 导入完成")
                _uiState.update { it.copy(successMessage = "导入成功") }
                clearSelectedFiles()
                
                delay(1500)
                _importTaskStatus.value = ImportTaskStatus.IDLE
                _importProgress.value = 0
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 异常: ${e.message}", e)
                simulateImportProcess(memberId)
            }
        }
    }
    
    /**
     * 本地模拟导入过程（后端不可用时使用）
     */
    private suspend fun simulateImportProcess(memberId: Long) {
        try {
            // 模拟上传
            for (i in 1..30) { delay(50); _importProgress.value = i }
            
            // OCR识别
            _importTaskStatus.value = ImportTaskStatus.OCR_PROCESSING
            for (i in 31..60) { delay(50); _importProgress.value = i }
            
            // 信息抽取
            _importTaskStatus.value = ImportTaskStatus.EXTRACTING
            for (i in 61..90) { delay(50); _importProgress.value = i }
            
            // 本地保存
            val record = MedicalRecord(
                memberId = memberId,
                recordType = MedicalRecord.TYPE_OUTPATIENT,
                hospitalName = "待识别",
                completeness = 0
            )
            val recordId = medicalRecordRepository.addRecord(record)
            
            _selectedFiles.value.forEach { file ->
                medicalRecordRepository.addDocument(Document(
                    recordId = recordId,
                    docType = "待识别",
                    fileFormat = file.fileType,
                    fileUrl = file.uri.toString(),
                    fileSize = file.fileSize,
                    fileName = file.fileName,
                    ocrStatus = 0,
                    extractStatus = 0
                ))
            }
            
            _importProgress.value = 100
            _importTaskStatus.value = ImportTaskStatus.COMPLETED
            _uiState.update { it.copy(successMessage = "导入成功（离线模式）", lastCreatedRecordId = recordId) }
            clearSelectedFiles()
            
            delay(1500)
            _importTaskStatus.value = ImportTaskStatus.IDLE
            _importProgress.value = 0
        } catch (e: Exception) {
            _importTaskStatus.value = ImportTaskStatus.FAILED
            _uiState.update { it.copy(error = "导入失败：${e.message}") }
        }
    }
    
    /**
     * 重置导入状态
     */
    fun resetImportStatus() {
        _importTaskStatus.value = ImportTaskStatus.IDLE
        _importProgress.value = 0
        clearSelectedFiles()
    }
    
    /**
     * 添加病历记录
     */
    fun addRecord(
        memberId: Long,
        recordType: Int,
        hospitalName: String?,
        department: String?,
        doctorName: String?,
        visitDate: String?,
        mainDiagnosis: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val record = MedicalRecord(
                    memberId = memberId,
                    recordType = recordType,
                    hospitalName = hospitalName,
                    department = department,
                    doctorName = doctorName,
                    visitDate = visitDate,
                    mainDiagnosis = mainDiagnosis
                )
                
                val recordId = medicalRecordRepository.addRecord(record)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "添加成功",
                        lastCreatedRecordId = recordId,
                        error = null
                    )
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
     * 删除病历记录
     */
    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                medicalRecordRepository.deleteRecord(recordId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "删除成功",
                        error = null
                    )
                }
                _selectedRecord.value = null
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
     * 选择病历记录
     */
    fun selectRecord(record: MedicalRecord?) {
        _selectedRecord.value = record
        record?.let { loadDocuments(it.id) }
    }
    
    /**
     * 加载病历详情
     */
    fun loadRecordDetail(recordId: Long) {
        viewModelScope.launch {
            try {
                val record = medicalRecordRepository.getRecordById(recordId)
                _selectedRecord.value = record
                record?.let { loadDocuments(it.id) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "加载失败：${e.message}")
                }
            }
        }
    }
    
    /**
     * 加载病历文书
     */
    private fun loadDocuments(recordId: Long) {
        viewModelScope.launch {
            try {
                val documents = medicalRecordRepository.getDocumentsByRecordIdSync(recordId)
                _currentDocuments.value = documents
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "加载文书失败：${e.message}")
                }
            }
        }
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
}

/**
 * 病历记录UI状态
 */
data class RecordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val lastCreatedRecordId: Long? = null,
    val searchResults: List<MedicalRecord> = emptyList()
)

/**
 * MedicalRecordViewModel工厂
 */
class MedicalRecordViewModelFactory(
    private val medicalRecordRepository: MedicalRecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalRecordViewModel(medicalRecordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
