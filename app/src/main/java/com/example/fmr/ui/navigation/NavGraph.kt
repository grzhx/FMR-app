package com.example.fmr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fmr.ui.family.AddMemberScreen
import com.example.fmr.ui.family.FamilyListScreen
import com.example.fmr.ui.family.FamilyViewModel
import com.example.fmr.ui.home.HomeScreen
import com.example.fmr.ui.home.HomeViewModel
import com.example.fmr.ui.lifestyle.LifestyleScreen
import com.example.fmr.ui.lifestyle.LifestyleViewModel
import com.example.fmr.ui.medication.AddMedicationScreen
import com.example.fmr.ui.medication.MedicationViewModel
import com.example.fmr.ui.record.AddRecordScreen
import com.example.fmr.ui.record.MedicalRecordViewModel
import com.example.fmr.ui.record.RecordListScreen
import com.example.fmr.ui.report.AddReportScreen
import com.example.fmr.ui.report.ReportDetailScreen
import com.example.fmr.ui.report.ReportListScreen
import com.example.fmr.ui.report.ReportViewModel
import com.example.fmr.ui.profile.LoginScreen
import com.example.fmr.ui.profile.ProfileScreen
import com.example.fmr.ui.profile.ProfileViewModel
import com.example.fmr.ui.profile.RegisterScreen

/**
 * 导航路由
 */
object Routes {
    // 主页面（底部导航栏）
    const val HOME = "home"
    const val FAMILY_LIST = "family_list"
    const val RECORD_LIST = "record_list"
    const val LIFESTYLE = "lifestyle"
    const val REPORT_LIST = "report_list"
    const val PROFILE = "profile"

    // 子页面
    const val ADD_MEMBER = "add_member"
    const val ADD_RECORD = "add_record"
    const val ADD_MEDICATION = "add_medication"
    const val ADD_REPORT = "add_report"
    const val REPORT_DETAIL = "report_detail/{reportId}"

    // 认证页面
    const val LOGIN = "login"
    const val REGISTER = "register"

    // 辅助函数
    fun reportDetail(reportId: Long) = "report_detail/$reportId"
}

/**
 * 主页面路由列表（用于判断是否显示底部导航栏）
 */
val mainRoutes = listOf(
    Routes.HOME,
    Routes.FAMILY_LIST,
    Routes.RECORD_LIST,
    Routes.LIFESTYLE,
    Routes.REPORT_LIST,
    Routes.PROFILE
)

