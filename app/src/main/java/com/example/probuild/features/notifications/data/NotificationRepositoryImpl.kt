package com.example.probuild.features.notifications.data

import com.example.probuild.features.notifications.domain.Notification
import com.example.probuild.features.notifications.domain.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl : NotificationRepository {
    
    private val mockNotifications = MutableStateFlow(
        listOf(
            Notification(
                id = "1",
                title = "Material Delivery",
                message = "Cement truck arriving at Skyline Apartment in 30 mins.",
                timestamp = "10 mins ago"
            ),
            Notification(
                id = "2",
                title = "Safety Alert",
                message = "Weekly safety inspection scheduled for Highway 101.",
                timestamp = "1 hour ago"
            ),
            Notification(
                id = "3",
                title = "Report Ready",
                message = "Monthly cost estimation report for Villa is ready to view.",
                timestamp = "2 hours ago"
            )
        )
    )

    override fun getNotifications(): Flow<List<Notification>> = mockNotifications

    override fun getUnreadCount(): Flow<Int> = mockNotifications.map { list ->
        list.count { !it.isRead }
    }

    override suspend fun markAsRead(id: String) {
        val currentList = mockNotifications.value
        val updatedList = currentList.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        mockNotifications.value = updatedList
    }
}
