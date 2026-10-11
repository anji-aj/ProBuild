package com.example.probuild.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.probuild.ui.MainScreen

@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Graph.Main,
        route = Graph.Root::class
    ) {
        composable<Graph.Main> {
            MainScreen()
        }
        
        composable<Graph.Auth> {
            // Auth skeleton
        }
        
        composable<Graph.Onboarding> {
            // Onboarding skeleton
        }
    }
}
