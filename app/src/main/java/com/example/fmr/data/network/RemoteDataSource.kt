package com.example.fmr.data.network

import android.util.Log
import com.example.fmr.data.entity.FamilyMember
import com.example.fmr.data.network.api.FmrApiService
import com.example.fmr.data.network.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 远程数据源
 * 负责处理所有与后端服务器的API交互
 */
class RemoteDataSource(
    private val apiService: FmrApiService,
    private val networkManager: NetworkManager
) {
    
    companion object {
        private const val TAG = "FMR_RemoteDataSource"
    }
    
    // ==================== 首页模块 ====================
    
    /**
     * 获取首页仪表盘数据
     */
    suspend fun getDashboard(memberId: Long): NetworkResult<HomeDashboardDto> {
        return safeApiCall {
            apiService.getDashboard(memberId)
        }
    }
    
    // ==================== 家庭成员管理模块 ====================
    
    /**
     * 获取家庭成员列表
     */
    suspend fun getMembers(familyId: Long? = null): NetworkResult<List<FamilyMemberDto>> {
        return safeApiCall {
            apiService.getMembers(familyId)
        }
    }
    
    /**
     * 获取成员详情
     */
    suspend fun getMemberDetail(memberId: Long): NetworkResult<MemberProfileDto> {
        return safeApiCall {
            apiService.getMemberDetail(memberId)
        }
    }
    
    /**
     * 添加家庭成员
     */
    suspend fun addMember(member: FamilyMember): NetworkResult<Long> {
        return safeApiCall {
            apiService.addMember(member.toAddRequest())
        }
    }
    
    /**
     * 更新成员信息
     */
    suspend fun updateMember(member: FamilyMember): NetworkResult<Unit> {
        return safeApiCall {
            apiService.updateMember(member.id, member.toUpdateRequest())
        }
    }
    
    /**
     * 删除成员
     */
    suspend fun deleteMember(memberId: Long): NetworkResult<Unit> {
        return safeApiCall {
            apiService.deleteMember(memberId)
        }
    }
    
    /**
     * 获取成员详情（含健康档案）
     */
    suspend fun getMemberProfile(memberId: Long): NetworkResult<MemberProfileDto> {
        return safeApiCall {
            apiService.getMemberDetail(memberId)
        }
    }
    
    /**
     * 更新健康档案
     */
    suspend fun updateHealthProfile(memberId: Long, request: UpdateHealthProfileRequest): NetworkResult<Unit> {
        return safeApiCall {
            apiService.updateHealthProfile(memberId, request)
        }
    }

    // ==================== 用户认证模块 ====================

    /**
     * 用户注册
     */
    suspend fun register(request: RegisterRequest): NetworkResult<Long> {
        return safeApiCall {
            apiService.register(request)
        }
    }

    /**
     * 用户登录
     */
    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse> {
        return safeApiCall {
            apiService.login(request)
        }
    }

    /**
     * 退出登录
     */
    suspend fun logout(): NetworkResult<Unit> {
        return safeApiCall {
            apiService.logout()
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getUserInfo(): NetworkResult<UserInfo> {
        return safeApiCall {
            apiService.getUserInfo()
        }
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(request: UpdateUserInfoRequest): NetworkResult<Unit> {
        return safeApiCall {
            apiService.updateUserInfo(request)
        }
    }

    // ==================== 病历导入模块 ====================

    /**
     * 创建病历导入任务
     */
    suspend fun importRecords(request: ImportRecordRequest): Result<ImportTaskDto> {
        Log.d(TAG, "========== importRecords 开始 ==========")
        Log.d(TAG, "请求: memberId=${request.memberId}, files=${request.files.size}")
        request.files.forEachIndexed { i, f -> Log.d(TAG, "  文件[$i]: ${f.fileName}, ${f.fileSize}B") }
        
        val result = safeApiCall { apiService.importRecords(request) }
        Log.d(TAG, "importRecords safeApiCall 结果: $result")
        
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "✅ importRecords 成功: ${result.data}")
                Result.success(result.data)
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "❌ importRecords 错误: code=${result.code}, msg=${result.message}")
                Result.failure(Exception(result.message))
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "❌ importRecords 异常: ${result.throwable.message}", result.throwable)
                Result.failure(result.throwable)
            }
            is NetworkResult.Loading -> Result.failure(Exception("Loading state not expected"))
        }
    }

    /**
     * 查询导入任务状态
     */
    suspend fun getTaskStatus(taskId: String): Result<TaskStatusDto> {
        Log.d(TAG, "getTaskStatus 调用, taskId=$taskId")
        
        val result = safeApiCall { apiService.getTaskStatus(taskId) }
        Log.d(TAG, "getTaskStatus 结果: $result")
        
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "✅ 状态: ${result.data}")
                Result.success(result.data)
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "❌ 错误: ${result.message}")
                Result.failure(Exception(result.message))
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "❌ 异常: ${result.throwable.message}")
                Result.failure(result.throwable)
            }
            is NetworkResult.Loading -> Result.failure(Exception("Loading state not expected"))
        }
    }

    /**
     * 获取病历列表
     */
    suspend fun getRecordList(memberId: Long, page: Int = 1): Result<RecordListDto> {
        Log.d(TAG, "getRecordList 调用, memberId=$memberId, page=$page")
        
        val result = safeApiCall { apiService.getRecordList(memberId, page) }
        
        return when (result) {
            is NetworkResult.Success -> {
                Log.d(TAG, "✅ 获取到 ${result.data.list.size} 条记录")
                Result.success(result.data)
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "❌ 错误: ${result.message}")
                Result.failure(Exception(result.message))
            }
            is NetworkResult.Exception -> {
                Log.e(TAG, "❌ 异常: ${result.throwable.message}")
                Result.failure(result.throwable)
            }
            is NetworkResult.Loading -> Result.failure(Exception("Loading state not expected"))
        }
    }

    // ==================== 通用方法 ====================
    
    /**
     * 安全的API调用封装
     * 处理网络异常和响应解析
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        Log.d(TAG, "========== safeApiCall 开始 ==========")
        try {
            // 检查网络状态
            val networkAvailable = networkManager.checkNetworkAvailability()
            Log.d(TAG, "网络状态检查: $networkAvailable")
            
            if (!networkAvailable) {
                Log.w(TAG, "❌ 网络不可用")
                return@withContext NetworkResult.Error(
                    code = -1,
                    message = "网络不可用，请检查网络连接"
                )
            }
            
            Log.d(TAG, "正在发起API请求...")
            val response = apiCall()
            Log.d(TAG, "API请求完成, HTTP状态码: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ HTTP请求成功 (${response.code()})")
                val body = response.body()
                Log.d(TAG, "响应体是否为空: ${body == null}")
                
                if (body != null) {
                    Log.d(TAG, "响应体内容: code=${body.code}, message=${body.message}, data=${body.data}")
                    // 更新服务器可达状态
                    networkManager.updateServerReachable(true)
                    Log.d(TAG, "已更新服务器可达状态为: true")
                    
                    if (body.isSuccess()) {
                        Log.d(TAG, "✅ API业务逻辑成功")
                        if (body.data != null) {
                            Log.d(TAG, "返回数据: ${body.data}")
                            NetworkResult.Success(body.data)
                        } else {
                            Log.d(TAG, "返回数据为空 (Unit)")
                            @Suppress("UNCHECKED_CAST")
                            NetworkResult.Success(Unit as T)
                        }
                    } else {
                        Log.w(TAG, "❌ API业务逻辑失败: code=${body.code}, message=${body.message}")
                        NetworkResult.Error(body.code, body.message)
                    }
                } else {
                    Log.w(TAG, "❌ 响应体为空")
                    NetworkResult.Error(
                        code = response.code(),
                        message = "响应数据为空"
                    )
                }
            } else {
                Log.w(TAG, "❌ HTTP请求失败: ${response.code()}")
                // 尝试解析错误响应
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string() ?: "请求失败"
                    Log.w(TAG, "错误响应体: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    Log.e(TAG, "解析错误响应体失败", e)
                    "请求失败: ${response.code()}"
                }
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "❌ ConnectException: 无法连接到服务器", e)
            networkManager.updateServerReachable(false)
            NetworkResult.Error(-1, "无法连接到服务器")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "❌ SocketTimeoutException: 连接超时", e)
            networkManager.updateServerReachable(false)
            NetworkResult.Error(-1, "连接超时，请稍后重试")
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "❌ UnknownHostException: 无法解析服务器地址", e)
            networkManager.updateServerReachable(false)
            NetworkResult.Error(-1, "无法解析服务器地址")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 未知异常: ${e.javaClass.simpleName}", e)
            Log.e(TAG, "异常信息: ${e.message}")
            Log.e(TAG, "异常堆栈:", e)
            NetworkResult.Exception(e)
        }
    }
    
    /**
     * 检查服务器是否可达
     */
    suspend fun checkServerHealth(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "========== checkServerHealth 开始 ==========")
        try {
            val networkAvailable = networkManager.checkNetworkAvailability()
            Log.d(TAG, "网络状态: $networkAvailable")
            
            if (!networkAvailable) {
                Log.w(TAG, "❌ 网络不可用，跳过服务器健康检查")
                return@withContext false
            }
            
            Log.d(TAG, "正在检查服务器健康状态...")
            // 尝试获取成员列表来检查服务器状态
            val response = apiService.getMembers()
            val isReachable = response.isSuccessful
            Log.d(TAG, "服务器响应: HTTP ${response.code()}, isReachable=$isReachable")
            
            networkManager.updateServerReachable(isReachable)
            Log.d(TAG, "已更新服务器可达状态为: $isReachable")
            
            isReachable
        } catch (e: Exception) {
            Log.e(TAG, "❌ checkServerHealth 异常: ${e.javaClass.simpleName}", e)
            Log.e(TAG, "异常信息: ${e.message}")
            networkManager.updateServerReachable(false)
            false
        }
    }
}
