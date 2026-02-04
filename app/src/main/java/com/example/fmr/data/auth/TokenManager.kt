package com.example.fmr.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Token管理器
 * 负责JWT Token的存储、获取和清除
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * 保存Token
     * @param token JWT Token
     * @param expiresIn 有效期（秒）
     */
    fun saveToken(token: String, expiresIn: Long) {
        val expiresAt = System.currentTimeMillis() + expiresIn * 1000
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    /**
     * 获取Token
     * @return Token字符串，如果不存在或已过期则返回null
     */
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)

        // 检查Token是否过期
        if (token != null && System.currentTimeMillis() < expiresAt) {
            return token
        }

        // Token过期，清除
        if (token != null) {
            clearToken()
        }

        return null
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * 清除Token
     */
    fun clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    /**
     * 保存用户信息
     */
    fun saveUserInfo(userId: Long, username: String, nickname: String, avatarUrl: String?) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .apply()
    }

    /**
     * 获取用户ID
     */
    fun getUserId(): Long? {
        val userId = prefs.getLong(KEY_USER_ID, -1)
        return if (userId != -1L) userId else null
    }

    /**
     * 获取用户名
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * 获取昵称
     */
    fun getNickname(): String? {
        return prefs.getString(KEY_NICKNAME, null)
    }

    /**
     * 获取头像URL
     */
    fun getAvatarUrl(): String? {
        return prefs.getString(KEY_AVATAR_URL, null)
    }

    /**
     * 清除所有数据
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR_URL = "avatar_url"
    }
}
