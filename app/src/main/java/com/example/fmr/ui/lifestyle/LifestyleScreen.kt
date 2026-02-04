package com.example.fmr.ui.lifestyle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fmr.data.entity.DailyGoal
import com.example.fmr.data.entity.FollowUp
import com.example.fmr.data.entity.Medication
import com.example.fmr.data.entity.MedicationSchedule
import com.example.fmr.ui.medication.MedicationScheduleWithMedication
import com.example.fmr.ui.medication.MedicationUiState

/**
 * 生活管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifestyleScreen(
    lifestyleUiState: LifestyleUiState,
    medicationUiState: MedicationUiState,
    medications: List<Medication>,
    todaySchedules: List<MedicationScheduleWithMedication>,
    dailyGoals: List<DailyGoal>,
    followUps: List<FollowUp>,
    onNavigateToAddMedication: () -> Unit,
    onMarkMedicationTaken: (Long) -> Unit,
    onUpdateGoalProgress: (Long, Int) -> Unit,
    onCompleteFollowUp: (Long) -> Unit,
    showBackButton: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("用药助手", "每日目标", "复诊计划")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("生活管理") }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onNavigateToAddMedication) {
                    Icon(Icons.Default.Add, contentDescription = "添加药品")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab栏
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> MedicationTab(
                    uiState = medicationUiState,
                    todaySchedules = todaySchedules,
                    medications = medications,
                    onMarkTaken = onMarkMedicationTaken
                )
                1 -> DailyGoalsTab(
                    goals = dailyGoals,
                    onUpdateProgress = onUpdateGoalProgress
                )
                2 -> FollowUpTab(
                    followUps = followUps,
                    onComplete = onCompleteFollowUp
                )
            }
        }
    }
}

/**
 * 用药助手Tab
 */
@Composable
private fun MedicationTab(
    uiState: MedicationUiState,
    todaySchedules: List<MedicationScheduleWithMedication>,
    medications: List<Medication>,
    onMarkTaken: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 今日进度卡片
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
                        text = "今日服药进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.completionRate },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.todayCompleted}/${uiState.todayTotal} 已完成",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // 今日服药计划
        item {
            Text(
                text = "今日服药计划",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (todaySchedules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "今日暂无服药计划",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todaySchedules) { scheduleWithMed ->
                MedicationScheduleCard(
                    scheduleWithMedication = scheduleWithMed,
                    onMarkTaken = { onMarkTaken(scheduleWithMed.schedule.id) }
                )
            }
        }
        
        // 当前用药列表
        item {
            Text(
                text = "当前用药 (${medications.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(medications) { medication ->
            MedicationCard(medication = medication)
        }
    }
}

/**
 * 服药计划卡片
 */
@Composable
private fun MedicationScheduleCard(
    scheduleWithMedication: MedicationScheduleWithMedication,
    onMarkTaken: () -> Unit
) {
    val schedule = scheduleWithMedication.schedule
    val medication = scheduleWithMedication.medication
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = schedule.scheduleTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = schedule.getPeriodText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 药品信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = medication.dosage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 完成按钮
            if (schedule.taken) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已服用",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                IconButton(onClick = onMarkTaken) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "标记服用",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * 药品卡片
 */
@Composable
private fun MedicationCard(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${medication.dosage} · ${medication.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                medication.instructions?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 每日目标Tab
 */
@Composable
private fun DailyGoalsTab(
    goals: List<DailyGoal>,
    onUpdateProgress: (Long, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无每日目标",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(goals) { goal ->
                DailyGoalCard(
                    goal = goal,
                    onIncrement = { onUpdateProgress(goal.id, 1) }
                )
            }
        }
    }
}

/**
 * 每日目标卡片
 */
@Composable
private fun DailyGoalCard(
    goal: DailyGoal,
    onIncrement: () -> Unit
) {
    val icon = when (goal.type) {
        DailyGoal.TYPE_STEPS -> Icons.Default.DirectionsWalk
        DailyGoal.TYPE_WATER -> Icons.Default.WaterDrop
        DailyGoal.TYPE_EXERCISE -> Icons.Default.FitnessCenter
        DailyGoal.TYPE_SLEEP -> Icons.Default.Bedtime
        else -> Icons.Default.Flag
    }
    
    val color = when (goal.type) {
        DailyGoal.TYPE_STEPS -> Color(0xFF4CAF50)
        DailyGoal.TYPE_WATER -> Color(0xFF2196F3)
        DailyGoal.TYPE_EXERCISE -> Color(0xFFFF9800)
        DailyGoal.TYPE_SLEEP -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.getTypeText(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${goal.currentVal}/${goal.targetVal} ${goal.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onIncrement) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "增加"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { goal.getProgress() },
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
        }
    }
}

/**
 * 复诊计划Tab
 */
@Composable
private fun FollowUpTab(
    followUps: List<FollowUp>,
    onComplete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (followUps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无待复诊计划",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(followUps) { followUp ->
                FollowUpCard(
                    followUp = followUp,
                    onComplete = { onComplete(followUp.id) }
                )
            }
        }
    }
}

/**
 * 复诊计划卡片
 */
@Composable
private fun FollowUpCard(
    followUp: FollowUp,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = followUp.appointmentDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    followUp.appointmentTime?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text(followUp.getStatusText()) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            followUp.hospitalName?.let {
                Text(
                    text = "医院：$it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            followUp.department?.let {
                Text(
                    text = "科室：$it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            followUp.doctorName?.let {
                Text(
                    text = "医生：$it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            followUp.reason?.let {
                Text(
                    text = "原因：$it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (followUp.status == FollowUp.STATUS_PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("标记已完成")
                }
            }
        }
    }
}
