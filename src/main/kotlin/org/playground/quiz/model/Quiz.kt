package org.playground.quiz.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Fetch

@Entity
@Table(name = "quizzes")
data class Quiz(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    val id: Long = 0,

    val title: String,

    val text: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    val options: List<String>,

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    val answer: Set<Int> = setOf(),

    @JsonIgnore
    @ManyToOne
    @JoinColumn(
        name = "author_id",  // Column name in Quiz table
        referencedColumnName = "id", // Primary key column in appusers
        foreignKey = ForeignKey(name = "fk_quiz_author")
    )
    val author: AppUser,

    // Add this relationship to cascade delete completions
    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val completions: List<QuizCompletion> = emptyList()
)
