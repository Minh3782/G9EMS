package com.example.myapplication.data

// TeddyBearRecord.kt
data class TeddyBearRecord(
    val recordId: String = "",
    val formNumber: String = "", // Form #TB-260209-3502
    val distributionDate: String = "", // 2026-02-09
    val distributionTime: String = "", // 21:31:22
    val primaryMedicFirstName: String = "",
    val primaryMedicLastName: String = "",
    val primaryMedicNumber: String = "", // e.g., 10452
    val secondaryMedicFirstName: String = "", // Optional
    val secondaryMedicLastName: String = "", // Optional
    val secondaryMedicNumber: String = "", // Optional, e.g., 10453
    val recipientAge: Int = 0,
    val recipientGender: String = "", // Select gender
    val recipientType: String = "", // Select recipient type
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "", // User ID
    val documentType: String = "teddy_bear_record"
)