package com.example.fmr.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.LabResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加报告界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    uiState: ReportUiState,
    familyMembers: List<FamilyMember>,
    onNavigateBack: () -> Unit,
    onSaveReport: (Long, String, String?, String, List<LabResultInput>) -> Unit
) {
    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }
    var reportType by remember { mutableStateOf("血常规") }
    var hospitalName by remember { mutableStateOf("") }
    var reportDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    
    var memberExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    
    // 检查结果列表
    var results by remember { mutableStateOf(listOf(LabResultInputState())) }
    
    val reportTypes = listOf("血常规", "尿常规", "肝功能", "肾功能", "血脂", "血糖", "甲状腺功能", "心电图", "CT", "MRI", "其他")
    
    // 成功后返回
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加检查报告") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 选择成员
            item {
                ExposedDropdownMenuBox(
                    expanded = memberExpanded,
                    onExpandedChange = { memberExpanded = !memberExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择成员 *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = memberExpanded,
                        onDismissRequest = { memberExpanded = false }
                    ) {
                        familyMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedMember = member
                                    memberExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 报告类型
            item {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = reportType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("报告类型 *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        reportTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    reportType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 医院名称
            item {
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("医院名称") },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // 报告日期
            item {
                OutlinedTextField(
                    value = reportDate,
                    onValueChange = { reportDate = it },
                    label = { Text("报告日期 *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // 检查结果标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "检查结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(
                        onClick = {
                            results = results + LabResultInputState()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加项目")
                    }
                }
            }
            
            // 检查结果列表
            itemsIndexed(results) { index, result ->
                LabResultInputCard(
                    result = result,
                    onUpdate = { updated ->
                        results = results.toMutableList().apply {
                            this[index] = updated
                        }
                    },
                    onDelete = {
                        if (results.size > 1) {
                            results = results.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    },
                    canDelete = results.size > 1
                )
            }
            
            // 错误提示
            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // 保存按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val labResults = results
                            .filter { it.itemName.isNotBlank() && it.itemValue.isNotBlank() }
                            .map { state ->
                                LabResultInput(
                                    itemName = state.itemName,
                                    itemValue = state.itemValue,
                                    itemUnit = state.itemUnit.ifBlank { null },
                                    referenceRange = state.referenceRange.ifBlank { null },
                                    status = state.status
                                )
                            }
                        
                        selectedMember?.let { member ->
                            onSaveReport(
                                member.id,
                                reportType,
                                hospitalName.ifBlank { null },
                                reportDate,
                                labResults
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedMember != null && reportDate.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("保存")
                    }
                }
            }
        }
    }
}

/**
 * 检查结果输入状态
 */
data class LabResultInputState(
    val itemName: String = "",
    val itemValue: String = "",
    val itemUnit: String = "",
    val referenceRange: String = "",
    val status: Int = LabResult.STATUS_NORMAL
)

/**
 * 检查结果输入卡片
 */
@Composable
private fun LabResultInputCard(
    result: LabResultInputState,
    onUpdate: (LabResultInputState) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "检查项目",
                    style = MaterialTheme.typography.titleSmall
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = result.itemName,
                onValueChange = { onUpdate(result.copy(itemName = it)) },
                label = { Text("项目名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = result.itemValue,
                    onValueChange = { onUpdate(result.copy(itemValue = it)) },
                    label = { Text("检测值 *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = result.itemUnit,
                    onValueChange = { onUpdate(result.copy(itemUnit = it)) },
                    label = { Text("单位") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            OutlinedTextField(
                value = result.referenceRange,
                onValueChange = { onUpdate(result.copy(referenceRange = it)) },
                label = { Text("参考范围") },
                placeholder = { Text("如：3.5-5.5") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 状态选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = result.status == LabResult.STATUS_NORMAL,
                    onClick = { onUpdate(result.copy(status = LabResult.STATUS_NORMAL)) },
                    label = { Text("正常") }
                )
                FilterChip(
                    selected = result.status == LabResult.STATUS_HIGH,
                    onClick = { onUpdate(result.copy(status = LabResult.STATUS_HIGH)) },
                    label = { Text("偏高") }
                )
                FilterChip(
                    selected = result.status == LabResult.STATUS_LOW,
                    onClick = { onUpdate(result.copy(status = LabResult.STATUS_LOW)) },
                    label = { Text("偏低") }
                )
            }
        }
    }
}
