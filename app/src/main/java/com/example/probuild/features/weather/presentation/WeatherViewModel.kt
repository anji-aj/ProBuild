package com.example.probuild.features.weather.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.weather.domain.WeatherRepository

class WeatherViewModel(
    private val repository: WeatherRepository = ServiceLocator.weatherRepository
) : ViewModel()
