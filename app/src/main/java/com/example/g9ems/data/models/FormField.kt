package com.example.g9ems.data.models

data class FormField(
    val key: String,
    val label: String,
    val value: String = "",
    val required: Boolean = false,
    val type: String = "string",
    val allowedValues: List<String> = emptyList()
)

