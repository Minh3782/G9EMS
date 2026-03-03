package com.example.myapplication.data

// OccurrenceReport.kt
data class OccurrenceReport(
    val reportId: String = "",
    val submissionDate: String = "", // 2026-02-09
    val submissionTime: String = "", // 22:48:51
    val callNumber: String = "", // e.g., 2026-00412
    val occurrenceType: String = "", // -- Select --
    val occurrenceReference: String = "", // e.g., OCC-2026-0087
    val service: String = "", // -- Select --
    val vehicle: String = "", // e.g., Type III Ambulance
    val vehicleDescription: String = "", // e.g., Type III Ambulance
    val additionalDetails: String = "", // Additional classification details
    val personnelRole: String = "", // -- Select --
    val additionalNotes: String = "", // Fire Department Police
    val observationDescription: String = "", // Describe in detail
    val actionTaken: String = "", // Describe immediate actions
    val suggestedResolution: String = "", // Recommended steps
    val managementNotes: String = "", // Notes for supervisory review
    val requestedBy: String = "", // Name of requester
    val reportCreator: String = "", // Name of person completing form
    val requestedByContact: String = "", // Title, department, or contact info
    val creatorContact: String = "", // Title, badge #, or contact info
    val status: OccurrenceStatus = OccurrenceStatus.SUBMITTED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "", // User ID
    val documentType: String = "occurrence_report"
)

enum class OccurrenceStatus {
    SUBMITTED, UNDER_REVIEW, RESOLVED, CLOSED
}