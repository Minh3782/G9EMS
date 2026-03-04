package com.example.g9ems.data.models

data class FormSession(
    val formType: FormType,
    val fields: List<FormField>,
    val status: SessionStatus = SessionStatus.DRAFT
)

enum class FormType { FORM1_OCCURRENCE, FORM2_TEDDY, FORM3_PATIENT_REPORT, FORM4_STATUS }
enum class SessionStatus { DRAFT, READY_TO_REVIEW, SUBMITTED }

