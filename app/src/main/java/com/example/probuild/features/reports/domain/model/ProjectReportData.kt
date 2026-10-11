package com.example.probuild.features.reports.domain.model

import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.domain.model.Project

data class ProjectReportData(
    val activeProject: Project? = null,
    val totalProjectsCount: Int = 0,
    val allProjects: List<Project> = emptyList(),
    val totalCalculationsCount: Int = 0,
    val calculationsByType: Map<CalculationType, Int> = emptyMap(),
    val totalConcreteVolume: Double = 0.0,
    val totalSteelWeight: Double = 0.0,
    val totalBricksCount: Double = 0.0,
    val totalAreaCalculated: Double = 0.0,
    val totalVolumeCalculated: Double = 0.0,
    val totalDiaryEntriesCount: Int = 0,
    val totalLabourRecorded: Int = 0,
    val averageLabourPerEntry: Double = 0.0,
    val recentCalculations: List<Calculation> = emptyList(),
    val recentDiaryEntries: List<DiaryEntry> = emptyList()
)
