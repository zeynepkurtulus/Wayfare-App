package com.zeynekurtulus.wayfare.data.api.interceptors

import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding authorization header if it's already present (for login/register endpoints)
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }
        
        val accessToken = sharedPreferencesManager.getAccessToken()
        
        return if (accessToken != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}


 
 
 
 