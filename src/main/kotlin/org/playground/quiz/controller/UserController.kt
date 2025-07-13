package org.playground.quiz.controller

import org.playground.quiz.service.UserService
import jakarta.validation.Valid
import org.playground.quiz.dto.request.RegisterRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class UserController(private val userService: UserService) {
    @PostMapping("/register")
    fun registerUser(@RequestBody @Valid request: RegisterRequest): ResponseEntity<Any> {
        try {
            userService.registerUser(request.email, request.password)
            return ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }
}