package com.example.probuild.features.reports.data

import com.example.probuild.data.local.CalculationDao
import com.example.probuild.data.local.DiaryEntryDao
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.data.local.ProjectDao
import com.example.probuild.features.engineering.data.mapper.toDomain
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.data.mapper.toDomain
import com.example.probuild.features.reports.domain.ReportsRepository
import com.example.probuild.features.reports.domain.model.ProjectReportData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ReportsRepositoryImpl(
    private val projectDao: ProjectDao,
    private val calculationDao: CalculationDao,
    private val diaryEntryDao: DiaryEntryDao,
    private val preferenceManager: PreferenceManager
) : ReportsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getProjectReportData(): Flow<ProjectReportData> {
        return combine(
            projectDao.getProjects(),
            preferenceManager.selectedProjectId
        ) { projectEntities, selectedId ->
            val allProjects = projectEntities.map { it.toDomain() }
            val activeProject = allProjects.find { it.id == selectedId }
                ?: allProjects.firstOrNull()
            allProjects to activeProject
        }.flatMapLatest { (allProjects, activeProject) ->
            if (activeProject == null) {
                flowOf(
                    ProjectReportData(
                        activeProject = null,
                        totalProjectsCount = allProjects.size,
                        allProjects = allProjects
                    )
                )
            } else {
                combine(
                    calculationDao.getCalculationsByProject(activeProject.id),
                    diaryEntryDao.observeEntriesByProject(activeProject.id)
                ) { calcEntities, diaryEntities ->
                    val calculations = calcEntities.map { it.toDomain() }
                    val diaryEntries = diaryEntities.map { it.toDomain() }

                    val calcTypeMap = calculations
                        .groupingBy { it.type }
                        .eachCount()

                    val concreteVolume = calculations
                        .filter { it.type == CalculationType.CONCRETE || it.type == CalculationType.SLAB || it.type == CalculationType.FOUNDATION || it.type == CalculationType.COLUMN || it.type == CalculationType.BEAM }
                        .sumOf { it.resultVal }

                    val steelWeight = calculations
                        .filter { it.type == CalculationType.STEEL }
                        .sumOf { it.resultVal }

                    val bricksCount = calculations
                        .filter { it.type == CalculationType.BRICK }
                        .sumOf { it.resultVal }

                    val areaCalc = calculations
                        .filter { it.type == CalculationType.AREA }
                        .sumOf { it.resultVal }

                    val volumeCalc = calculations
                        .filter { it.type == CalculationType.VOLUME }
                        .sumOf { it.resultVal }

                    val totalLabour = diaryEntries.sumOf { it.labourCount }
                    val avgLabour = if (diaryEntries.isNotEmpty()) {
                        totalLabour.toDouble() / diaryEntries.size
                    } else {
                        0.0
                    }

                    ProjectReportData(
                        activeProject = activeProject,
                        totalProjectsCount = allProjects.size,
                        allProjects = allProjects,
                        totalCalculationsCount = calculations.size,
                        calculationsByType = calcTypeMap,
                        totalConcreteVolume = concreteVolume,
                        totalSteelWeight = steelWeight,
                        totalBricksCount = bricksCount,
                        totalAreaCalculated = areaCalc,
                        totalVolumeCalculated = volumeCalc,
                        totalDiaryEntriesCount = diaryEntries.size,
                        totalLabourRecorded = totalLabour,
                        averageLabourPerEntry = avgLabour,
                        recentCalculations = calculations.take(5),
                        recentDiaryEntries = diaryEntries.take(5)
                    )
                }
            }
        }
    }

    override suspend fun selectActiveProject(projectId: String) {
        preferenceManager.saveSelectedProjectId(projectId)
    }
}
