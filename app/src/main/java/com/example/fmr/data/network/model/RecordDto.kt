package com.example.fmr.data.network.model

/**
 * 病历导入请求
 */
data class ImportRecordRequest(
    val memberId: Long,
    val files: List<ImportFileDto>
)

data class ImportFileDto(
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val fileType: String
)

/**
 * 导入任务响应
 */
data class ImportTaskDto(
    val taskId: String,
    val status: String
)

/**
 * 任务状态响应
 */
data class TaskStatusDto(
    val taskId: String,
    val status: String,      // QUEUED, PROCESSING, COMPLETED, FAILED
    val progress: Int,
    val currentStep: String  // UPLOADING, OCR_RECOGNITION, EXTRACTING, DONE
)

/**
 * 病历列表响应
 */
data class RecordListDto(
    val total: Int,
    val list: List<RecordSummaryDto>
)

data class RecordSummaryDto(
    val id: Long,
    val hospitalName: String?,
    val department: String?,
    val visitDate: String?,
    val recordType: Int,
    val mainDiagnosis: String?,
    val docCount: Int,
    val completeness: Int
)
