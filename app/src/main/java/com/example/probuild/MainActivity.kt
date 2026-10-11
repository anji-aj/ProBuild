package com.example.probuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.probuild.di.ServiceLocator
import com.example.probuild.navigation.RootNavGraph
import com.example.probuild.ui.theme.ProBuildTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by ServiceLocator.preferenceManager.themeMode.collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemInDark
            }

            ProBuildTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                RootNavGraph(navController = navController)
            }
        }
    }
}
