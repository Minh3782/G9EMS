package com.example.g9ems.data.repository

import com.example.g9ems.data.models.FormField
import com.example.g9ems.data.models.FormSession
import com.example.g9ems.data.models.FormType
import kotlinx.coroutines.delay

class FormRepository {

    // Later: connect MongoDB via your Node API
    suspend fun saveSession(session: FormSession) {
        delay(150) // simulate
        android.util.Log.d("EMS-REPO", "Saving session: $session")
    }

    fun createBlankSession(formType: FormType): FormSession {
        val fields = when (formType) {
            FormType.FORM2_TEDDY -> listOf(
                FormField("teddy_id", "Teddy ID", required = true),
                FormField("patient_name", "Patient Name"),
                FormField("handoff_to", "Hand-off To"),
            )
            FormType.FORM4_STATUS -> listOf(
                FormField("unit_id", "Unit ID", required = true),
                FormField("status", "Status (Available/Busy/Out of Service)", required = true),
                FormField("location", "Current Location")
            )
            FormType.FORM1_OCCURRENCE -> listOf(
                FormField("incident_id", "Incident ID", required = true),
                FormField("date_time", "Date/Time", required = true),
                FormField("location", "Location", required = true),
                FormField("summary", "Summary", required = true)
            )
            FormType.FORM3_PATIENT_REPORT -> listOf(
                FormField("patient_name", "Patient Name", required = true),
                FormField("patient_age", "Patient Age"),
                FormField("patient_gender", "Patient Gender"),
                FormField("dob", "Date of Birth"),
                FormField("chief_complaint", "Chief Complaint", required = true),
                FormField("vitals", "Vitals"),
                FormField("assessment", "Assessment", required = true)
            )
        }
        return FormSession(formType = formType, fields = fields)
    }
}