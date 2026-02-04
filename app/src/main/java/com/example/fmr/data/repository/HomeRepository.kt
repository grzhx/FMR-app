package com.example.fmr.data.repository

import com.example.fmr.data.network.NetworkManager
import com.example.fmr.data.network.RemoteDataSource
import com.example.fmr.data.network.model.HomeDashboardDto
import com.example.fmr.data.network.model.NetworkResult
import com.example.fmr.data.network.model.ProgressItemDto
import com.example.fmr.data.network.model.TodayProgressDto
import java.util.Calendar

/**
 * 首页数据仓库
 * 负责首页仪表盘数据的获取
 * 
 * 策略：
 * - 联网时从服务器获取数据
 * - 离线时使用本地生成的默认数据
 */
class HomeRepository(
    private val remoteDataSource: RemoteDataSource? = null,
    private val networkManager: NetworkManager? = null
) {
    
    /**
     * 获取首页仪表盘数据
     * @param memberId 成员ID
     * @return 仪表盘数据结果
     */
    suspend fun getDashboard(memberId: Long): DashboardResult {
        // 尝试从服务器获取
        if (isOnline()) {
            return when (val result = remoteDataSource?.getDashboard(memberId)) {
                is NetworkResult.Success -> {
                    DashboardResult.Success(result.data, fromServer = true)
                }
                is NetworkResult.Error -> {
                    // 服务器返回错误，使用本地数据
                    DashboardResult.Success(generateLocalDashboard(), fromServer = false, error = result.message)
                }
                is NetworkResult.Exception -> {
                    // 网络异常，使用本地数据
                    DashboardResult.Success(generateLocalDashboard(), fromServer = false, error = result.throwable.message)
                }
                else -> {
                    DashboardResult.Success(generateLocalDashboard(), fromServer = false)
                }
            }
        }
        
        // 离线模式，使用本地生成的数据
        return DashboardResult.Success(generateLocalDashboard(), fromServer = false, error = "离线模式")
    }
    
    /**
     * 生成本地默认仪表盘数据
     */
    private fun generateLocalDashboard(): HomeDashboardDto {
        return HomeDashboardDto(
            greeting = getLocalGreeting(),
            shortcuts = null, // 快捷入口由UI层处理
            todayProgress = TodayProgressDto(
                medication = ProgressItemDto(target = 0, current = 0, unit = "次"),
                exercise = ProgressItemDto(target = 0, current = 0, unit = "步")
            ),
            recentUpdates = emptyList()
        )
    }
    
    /**
     * 获取本地问候语
     */
    private fun getLocalGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 6 -> "凌晨好"
            hour < 9 -> "早上好"
            hour < 12 -> "上午好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            hour < 22 -> "晚上好"
            else -> "夜深了"
        }
    }
    
    /**
     * 检查是否在线
     */
    private fun isOnline(): Boolean {
        return networkManager?.checkNetworkAvailability() == true && remoteDataSource != null
    }
    
    /**
     * 检查服务器连接状态
     */
    suspend fun checkServerConnection(): Boolean {
        return remoteDataSource?.checkServerHealth() == true
    }
}

/**
 * 仪表盘数据结果
 */
sealed class DashboardResult {
    /**
     * 成功获取数据
     * @param data 仪表盘数据
     * @param fromServer 是否来自服务器
     * @param error 错误信息（如果有）
     */
    data class Success(
        val data: HomeDashboardDto,
        val fromServer: Boolean,
        val error: String? = null
    ) : DashboardResult()
    
    /**
     * 获取失败
     */
    data class Error(val message: String) : DashboardResult()
}
