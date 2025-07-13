package org.playground.quiz.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val email: String,

    val password: String,

    var userAuthority: String = "ROLE_USER",

    @Column
    @OneToMany(fetch = FetchType.EAGER)
    val quizzes: MutableList<Quiz> = mutableListOf()
)