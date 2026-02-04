package com.example.fmr.data.network.api

import com.example.fmr.data.network.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * FMR后端API服务接口
 */
interface FmrApiService {
    
    // ==================== 首页模块 ====================
    
    /**
     * 获取首页仪表盘数据
     * @param memberId 成员ID
     */
    @GET("home/dashboard")
    suspend fun getDashboard(
        @Query("memberId") memberId: Long
    ): Response<ApiResponse<HomeDashboardDto>>
    
    // ==================== 家庭成员管理模块 ====================
    
    /**
     * 添加家庭成员
     */
    @POST("family/members")
    suspend fun addMember(
        @Body request: AddMemberRequest
    ): Response<ApiResponse<Long>>
    
    /**
     * 获取家庭成员列表
     * @param familyId 家庭ID（可选）
     */
    @GET("family/members")
    suspend fun getMembers(
        @Query("familyId") familyId: Long? = null
    ): Response<ApiResponse<List<FamilyMemberDto>>>
    
    /**
     * 获取成员详情
     * @param id 成员ID
     */
    @GET("family/members/{id}")
    suspend fun getMemberDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<MemberProfileDto>>
    
    /**
     * 更新成员信息
     * @param id 成员ID
     * @param request 更新请求
     */
    @PUT("family/members/{id}")
    suspend fun updateMember(
        @Path("id") id: Long,
        @Body request: AddMemberRequest
    ): Response<ApiResponse<Unit>>
    
    /**
     * 删除成员
     * @param id 成员ID
     */
    @DELETE("family/members/{id}")
    suspend fun deleteMember(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    // ==================== 用户认证模块 ====================

    /**
     * 用户注册
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<Long>>

    /**
     * 用户登录
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * 退出登录
     */
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    /**
     * 获取当前用户信息
     */
    @GET("auth/info")
    suspend fun getUserInfo(): Response<ApiResponse<UserInfo>>

    // ==================== 病历导入模块 ====================

    /**
     * 创建病历导入任务
     */
    @POST("records/import")
    suspend fun importRecords(
        @Body request: ImportRecordRequest
    ): Response<ApiResponse<ImportTaskDto>>

    /**
     * 查询导入任务状态
     */
    @GET("records/tasks/{taskId}/status")
    suspend fun getTaskStatus(
        @Path("taskId") taskId: String
    ): Response<ApiResponse<TaskStatusDto>>

    /**
     * 获取病历列表
     */
    @GET("records/list")
    suspend fun getRecordList(
        @Query("memberId") memberId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<ApiResponse<RecordListDto>>
}
