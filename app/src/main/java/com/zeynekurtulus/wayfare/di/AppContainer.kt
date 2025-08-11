package com.zeynekurtulus.wayfare.di

import android.content.Context
import com.zeynekurtulus.wayfare.data.api.NetworkConfig
import com.zeynekurtulus.wayfare.data.api.services.*
import com.zeynekurtulus.wayfare.data.local.WayfareDatabase
import com.zeynekurtulus.wayfare.data.repository.*
import com.zeynekurtulus.wayfare.domain.repository.*
import com.zeynekurtulus.wayfare.presentation.ViewModelFactory
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import com.zeynekurtulus.wayfare.utils.NetworkConnectivityManager
import retrofit2.Retrofit

/**
 * Dependency injection container for the application.
 * This class provides instances of repositories, API services, and other dependencies.
 */
class AppContainer(private val context: Context) {
    
    // Shared Preferences Manager
    val sharedPreferencesManager: SharedPreferencesManager by lazy {
        SharedPreferencesManager(context)
    }
    
    // Network Connectivity Manager
    val networkConnectivityManager: NetworkConnectivityManager by lazy {
        NetworkConnectivityManager(context)
    }
    
    // Database instance
    private val database: WayfareDatabase by lazy {
        WayfareDatabase.getDatabase(context)
    }
    
    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        NetworkConfig.createRetrofit(sharedPreferencesManager)
    }
    
    // API Services
    private val userApiService: UserApiService by lazy {
        NetworkConfig.createUserApiService(retrofit)
    }
    
    private val routeApiService: RouteApiService by lazy {
        NetworkConfig.createRouteApiService(retrofit)
    }
    
    private val placeApiService: PlaceApiService by lazy {
        NetworkConfig.createPlaceApiService(retrofit)
    }
    
    private val locationApiService: LocationApiService by lazy {
        NetworkConfig.createLocationApiService(retrofit)
    }
    
    private val feedbackApiService: FeedbackApiService by lazy {
        NetworkConfig.createFeedbackApiService(retrofit)
    }
    
    private val cityApiService: CityApiService by lazy {
        NetworkConfig.createCityApiService(retrofit)
    }
    
    private val mustVisitApiService: MustVisitApiService by lazy {
        NetworkConfig.createMustVisitApiService(retrofit)
    }
    
    // Repository implementations
    val routeRepository: RouteRepository by lazy {
        RouteRepositoryImpl(
            routeApiService, 
            database.routeDao(), 
            networkConnectivityManager, 
            sharedPreferencesManager
        )
    }
    
    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userApiService, sharedPreferencesManager, routeRepository)
    }
    
    val placeRepository: PlaceRepository by lazy {
        PlaceRepositoryImpl(placeApiService, sharedPreferencesManager)
    }
    
    val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(locationApiService, sharedPreferencesManager)
    }
    
    val feedbackRepository: FeedbackRepository by lazy {
        FeedbackRepositoryImpl(feedbackApiService, sharedPreferencesManager)
    }
    
    val cityRepository: CityRepository by lazy {
        CityRepositoryImpl(cityApiService, sharedPreferencesManager)
    }
    
    val mustVisitRepository: MustVisitRepository by lazy {
        MustVisitRepositoryImpl(mustVisitApiService)
    }
    
    // ViewModelFactory
    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(
            userRepository = userRepository,
            routeRepository = routeRepository,
            placeRepository = placeRepository,
            locationRepository = locationRepository,
            feedbackRepository = feedbackRepository,
            cityRepository = cityRepository,
            mustVisitRepository = mustVisitRepository,
            networkConnectivityManager = networkConnectivityManager
        )
    }
}