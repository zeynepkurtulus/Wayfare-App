package com.zeynekurtulus.wayfare.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    
    // Authentication
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(Constants.PREF_ACCESS_TOKEN, token).apply()
    }
    
    fun getAccessToken(): String? {
        return sharedPreferences.getString(Constants.PREF_ACCESS_TOKEN, null)
    }
    
    fun clearAccessToken() {
        sharedPreferences.edit().remove(Constants.PREF_ACCESS_TOKEN).apply()
    }
    
    // User Info
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(Constants.PREF_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.PREF_USER_ID, null)
    }
    
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(Constants.PREF_USERNAME, username).apply()
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(Constants.PREF_USERNAME, null)
    }
    
    fun saveEmail(email: String) {
        sharedPreferences.edit().putString(Constants.PREF_EMAIL, email).apply()
    }
    
    fun getEmail(): String? {
        return sharedPreferences.getString(Constants.PREF_EMAIL, null)
    }
    
    // Login State
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false) && 
               getAccessToken() != null
    }
    
    // User Session Management
    fun saveUserSession(
        accessToken: String,
        userId: String,
        username: String,
        email: String
    ) {
        sharedPreferences.edit().apply {
            putString(Constants.PREF_ACCESS_TOKEN, accessToken)
            putString(Constants.PREF_USER_ID, userId)
            putString(Constants.PREF_USERNAME, username)
            putString(Constants.PREF_EMAIL, email)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun clearUserSession() {
        sharedPreferences.edit().apply {
            remove(Constants.PREF_ACCESS_TOKEN)
            remove(Constants.PREF_USER_ID)
            remove(Constants.PREF_USERNAME)
            remove(Constants.PREF_EMAIL)
            putBoolean(Constants.PREF_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    // Generic Methods
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
    
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    fun removeKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    // Check if key exists
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}