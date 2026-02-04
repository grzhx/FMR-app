package com.example.fmr.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 网络状态管理器
 * 负责监控网络连接状态和服务器可用性
 */
class NetworkManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isNetworkAvailable = MutableStateFlow(checkNetworkAvailability())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val _isServerReachable = MutableStateFlow(false)
    val isServerReachable: StateFlow<Boolean> = _isServerReachable.asStateFlow()
    
    private val _connectionState = MutableStateFlow(ConnectionState.UNKNOWN)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    init {
        registerNetworkCallback()
    }
    
    /**
     * 检查当前网络是否可用
     */
    fun checkNetworkAvailability(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * 注册网络状态回调
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isNetworkAvailable.value = true
                updateConnectionState()
            }
            
            override fun onLost(network: Network) {
                _isNetworkAvailable.value = false
                _isServerReachable.value = false
                _connectionState.value = ConnectionState.OFFLINE
            }
            
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isNetworkAvailable.value = hasInternet && isValidated
                updateConnectionState()
            }
        })
    }
    
    /**
     * 更新连接状态
     */
    private fun updateConnectionState() {
        _connectionState.value = when {
            !_isNetworkAvailable.value -> ConnectionState.OFFLINE
            _isServerReachable.value -> ConnectionState.CONNECTED
            else -> ConnectionState.NETWORK_ONLY
        }
    }
    
    /**
     * 更新服务器可达状态
     */
    fun updateServerReachable(reachable: Boolean) {
        _isServerReachable.value = reachable
        updateConnectionState()
    }
    
    /**
     * 获取网络状态Flow
     */
    fun observeNetworkState(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // 发送初始状态
        trySend(checkNetworkAvailability())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

/**
 * 连接状态枚举
 */
enum class ConnectionState {
    /** 未知状态 */
    UNKNOWN,
    /** 离线 - 无网络连接 */
    OFFLINE,
    /** 仅网络 - 有网络但服务器不可达 */
    NETWORK_ONLY,
    /** 已连接 - 网络和服务器都可用 */
    CONNECTED
}
