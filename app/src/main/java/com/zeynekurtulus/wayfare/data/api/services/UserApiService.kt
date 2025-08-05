package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.user.*
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    
    @POST("user/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
    
    @POST("user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("user/addInfo")
    suspend fun addInfo(
        @Header("Authorization") authorization: String,
        @Body request: AddInfoRequest
    ): Response<ApiSuccessResponse>
    
    @GET("user/getCurrentUser")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<GetCurrentUserResponse>
    
    @POST("user/changePassword")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiSuccessResponse>
    
    @DELETE("user/delete")
    suspend fun deleteUser(
        @Header("Authorization") authorization: String,
        @Body request: DeleteUserRequest
    ): Response<ApiSuccessResponse>
    
    @POST("user/sendVerification")
    suspend fun sendVerification(
        @Body request: SendVerificationRequest
    ): Response<VerificationResponse>
    
    @POST("user/sendVerification/verifyCode")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<VerificationResponse>
}