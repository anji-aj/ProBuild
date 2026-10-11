package com.example.probuild.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room.Room
import com.example.probuild.data.local.PreferenceManager
import com.example.probuild.data.local.ProjectDatabase
import com.example.probuild.features.ai_assistant.data.AiAssistantRepositoryImpl
import com.example.probuild.features.ai_assistant.domain.AiAssistantRepository
import com.example.probuild.features.attendance.data.AttendanceRepositoryImpl
import com.example.probuild.features.attendance.domain.AttendanceRepository
import com.example.probuild.features.engineering.data.EngineeringRepositoryImpl
import com.example.probuild.features.engineering.domain.EngineeringRepository
import com.example.probuild.features.engineering.presentation.EngineeringWorkspaceViewModel
import com.example.probuild.features.dashboard.data.DashboardRepositoryImpl
import com.example.probuild.features.dashboard.domain.DashboardRepository
import com.example.probuild.features.dashboard.presentation.DashboardViewModel
import com.example.probuild.features.diary.data.SiteDiaryRepositoryImpl
import com.example.probuild.features.diary.data.DiaryPhotoRepositoryImpl
import com.example.probuild.features.diary.domain.DiaryPhotoRepository
import com.example.probuild.features.diary.domain.SiteDiaryRepository
import com.example.probuild.features.diary.presentation.DiaryViewModel
import com.example.probuild.features.equipment.data.EquipmentRepositoryImpl
import com.example.probuild.features.equipment.domain.EquipmentRepository
import com.example.probuild.features.estimator.data.CostEstimatorRepositoryImpl
import com.example.probuild.features.estimator.domain.CostEstimatorRepository
import com.example.probuild.features.maps.data.MapsRepositoryImpl
import com.example.probuild.features.maps.domain.MapsRepository
import com.example.probuild.features.material.data.MaterialTrackerRepositoryImpl
import com.example.probuild.features.material.domain.MaterialTrackerRepository
import com.example.probuild.features.profile.data.ProfileRepositoryImpl
import com.example.probuild.features.profile.domain.ProfileRepository
import com.example.probuild.features.engineering.presentation.concrete.ConcreteCalculatorViewModel
import com.example.probuild.features.engineering.presentation.brick.BrickCalculatorViewModel
import com.example.probuild.features.engineering.presentation.steel.SteelCalculatorViewModel
import com.example.probuild.features.engineering.presentation.area.AreaCalculatorViewModel
import com.example.probuild.features.engineering.presentation.volume.VolumeCalculatorViewModel
import com.example.probuild.features.engineering.presentation.beam.BeamCalculatorViewModel
import com.example.probuild.features.engineering.presentation.column.ColumnCalculatorViewModel
import com.example.probuild.features.engineering.presentation.slab.SlabCalculatorViewModel
import com.example.probuild.features.engineering.presentation.foundation.FoundationCalculatorViewModel
import com.example.probuild.features.engineering.presentation.unit_converter.UnitConverterViewModel
import com.example.probuild.features.projects.data.ProjectsRepositoryImpl
import com.example.probuild.features.projects.domain.ProjectsRepository
import com.example.probuild.features.projects.presentation.ProjectsViewModel
import com.example.probuild.features.projects.presentation.UpsertProjectViewModel
import com.example.probuild.features.reports.data.ReportsRepositoryImpl
import com.example.probuild.features.reports.domain.ReportsRepository
import com.example.probuild.features.reports.presentation.ReportsViewModel
import com.example.probuild.features.settings.data.SettingsRepositoryImpl
import com.example.probuild.features.settings.domain.SettingsRepository
import com.example.probuild.features.settings.presentation.SettingsViewModel
import com.example.probuild.features.weather.data.WeatherRepositoryImpl
import com.example.probuild.features.weather.domain.WeatherRepository
import com.example.probuild.features.notifications.data.NotificationRepositoryImpl
import com.example.probuild.features.notifications.domain.NotificationRepository

/**
 * A lightweight Service Locator for Version 1.0 Foundation.
 * Designed to be easily replaced by Hilt in the future.
 */
object ServiceLocator {
    
