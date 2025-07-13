package org.playground.quiz.service

import jakarta.transaction.Transactional
import org.playground.quiz.dto.request.AddQuizRequest
import org.playground.quiz.dto.request.AnswerRequest
import org.playground.quiz.dto.response.Feedback
import org.playground.quiz.dto.response.QuizCompletionResponse
import org.playground.quiz.model.Quiz
import org.playground.quiz.model.QuizCompletion
import org.playground.quiz.repository.QuizCompletionRepository
import org.playground.quiz.repository.QuizRepository
import org.playground.quiz.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class QuizService @Autowired constructor(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository,
    private val quizCompletionRepository: QuizCompletionRepository
) {

    fun solveQuiz(id: Long, request: AnswerRequest, email: String): Feedback {
        val quiz = getQuiz(id) ?: throw QuizNotFoundException()
        if (quiz.answer == request.answer) {
            val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found by email: $email")
            val completedQuiz = QuizCompletion(quiz = quiz, author = user, completedAt = LocalDateTime.now())
            quizCompletionRepository.save(completedQuiz)
            return Feedback(true, "Congratulations, you're right!")

        } else return Feedback(false, "Wrong! Please, try again.")
    }

    fun createQuiz(request: AddQuizRequest, email: String): Quiz {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("User not found by email: $email")
        val quiz = Quiz(
            title = request.title,
            text = request.text,
            options = request.options,
            answer = request.answer,
            author = user,
        )
        quizRepository.save(quiz)
        return quiz
    }

    fun getQuiz(id: Long): Quiz? {
        return quizRepository.findById(id).orElse(null)
    }

    fun getAllQuizzes(pageable: Pageable): Page<Quiz> = quizRepository.findAll(pageable)

    fun deleteQuiz(id: Long, email: String) {
        val quiz = getQuiz(id) ?: throw QuizNotFoundException()
        if (quiz.author.email == email) {
            quizRepository.deleteById(id)
        } else {
            throw UnauthorizedException("You can only delete your own quizzes")
        }
    }

    fun getQuizCompletions(userEmail: String, pageable: Pageable): Page<QuizCompletionResponse> {
        val user = userRepository.findByEmail(userEmail) ?: throw UnauthorizedException("User not found by email: $userEmail")
        val completions = quizCompletionRepository.findByAuthorOrderByCompletedAtDesc(user, pageable)
        return completions.map { quizCompletion ->
            QuizCompletionResponse(
                id = quizCompletion.quiz.id,
                completedAt = quizCompletion.completedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }

    class UnauthorizedException(message: String) : RuntimeException(message)
    class QuizNotFoundException: RuntimeException()
}