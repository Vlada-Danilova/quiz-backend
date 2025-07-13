package org.playground.quiz.controller

import org.playground.quiz.dto.request.AddQuizRequest
import org.playground.quiz.dto.request.AnswerRequest
import org.playground.quiz.dto.response.Feedback
import org.playground.quiz.dto.response.QuizCompletionResponse
import org.playground.quiz.dto.response.QuizResponse
import org.playground.quiz.model.Quiz
import org.playground.quiz.service.QuizService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/quizzes")
@RestController
class QuizController @Autowired constructor(
    private val quizService: QuizService
) {

    @PostMapping()
    fun createQuiz(@RequestBody request: AddQuizRequest): ResponseEntity<QuizResponse> {
        val email = SecurityContextHolder.getContext().authentication.name

        val quiz = quizService.createQuiz(request, email)
        val response = QuizResponse(
            id = quiz.id,
            title = quiz.title,
            text = quiz.text,
            options = quiz.options
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getQuiz(@PathVariable id: Long): ResponseEntity<QuizResponse> {
        val quiz = quizService.getQuiz(id)
        return if (quiz != null) {
            val response =
                QuizResponse(
                    id = quiz.id,
                    title = quiz.title,
                    text = quiz.text,
                    options = quiz.options
                )
            ResponseEntity.ok(response)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }


    // todo rename to match actual logic
    @GetMapping()
    fun getAllQuizzes(@PageableDefault(size = 10, sort = ["id"]) pageable: Pageable): Page<Quiz> {
        return quizService.getAllQuizzes(pageable)
    }

    @PostMapping("/{id}/solve")
    fun solveQuiz(@PathVariable id: Long, @RequestBody request: AnswerRequest): Feedback {
        val email = SecurityContextHolder.getContext().authentication.name
        return quizService.solveQuiz(id, request, email)
    }

    @DeleteMapping("/{id}")
    fun deleteQuiz(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val email = SecurityContextHolder.getContext().authentication.name
            quizService.deleteQuiz(id, email)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (e: QuizService.QuizNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        } catch (e: QuizService.UnauthorizedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @GetMapping("/completed")
    fun getCompletedQuizzes(@PageableDefault(size = 10, sort = ["id"]) pageable: Pageable): Page<QuizCompletionResponse> {
        val userEmail = SecurityContextHolder.getContext().authentication.name
        return quizService.getQuizCompletions(userEmail, pageable)
    }
}
