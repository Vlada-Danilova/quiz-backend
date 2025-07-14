package org.playground.quiz.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.playground.quiz.dto.request.AddQuizRequest
import org.playground.quiz.dto.request.AnswerRequest
import org.playground.quiz.dto.response.Feedback
import org.playground.quiz.dto.response.QuizCompletionResponse
import org.playground.quiz.model.AppUser
import org.playground.quiz.model.Quiz
import org.playground.quiz.service.QuizService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class QuizControllerTest {

    @Mock
    private lateinit var quizService: QuizService

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var authentication: Authentication

    @InjectMocks
    private lateinit var quizController: QuizController

    private lateinit var testUser: AppUser
    private lateinit var testQuiz: Quiz
    private val testEmail = "test@example.com"

    @BeforeEach
    fun setUp() {
        // Set up test data
        testUser = AppUser(
            id = 1L,
            email = testEmail,
            password = "password"
        )

        testQuiz = Quiz(
            id = 1L,
            title = "Test Quiz",
            text = "What is the capital of France?",
            options = listOf("London", "Paris", "Berlin", "Madrid"),
            answer = setOf(1),
            author = testUser
        )

        // Set up SecurityContextHolder mock
        lenient().`when`(authentication.name).thenReturn(testEmail)
        lenient().`when`(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
    }

    @Test
    fun `createQuiz returns quiz response when successful`() {
        // Arrange
        val request = AddQuizRequest(
            title = "New Quiz",
            text = "What is 2+2?",
            options = listOf("3", "4", "5", "6"),
            answer = setOf(1)
        )

        `when`(quizService.createQuiz(request, testEmail)).thenReturn(testQuiz)

        // Act
        val response = quizController.createQuiz(request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(testQuiz.id, response.body?.id)
        assertEquals(testQuiz.title, response.body?.title)
        assertEquals(testQuiz.text, response.body?.text)
        assertEquals(testQuiz.options, response.body?.options)
    }

    @Test
    fun `getQuiz returns quiz response when quiz exists`() {
        // Arrange
        `when`(quizService.getQuiz(1L)).thenReturn(testQuiz)

        // Act
        val response = quizController.getQuiz(1L)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(testQuiz.id, response.body?.id)
        assertEquals(testQuiz.title, response.body?.title)
        assertEquals(testQuiz.text, response.body?.text)
        assertEquals(testQuiz.options, response.body?.options)
    }

    @Test
    fun `getQuiz returns 404 when quiz does not exist`() {
        // Arrange
        `when`(quizService.getQuiz(999L)).thenReturn(null)

        // Act
        val response = quizController.getQuiz(999L)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `solveQuiz returns feedback when answer is submitted`() {
        // Arrange
        val request = AnswerRequest(setOf(1))
        val feedback = Feedback(true, "Congratulations, you're right!")

        `when`(quizService.solveQuiz(1L, request, testEmail)).thenReturn(feedback)

        // Act
        val result = quizController.solveQuiz(1L, request)

        // Assert
        assertTrue(result.success)
        assertEquals("Congratulations, you're right!", result.message)
    }

    @Test
    fun `deleteQuiz returns 204 when quiz is deleted successfully`() {
        // Arrange
        doNothing().`when`(quizService).deleteQuiz(1L, testEmail)

        // Act
        val response = quizController.deleteQuiz(1L)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `deleteQuiz returns 404 when quiz is not found`() {
        // Arrange
        doThrow(QuizService.QuizNotFoundException()).`when`(quizService).deleteQuiz(999L, testEmail)

        // Act
        val response = quizController.deleteQuiz(999L)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `deleteQuiz returns 403 when user is not authorized`() {
        // Arrange
        doThrow(QuizService.UnauthorizedException("You can only delete your own quizzes"))
            .`when`(quizService).deleteQuiz(1L, testEmail)

        // Act
        val response = quizController.deleteQuiz(1L)

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `getAllQuizzes returns page of quizzes`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val quizzes = listOf(testQuiz)
        val page = PageImpl(quizzes, pageable, quizzes.size.toLong())

        `when`(quizService.getAllQuizzes(pageable)).thenReturn(page)

        // Act
        val result = quizController.getAllQuizzes(pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(testQuiz, result.content[0])
    }

    @Test
    fun `getCompletedQuizzes returns page of quiz completions`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val completionTime = LocalDateTime.now()
        val completionResponse = QuizCompletionResponse(
            id = 1L,
            completedAt = completionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        val completions = listOf(completionResponse)
        val page = PageImpl(completions, pageable, completions.size.toLong())

        `when`(quizService.getQuizCompletions(testEmail, pageable)).thenReturn(page)

        // Act
        val result = quizController.getCompletedQuizzes(pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(1L, result.content[0].id)
    }
}
