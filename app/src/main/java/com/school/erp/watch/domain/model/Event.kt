package com.school.erp.watch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val success: Boolean,
    val data: List<Event>
)

@Serializable
data class Event(
    val id: Long,
    val title: String,
    val description: String?,
    val date: String,
    val startTime: String?,
    val endTime: String?,
    val venue: String?,
    val category: String,
    val audience: String,
    val isRecurring: Boolean
)
