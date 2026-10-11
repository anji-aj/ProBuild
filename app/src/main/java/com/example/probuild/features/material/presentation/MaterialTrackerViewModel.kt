package com.example.probuild.features.material.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.material.domain.MaterialTrackerRepository

class MaterialTrackerViewModel(
    private val repository: MaterialTrackerRepository = ServiceLocator.materialTrackerRepository
) : ViewModel()
