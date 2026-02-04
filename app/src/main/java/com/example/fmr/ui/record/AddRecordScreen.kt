package com.example.fmr.ui.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.MedicalRecord

/**
 * 添加病历界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    uiState: RecordUiState,
    familyMembers: List<FamilyMember>,
    onNavigateBack: () -> Unit,
    onSaveRecord: (memberId: Long, recordType: Int, hospitalName: String?, department: String?, doctorName: String?, visitDate: String?, mainDiagnosis: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var recordType by remember { mutableIntStateOf(MedicalRecord.TYPE_OUTPATIENT) }
    var hospitalName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var mainDiagnosis by remember { mutableStateOf("") }
    var showMemberMenu by remember { mutableStateOf(false) }
    
    val recordTypes: List<Pair<Int, String>> = listOf(
        MedicalRecord.TYPE_OUTPATIENT to "门诊",
        MedicalRecord.TYPE_INPATIENT to "住院",
        MedicalRecord.TYPE_CHECKUP to "体检"
    )
    
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "添加成功") {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加病历") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            selectedMemberId?.let { memberId ->
                                onSaveRecord(
                                    memberId,
                                    recordType,
                                    hospitalName.ifBlank { null },
                                    department.ifBlank { null },
                                    doctorName.ifBlank { null },
                                    visitDate.ifBlank { null },
                                    mainDiagnosis.ifBlank { null }
                                )
                            }
                        },
                        enabled = !uiState.isLoading && selectedMemberId != null
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 选择成员
            ExposedDropdownMenuBox(
                expanded = showMemberMenu,
                onExpandedChange = { showMemberMenu = it }
            ) {
                OutlinedTextField(
                    value = familyMembers.find { it.id == selectedMemberId }?.name ?: "",
                    onValueChange = {},
                    label = { Text("选择成员 *") },
                    placeholder = { Text("请选择家庭成员") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMemberMenu) },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
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
            
            // 病历类型
            Text("病历类型 *", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recordTypes.forEach { pair: Pair<Int, String> ->
                    val type = pair.first
                    val label = pair.second
                    FilterChip(
                        selected = recordType == type,
                        onClick = { recordType = type },
                        label = { Text(label) },
                        leadingIcon = if (recordType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            
            // 医院名称
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("医院名称") },
                placeholder = { Text("请输入医院名称") },
                leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 科室
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("科室") },
                placeholder = { Text("请输入科室名称") },
                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 医生姓名
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("医生姓名") },
                placeholder = { Text("请输入医生姓名") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 就诊日期
            OutlinedTextField(
                value = visitDate,
                onValueChange = { visitDate = it },
                label = { Text("就诊日期") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("格式：2024-01-01") }
            )
            
            // 主要诊断
            OutlinedTextField(
                value = mainDiagnosis,
                onValueChange = { mainDiagnosis = it },
                label = { Text("主要诊断") },
                placeholder = { Text("请输入诊断结果") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 错误提示
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    selectedMemberId?.let { memberId ->
                        onSaveRecord(
                            memberId,
                            recordType,
                            hospitalName.ifBlank { null },
                            department.ifBlank { null },
                            doctorName.ifBlank { null },
                            visitDate.ifBlank { null },
                            mainDiagnosis.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && selectedMemberId != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("保存病历")
            }
        }
    }
}
