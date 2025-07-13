package org.playground.quiz.repository

import org.playground.quiz.model.AppUser
import org.playground.quiz.model.QuizCompletion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuizCompletionRepository : JpaRepository<QuizCompletion, String> {
    fun findByAuthorOrderByCompletedAtDesc(
        author: AppUser,
        pageable: Pageable
    ): Page<QuizCompletion>
}