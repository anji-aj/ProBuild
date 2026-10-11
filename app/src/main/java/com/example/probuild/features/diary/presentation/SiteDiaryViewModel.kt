package com.example.probuild.features.diary.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.diary.domain.SiteDiaryRepository

class SiteDiaryViewModel(
    private val repository: SiteDiaryRepository = ServiceLocator.siteDiaryRepository
) : ViewModel()
