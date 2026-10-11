package com.example.probuild.di

import com.example.probuild.repository.ProBuildRepository
import com.example.probuild.repository.ProBuildRepositoryImpl

/**
 * Dependency injection container for the application.
 */
interface AppContainer {
    val repository: ProBuildRepository
}

/**
 * Default implementation of [AppContainer].
 */
class DefaultAppContainer : AppContainer {
    override val repository: ProBuildRepository by lazy {
        ProBuildRepositoryImpl()
    }
}
