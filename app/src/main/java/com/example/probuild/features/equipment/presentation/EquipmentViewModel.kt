package com.example.probuild.features.equipment.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.equipment.domain.EquipmentRepository

class EquipmentViewModel(
    private val repository: EquipmentRepository = ServiceLocator.equipmentRepository
) : ViewModel()
