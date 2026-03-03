package com.example.g9ems.data

// ShiftReport.kt
data class ShiftReport(
    val id: String = "",

    val timestamp: Long = System.currentTimeMillis(),

    val patientName: String? = null,
    val patientId: String? = null,
    val patientAge: Int? = null,
    val patientGender: Gender? = null,
    val patientType: PatientType? = null,
    val patientCondition: PatientCondition? = null,
    val patientStatus: PatientOutcome? = null,
    val patientLocation: String? = null,
    val patientDetails: String? = null,

    val paramedicId: String,
    val stationId: String? = null,

    val documentType: String = "shift_report",
    val documentVersion: Int = 1
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    UNKNOWN
}

enum class PatientType {
    EMERGENCY,
    NON_EMERGENCY
}

enum class PatientCondition {
    CRITICAL,
    SERIOUS,
    STABLE
}

enum class PatientOutcome {
    HOSPITALIZED,
    RELEASED,
    REFUSED_CARE,
    DECEASED
}