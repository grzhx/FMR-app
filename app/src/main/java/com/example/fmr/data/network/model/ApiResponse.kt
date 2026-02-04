package com.example.fmr.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * 统一API响应封装类
 * 对应后端的Result类
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("timestamp")
    val timestamp: Long?
) {
    /**
     * 判断请求是否成功
     */
    fun isSuccess(): Boolean = code == 200
    
    companion object {
        const val CODE_SUCCESS = 200
        const val CODE_BAD_REQUEST = 400
        const val CODE_UNAUTHORIZED = 401
        const val CODE_FORBIDDEN = 403
        const val CODE_NOT_FOUND = 404
        const val CODE_SERVER_ERROR = 500
        const val CODE_MEMBER_NOT_FOUND = 1001
        const val CODE_MEMBER_LIMIT_EXCEEDED = 1002
    }
}

/**
 * 网络请求结果封装
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
