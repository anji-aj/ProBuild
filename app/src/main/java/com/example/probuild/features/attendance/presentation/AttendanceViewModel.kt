package com.example.probuild.features.attendance.presentation

import androidx.lifecycle.ViewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.attendance.domain.AttendanceRepository

class AttendanceViewModel(
    private val repository: AttendanceRepository = ServiceLocator.attendanceRepository
) : ViewModel()
