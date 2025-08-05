package com.zeynekurtulus.wayfare.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkUtils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> Constants.ERROR_NETWORK
            is SocketTimeoutException -> Constants.ERROR_NETWORK
            is HttpException -> {
                when (throwable.code()) {
                    401 -> Constants.ERROR_UNAUTHORIZED
                    400 -> Constants.ERROR_VALIDATION
                    in 500..599 -> Constants.ERROR_SERVER
                    else -> Constants.ERROR_UNKNOWN
                }
            }
            else -> Constants.ERROR_UNKNOWN
        }
    }
    
    fun handleApiError(throwable: Throwable): ApiResult.Error {
        val message = getErrorMessage(throwable)
        val code = if (throwable is HttpException) throwable.code() else -1
        return ApiResult.Error(message, code)
    }
}

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (String, Int) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(message, code)
    return this
}

inline fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) action()
    return this
}