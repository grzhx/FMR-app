package com.example.fmr.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.entity.MedicalRecord

/**
 * 首页界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    familyMembers: List<FamilyMember>,
    onNavigateToFamily: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.greeting,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "家庭病历本",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 快捷入口
                item {
                    ShortcutsSection(
                        onScanMedicine = { /* TODO */ },
                        onUploadRecord = onNavigateToAddRecord,
                        onViewFamily = onNavigateToFamily,
                        onViewRecords = onNavigateToRecords
                    )
                }
                
                // 家庭成员概览
                item {
                    FamilyMembersSection(
                        members = familyMembers,
                        onViewAll = onNavigateToFamily
                    )
                }
                
                // 健康摘要
                item {
                    HealthSummarySection(
                        memberCount = uiState.memberCount,
                        recordCount = uiState.recordCount
                    )
                }
                
                // 最近动态
                item {
                    RecentRecordsSection(
                        records = uiState.recentRecords,
                        onViewAll = onNavigateToRecords
                    )
                }
            }
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

/**
 * 快捷入口区域
 */
@Composable
private fun ShortcutsSection(
    onScanMedicine: () -> Unit,
    onUploadRecord: () -> Unit,
    onViewFamily: () -> Unit,
    onViewRecords: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "快捷入口",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShortcutItem(
                    icon = Icons.Default.QrCodeScanner,
                    label = "扫药盒",
                    color = Color(0xFF4CAF50),
                    onClick = onScanMedicine
                )
                ShortcutItem(
                    icon = Icons.Default.CameraAlt,
                    label = "拍病历",
                    color = Color(0xFF2196F3),
                    onClick = onUploadRecord
                )
                ShortcutItem(
                    icon = Icons.Default.People,
                    label = "家庭成员",
                    color = Color(0xFFFF9800),
                    onClick = onViewFamily
                )
                ShortcutItem(
                    icon = Icons.Default.FolderOpen,
                    label = "病历档案",
                    color = Color(0xFF9C27B0),
                    onClick = onViewRecords
                )
            }
        }
    }
}

/**
 * 快捷入口项
 */
@Composable
private fun ShortcutItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * 家庭成员概览区域
 */
@Composable
private fun FamilyMembersSection(
    members: List<FamilyMember>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "家庭成员",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAll) {
                    Text("查看全部")
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (members.isEmpty()) {
                Text(
                    text = "暂无家庭成员，点击添加",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(members) { member ->
                        MemberAvatar(member = member)
                    }
                }
            }
        }
    }
}

/**
 * 成员头像
 */
@Composable
private fun MemberAvatar(member: FamilyMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when (member.relation) {
                        FamilyMember.RELATION_SELF -> Color(0xFF4CAF50)
                        FamilyMember.RELATION_SPOUSE -> Color(0xFFE91E63)
                        FamilyMember.RELATION_CHILD -> Color(0xFF2196F3)
                        FamilyMember.RELATION_PARENT -> Color(0xFFFF9800)
                        else -> Color(0xFF9E9E9E)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = member.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
        Text(
            text = member.getRelationText(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 健康摘要区域
 */
@Composable
private fun HealthSummarySection(
    memberCount: Int,
    recordCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            title = "家庭成员",
            value = memberCount.toString(),
            unit = "人",
            icon = Icons.Default.People,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "病历档案",
            value = recordCount.toString(),
            unit = "份",
            icon = Icons.Default.Description,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 摘要卡片
 */
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 最近动态区域
 */
@Composable
private fun RecentRecordsSection(
    records: List<MedicalRecord>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近动态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAll) {
                    Text("查看全部")
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无病历记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                records.forEach { record ->
                    RecordItem(record = record)
                    if (record != records.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 病历记录项
 */
@Composable
private fun RecordItem(record: MedicalRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (record.recordType) {
                        MedicalRecord.TYPE_OUTPATIENT -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        MedicalRecord.TYPE_INPATIENT -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        MedicalRecord.TYPE_CHECKUP -> Color(0xFF2196F3).copy(alpha = 0.1f)
                        else -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (record.recordType) {
                    MedicalRecord.TYPE_OUTPATIENT -> Icons.Default.LocalHospital
                    MedicalRecord.TYPE_INPATIENT -> Icons.Default.Hotel
                    MedicalRecord.TYPE_CHECKUP -> Icons.Default.FactCheck
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = when (record.recordType) {
                    MedicalRecord.TYPE_OUTPATIENT -> Color(0xFF4CAF50)
                    MedicalRecord.TYPE_INPATIENT -> Color(0xFFFF9800)
                    MedicalRecord.TYPE_CHECKUP -> Color(0xFF2196F3)
                    else -> Color(0xFF9E9E9E)
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = record.hospitalName ?: "未知医院",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${record.getRecordTypeText()} · ${record.visitDate ?: "未知日期"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
