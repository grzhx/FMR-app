# 用户认证模块 API 文档

## 概述

本文档描述了家庭医疗档案管理系统（FMR-Backend）的用户认证相关接口，供客户端开发参考。

**基础URL**: `http://localhost:8080/api`

**认证方式**: JWT Token

## 认证流程

```
1. 用户注册 → POST /api/auth/register → 返回用户ID
2. 用户登录 → POST /api/auth/login → 返回 Token
3. 携带Token访问 → Header: Authorization: Bearer {token} → 业务接口
4. 退出登录 → POST /api/auth/logout → 清除Token
```

## Token 使用说明

登录成功后，服务端返回 `token` 字段，客户端需要：

1. **存储Token**: 将 token 安全存储在本地（如 SharedPreferences、Keychain）
2. **携带Token**: 后续所有需要认证的请求，在 HTTP Header 中添加：
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. **Token过期**: 当收到 `2004` 错误码时，需要重新登录获取新 Token
4. **主动退出**: 调用退出接口，服务端会使 Token 失效

---

## 接口列表

### 1. 用户注册

注册新用户账号。

**请求**

```
POST /api/auth/register
Content-Type: application/json
```

**请求体**

```json
{
  "username": "zhangsan",
  "password": "123456",
  "confirmPassword": "123456",
  "phone": "13800138000",
  "nickname": "张三"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（4-20字符，只能包含字母、数字、下划线） |
| password | String | 是 | 密码（6-20字符） |
| confirmPassword | String | 否 | 确认密码（如提供，需与password一致） |
| phone | String | 否 | 手机号（11位中国大陆手机号） |
| nickname | String | 否 | 昵称（不填则默认为用户名） |

**成功响应**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": 1,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 400 | 参数校验失败（用户名格式错误、密码长度不符等） |
| 1003 | 用户名已存在 / 手机号已被注册 |

---

### 2. 用户登录

用户登录获取访问令牌。

**请求**

```
POST /api/auth/login
Content-Type: application/json
```

**请求体**

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "avatarUrl": "https://example.com/avatar.jpg",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ6aGFuZ3NhbiIsImlhdCI6MTcwNjUwMDAwMCwiZXhwIjoxNzA2NTg2NDAwfQ.xxxxx",
    "expiresIn": 86400
  },
  "timestamp": 1706500000000
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| avatarUrl | String | 头像URL（可能为null） |
| token | String | JWT访问令牌 |
| expiresIn | Long | 令牌有效期（秒），默认86400（24小时） |

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 2001 | 用户不存在 |
| 2002 | 密码错误 |
| 2003 | 用户已禁用 |

---

### 3. 退出登录

退出当前登录状态，使Token失效。

**请求**

```
POST /api/auth/logout
Authorization: Bearer {token}
```

**成功响应**

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 401 | 未授权（未携带Token或Token无效） |

---

### 4. 更新用户信息

更新当前登录用户的个人信息。

**请求**

```
PUT /api/auth/info
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**

```json
{
  "nickname": "新昵称",
  "phone": "13800138001",
  "email": "user@example.com",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称（最多20字符） |
| phone | String | 否 | 手机号（11位中国大陆手机号） |
| email | String | 否 | 邮箱 |
| avatarUrl | String | 否 | 头像URL |

**成功响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 400 | 参数校验失败（手机号格式错误、邮箱格式错误等） |
| 401 | 未授权 |
| 1003 | 手机号已被使用 |
| 2001 | 用户不存在 |

---

### 5. 获取当前用户信息

获取当前登录用户的详细信息。

**请求**

```
GET /api/auth/info
Authorization: Bearer {token}
```

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "phone": "138****8000",
    "email": "zhangsan@example.com",
    "nickname": "张三",
    "avatarUrl": "https://example.com/avatar.jpg",
    "status": 1,
    "lastLoginTime": "2026-01-29 10:30:00",
    "createTime": "2026-01-01 08:00:00"
  },
  "timestamp": 1706500000000
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| phone | String | 手机号（脱敏显示） |
| email | String | 邮箱 |
| nickname | String | 昵称 |
| avatarUrl | String | 头像URL |
| status | Integer | 状态：1-正常，2-禁用 |
| lastLoginTime | String | 最后登录时间 |
| createTime | String | 注册时间 |

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 401 | 未授权 |
| 2001 | 用户不存在 |

---

## 错误码汇总

### 认证相关错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 401 | 未授权访问 | 跳转登录页 |
| 2001 | 用户不存在 | 提示用户检查用户名 |
| 2002 | 密码错误 | 提示密码错误 |
| 2003 | 用户已禁用 | 提示账号被禁用，联系客服 |
| 2004 | 登录已过期 | 清除本地Token，跳转登录页 |
| 2005 | 无效的Token | 清除本地Token，跳转登录页 |

### 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |
| 1001 | 参数校验失败 |
| 1003 | 数据已存在 |

---

## 客户端实现建议

### 1. Token 存储

```kotlin
// Android (Kotlin) 示例
class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    
    fun saveToken(token: String, expiresIn: Long) {
        prefs.edit()
            .putString("token", token)
            .putLong("expires_at", System.currentTimeMillis() + expiresIn * 1000)
            .apply()
    }
    
    fun getToken(): String? = prefs.getString("token", null)
    
    fun clearToken() = prefs.edit().clear().apply()
}
```

### 2. 请求拦截器

```kotlin
// OkHttp 拦截器示例
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenManager.getToken()
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        val response = chain.proceed(newRequest)
        
        // 处理Token过期
        if (response.code == 200) {
            val body = response.peekBody(Long.MAX_VALUE).string()
            val result = Gson().fromJson(body, Result::class.java)
            if (result.code == 2004 || result.code == 2005) {
                tokenManager.clearToken()
                // 通知UI跳转登录页
            }
        }
        
        return response
    }
}
```

### 3. 错误处理

```kotlin
// 统一错误处理
fun handleApiError(code: Int, message: String) {
    when (code) {
        401, 2004, 2005 -> {
            // Token失效，跳转登录
            navigateToLogin()
        }
        2001 -> showToast("用户不存在")
        2002 -> showToast("密码错误")
        2003 -> showToast("账号已被禁用")
        else -> showToast(message)
    }
}
```

---

## 在线文档

启动应用后，可访问 Swagger UI 查看交互式 API 文档：

```
http://localhost:8080/swagger-ui.html
```

或使用 Knife4j 增强文档：

```
http://localhost:8080/doc.html
