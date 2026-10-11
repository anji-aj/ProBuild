package com.example.probuild.features.profile.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.profile.domain.ProfileRepository

class ProfileViewModel(
    private val repository: ProfileRepository = ServiceLocator.profileRepository
) : ViewModel()
