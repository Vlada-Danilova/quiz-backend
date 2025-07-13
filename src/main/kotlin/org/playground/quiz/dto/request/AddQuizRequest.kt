package org.playground.quiz.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddQuizRequest (
    @NotBlank
    val title: String,
    @NotBlank
    val text: String,
    @Size(min = 2)
    val options: List<String>,
    val answer: Set<Int> = setOf()
)