package com.example.g9ems.data

// ParamedicStatusReport.kt
data class ParamedicStatusReport(
    val id: String = "",
    val paramedicId: String,
    val teamId: String? = null,

    val reportDate: Long = System.currentTimeMillis(),

    val checklist: Map<ChecklistType, ChecklistItem> = emptyMap(),

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val documentType: String = "paramedic_status_report",
    val documentVersion: Int = 1
)

data class ChecklistItem(
    val status: ChecklistStatus = ChecklistStatus.GOOD,
    val issueCount: Int = 0,
    val notes: String? = null
)

enum class ChecklistStatus {
    GOOD,
    WARNING,
    CRITICAL
}

enum class ChecklistType {
    ACR,
    ACE_RESPONSE,
    DRIVERS_LICENSE,
    VACCINATIONS,
    EDUCATION,
    UNIFORM,
    CRC,
    ACP_STATUS,
    VACATION,
    MISSED_MEALS,
    OVERTIME
}