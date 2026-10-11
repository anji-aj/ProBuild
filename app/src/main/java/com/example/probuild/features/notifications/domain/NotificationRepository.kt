package com.example.probuild.features.notifications.domain

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: String)
}
