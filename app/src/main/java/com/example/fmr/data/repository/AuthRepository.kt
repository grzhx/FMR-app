package com.example.fmr.data.repository

import com.example.fmr.data.auth.TokenManager
import com.example.fmr.data.network.RemoteDataSource
import com.example.fmr.data.network.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 认证仓库
 * 处理用户注册、登录、登出等认证相关操作
 */
class AuthRepository(
    private val remoteDataSource: RemoteDataSource,
    private val tokenManager: TokenManager
) {

    /**
     * 用户注册
     */
    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        phone: String? = null,
        nickname: String? = null
    ): Flow<NetworkResult<Long>> = flow {
        emit(NetworkResult.Loading)

        val request = RegisterRequest(
            username = username,
            password = password,
            confirmPassword = confirmPassword,
            phone = phone,
            nickname = nickname
        )

        val result = remoteDataSource.register(request)
        emit(result)
    }

    /**
     * 用户登录
     */
    fun login(
        username: String,
        password: String
    ): Flow<NetworkResult<LoginResponse>> = flow {
        emit(NetworkResult.Loading)

        val request = LoginRequest(
            username = username,
            password = password
        )

        val result = remoteDataSource.login(request)

        // 登录成功后保存Token和用户信息
        if (result is NetworkResult.Success) {
            val loginResponse = result.data
            tokenManager.saveToken(loginResponse.token, loginResponse.expiresIn)
            tokenManager.saveUserInfo(
                userId = loginResponse.userId,
                username = loginResponse.username,
                nickname = loginResponse.nickname,
                avatarUrl = loginResponse.avatarUrl
            )
        }

        emit(result)
    }

    /**
     * 退出登录
     */
    fun logout(): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading)

        // 调用后端退出接口
        val result = remoteDataSource.logout()

        // 无论后端是否成功，都清除本地Token
        tokenManager.clearAll()

        emit(result)
    }

    /**
     * 获取当前用户信息
     */
    fun getUserInfo(): Flow<NetworkResult<UserInfo>> = flow {
        emit(NetworkResult.Loading)

        val result = remoteDataSource.getUserInfo()
        emit(result)
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * 获取本地缓存的用户信息
     */
    fun getCachedUserInfo(): CachedUserInfo? {
        val userId = tokenManager.getUserId() ?: return null
        val username = tokenManager.getUsername() ?: return null
        val nickname = tokenManager.getNickname() ?: return null
        val avatarUrl = tokenManager.getAvatarUrl()

        return CachedUserInfo(
            userId = userId,
            username = username,
            nickname = nickname,
            avatarUrl = avatarUrl
        )
    }

    /**
     * 本地缓存的用户信息
     */
    data class CachedUserInfo(
        val userId: Long,
        val username: String,
        val nickname: String,
        val avatarUrl: String?
    )
}
