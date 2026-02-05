package com.example.fmr.ui.family

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fmr.data.entity.FamilyMember

/**
 * 成员编辑页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEditScreen(
    member: FamilyMember?,
    uiState: FamilyUiState,
    onNavigateBack: () -> Unit,
    onUpdateMember: (FamilyMember) -> Unit,
    modifier: Modifier = Modifier
) {
    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var name by remember(member) { mutableStateOf(member.name) }
    var gender by remember(member) { mutableIntStateOf(member.gender) }
    var birthDate by remember(member) { mutableStateOf(member.birthDate) }
    var relation by remember(member) { mutableStateOf(member.relation) }
    var isAdmin by remember(member) { mutableStateOf(member.role == FamilyMember.ROLE_ADMIN) }
    var avatarUri by remember(member) { mutableStateOf<Uri?>(member.avatarUrl?.let { Uri.parse(it) }) }
    var showRelationMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> avatarUri = uri }

    val relations = listOf(
        FamilyMember.RELATION_SELF to "本人",
        FamilyMember.RELATION_SPOUSE to "配偶",
        FamilyMember.RELATION_PARENT to "父母",
        FamilyMember.RELATION_CHILD to "子女",
        FamilyMember.RELATION_OTHER to "其他"
    )

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("成功") == true) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑个人信息") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onUpdateMember(member.copy(
                                name = name,
                                gender = gender,
                                birthDate = birthDate,
                                relation = relation,
                                role = if (isAdmin) FamilyMember.ROLE_ADMIN else FamilyMember.ROLE_MEMBER,
                                avatarUrl = avatarUri?.toString()
                            ))
                        },
                        enabled = !uiState.isLoading
                    ) { Text("保存") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(avatarUri).crossfade(true).build(),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                }
            }
            Text("点击更换头像", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // 姓名
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                label = { Text("姓名") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 性别
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = gender == FamilyMember.GENDER_MALE,
                    onClick = { gender = FamilyMember.GENDER_MALE },
                    label = { Text("男") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gender == FamilyMember.GENDER_FEMALE,
                    onClick = { gender = FamilyMember.GENDER_FEMALE },
                    label = { Text("女") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 出生日期
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("出生日期") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 关系
            ExposedDropdownMenuBox(expanded = showRelationMenu, onExpandedChange = { showRelationMenu = it }) {
                OutlinedTextField(
                    value = relations.find { it.first == relation }?.second ?: "",
                    onValueChange = {},
                    label = { Text("与户主关系") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRelationMenu) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = showRelationMenu, onDismissRequest = { showRelationMenu = false }) {
                    relations.forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { relation = value; showRelationMenu = false })
                    }
                }
            }

            // 管理员开关
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("设为管理员", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isAdmin, onCheckedChange = { isAdmin = it })
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
