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
import com.example.fmr.data.entity.FamilyMember

/**
 * 添加成员界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    uiState: FamilyUiState,
    onNavigateBack: () -> Unit,
    onSaveMember: (name: String, gender: Int, birthDate: String, relation: String, role: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableIntStateOf(FamilyMember.GENDER_MALE) }
    var birthDate by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRelationMenu by remember { mutableStateOf(false) }
    
    val relations = listOf(
        FamilyMember.RELATION_SELF to "本人",
        FamilyMember.RELATION_SPOUSE to "配偶",
        FamilyMember.RELATION_PARENT to "父母",
        FamilyMember.RELATION_CHILD to "子女",
        FamilyMember.RELATION_OTHER to "其他"
    )
    
    // 监听成功消息，返回上一页
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "添加成功") {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加成员") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSaveMember(
                                name,
                                gender,
                                birthDate,
                                relation,
                                if (isAdmin) FamilyMember.ROLE_ADMIN else FamilyMember.ROLE_MEMBER
                            )
                        },
                        enabled = !uiState.isLoading
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
            // 姓名输入
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                label = { Text("姓名 *") },
                placeholder = { Text("请输入成员姓名") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${name.length}/50") }
            )
            
            // 性别选择
            Text(
                text = "性别 *",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = gender == FamilyMember.GENDER_MALE,
                    onClick = { gender = FamilyMember.GENDER_MALE },
                    label = { Text("男") },
                    leadingIcon = if (gender == FamilyMember.GENDER_MALE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gender == FamilyMember.GENDER_FEMALE,
                    onClick = { gender = FamilyMember.GENDER_FEMALE },
                    label = { Text("女") },
                    leadingIcon = if (gender == FamilyMember.GENDER_FEMALE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 出生日期
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("出生日期 *") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                readOnly = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("格式：2000-01-01") }
            )
            
            // 关系选择
            ExposedDropdownMenuBox(
                expanded = showRelationMenu,
                onExpandedChange = { showRelationMenu = it }
            ) {
                OutlinedTextField(
                    value = relations.find { it.first == relation }?.second ?: "",
                    onValueChange = {},
                    label = { Text("与户主关系 *") },
                    placeholder = { Text("请选择关系") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRelationMenu) },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showRelationMenu,
                    onDismissRequest = { showRelationMenu = false }
                ) {
                    relations.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                relation = value
                                showRelationMenu = false
                            }
                        )
                    }
                }
            }
            
            // 管理员开关
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "设为管理员",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "管理员可以管理所有家庭成员的数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAdmin,
                        onCheckedChange = { isAdmin = it }
                    )
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 保存按钮
            Button(
                onClick = {
                    onSaveMember(
                        name,
                        gender,
                        birthDate,
                        relation,
                        if (isAdmin) FamilyMember.ROLE_ADMIN else FamilyMember.ROLE_MEMBER
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("保存成员")
            }
        }
    }
}
