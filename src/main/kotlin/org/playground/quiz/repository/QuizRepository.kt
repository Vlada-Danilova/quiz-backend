package org.playground.quiz.repository

import org.playground.quiz.model.Quiz
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuizRepository : JpaRepository<Quiz, Long> {
    override fun findAll(pageable: Pageable): Page<Quiz>
}