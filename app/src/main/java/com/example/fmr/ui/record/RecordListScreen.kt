package com.example.fmr.ui.record

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.MedicalRecord

/**
 * 病历记录列表界面 - 时间轴样式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    uiState: RecordUiState,
    records: List<MedicalRecord>,
    familyMembers: List<FamilyMember>,
    selectedFiles: List<SelectedFile>,
    importTaskStatus: ImportTaskStatus,
    importProgress: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onRecordClick: (MedicalRecord) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onAddSelectedFile: (SelectedFile) -> Unit,
    onRemoveSelectedFile: (SelectedFile) -> Unit,
    onStartImport: (Long) -> Unit,
    onResetImport: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf<MedicalRecord?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var triggerFilePicker by remember { mutableStateOf(false) }
    var triggerCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            // 获取真实文件名
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val fileName = cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) it.getString(nameIndex) else uri.lastPathSegment
                } else uri.lastPathSegment
            } ?: uri.lastPathSegment ?: "未知文件"
            
            val fileType = when {
                fileName.endsWith(".pdf", true) -> "PDF"
                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "JPG"
                fileName.endsWith(".png", true) -> "PNG"
                else -> "JPG" // 默认当作图片
            }
            // 获取文件大小
            val fileSize = try {
                context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
            } catch (e: Exception) { 0L }
            
            onAddSelectedFile(SelectedFile(uri, fileName, fileSize, fileType))
        }
    }
    
    // 拍照
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            onAddSelectedFile(SelectedFile(photoUri!!, fileName, 0L, "JPG"))
        }
    }
    
    // 触发文件选择
    LaunchedEffect(triggerFilePicker) {
        if (triggerFilePicker) {
            filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
            triggerFilePicker = false
        }
    }
    
    // 触发拍照
    LaunchedEffect(triggerCamera) {
        if (triggerCamera) {
            try {
                val photoFile = java.io.File(
                    context.cacheDir,
                    "photo_${System.currentTimeMillis()}.jpg"
                )
                photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                photoUri?.let { cameraLauncher.launch(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            triggerCamera = false
        }
    }
    
    // 按日期分组记录
    val groupedRecords = records.groupBy { it.visitDate ?: "未知日期" }
        .toSortedMap(compareByDescending { it })
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("病历档案") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 手动添加按钮
                    IconButton(onClick = onNavigateToAddRecord) {
                        Icon(Icons.Default.Edit, contentDescription = "手动添加")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showImportDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                text = { Text("导入病历") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (records.isEmpty()) {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onImportClick = { showImportDialog = true }
                )
            } else {
                // 时间轴列表
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    groupedRecords.forEach { (date, dateRecords) ->
                        item {
                            // 日期标题
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        itemsIndexed(dateRecords) { index, record ->
                            TimelineRecordItem(
                                record = record,
                                isLast = index == dateRecords.size - 1,
                                onClick = { onRecordClick(record) },
                                onDeleteClick = { showDeleteDialog = record }
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    
                    item {
                        Text(
                            text = "共 ${records.size} 条病历记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = onClearError) { Text("关闭") }
                    }
                ) {
                    Text(error)
                }
            }
        }
        
        // 删除确认弹窗
        showDeleteDialog?.let { record ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除这条病历记录吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteRecord(record.id)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("删除") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
                }
            )
        }
        
        // 导入弹窗
        if (showImportDialog) {
            ImportRecordDialog(
                familyMembers = familyMembers,
                selectedFiles = selectedFiles,
                importTaskStatus = importTaskStatus,
                importProgress = importProgress,
                onDismiss = {
                    showImportDialog = false
                    onResetImport()
                },
                onSelectFiles = {
                    triggerFilePicker = true
                },
                onTakePhoto = {
                    triggerCamera = true
                },
                onRemoveFile = onRemoveSelectedFile,
                onStartImport = { memberId ->
                    onStartImport(memberId)
                }
            )
        }
    }
}

@Composable
private fun TimelineRecordItem(
    record: MedicalRecord,
    isLast: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // 时间轴线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(getRecordTypeColor(record.recordType))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 记录卡片
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getRecordTypeColor(record.recordType).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getRecordTypeIcon(record.recordType),
                        contentDescription = null,
                        tint = getRecordTypeColor(record.recordType),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            record.hospitalName ?: "未知医院",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = getRecordTypeColor(record.recordType).copy(alpha = 0.1f)
                        ) {
                            Text(
                                record.getRecordTypeText(),
                                style = MaterialTheme.typography.labelSmall,
                                color = getRecordTypeColor(record.recordType),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        record.department ?: "未知科室",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    record.mainDiagnosis?.let {
                        Text(
                            "诊断：$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, onImportClick: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "暂无病历记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "点击下方按钮导入病历文书",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onImportClick) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导入病历")
        }
    }
}

private fun getRecordTypeColor(type: Int): Color = when (type) {
    MedicalRecord.TYPE_OUTPATIENT -> Color(0xFF4CAF50)
    MedicalRecord.TYPE_INPATIENT -> Color(0xFFFF9800)
    MedicalRecord.TYPE_CHECKUP -> Color(0xFF2196F3)
    else -> Color(0xFF9E9E9E)
}

private fun getRecordTypeIcon(type: Int) = when (type) {
    MedicalRecord.TYPE_OUTPATIENT -> Icons.Default.LocalHospital
    MedicalRecord.TYPE_INPATIENT -> Icons.Default.Hotel
    MedicalRecord.TYPE_CHECKUP -> Icons.Default.FactCheck
    else -> Icons.Default.Description
}
