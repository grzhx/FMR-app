package com.example.fmr.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

/**
 * 个人中心界面（我的页面）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onClearMessages: () -> Unit,
    onStartEditing: () -> Unit = {},
    onCancelEditing: () -> Unit = {},
    onSaveUserInfo: () -> Unit = {},
    onUpdateEditField: (nickname: String?, phone: String?, email: String?, avatarUrl: String?) -> Unit = { _, _, _, _ -> }
) {
    // 显示消息后自动清除
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            onClearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    if (uiState.isLoggedIn) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
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
        ) {
            if (uiState.isLoggedIn) {
                LoggedInContent(
                    uiState = uiState,
                    onLogout = onLogout,
                    onStartEditing = onStartEditing
                )
            } else {
                LoggedOutContent(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
            }

            // 消息提示
            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = uiState.successMessage,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    // 编辑对话框
    if (uiState.isEditing) {
        EditProfileDialog(
            uiState = uiState,
            onDismiss = onCancelEditing,
            onSave = onSaveUserInfo,
            onUpdateField = onUpdateEditField
        )
    }
}

/**
 * 编辑个人资料对话框
 */
@Composable
private fun EditProfileDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUpdateField: (nickname: String?, phone: String?, email: String?, avatarUrl: String?) -> Unit
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdateField(null, null, null, it.toString()) }
    }

    // 拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempPhotoUri?.let { onUpdateField(null, null, null, it.toString()) }
        }
    }

    // 图片来源选择对话框
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("选择头像来源") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("从相册选择") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("拍照") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            val photoFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                            tempPhotoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempPhotoUri?.let { cameraLauncher.launch(it) }
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) { Text("取消") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑个人资料") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 头像选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像", modifier = Modifier.width(60.dp))
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.editAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = uiState.editAvatarUrl,
                                contentDescription = "头像预览",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "选择头像",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(onClick = { showImageSourceDialog = true }) {
                        Text("更换头像")
                    }
                }

                OutlinedTextField(
                    value = uiState.editNickname,
                    onValueChange = { onUpdateField(it, null, null, null) },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.editPhone,
                    onValueChange = { onUpdateField(null, it, null, null) },
                    label = { Text("手机号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.editEmail,
                    onValueChange = { onUpdateField(null, null, it, null) },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = !uiState.isLoading) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("保存")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 已登录内容
 */
@Composable
private fun LoggedInContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onStartEditing: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 用户信息卡片
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "头像",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 昵称
                Text(
                    text = uiState.nickname ?: "未设置昵称",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // 用户名
                Text(
                    text = "@${uiState.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 编辑按钮
                OutlinedButton(onClick = onStartEditing) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑资料")
                }
            }
        }

        // 用户详细信息
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "个人信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                InfoRow(Icons.Default.Person, "用户ID", uiState.userId?.toString() ?: "-")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.Phone, "手机号", uiState.phone ?: "未设置")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.Email, "邮箱", uiState.email ?: "未设置")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.DateRange, "注册时间", uiState.createTime ?: "-")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Default.AccessTime, "最后登录", uiState.lastLoginTime ?: "-")
            }
        }

        // 退出登录按钮
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onError)
            } else {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
        }
    }
}

/**
 * 未登录内容
 */
@Composable
private fun LoggedOutContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "欢迎使用家庭医疗档案管理",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "登录后可查看和管理个人信息",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("登录")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("注册")
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
