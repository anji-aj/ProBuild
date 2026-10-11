package com.example.probuild.features.maps.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.maps.domain.MapsRepository

class MapsViewModel(
    private val repository: MapsRepository = ServiceLocator.mapsRepository
) : ViewModel()
