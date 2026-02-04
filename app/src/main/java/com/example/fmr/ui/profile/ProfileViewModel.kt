package com.example.fmr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmr.data.network.model.NetworkResult
import com.example.fmr.data.network.model.UserInfo
import com.example.fmr.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 个人中心ViewModel
 */
class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // 初始化时检查登录状态
        checkLoginStatus()
    }

    /**
     * 检查登录状态
     */
    fun checkLoginStatus() {
        val isLoggedIn = authRepository.isLoggedIn()
        _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)

        if (isLoggedIn) {
            // 加载缓存的用户信息
            val cachedUserInfo = authRepository.getCachedUserInfo()
            if (cachedUserInfo != null) {
                _uiState.value = _uiState.value.copy(
                    userId = cachedUserInfo.userId,
                    username = cachedUserInfo.username,
                    nickname = cachedUserInfo.nickname,
                    avatarUrl = cachedUserInfo.avatarUrl
                )
            }
            // 从服务器刷新用户信息
            refreshUserInfo()
        }
    }

    /**
     * 刷新用户信息
     */
    fun refreshUserInfo() {
        viewModelScope.launch {
            authRepository.getUserInfo().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is NetworkResult.Success -> {
                        val userInfo = result.data
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userId = userInfo.userId,
                            username = userInfo.username,
                            nickname = userInfo.nickname,
                            phone = userInfo.phone,
                            email = userInfo.email,
                            avatarUrl = userInfo.avatarUrl,
                            status = userInfo.status,
                            lastLoginTime = userInfo.lastLoginTime,
                            createTime = userInfo.createTime
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Exception -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.throwable.message ?: "未知错误"
                        )
                    }
                }
            }
        }
    }

    /**
     * 用户注册
     */
    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        phone: String?,
        nickname: String?
    ) {
        viewModelScope.launch {
            authRepository.register(username, password, confirmPassword, phone, nickname)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        }
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "注册成功，请登录"
                            )
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                        is NetworkResult.Exception -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.throwable.message ?: "注册失败"
                            )
                        }
                    }
                }
        }
    }

    /**
     * 用户登录
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            authRepository.login(username, password).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is NetworkResult.Success -> {
                        val loginResponse = result.data
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userId = loginResponse.userId,
                            username = loginResponse.username,
                            nickname = loginResponse.nickname,
                            avatarUrl = loginResponse.avatarUrl,
                            successMessage = "登录成功"
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Exception -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.throwable.message ?: "登录失败"
                        )
                    }
                }
            }
        }
    }

    /**
     * 退出登录
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is NetworkResult.Success, is NetworkResult.Error, is NetworkResult.Exception -> {
                        // 无论后端是否成功，都清除本地状态
                        _uiState.value = ProfileUiState(
                            isLoggedIn = false,
                            successMessage = "已退出登录"
                        )
                    }
                }
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

/**
 * 个人中心UI状态
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: Long? = null,
    val username: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val status: Int? = null,
    val lastLoginTime: String? = null,
    val createTime: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * ProfileViewModel工厂
 */
class ProfileViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
