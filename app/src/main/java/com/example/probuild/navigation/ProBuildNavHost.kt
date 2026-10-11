package com.example.probuild.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.probuild.features.dashboard.presentation.DashboardScreen
import com.example.probuild.features.projects.presentation.ProjectsScreen
import com.example.probuild.features.projects.presentation.ProjectPreviewScreen
import com.example.probuild.features.projects.presentation.UpsertProjectScreen
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.engineering.presentation.EngineeringWorkspaceScreen
import com.example.probuild.features.engineering.presentation.concrete.ConcreteCalculatorScreen
import com.example.probuild.features.engineering.presentation.brick.BrickCalculatorScreen
import com.example.probuild.features.engineering.presentation.steel.SteelCalculatorScreen
import com.example.probuild.features.engineering.presentation.area.AreaCalculatorScreen
import com.example.probuild.features.engineering.presentation.volume.VolumeCalculatorScreen
import com.example.probuild.features.engineering.presentation.beam.BeamCalculatorScreen
import com.example.probuild.features.engineering.presentation.column.ColumnCalculatorScreen
import com.example.probuild.features.engineering.presentation.slab.SlabCalculatorScreen
import com.example.probuild.features.engineering.presentation.foundation.FoundationCalculatorScreen
import com.example.probuild.features.engineering.presentation.unit_converter.UnitConverterScreen
import com.example.probuild.features.diary.presentation.SiteDiaryScreen
import com.example.probuild.features.diary.presentation.DiaryEntryScreen
import com.example.probuild.features.diary.presentation.PhotoPreviewScreen
import com.example.probuild.features.reports.presentation.ReportsScreen
import com.example.probuild.features.settings.presentation.SettingsScreen
import com.example.probuild.features.material.presentation.MaterialTrackerScreen
import com.example.probuild.features.estimator.presentation.CostEstimatorScreen
import com.example.probuild.features.equipment.presentation.EquipmentScreen
import com.example.probuild.features.attendance.presentation.AttendanceScreen
import com.example.probuild.features.ai_assistant.presentation.AiAssistantScreen
import com.example.probuild.features.maps.presentation.MapsScreen
import com.example.probuild.features.weather.presentation.WeatherScreen
import com.example.probuild.features.profile.presentation.ProfileScreen
import com.example.probuild.features.notifications.presentation.NotificationsScreen

