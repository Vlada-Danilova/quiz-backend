package org.playground.quiz.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class QuizCompletion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    val quiz: Quiz,

    @JsonIgnore
    @ManyToOne
    val author: AppUser,

    val completedAt: LocalDateTime
)