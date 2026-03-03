package com.example.g9ems.ai

import com.example.g9ems.data.models.FormSession
import com.example.g9ems.data.models.FormField

object FormUpdateEngine {

    fun applyUpdates(
        session: FormSession,
        updates: Map<String, Any?>
    ): FormSession {

        val updatedFields = session.fields.map { field ->

            val newValue = updates[field.key]

            if (newValue == null) {
                field
            } else {
                when (field.type) {

                    "string" -> field.copy(value = newValue.toString())

                    "number" -> {
                        val number = newValue.toString().toIntOrNull()
                        if (number != null) {
                            field.copy(value = number.toString())
                        } else {
                            field // ignore invalid number
                        }
                    }

                    "enum" -> {
                        if (field.allowedValues.contains(newValue.toString())) {
                            field.copy(value = newValue.toString())
                        } else {
                            field // ignore invalid enum
                        }
                    }

                    else -> field
                }
            }
        }

        return session.copy(fields = updatedFields)
    }
}