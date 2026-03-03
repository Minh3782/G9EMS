package com.example.g9ems.data.models

data class FormField(val key: String,
                     val label: String,
                     val value: String = "",
                     val required: Boolean = false)
