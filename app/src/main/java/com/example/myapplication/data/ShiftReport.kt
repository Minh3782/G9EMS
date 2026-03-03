package com.example.myapplication.data

// ShiftReport.kt
data class ShiftReport(
    val reportId: String = "",
    val date: String = "", // 2023-01-01
    val time: String = "", // 09:00
    val patientName: String = "",
    val patientId: String = "", // 1234567890
    val patientAge: Int = 0,
    val patientGender: String = "", // Male/Female
    val patientType: String = "", // Emergency/Non-Emergency
    val patientCondition: String = "", // Critical/Stable/etc
    val patientStatus: String = "", // Hospitalised/Released/etc
    val patientLocation: String = "", // London/New York/etc
    val patientDetails: String = "", // [Details]
    val paramedicId: String = "", // User ID
    val stationId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val documentType: String = "shift_report"
)