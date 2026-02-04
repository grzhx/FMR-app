package com.example.fmr.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.FamilyMember
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加药品界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    uiState: MedicationUiState,
    familyMembers: List<FamilyMember>,
    onNavigateBack: () -> Unit,
    onSaveMedication: (Long, String, String, String, List<String>, String, String?) -> Unit
) {
    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("每日三次") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var instructions by remember { mutableStateOf("") }
    
    // 服药时间
    var morningTime by remember { mutableStateOf(true) }
    var noonTime by remember { mutableStateOf(true) }
    var eveningTime by remember { mutableStateOf(true) }
    
    var memberExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    
    val frequencyOptions = listOf("每日一次", "每日两次", "每日三次", "每周一次", "需要时服用")
    
    // 成功后返回
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加药品") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 选择成员
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
            
            // 药品名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("药品名称 *") },
                leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 剂量
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("剂量 *") },
                placeholder = { Text("如：1片、5ml") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 服药频率
            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = !frequencyExpanded }
            ) {
                OutlinedTextField(
                    value = frequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("服药频率") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    frequencyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                frequency = option
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }
            
            // 服药时间
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "服药时间",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = morningTime,
                            onClick = { morningTime = !morningTime },
                            label = { Text("早上 8:00") }
                        )
                        FilterChip(
                            selected = noonTime,
                            onClick = { noonTime = !noonTime },
                            label = { Text("中午 12:00") }
                        )
                        FilterChip(
                            selected = eveningTime,
                            onClick = { eveningTime = !eveningTime },
                            label = { Text("晚上 18:00") }
                        )
                    }
                }
            }
            
            // 开始日期
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("开始日期") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 用药说明
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("用药说明") },
                placeholder = { Text("如：饭后服用、空腹服用") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            // 错误提示
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    val times = mutableListOf<String>()
                    if (morningTime) times.add("08:00")
                    if (noonTime) times.add("12:00")
                    if (eveningTime) times.add("18:00")
                    
                    selectedMember?.let { member ->
                        onSaveMedication(
                            member.id,
                            name,
                            dosage,
                            frequency,
                            times,
                            startDate,
                            instructions.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMember != null && name.isNotBlank() && dosage.isNotBlank() && !uiState.isLoading
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
