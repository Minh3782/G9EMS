package com.example.g9ems.data

// ParamedicStatusReport.kt
data class ParamedicStatusReport(
    val reportId: String = "",
    val paramedicId: String = "", // User ID
    val reportDate: String = "", // 20260225 (revision date)
    val teamId: String = "", // For team-specific reports

    // Checklist Items
    val acrStatus: ChecklistItem = ChecklistItem(), // ACR Completion
    val aceResponseStatus: ChecklistItem = ChecklistItem(), // ACE Response
    val driversLicenseStatus: ChecklistItem = ChecklistItem(), // Drivers License
    val vaccinationsStatus: ChecklistItem = ChecklistItem(), // Vaccinations
    val educationStatus: ChecklistItem = ChecklistItem(), // Continuous Education
    val uniformStatus: ChecklistItem = ChecklistItem(), // Uniform credits
    val criminalRecordStatus: ChecklistItem = ChecklistItem(), // CRC
    val acpStatus: ChecklistItem = ChecklistItem(), // ACP Status
    val vacationStatus: ChecklistItem = ChecklistItem(), // Vacation
    val missedMealsStatus: ChecklistItem = ChecklistItem(), // Missed Meals
    val overtimeStatus: ChecklistItem = ChecklistItem(), // Overtime Requests

    val createdAt: Long = System.currentTimeMillis(),
    val documentType: String = "paramedic_status_report"
)

data class ChecklistItem(
    val status: String = "GOOD", // GOOD/BAD
    val issueCount: Int = 0,
    val notes: String = ""
)