/**
 * 导航图
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    familyViewModel: FamilyViewModel,
    recordViewModel: MedicalRecordViewModel,
    medicationViewModel: MedicationViewModel,
    lifestyleViewModel: LifestyleViewModel,
    reportViewModel: ReportViewModel,
    profileViewModel: ProfileViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // ==================== 首页 ====================
        composable(Routes.HOME) {
            val uiState by homeViewModel.uiState.collectAsState()
            val familyMembers by homeViewModel.familyMembers.collectAsState()
            
            HomeScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                onNavigateToFamily = { navController.navigate(Routes.FAMILY_LIST) },
                onNavigateToRecords = { navController.navigate(Routes.RECORD_LIST) },
                onNavigateToAddRecord = { navController.navigate(Routes.ADD_RECORD) }
            )
        }
        
        // ==================== 家庭成员模块 ====================
        composable(Routes.FAMILY_LIST) {
            val uiState by familyViewModel.uiState.collectAsState()
            val familyMembers by familyViewModel.familyMembers.collectAsState()
            
            FamilyListScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMember = { navController.navigate(Routes.ADD_MEMBER) },
                onMemberClick = { /* TODO: 查看成员详情 */ },
                onDeleteMember = { memberId -> familyViewModel.deleteMember(memberId) }
            )
        }
        
        composable(Routes.ADD_MEMBER) {
            val uiState by familyViewModel.uiState.collectAsState()
            
            AddMemberScreen(
                uiState = uiState,
                onNavigateBack = { 
                    familyViewModel.clearSuccessMessage()
                    navController.popBackStack() 
                },
                onSaveMember = { name, gender, birthDate, relation, role ->
                    familyViewModel.addMember(name, gender, birthDate, relation, role)
                }
            )
        }
        
        // ==================== 病历导入模块 ====================
        composable(Routes.RECORD_LIST) {
            val uiState by recordViewModel.uiState.collectAsState()
            val records by recordViewModel.allRecords.collectAsState()
            val familyMembers by familyViewModel.familyMembers.collectAsState()
            val selectedFiles by recordViewModel.selectedFiles.collectAsState()
            val importTaskStatus by recordViewModel.importTaskStatus.collectAsState()
            val importProgress by recordViewModel.importProgress.collectAsState()
            
            RecordListScreen(
                uiState = uiState,
                records = records,
                familyMembers = familyMembers,
                selectedFiles = selectedFiles,
                importTaskStatus = importTaskStatus,
                importProgress = importProgress,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRecord = { navController.navigate(Routes.ADD_RECORD) },
                onRecordClick = { /* TODO: 查看病历详情 */ },
                onDeleteRecord = { recordId -> recordViewModel.deleteRecord(recordId) },
                onAddSelectedFile = { file -> recordViewModel.addSelectedFile(file) },
                onRemoveSelectedFile = { file -> recordViewModel.removeSelectedFile(file) },
                onStartImport = { memberId -> recordViewModel.startImportTask(memberId) },
                onResetImport = { recordViewModel.resetImportStatus() },
                onClearError = { recordViewModel.clearError() }
            )
        }
        
        composable(Routes.ADD_RECORD) {
            val uiState by recordViewModel.uiState.collectAsState()
            val familyMembers by familyViewModel.familyMembers.collectAsState()
            
            AddRecordScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                onNavigateBack = { 
                    recordViewModel.clearSuccessMessage()
                    navController.popBackStack() 
                },
                onSaveRecord = { memberId, recordType, hospitalName, department, doctorName, visitDate, mainDiagnosis ->
                    recordViewModel.addRecord(memberId, recordType, hospitalName, department, doctorName, visitDate, mainDiagnosis)
                }
            )
        }
        
        // ==================== 生活管理模块（含用药助手） ====================
        composable(Routes.LIFESTYLE) {
            val lifestyleUiState by lifestyleViewModel.uiState.collectAsState()
            val medicationUiState by medicationViewModel.uiState.collectAsState()
            val medications by medicationViewModel.allMedications.collectAsState()
            val todaySchedules by medicationViewModel.todaySchedules.collectAsState()
            val dailyGoals by lifestyleViewModel.todayGoals.collectAsState()
            val followUps by lifestyleViewModel.pendingFollowUps.collectAsState()
            
            LifestyleScreen(
                lifestyleUiState = lifestyleUiState,
                medicationUiState = medicationUiState,
                medications = medications,
                todaySchedules = todaySchedules,
                dailyGoals = dailyGoals,
                followUps = followUps,
                onNavigateToAddMedication = { navController.navigate(Routes.ADD_MEDICATION) },
                onMarkMedicationTaken = { scheduleId -> medicationViewModel.markAsTaken(scheduleId) },
                onUpdateGoalProgress = { goalId, delta -> lifestyleViewModel.incrementGoalProgress(goalId, delta) },
                onCompleteFollowUp = { followUpId -> lifestyleViewModel.completeFollowUp(followUpId) }
            )
        }
        
        composable(Routes.ADD_MEDICATION) {
            val uiState by medicationViewModel.uiState.collectAsState()
            val familyMembers by familyViewModel.familyMembers.collectAsState()
            
            AddMedicationScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                onNavigateBack = {
                    medicationViewModel.clearMessages()
                    navController.popBackStack()
                },
                onSaveMedication = { memberId, name, dosage, frequency, times, startDate, instructions ->
                    medicationViewModel.addMedication(memberId, name, dosage, frequency, times, startDate, instructions)
                }
            )
        }
        
        // ==================== 报告解读模块 ====================
        composable(Routes.REPORT_LIST) {
            val uiState by reportViewModel.uiState.collectAsState()
            val reports by reportViewModel.allReports.collectAsState()
            
            ReportListScreen(
                uiState = uiState,
                reports = reports,
                onNavigateToAddReport = { navController.navigate(Routes.ADD_REPORT) },
                onReportClick = { reportId -> navController.navigate(Routes.reportDetail(reportId)) },
                onDeleteReport = { reportId -> reportViewModel.deleteReport(reportId) }
            )
        }
        
        composable(Routes.ADD_REPORT) {
            val uiState by reportViewModel.uiState.collectAsState()
            val familyMembers by familyViewModel.familyMembers.collectAsState()
            
            AddReportScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                onNavigateBack = {
                    reportViewModel.clearMessages()
                    navController.popBackStack()
                },
                onSaveReport = { memberId, reportType, hospitalName, reportDate, results ->
                    reportViewModel.addReport(memberId, reportType, hospitalName, reportDate, results)
                }
            )
        }
        
        composable(Routes.REPORT_DETAIL) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")?.toLongOrNull() ?: return@composable
            val uiState by reportViewModel.uiState.collectAsState()

            ReportDetailScreen(
                reportId = reportId,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onLoadReport = { reportViewModel.loadReportDetail(reportId) }
            )
        }

        // ==================== 个人中心模块 ====================
        composable(Routes.PROFILE) {
            val uiState by profileViewModel.uiState.collectAsState()

            ProfileScreen(
                uiState = uiState,
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLogout = { profileViewModel.logout() },
                onRefresh = { profileViewModel.refreshUserInfo() },
                onClearMessages = { profileViewModel.clearMessages() }
            )
        }

        composable(Routes.LOGIN) {
            val uiState by profileViewModel.uiState.collectAsState()

            LoginScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { username, password ->
                    profileViewModel.login(username, password)
                },
                onClearMessages = { profileViewModel.clearMessages() }
            )
        }

        composable(Routes.REGISTER) {
            val uiState by profileViewModel.uiState.collectAsState()

            RegisterScreen(
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.popBackStack()
                    navController.navigate(Routes.LOGIN)
                },
                onRegister = { username, password, confirmPassword, phone, nickname ->
                    profileViewModel.register(username, password, confirmPassword, phone, nickname)
                },
                onClearMessages = { profileViewModel.clearMessages() }
            )
        }
    }
}
