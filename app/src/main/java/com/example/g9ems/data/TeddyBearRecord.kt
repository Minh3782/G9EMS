package com.example.g9ems.data

// TeddyBearRecord.kt
data class TeddyBearRecord(
    val id: String = "",
    val formNumber: String? = null,

    val distributionTimestamp: Long = System.currentTimeMillis(),

    val primaryMedicFirstName: String = "",
    val primaryMedicLastName: String = "",
    val primaryMedicNumber: String = "",
    val secondaryMedicFirstName: String = "",
    val secondaryMedicLastName: String = "",
    val secondaryMedicNumber: String = "",

    val recipientAge: Int? = null,
    val recipientGender: Gender? = null,
    val recipientType: RecipientType? = null,

    val createdBy: String = "",

    val documentType: String = "teddy_bear_record",
)

enum class RecipientType {
    PEDIATRIC_PATIENT,
    TRAUMA_PATIENT,
    COMMUNITY_EVENT,
    OTHER
}