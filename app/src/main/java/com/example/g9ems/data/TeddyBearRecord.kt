package com.example.g9ems.data

// TeddyBearRecord.kt
data class TeddyBearRecord(
    val id: String = "",
    val formNumber: String? = null,

    val distributionTimestamp: Long = System.currentTimeMillis(),

    val primaryMedicId: String,
    val secondaryMedicId: String? = null,

    val recipientAge: Int? = null,
    val recipientGender: Gender? = null,
    val recipientType: RecipientType? = null,

    val createdBy: String,

    val documentType: String = "teddy_bear_record",
    val documentVersion: Int = 1
)

enum class RecipientType {
    PEDIATRIC_PATIENT,
    TRAUMA_PATIENT,
    COMMUNITY_EVENT,
    OTHER
}