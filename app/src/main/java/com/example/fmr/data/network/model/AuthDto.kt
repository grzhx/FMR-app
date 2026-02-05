package com.example.fmr.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * 用户注册请求DTO
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirmPassword")
    val confirmPassword: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("nickname")
    val nickname: String? = null
)

/**
 * 用户登录请求DTO
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

/**
 * 登录响应DTO
 */
data class LoginResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("token")
    val token: String,

    @SerializedName("expiresIn")
    val expiresIn: Long
)

/**
 * 更新用户信息请求DTO
 */
data class UpdateUserInfoRequest(
    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

/**
 * 用户信息DTO
 */
data class UserInfo(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("status")
    val status: Int,

    @SerializedName("lastLoginTime")
    val lastLoginTime: String?,

    @SerializedName("createTime")
    val createTime: String?
)
