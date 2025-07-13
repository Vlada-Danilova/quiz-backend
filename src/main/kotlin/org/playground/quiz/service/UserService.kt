package org.playground.quiz.service

import org.playground.quiz.model.AppUser
import org.playground.quiz.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun registerUser(email: String, password: String): AppUser {
        if (userRepository.findByEmail(email) != null) {
            throw UserAlreadyExistsException("User with email $email already exists")
        }

        val encodedPassword = passwordEncoder.encode(password)
        val appUser = AppUser(email = email, password = encodedPassword)
        return userRepository.save(appUser)
    }
}

class UserAlreadyExistsException(message: String) : RuntimeException(message)
