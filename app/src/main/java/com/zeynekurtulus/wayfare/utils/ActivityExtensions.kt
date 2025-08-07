package com.zeynekurtulus.wayfare.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.zeynekurtulus.wayfare.WayfareApplication
import com.zeynekurtulus.wayfare.di.AppContainer
import com.zeynekurtulus.wayfare.presentation.ViewModelFactory

/**
 * Extension function to easily access the AppContainer from Activities
 */
fun ComponentActivity.getAppContainer(): AppContainer {
    return (application as WayfareApplication).container
}

/**
 * Extension function to easily access the AppContainer from Fragments
 */
fun Fragment.getAppContainer(): AppContainer {
    return (requireActivity().application as WayfareApplication).container
}

/**
 * Extension function to create ViewModelFactory from AppContainer
 */
fun AppContainer.createViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(
        userRepository = userRepository,
        routeRepository = routeRepository,
        placeRepository = placeRepository,
        locationRepository = locationRepository,
        feedbackRepository = feedbackRepository,
        cityRepository = cityRepository,
        mustVisitRepository = mustVisitRepository
    )
}