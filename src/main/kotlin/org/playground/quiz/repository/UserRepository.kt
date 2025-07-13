package org.playground.quiz.repository

import org.playground.quiz.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByEmail(email: String): AppUser?
}