package org.playground.quiz.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.playground.quiz.model.AppUser
import org.playground.quiz.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    private val testEmail = "test@example.com"
    private val testPassword = "password"
    private val encodedPassword = "encodedPassword"

    @BeforeEach
    fun setUp() {
        // No common setup needed
    }

    @Test
    fun `registerUser creates and returns a new user when email is not taken`() {
        // Arrange
        val expectedUser = AppUser(
            id = 1L,
            email = testEmail,
            password = encodedPassword
        )

        `when`(userRepository.findByEmail(testEmail)).thenReturn(null)
        `when`(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword)
        `when`(userRepository.save(any())).thenReturn(expectedUser)

        // Act
        val result = userService.registerUser(testEmail, testPassword)

        // Assert
        assertNotNull(result)
        assertEquals(testEmail, result.email)
        assertEquals(encodedPassword, result.password)
        verify(passwordEncoder).encode(testPassword)
        verify(userRepository).save(any())
    }

    @Test
    fun `registerUser throws UserAlreadyExistsException when email is already taken`() {
        // Arrange
        val existingUser = AppUser(
            id = 1L,
            email = testEmail,
            password = encodedPassword
        )

        `when`(userRepository.findByEmail(testEmail)).thenReturn(existingUser)

        // Act & Assert
        val exception = assertThrows(UserAlreadyExistsException::class.java) {
            userService.registerUser(testEmail, testPassword)
        }

        assertEquals("User with email $testEmail already exists", exception.message)
        verify(userRepository, never()).save(any())
    }
}
