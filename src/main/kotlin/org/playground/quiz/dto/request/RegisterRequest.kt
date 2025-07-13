package org.playground.quiz.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Email(regexp = ".+@.+\\..+")
    val email: String,

    @field:NotBlank
    @field:Size(min = 5)
    val password: String
)