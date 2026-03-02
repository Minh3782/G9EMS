package com.example.myapplication

data class PatientReport(
    val id: String = "",
    val patientName: String = "",
    val chiefComplaint: String = "",
    val timestamp: Long = System.currentTimeMillis()
)