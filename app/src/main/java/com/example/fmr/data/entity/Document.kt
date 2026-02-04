package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 病历文书实体类
 * 对应数据库表 t_document
 */
@Entity(
    tableName = "t_document",
    foreignKeys = [
        ForeignKey(
            entity = MedicalRecord::class,
            parentColumns = ["id"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["record_id"])
    ]
)
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "record_id")
    val recordId: Long,
    
    @ColumnInfo(name = "doc_type")
    val docType: String, // 文书类型：处方单、检查报告等
    
    @ColumnInfo(name = "file_format")
    val fileFormat: String, // PDF, JPG, PNG
    
    @ColumnInfo(name = "file_url")
    val fileUrl: String, // 文件存储URL或本地路径
    
    @ColumnInfo(name = "masked_url")
    val maskedUrl: String? = null, // 脱敏后文件URL
    
    @ColumnInfo(name = "ocr_status")
    val ocrStatus: Int = 0, // 0-待处理，1-成功，2-失败
    
    @ColumnInfo(name = "extract_status")
    val extractStatus: Int = 0, // 0-待处理，1-成功，2-失败
    
    @ColumnInfo(name = "ocr_text")
    val ocrText: String? = null, // OCR识别的文本内容
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long, // 文件大小（字节）
    
    @ColumnInfo(name = "file_name")
    val fileName: String? = null, // 原始文件名
    
    @ColumnInfo(name = "upload_time")
    val uploadTime: Long = System.currentTimeMillis()
) {
    companion object {
        // OCR状态常量
        const val OCR_PENDING = 0
        const val OCR_SUCCESS = 1
        const val OCR_FAILED = 2
        
        // 抽取状态常量
        const val EXTRACT_PENDING = 0
        const val EXTRACT_SUCCESS = 1
        const val EXTRACT_FAILED = 2
        
        // 文书类型常量
        const val DOC_TYPE_PRESCRIPTION = "处方单"
        const val DOC_TYPE_REPORT = "检查报告"
        const val DOC_TYPE_DIAGNOSIS = "诊断证明"
        const val DOC_TYPE_BILL = "费用清单"
        const val DOC_TYPE_OTHER = "其他"
        
        // 文件格式常量
        const val FORMAT_PDF = "PDF"
        const val FORMAT_JPG = "JPG"
        const val FORMAT_PNG = "PNG"
        const val FORMAT_JPEG = "JPEG"
    }
    
    /**
     * 获取OCR状态显示文本
     */
    fun getOcrStatusText(): String = when (ocrStatus) {
        OCR_PENDING -> "待处理"
        OCR_SUCCESS -> "识别成功"
        OCR_FAILED -> "识别失败"
        else -> "未知"
    }
    
    /**
     * 获取文件大小显示文本
     */
    fun getFileSizeText(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            else -> "${fileSize / (1024 * 1024)}MB"
        }
    }
    
    /**
     * 判断是否为图片格式
     */
    fun isImage(): Boolean {
        return fileFormat.uppercase() in listOf(FORMAT_JPG, FORMAT_PNG, FORMAT_JPEG)
    }
    
    /**
     * 判断是否为PDF格式
     */
    fun isPdf(): Boolean {
        return fileFormat.uppercase() == FORMAT_PDF
    }
}
