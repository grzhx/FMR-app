package com.example.fmr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fmr.data.network.ConnectionState

/**
 * 网络状态指示器
 * 显示当前的网络连接状态
 */
@Composable
fun NetworkStatusIndicator(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon, text) = when (connectionState) {
        ConnectionState.OFFLINE -> Quadruple(
            Color(0xFFFFCDD2),
            Color(0xFFC62828),
            Icons.Default.CloudOff,
            "离线模式 - 数据仅保存在本地"
        )
        ConnectionState.NETWORK_ONLY -> Quadruple(
            Color(0xFFFFF9C4),
            Color(0xFFF57F17),
            Icons.Default.CloudQueue,
            "网络可用 - 服务器连接中..."
        )
        ConnectionState.CONNECTED -> Quadruple(
            Color(0xFFC8E6C9),
            Color(0xFF2E7D32),
            Icons.Default.CloudDone,
            "已连接服务器"
        )
        ConnectionState.UNKNOWN -> Quadruple(
            Color(0xFFE0E0E0),
            Color(0xFF616161),
            Icons.Default.CloudQueue,
            "检测网络状态..."
        )
    }
    
    // 只在非连接状态时显示
    AnimatedVisibility(
        visible = connectionState != ConnectionState.CONNECTED,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 同步状态指示器
 * 显示数据同步状态
 */
@Composable
fun SyncStatusIndicator(
    isSyncing: Boolean,
    isSynced: Boolean,
    syncMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isSyncing || syncMessage != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        val backgroundColor = when {
            isSyncing -> Color(0xFFE3F2FD)
            isSynced -> Color(0xFFC8E6C9)
            else -> Color(0xFFFFF9C4)
        }
        
        val textColor = when {
            isSyncing -> Color(0xFF1565C0)
            isSynced -> Color(0xFF2E7D32)
            else -> Color(0xFFF57F17)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = textColor
                    )
                } else {
                    Icon(
                        imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isSyncing -> "正在同步..."
                        syncMessage != null -> syncMessage
                        else -> ""
                    },
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (!isSyncing && syncMessage != null) {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "关闭",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * 离线模式提示卡片
 */
@Composable
fun OfflineModeCard(
    isOffline: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "离线模式",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "当前无法连接到服务器，数据将保存在本地。\n连接恢复后会自动同步。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF795548),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE65100)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重试连接")
                }
            }
        }
    }
}

/**
 * 四元组数据类
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
