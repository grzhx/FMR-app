package com.example.fmr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fmr.data.auth.TokenManager
import com.example.fmr.data.database.AppDatabase
import com.example.fmr.data.network.ConnectionState
import com.example.fmr.data.network.NetworkManager
import com.example.fmr.data.network.RemoteDataSource
import com.example.fmr.data.network.RetrofitClient
import com.example.fmr.data.repository.*
import com.example.fmr.ui.components.NetworkStatusIndicator
import com.example.fmr.ui.family.FamilyViewModel
import com.example.fmr.ui.family.FamilyViewModelFactory
import com.example.fmr.ui.home.HomeViewModel
import com.example.fmr.ui.home.HomeViewModelFactory
import com.example.fmr.ui.lifestyle.LifestyleViewModel
import com.example.fmr.ui.lifestyle.LifestyleViewModelFactory
import com.example.fmr.ui.medication.MedicationViewModel
import com.example.fmr.ui.medication.MedicationViewModelFactory
import com.example.fmr.ui.navigation.BottomNavBar
import com.example.fmr.ui.navigation.NavGraph
import com.example.fmr.ui.navigation.mainRoutes
import com.example.fmr.ui.profile.ProfileViewModel
import com.example.fmr.ui.profile.ProfileViewModelFactory
import com.example.fmr.ui.record.MedicalRecordViewModel
import com.example.fmr.ui.record.MedicalRecordViewModelFactory
import com.example.fmr.ui.report.ReportViewModel
import com.example.fmr.ui.report.ReportViewModelFactory
import com.example.fmr.ui.theme.FMRTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    
    // 网络管理器（需要在Activity级别保持引用）
    private lateinit var networkManager: NetworkManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化数据库
        val database = AppDatabase.getDatabase(applicationContext)

        // 初始化TokenManager
        val tokenManager = TokenManager(applicationContext)

        // 配置RetrofitClient的Token提供者
        RetrofitClient.setTokenProvider { tokenManager.getToken() }

        // 初始化网络组件
        networkManager = NetworkManager(applicationContext)
        val apiService = RetrofitClient.getApiService()
        val remoteDataSource = RemoteDataSource(apiService, networkManager)

        // 初始化Repository（支持本地+远程数据源）
        val familyRepository = FamilyRepository(
            database.familyMemberDao(),
            remoteDataSource,
            networkManager
        )
        val medicalRecordRepository = MedicalRecordRepository(
            database.medicalRecordDao(),
            database.documentDao(),
            remoteDataSource
        )
        val homeRepository = HomeRepository(remoteDataSource, networkManager)
        val authRepository = AuthRepository(remoteDataSource, tokenManager)

        // 以下Repository暂不支持远程数据源（后端未提供相关API）
        val medicationRepository = MedicationRepository(database.medicationDao())
        val labReportRepository = LabReportRepository(database.labReportDao())
        val lifestyleRepository = LifestyleRepository(database.lifestyleDao())
        
        setContent {
            FMRTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // 判断是否显示底部导航栏
                val showBottomBar = currentRoute in mainRoutes
                
                // 监听网络连接状态
                val connectionState by networkManager.connectionState.collectAsState()
                
                // 初始化ViewModels（支持网络状态）
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        familyRepository,
                        medicalRecordRepository,
                        homeRepository,
                        networkManager
                    )
                )
                
                val familyViewModel: FamilyViewModel = viewModel(
                    factory = FamilyViewModelFactory(familyRepository, networkManager)
                )
                
                val recordViewModel: MedicalRecordViewModel = viewModel(
                    factory = MedicalRecordViewModelFactory(medicalRecordRepository)
                )
                
                val medicationViewModel: MedicationViewModel = viewModel(
                    factory = MedicationViewModelFactory(medicationRepository)
                )
                
                val lifestyleViewModel: LifestyleViewModel = viewModel(
                    factory = LifestyleViewModelFactory(lifestyleRepository)
                )
                
                val reportViewModel: ReportViewModel = viewModel(
                    factory = ReportViewModelFactory(labReportRepository)
                )

                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(authRepository)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        // 避免重复添加到回退栈
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 网络状态提示栏（未连接服务器时显示）
                        NetworkStatusIndicator(
                            connectionState = connectionState
                        )
                        
                        // 主内容区域
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            NavGraph(
                                navController = navController,
                                homeViewModel = homeViewModel,
                                familyViewModel = familyViewModel,
                                recordViewModel = recordViewModel,
                                medicationViewModel = medicationViewModel,
                                lifestyleViewModel = lifestyleViewModel,
                                reportViewModel = reportViewModel,
                                profileViewModel = profileViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
