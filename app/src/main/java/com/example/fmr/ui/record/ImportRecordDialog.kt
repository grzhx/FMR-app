package com.example.fmr.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fmr.data.entity.FamilyMember

/**
 * 病历导入弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRecordDialog(
    familyMembers: List<FamilyMember>,
    selectedFiles: List<SelectedFile>,
    importTaskStatus: ImportTaskStatus,
    importProgress: Int,
    onDismiss: () -> Unit,
    onSelectFiles: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemoveFile: (SelectedFile) -> Unit,
    onStartImport: (memberId: Long) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Long?>(familyMembers.firstOrNull()?.id) }
    var showMemberMenu by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = { if (importTaskStatus == ImportTaskStatus.IDLE || importTaskStatus == ImportTaskStatus.FAILED || importTaskStatus == ImportTaskStatus.COMPLETED) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("导入病历", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (importTaskStatus == ImportTaskStatus.IDLE) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
                
                Divider()
                
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 选择成员
                    item {
                        Text("选择成员", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = showMemberMenu,
                            onExpandedChange = { if (importTaskStatus == ImportTaskStatus.IDLE) showMemberMenu = it }
                        ) {
                            OutlinedTextField(
                                value = familyMembers.find { it.id == selectedMemberId }?.name ?: "请选择",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMemberMenu) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = importTaskStatus == ImportTaskStatus.IDLE
                            )
                            ExposedDropdownMenu(
                                expanded = showMemberMenu,
                                onDismissRequest = { showMemberMenu = false }
                            ) {
                                familyMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text("${member.name} (${member.getRelationText()})") },
                                        onClick = {
                                            selectedMemberId = member.id
                                            showMemberMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // 文件选择区
                    item {
                        Text("选择文件", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 选择文件按钮
                            FileSelectButton(
                                icon = Icons.Default.FolderOpen,
                                text = "选择文件",
                                enabled = importTaskStatus == ImportTaskStatus.IDLE && selectedFiles.size < 10,
                                onClick = onSelectFiles,
                                modifier = Modifier.weight(1f)
                            )
                            // 拍照按钮
                            FileSelectButton(
                                icon = Icons.Default.CameraAlt,
                                text = "拍照上传",
                                enabled = importTaskStatus == ImportTaskStatus.IDLE && selectedFiles.size < 10,
                                onClick = onTakePhoto,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "支持PDF、JPG、PNG格式，单个文件≤20MB，最多10个",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 已选文件列表
                    if (selectedFiles.isNotEmpty()) {
                        item {
                            Text(
                                "已选文件 (${selectedFiles.size}/10)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(selectedFiles) { file ->
                            FileItem(
                                file = file,
                                onRemove = { onRemoveFile(file) },
                                enabled = importTaskStatus == ImportTaskStatus.IDLE
                            )
                        }
                    }
                    
                    // 处理进度
                    if (importTaskStatus != ImportTaskStatus.IDLE) {
                        item {
                            ImportProgressSection(
                                status = importTaskStatus,
                                progress = importProgress
                            )
                        }
                    }
                    
                    // 自动处理说明
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("智能处理功能", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                ProcessFeatureItem(icon = Icons.Default.DocumentScanner, text = "OCR文字识别")
                                ProcessFeatureItem(icon = Icons.Default.Psychology, text = "AI信息抽取")
                                ProcessFeatureItem(icon = Icons.Default.Security, text = "个人信息脱敏")
                            }
                        }
                    }
                }
                
                Divider()
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (importTaskStatus == ImportTaskStatus.IDLE || importTaskStatus == ImportTaskStatus.FAILED || importTaskStatus == ImportTaskStatus.COMPLETED) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("关闭")
                        }
                    }
                    if (importTaskStatus != ImportTaskStatus.FAILED && importTaskStatus != ImportTaskStatus.COMPLETED) {
                        Button(
                            onClick = { selectedMemberId?.let { onStartImport(it) } },
                            modifier = Modifier.weight(1f),
                            enabled = importTaskStatus == ImportTaskStatus.IDLE && 
                                      selectedFiles.isNotEmpty() && 
                                      selectedMemberId != null
                        ) {
                            if (importTaskStatus != ImportTaskStatus.IDLE) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (importTaskStatus == ImportTaskStatus.IDLE) "开始上传" else "处理中...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileSelectButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .height(80.dp)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FileItem(
    file: SelectedFile,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.fileType.uppercase() == "PDF") Icons.Default.PictureAsPdf 
                             else Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(file.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (enabled) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportProgressSection(
    status: ImportTaskStatus,
    progress: Int
) {
    val isFailed = status == ImportTaskStatus.FAILED
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFailed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (isFailed) "处理失败" else "处理进度",
                style = MaterialTheme.typography.titleSmall,
                color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isFailed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI服务暂不可用，请稍后重试", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProgressStep("上传", status.ordinal >= ImportTaskStatus.UPLOADING.ordinal, status == ImportTaskStatus.UPLOADING)
                    ProgressStep("OCR识别", status.ordinal >= ImportTaskStatus.OCR_PROCESSING.ordinal, status == ImportTaskStatus.OCR_PROCESSING)
                    ProgressStep("信息抽取", status.ordinal >= ImportTaskStatus.EXTRACTING.ordinal, status == ImportTaskStatus.EXTRACTING)
                    ProgressStep("完成", status == ImportTaskStatus.COMPLETED, false)
                }
            }
        }
    }
}

@Composable
private fun ProgressStep(text: String, completed: Boolean, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        completed -> MaterialTheme.colorScheme.primary
                        active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (completed || active) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProcessFeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}
