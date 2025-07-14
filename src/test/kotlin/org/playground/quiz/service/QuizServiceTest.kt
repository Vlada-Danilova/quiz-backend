package org.playground.quiz.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.playground.quiz.dto.request.AddQuizRequest
import org.playground.quiz.dto.request.AnswerRequest
import org.playground.quiz.model.AppUser
import org.playground.quiz.model.Quiz
import org.playground.quiz.model.QuizCompletion
import org.playground.quiz.repository.QuizCompletionRepository
import org.playground.quiz.repository.QuizRepository
import org.playground.quiz.repository.UserRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class QuizServiceTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var quizCompletionRepository: QuizCompletionRepository

    @InjectMocks
    private lateinit var quizService: QuizService

    private lateinit var testUser: AppUser
    private lateinit var testQuiz: Quiz

    @BeforeEach
    fun setUp() {
        testUser = AppUser(
            id = 1L,
            email = "test@example.com",
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
    }

    @Test
    fun `getQuiz returns quiz when found`() {
        // Arrange
        `when`(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz))

        // Act
        val result = quizService.getQuiz(1L)

        // Assert
        assertNotNull(result)
        assertEquals(testQuiz, result)
        verify(quizRepository).findById(1L)
    }

    @Test
    fun `getQuiz returns null when not found`() {
        // Arrange
        `when`(quizRepository.findById(999L)).thenReturn(Optional.empty())

        // Act
        val result = quizService.getQuiz(999L)

        // Assert
        assertNull(result)
        verify(quizRepository).findById(999L)
    }

    @Test
    fun `createQuiz creates and returns a new quiz`() {
        // Arrange
        val request = AddQuizRequest(
            title = "New Quiz",
            text = "What is 2+2?",
            options = listOf("3", "4", "5", "6"),
            answer = setOf(1)
        )

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(testUser)
        `when`(quizRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val result = quizService.createQuiz(request, "test@example.com")

        // Assert
        assertNotNull(result)
        assertEquals(request.title, result.title)
        assertEquals(request.text, result.text)
        assertEquals(request.options, result.options)
        assertEquals(request.answer, result.answer)
        assertEquals(testUser, result.author)
        verify(quizRepository).save(any())
    }

    @Test
    fun `solveQuiz returns success feedback when answer is correct`() {
        // Arrange
        val request = AnswerRequest(setOf(1))

        `when`(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz))
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(testUser)
        `when`(quizCompletionRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        // Act
        val result = quizService.solveQuiz(1L, request, "test@example.com")

        // Assert
        assertTrue(result.success)
        assertEquals("Congratulations, you're right!", result.message)
        verify(quizCompletionRepository).save(any())
    }

    @Test
    fun `solveQuiz returns failure feedback when answer is incorrect`() {
        // Arrange
        val request = AnswerRequest(setOf(0))

        `when`(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz))

        // Act
        val result = quizService.solveQuiz(1L, request, "test@example.com")

        // Assert
        assertFalse(result.success)
        assertEquals("Wrong! Please, try again.", result.message)
        verify(quizCompletionRepository, never()).save(any())
    }

    @Test
    fun `solveQuiz throws QuizNotFoundException when quiz not found`() {
        // Arrange
        val request = AnswerRequest(setOf(1))

        `when`(quizRepository.findById(999L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(QuizService.QuizNotFoundException::class.java) {
            quizService.solveQuiz(999L, request, "test@example.com")
        }
    }

    @Test
    fun `deleteQuiz deletes quiz when user is author`() {
        // Arrange
        `when`(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz))
        doNothing().`when`(quizRepository).deleteById(1L)

        // Act
        quizService.deleteQuiz(1L, "test@example.com")

        // Assert
        verify(quizRepository).deleteById(1L)
    }

    @Test
    fun `deleteQuiz throws UnauthorizedException when user is not author`() {
        // Arrange
        `when`(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz))

        // Act & Assert
        assertThrows(QuizService.UnauthorizedException::class.java) {
            quizService.deleteQuiz(1L, "other@example.com")
        }
        verify(quizRepository, never()).deleteById(any())
    }

    @Test
    fun `deleteQuiz throws QuizNotFoundException when quiz not found`() {
        // Arrange
        `when`(quizRepository.findById(999L)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows(QuizService.QuizNotFoundException::class.java) {
            quizService.deleteQuiz(999L, "test@example.com")
        }
    }

    @Test
    fun `getAllQuizzes returns page of quizzes`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val quizzes = listOf(testQuiz)
        val page = PageImpl(quizzes, pageable, quizzes.size.toLong())

        `when`(quizRepository.findAll(pageable)).thenReturn(page)

        // Act
        val result = quizService.getAllQuizzes(pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(testQuiz, result.content[0])
    }

    @Test
    fun `getQuizCompletions returns page of quiz completions`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val completion = QuizCompletion(
            id = 1L,
            quiz = testQuiz,
            author = testUser,
            completedAt = LocalDateTime.now()
        )
        val completions = listOf(completion)
        val page = PageImpl(completions, pageable, completions.size.toLong())

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(testUser)
        `when`(quizCompletionRepository.findByAuthorOrderByCompletedAtDesc(testUser, pageable)).thenReturn(page)

        // Act
        val result = quizService.getQuizCompletions("test@example.com", pageable)

        // Assert
        assertEquals(1, result.content.size)
        assertEquals(testQuiz.id, result.content[0].id)
    }

    @Test
    fun `getQuizCompletions throws UnauthorizedException when user not found`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)

        `when`(userRepository.findByEmail("unknown@example.com")).thenReturn(null)

        // Act & Assert
        assertThrows(QuizService.UnauthorizedException::class.java) {
            quizService.getQuizCompletions("unknown@example.com", pageable)
        }
    }
}
