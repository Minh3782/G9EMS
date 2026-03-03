package com.example.g9ems.data

// OccurrenceReport.kt
data class OccurrenceReport(
    val id: String = "", // Couchbase document ID
    val callNumber: String? = null,
    val occurrenceReference: String? = null,

    val occurrenceType: OccurrenceType? = null,
    val service: ServiceType? = null,
    val vehicleType: VehicleType? = null,
    val personnelRole: PersonnelRole? = null,

    val observationDescription: String? = null,
    val actionTaken: String? = null,
    val suggestedResolution: String? = null,
    val additionalDetails: String? = null,
    val additionalNotes: String? = null,
    val managementNotes: String? = null,

    val requestedBy: String? = null,
    val requestedByContact: String? = null,
    val reportCreator: String? = null,
    val creatorContact: String? = null,

    val status: OccurrenceStatus = OccurrenceStatus.SUBMITTED,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",

    val documentType: String = "occurrence_report",
    val documentVersion: Int = 1
)

enum class OccurrenceType {
    SAFETY_INCIDENT,
    VEHICLE_DAMAGE,
    PATIENT_COMPLAINT,
    EQUIPMENT_FAILURE,
    OTHER
}

enum class OccurrenceStatus {
    SUBMITTED, UNDER_REVIEW, RESOLVED, CLOSED
}

enum class ServiceType {
    EMS,
    FIRE,
    POLICE,
    OTHER
}

enum class VehicleType {
    TYPE_I,
    TYPE_II,
    TYPE_III,
    SUPERVISOR_UNIT,
    OTHER
}

enum class PersonnelRole {
    PCP,
    ACP,
    DRIVER,
    SUPERVISOR
}