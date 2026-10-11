package com.example.probuild.features.ai_assistant.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.ai_assistant.domain.AiAssistantRepository

class AiAssistantViewModel(
    private val repository: AiAssistantRepository = ServiceLocator.aiAssistantRepository
) : ViewModel()
