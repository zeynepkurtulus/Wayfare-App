package com.zeynekurtulus.wayfare.data.api

import com.zeynekurtulus.wayfare.data.api.interceptors.AuthInterceptor
import com.zeynekurtulus.wayfare.data.api.services.*
import com.zeynekurtulus.wayfare.utils.Constants
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkConfig {

    fun createRetrofit(sharedPreferencesManager: SharedPreferencesManager): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(sharedPreferencesManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.API_TIMEOUT, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    fun createRouteApiService(retrofit: Retrofit): RouteApiService {
        return retrofit.create(RouteApiService::class.java)
    }

    fun createPlaceApiService(retrofit: Retrofit): PlaceApiService {
        return retrofit.create(PlaceApiService::class.java)
    }

    fun createLocationApiService(retrofit: Retrofit): LocationApiService {
        return retrofit.create(LocationApiService::class.java)
    }

    fun createFeedbackApiService(retrofit: Retrofit): FeedbackApiService {
        return retrofit.create(FeedbackApiService::class.java)
    }
    
    fun createCityApiService(retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }
}