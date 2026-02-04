package com.example.fmr.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.LabResult

/**
 * 报告详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Long,
    uiState: ReportUiState,
    onNavigateBack: () -> Unit,
    onLoadReport: () -> Unit
) {
    // 加载报告详情
    LaunchedEffect(reportId) {
        onLoadReport()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("报告详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 这里需要从ViewModel获取报告详情
            // 由于当前架构限制，我们显示一个占位界面
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "报告解读",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "报告ID: $reportId",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "详细的报告解读功能正在开发中...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                item {
                    Text(
                        text = "检查结果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 示例结果卡片
                item {
                    ResultItemCard(
                        itemName = "白细胞计数",
                        itemValue = "6.5",
                        itemUnit = "10^9/L",
                        referenceRange = "4.0-10.0",
                        status = LabResult.STATUS_NORMAL
                    )
                }
                
                item {
                    ResultItemCard(
                        itemName = "红细胞计数",
                        itemValue = "5.2",
                        itemUnit = "10^12/L",
                        referenceRange = "4.0-5.5",
                        status = LabResult.STATUS_NORMAL
                    )
                }
                
                item {
                    ResultItemCard(
                        itemName = "血红蛋白",
                        itemValue = "165",
                        itemUnit = "g/L",
                        referenceRange = "120-160",
                        status = LabResult.STATUS_HIGH
                    )
                }
            }
        }
    }
}

/**
 * 检查结果项卡片
 */
@Composable
private fun ResultItemCard(
    itemName: String,
    itemValue: String,
    itemUnit: String?,
    referenceRange: String?,
    status: Int
) {
    val statusColor = when (status) {
        LabResult.STATUS_HIGH -> Color(0xFFD32F2F)
        LabResult.STATUS_LOW -> Color(0xFF1976D2)
        else -> Color(0xFF388E3C)
    }
    
    val statusText = when (status) {
        LabResult.STATUS_HIGH -> "↑ 偏高"
        LabResult.STATUS_LOW -> "↓ 偏低"
        else -> "正常"
    }
    
    val statusBgColor = when (status) {
        LabResult.STATUS_HIGH -> Color(0xFFFFEBEE)
        LabResult.STATUS_LOW -> Color(0xFFE3F2FD)
        else -> Color(0xFFE8F5E9)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                referenceRange?.let {
                    Text(
                        text = "参考范围：$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = itemValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    itemUnit?.let {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = statusBgColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
