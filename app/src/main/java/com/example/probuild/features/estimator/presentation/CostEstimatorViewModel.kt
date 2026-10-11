package com.example.probuild.features.estimator.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.estimator.domain.CostEstimatorRepository

class CostEstimatorViewModel(
    private val repository: CostEstimatorRepository = ServiceLocator.costEstimatorRepository
) : ViewModel()
