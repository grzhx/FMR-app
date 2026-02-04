package com.example.fmr.data.network

import android.util.Log
import com.example.fmr.data.network.api.FmrApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例
 * 负责创建和管理API服务实例
 */
object RetrofitClient {

    // 默认服务器地址（可配置）
    private const val DEFAULT_BASE_URL = "https://nonobstetrical-charita-unaccusingly.ngrok-free.dev/api/"

    // 超时配置
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private var baseUrl: String = DEFAULT_BASE_URL
    private var retrofit: Retrofit? = null
    private var apiService: FmrApiService? = null

    // TokenManager引用（用于获取Token）
    private var tokenProvider: (() -> String?)? = null

    /**
     * 设置Token提供者
     * @param provider 返回Token的函数
     */
    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
        // 重置客户端以应用新的拦截器
        reset()
    }
    
    /**
     * 配置服务器地址
     * @param url 服务器基础URL
     */
    fun setBaseUrl(url: String) {
        if (url != baseUrl) {
            baseUrl = url
            retrofit = null
            apiService = null
        }
    }
    
    /**
     * 获取当前服务器地址
     */
    fun getBaseUrl(): String = baseUrl
    
    private const val TAG = "FMR_Network"
    
    /**
     * 创建OkHttpClient
     */
    private fun createOkHttpClient(): OkHttpClient {
        // 自定义日志拦截器，输出更详细的信息
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            // 请求拦截器 - 添加请求头并记录请求信息
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("ngrok-skip-browser-warning", "true")  // 跳过ngrok免费版的浏览器警告页面
                    .addHeader("User-Agent", "FMR-Android-App/1.0")  // 添加自定义User-Agent

                // 添加Authorization header（如果有Token）
                tokenProvider?.invoke()?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "添加Authorization header: Bearer ${token.take(20)}...")
                }

                val request = requestBuilder.build()
                
                Log.d(TAG, "========== 发起请求 ==========")
                Log.d(TAG, "URL: ${request.url}")
                Log.d(TAG, "Method: ${request.method}")
                Log.d(TAG, "Headers: ${request.headers}")
                
                try {
                    val response = chain.proceed(request)
                    
                    Log.d(TAG, "========== 收到响应 ==========")
                    Log.d(TAG, "Status Code: ${response.code}")
                    Log.d(TAG, "Response Headers: ${response.headers}")
                    
                    // 检查响应内容类型
                    val contentType = response.header("Content-Type")
                    Log.d(TAG, "Content-Type: $contentType")
                    
                    // 如果不是JSON响应，记录警告
                    if (contentType != null && !contentType.contains("application/json")) {
                        Log.w(TAG, "⚠️ 警告: 响应不是JSON格式! Content-Type: $contentType")
                        Log.w(TAG, "⚠️ 这可能是ngrok警告页面或其他HTML响应")
                    }
                    
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "========== 请求异常 ==========")
                    Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
                    Log.e(TAG, "异常信息: ${e.message}")
                    Log.e(TAG, "堆栈跟踪:", e)
                    throw e
                }
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * 获取Retrofit实例
     */
    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }
    
    /**
     * 获取API服务实例
     */
    fun getApiService(): FmrApiService {
        return apiService ?: synchronized(this) {
            apiService ?: getRetrofit().create(FmrApiService::class.java)
                .also { apiService = it }
        }
    }
    
    /**
     * 重置客户端（用于切换服务器等场景）
     */
    fun reset() {
        retrofit = null
        apiService = null
    }
}

/**
 * 服务器配置
 */
object ServerConfig {
    // 模拟器访问本机服务器地址
    const val EMULATOR_LOCALHOST = "http://10.0.2.2:8080/api/"
    
    // 真机访问本机服务器地址（需要替换为实际IP）
    const val DEVICE_LOCALHOST = "http://192.168.1.100:8080/api/"
    
    // 生产环境地址
    const val PRODUCTION = "https://api.example.com/api/"
}
