package com.example.fmr.ui.family

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.MedicalRecord
import com.example.fmr.data.network.model.MemberProfileDto
import com.example.fmr.ui.record.ImportTaskStatus
import com.example.fmr.ui.record.RecordUiState
import com.example.fmr.ui.record.SelectedFile
import com.example.fmr.ui.record.ImportRecordDialog

/**
 * 成员详情页面 - 展示个人信息、健康档案、病例记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    member: FamilyMember?,
    memberProfile: MemberProfileDto?,
    memberRecords: List<MedicalRecord>,
    uiState: FamilyUiState,
    recordUiState: RecordUiState,
    selectedFiles: List<SelectedFile>,
    importTaskStatus: ImportTaskStatus,
    importProgress: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEditMember: () -> Unit,
    onNavigateToEditHealth: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onDeleteMember: (Long) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onAddSelectedFile: (SelectedFile) -> Unit,
    onRemoveSelectedFile: (SelectedFile) -> Unit,
    onStartImport: (Long) -> Unit,
    onResetImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteRecordDialog by remember { mutableStateOf<MedicalRecord?>(null) }
    var triggerFilePicker by remember { mutableStateOf(false) }
    var triggerCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
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
                else -> "JPG"
            }
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

    LaunchedEffect(triggerFilePicker) {
        if (triggerFilePicker) {
            filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
            triggerFilePicker = false
        }
    }

    LaunchedEffect(triggerCamera) {
        if (triggerCamera) {
            try {
                val photoFile = java.io.File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context, "${context.packageName}.fileprovider", photoFile
                )
                photoUri?.let { cameraLauncher.launch(it) }
            } catch (e: Exception) { e.printStackTrace() }
            triggerCamera = false
        }
    }

    // 删除成员确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除成员 ${member.name} 吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMember(member.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }

    // 删除病历确认对话框
    showDeleteRecordDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { showDeleteRecordDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条病历记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(record.id)
                        showDeleteRecordDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteRecordDialog = null }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成员详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 个人信息卡片
            item {
                PersonalInfoCard(
                    member = member,
                    onEditClick = onNavigateToEditMember
                )
            }

            // 健康档案卡片
            item {
                HealthProfileCard(
                    profile = memberProfile,
                    onEditClick = onNavigateToEditHealth
                )
            }

            // 病例记录卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("病例记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                TextButton(onClick = { showImportDialog = true }) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("导入")
                                }
                                TextButton(onClick = onNavigateToAddRecord) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加")
                                }
                            }
                        }
                    }
                }
            }

            // 病例列表
            if (memberRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("暂无病例记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(memberRecords) { record ->
                    RecordItem(
                        record = record,
                        onDeleteClick = { showDeleteRecordDialog = record }
                    )
                }
            }
        }

        // 导入弹窗
        if (showImportDialog) {
            ImportRecordDialog(
                familyMembers = listOf(member),
                selectedFiles = selectedFiles,
                importTaskStatus = importTaskStatus,
                importProgress = importProgress,
                onDismiss = {
                    showImportDialog = false
                    onResetImport()
                },
                onSelectFiles = { triggerFilePicker = true },
                onTakePhoto = { triggerCamera = true },
                onRemoveFile = onRemoveSelectedFile,
                onStartImport = { onStartImport(member.id) }
            )
        }
    }
}

@Composable
private fun PersonalInfoCard(member: FamilyMember, onEditClick: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("个人信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修改")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.avatarUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(member.avatarUrl).crossfade(true).build(),
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(member.name.take(1), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(member.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                member.getRelationText(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (member.role == FamilyMember.ROLE_ADMIN) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF9800).copy(alpha = 0.1f)) {
                                Text(
                                    "管理员",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF9800),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${member.getGenderText()} · ${member.calculateAge()}岁 · ${member.birthDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthProfileCard(profile: MemberProfileDto?, onEditClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("健康档案", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修改")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (profile == null) {
                Text("暂无健康档案信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem("身高", profile.height?.let { "${it}cm" } ?: "-", Modifier.weight(1f))
                    InfoItem("体重", profile.weight?.let { "${it}kg" } ?: "-", Modifier.weight(1f))
                    InfoItem("血型", profile.bloodType ?: "-", Modifier.weight(1f))
                    val bmi = if (profile.height != null && profile.weight != null && profile.height > 0) {
                        val hm = profile.height / 100
                        String.format("%.1f", profile.weight / (hm * hm))
                    } else "-"
                    InfoItem("BMI", bmi, Modifier.weight(1f))
                }
                if (!profile.allergies.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("过敏史：${profile.allergies.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                if (!profile.chronicDiseases.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("慢性病：${profile.chronicDiseases.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecordItem(record: MedicalRecord, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(getRecordTypeColor(record.recordType).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(getRecordTypeIcon(record.recordType), contentDescription = null, tint = getRecordTypeColor(record.recordType), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(record.hospitalName ?: "未知医院", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = getRecordTypeColor(record.recordType).copy(alpha = 0.1f)) {
                        Text(record.getRecordTypeText(), style = MaterialTheme.typography.labelSmall, color = getRecordTypeColor(record.recordType), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(record.visitDate ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                record.mainDiagnosis?.let {
                    Text("诊断：$it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
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