@Composable
fun ProBuildNavHost(
    navController: NavHostController,
    homeReselectedTrigger: Int = 0,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard,
        modifier = modifier.padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigate = { screen -> 
                    navController.navigate(screen) {
                        launchSingleTop = true
                    }
                },
                scrollToTopTrigger = homeReselectedTrigger
            )
        }
        
        composable<Screen.Projects> {
            ProjectsScreen(
                onProjectClick = { id -> navController.navigate(Screen.ProjectPreview(id)) },
                onAddProjectClick = { navController.navigate(Screen.UpsertProject()) }
            )
        }
        
        composable<Screen.UpsertProject> { backStackEntry ->
            val route: Screen.UpsertProject = backStackEntry.toRoute()
            UpsertProjectScreen(
                projectId = route.id,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.EngineeringWorkspace> {
            EngineeringWorkspaceScreen(
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) },
                onCalculatorClick = { type, id ->
                    when (type) {
                        CalculationType.CONCRETE -> navController.navigate(Screen.ConcreteCalculator(id))
                        CalculationType.BRICK -> navController.navigate(Screen.BrickCalculator(id))
                        CalculationType.STEEL -> navController.navigate(Screen.SteelCalculator(id))
                        CalculationType.AREA -> navController.navigate(Screen.AreaCalculator(id))
                        CalculationType.VOLUME -> navController.navigate(Screen.VolumeCalculator(id))
                        CalculationType.BEAM -> navController.navigate(Screen.BeamCalculator(id))
                        CalculationType.COLUMN -> navController.navigate(Screen.ColumnCalculator(id))
                        CalculationType.SLAB -> navController.navigate(Screen.SlabCalculator(id))
                        CalculationType.FOUNDATION -> navController.navigate(Screen.FoundationCalculator(id))
                        CalculationType.UNIT_CONVERTER -> navController.navigate(Screen.UnitConverter(id))
                        else -> { /* TODO: Other calculators */ }
                    }
                }
            )
        }

        composable<Screen.SiteDiary> {
            SiteDiaryScreen(
                onNavigateToEntry = { id -> navController.navigate(Screen.DiaryEntry(id)) },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.DiaryEntry> { backStackEntry ->
            val route: Screen.DiaryEntry = backStackEntry.toRoute()
            DiaryEntryScreen(
                entryId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToPhotoPreview = { photoId -> navController.navigate(Screen.PhotoPreview(photoId)) }
            )
        }

        composable<Screen.PhotoPreview> { backStackEntry ->
            val route: Screen.PhotoPreview = backStackEntry.toRoute()
            PhotoPreviewScreen(
                photoId = route.photoId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.MaterialTracker> { MaterialTrackerScreen() }
        composable<Screen.CostEstimator> { CostEstimatorScreen() }
        composable<Screen.Reports> { 
            ReportsScreen(
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            ) 
        }
        composable<Screen.Equipment> { EquipmentScreen() }
        composable<Screen.Attendance> { AttendanceScreen() }
        composable<Screen.AiAssistant> { AiAssistantScreen() }
        composable<Screen.Maps> { MapsScreen() }
        composable<Screen.Weather> { WeatherScreen() }
        composable<Screen.Settings> { SettingsScreen() }
        
        composable<Screen.Profile> { 
            ProfileScreen(onBackClick = { navController.popBackStack() }) 
        }

        composable<Screen.Notifications> { 
            NotificationsScreen(onBackClick = { navController.popBackStack() }) 
        }

        composable<Screen.ProjectPreview> { backStackEntry ->
            val route: Screen.ProjectPreview = backStackEntry.toRoute()
            ProjectPreviewScreen(
                projectId = route.id,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.UpsertProject(id)) },
                onCalculatorClick = { type, id ->
                    when (type) {
                        CalculationType.CONCRETE -> navController.navigate(Screen.ConcreteCalculator(id))
                        CalculationType.BRICK -> navController.navigate(Screen.BrickCalculator(id))
                        CalculationType.STEEL -> navController.navigate(Screen.SteelCalculator(id))
                        CalculationType.AREA -> navController.navigate(Screen.AreaCalculator(id))
                        CalculationType.VOLUME -> navController.navigate(Screen.VolumeCalculator(id))
                        CalculationType.BEAM -> navController.navigate(Screen.BeamCalculator(id))
                        CalculationType.COLUMN -> navController.navigate(Screen.ColumnCalculator(id))
                        CalculationType.SLAB -> navController.navigate(Screen.SlabCalculator(id))
                        CalculationType.FOUNDATION -> navController.navigate(Screen.FoundationCalculator(id))
                        CalculationType.UNIT_CONVERTER -> navController.navigate(Screen.UnitConverter(id))
                        else -> {}
                    }
                },
                onDiaryEntryClick = { id -> navController.navigate(Screen.DiaryEntry(id)) }
            )
        }

        composable<Screen.ConcreteCalculator> { backStackEntry ->
            val route: Screen.ConcreteCalculator = backStackEntry.toRoute()
            ConcreteCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.BrickCalculator> { backStackEntry ->
            val route: Screen.BrickCalculator = backStackEntry.toRoute()
            BrickCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.SteelCalculator> { backStackEntry ->
            val route: Screen.SteelCalculator = backStackEntry.toRoute()
            SteelCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.AreaCalculator> { backStackEntry ->
            val route: Screen.AreaCalculator = backStackEntry.toRoute()
            AreaCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.VolumeCalculator> { backStackEntry ->
            val route: Screen.VolumeCalculator = backStackEntry.toRoute()
            VolumeCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.BeamCalculator> { backStackEntry ->
            val route: Screen.BeamCalculator = backStackEntry.toRoute()
            BeamCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.ColumnCalculator> { backStackEntry ->
            val route: Screen.ColumnCalculator = backStackEntry.toRoute()
            ColumnCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.SlabCalculator> { backStackEntry ->
            val route: Screen.SlabCalculator = backStackEntry.toRoute()
            SlabCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.FoundationCalculator> { backStackEntry ->
            val route: Screen.FoundationCalculator = backStackEntry.toRoute()
            FoundationCalculatorScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }

        composable<Screen.UnitConverter> { backStackEntry ->
            val route: Screen.UnitConverter = backStackEntry.toRoute()
            UnitConverterScreen(
                calculationId = route.id,
                onBackClick = { navController.popBackStack() },
                onNavigateToProjectCreation = { navController.navigate(Screen.UpsertProject()) }
            )
        }
    }
}
