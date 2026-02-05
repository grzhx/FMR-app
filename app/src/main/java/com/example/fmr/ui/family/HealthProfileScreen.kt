package com.example.fmr.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fmr.data.network.model.MemberProfileDto

/**
 * 健康档案编辑页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(
    memberId: Long,
    profile: MemberProfileDto?,
    uiState: FamilyUiState,
    onNavigateBack: () -> Unit,
    onSaveProfile: (Long, Double?, Double?, String?, List<String>?, List<String>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var bloodType by remember(profile) { mutableStateOf(profile?.bloodType ?: "") }
    var allergies by remember(profile) { mutableStateOf(profile?.allergies?.joinToString(", ") ?: "") }
    var chronicDiseases by remember(profile) { mutableStateOf(profile?.chronicDiseases?.joinToString(", ") ?: "") }
    var showBloodTypeMenu by remember { mutableStateOf(false) }
    
    val bloodTypes = listOf("A", "B", "AB", "O", "未知")
    
    // 监听成功消息
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("健康档案") == true) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健康档案") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSaveProfile(
                                memberId,
                                height.toDoubleOrNull(),
                                weight.toDoubleOrNull(),
                                bloodType.ifBlank { null },
                                allergies.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
                                chronicDiseases.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                            )
                        },
                        enabled = !uiState.isLoading
                    ) { Text("保存") }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 基本信息卡片
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("基本信息", style = MaterialTheme.typography.titleMedium)
                        
                        // 身高
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("身高 (cm)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 体重
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("体重 (kg)") },
                            leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // BMI显示
                        val bmi = remember(height, weight) {
                            val h = height.toDoubleOrNull()
                            val w = weight.toDoubleOrNull()
                            if (h != null && w != null && h > 0) {
                                val hm = h / 100
                                String.format("%.1f", w / (hm * hm))
                            } else null
                        }
                        if (bmi != null) {
                            Text("BMI: $bmi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        // 血型
                        ExposedDropdownMenuBox(expanded = showBloodTypeMenu, onExpandedChange = { showBloodTypeMenu = it }) {
                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = {},
                                label = { Text("血型") },
                                leadingIcon = { Icon(Icons.Default.Bloodtype, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodTypeMenu) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = showBloodTypeMenu, onDismissRequest = { showBloodTypeMenu = false }) {
                                bloodTypes.forEach { type ->
                                    DropdownMenuItem(text = { Text(type) }, onClick = { bloodType = type; showBloodTypeMenu = false })
                                }
                            }
                        }
                    }
                }
                
                // 过敏史卡片
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("过敏史", style = MaterialTheme.typography.titleMedium)
                        }
                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("过敏原（多个用逗号分隔）") },
                            placeholder = { Text("如：青霉素, 花粉, 海鲜") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                
                // 慢性病卡片
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("慢性病史", style = MaterialTheme.typography.titleMedium)
                        }
                        OutlinedTextField(
                            value = chronicDiseases,
                            onValueChange = { chronicDiseases = it },
                            label = { Text("慢性病（多个用逗号分隔）") },
                            placeholder = { Text("如：高血压, 糖尿病") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                
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
            }
        }
    }
}
