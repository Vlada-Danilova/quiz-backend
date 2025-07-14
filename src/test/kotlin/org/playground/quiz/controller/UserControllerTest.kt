package org.playground.quiz.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.playground.quiz.dto.request.RegisterRequest
import org.playground.quiz.model.AppUser
import org.playground.quiz.service.UserAlreadyExistsException
import org.playground.quiz.service.UserService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class UserControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var userController: UserController

    private val testEmail = "test@example.com"
    private val testPassword = "password"

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `registerUser returns 200 when registration is successful`() {
        // Arrange
        val request = RegisterRequest(email = testEmail, password = testPassword)
        val user = AppUser(id = 1L, email = testEmail, password = "encodedPassword")
        
        `when`(userService.registerUser(testEmail, testPassword)).thenReturn(user)

        // Act & Assert
        mockMvc.perform(post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
    }

    @Test
    fun `registerUser returns 400 when email is already taken`() {
        // Arrange
        val request = RegisterRequest(email = testEmail, password = testPassword)
        
        `when`(userService.registerUser(testEmail, testPassword))
            .thenThrow(UserAlreadyExistsException("User with email $testEmail already exists"))

        // Act & Assert
        mockMvc.perform(post("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }
}