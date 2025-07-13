package org.playground.quiz.dto.response

data class QuizResponse (
    val id: Long,
    val title: String,
    val text: String,
    val options: List<String>
)