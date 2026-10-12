package com.example.data.repository

import com.example.domain.model.SecurityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object SecurityEventRepository {
    private val _events = MutableStateFlow<List<SecurityEvent>>(
        listOf(
            SecurityEvent(
                id = UUID.randomUUID().toString(),
                eventType = "System Initialized",
                timestamp = System.currentTimeMillis() - 3600000,
                details = "LockMe engine initialized with encrypted credentials",
                isSuccess = true
            )
        )
    )
    val events: StateFlow<List<SecurityEvent>> = _events.asStateFlow()

    fun logEvent(eventType: String, details: String, isSuccess: Boolean) {
        val newEvent = SecurityEvent(
            id = UUID.randomUUID().toString(),
            eventType = eventType,
            timestamp = System.currentTimeMillis(),
            details = details,
            isSuccess = isSuccess
        )
        val current = _events.value.toMutableList()
        current.add(0, newEvent)
        if (current.size > 50) {
            _events.value = current.take(50)
        } else {
            _events.value = current
        }
    }

    fun clearEvents() {
        _events.value = emptyList()
    }
}