    private var database: ProjectDatabase? = null
    private var _preferenceManager: PreferenceManager? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        if (database == null) {
            database = Room.databaseBuilder(
                applicationContext,
                ProjectDatabase::class.java,
                "probuild_db"
            ).fallbackToDestructiveMigration().build()
        }
        if (_preferenceManager == null) {
            _preferenceManager = PreferenceManager(applicationContext)
        }
    }

    private fun getDatabase(): ProjectDatabase {
        return database ?: throw IllegalStateException("ServiceLocator not initialized")
    }

    val preferenceManager: PreferenceManager
        get() = _preferenceManager ?: throw IllegalStateException("ServiceLocator not initialized")

    val projectsRepository: ProjectsRepository by lazy { 
        ProjectsRepositoryImpl(getDatabase().projectDao()) 
    }
    
    val engineeringRepository: EngineeringRepository by lazy { 
        EngineeringRepositoryImpl(getDatabase().calculationDao()) 
    }

    val siteDiaryRepository: SiteDiaryRepository by lazy { 
        SiteDiaryRepositoryImpl(
            diaryEntryDao = getDatabase().diaryEntryDao(),
            diaryPhotoDao = getDatabase().diaryPhotoDao()
        )
    }

    val diaryPhotoRepository: DiaryPhotoRepository by lazy {
        DiaryPhotoRepositoryImpl(
            context = appContext ?: throw IllegalStateException("ServiceLocator not initialized"),
            diaryPhotoDao = getDatabase().diaryPhotoDao()
        )
    }

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepositoryImpl(
            projectDao = getDatabase().projectDao(),
            calculationDao = getDatabase().calculationDao(),
            diaryEntryDao = getDatabase().diaryEntryDao(),
            preferenceManager = preferenceManager
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(
            projectDao = getDatabase().projectDao(),
            calculationDao = getDatabase().calculationDao(),
            preferenceManager = preferenceManager
        )
    }
    
    val dashboardRepository: DashboardRepository by lazy { 
        DashboardRepositoryImpl(projectsRepository) 
    }
    
    val materialTrackerRepository: MaterialTrackerRepository by lazy { MaterialTrackerRepositoryImpl() }
    val equipmentRepository: EquipmentRepository by lazy { EquipmentRepositoryImpl() }
    val attendanceRepository: AttendanceRepository by lazy { AttendanceRepositoryImpl() }
    val aiAssistantRepository: AiAssistantRepository by lazy { AiAssistantRepositoryImpl() }
    val mapsRepository: MapsRepository by lazy { MapsRepositoryImpl() }
    val weatherRepository: WeatherRepository by lazy { WeatherRepositoryImpl() }
    val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl() }
    val costEstimatorRepository: CostEstimatorRepository by lazy { CostEstimatorRepositoryImpl() }
    val notificationRepository: NotificationRepository by lazy { NotificationRepositoryImpl() }

    fun provideViewModelFactory(): ViewModelProvider.Factory = ProBuildViewModelFactory(
        projectsRepository = projectsRepository,
        engineeringRepository = engineeringRepository,
        siteDiaryRepository = siteDiaryRepository,
        diaryPhotoRepository = diaryPhotoRepository,
        dashboardRepository = dashboardRepository,
        notificationRepository = notificationRepository,
        reportsRepository = reportsRepository,
        settingsRepository = settingsRepository,
        preferenceManager = preferenceManager
    )
}

class ProBuildViewModelFactory(
    private val projectsRepository: ProjectsRepository,
    private val engineeringRepository: EngineeringRepository,
    private val siteDiaryRepository: SiteDiaryRepository,
    private val diaryPhotoRepository: DiaryPhotoRepository,
    private val dashboardRepository: DashboardRepository,
    private val notificationRepository: NotificationRepository,
    private val reportsRepository: ReportsRepository,
    private val settingsRepository: SettingsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(dashboardRepository, notificationRepository, siteDiaryRepository, preferenceManager, projectsRepository) as T
            }
            modelClass.isAssignableFrom(EngineeringWorkspaceViewModel::class.java) -> {
                EngineeringWorkspaceViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> {
                ProjectsViewModel(projectsRepository) as T
            }
            modelClass.isAssignableFrom(UpsertProjectViewModel::class.java) -> {
                UpsertProjectViewModel(projectsRepository) as T
            }
            modelClass.isAssignableFrom(DiaryViewModel::class.java) -> {
                DiaryViewModel(siteDiaryRepository, diaryPhotoRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(ReportsViewModel::class.java) -> {
                ReportsViewModel(reportsRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(ConcreteCalculatorViewModel::class.java) -> {
                ConcreteCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(BrickCalculatorViewModel::class.java) -> {
                BrickCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(SteelCalculatorViewModel::class.java) -> {
                SteelCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(AreaCalculatorViewModel::class.java) -> {
                AreaCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(VolumeCalculatorViewModel::class.java) -> {
                VolumeCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(BeamCalculatorViewModel::class.java) -> {
                BeamCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(ColumnCalculatorViewModel::class.java) -> {
                ColumnCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(SlabCalculatorViewModel::class.java) -> {
                SlabCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(FoundationCalculatorViewModel::class.java) -> {
                FoundationCalculatorViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            modelClass.isAssignableFrom(UnitConverterViewModel::class.java) -> {
                UnitConverterViewModel(engineeringRepository, projectsRepository, preferenceManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
