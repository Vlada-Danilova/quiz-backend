# Quiz Application

A RESTful web service for creating and taking quizzes, built with Spring Boot and Kotlin.

## Overview

This application allows users to:
- Register an account
- Create quizzes with multiple-choice questions
- Browse available quizzes
- Solve quizzes and get immediate feedback
- Track completed quizzes
- Delete their own quizzes

## Technologies Used

- **Kotlin**: Programming language
- **Spring Boot**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **H2 Database**: In-memory database
- **Gradle**: Build tool
- **JUnit 5**: Testing framework

## Setup Instructions

### Prerequisites
- JDK 17 or higher
- Gradle

## API Endpoints

### User Management

- **POST /api/register**: Register a new user
  - Request body: `{ "email": "user@example.com", "password": "password" }`
  - Response: 200 OK if successful, 400 Bad Request if email already exists

### Quiz Management

- **POST /api/quizzes**: Create a new quiz
  - Request body: 
    ```json
    {
      "title": "Quiz Title",
      "text": "Quiz question text",
      "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
      "answer": [0, 2]
    }
    ```
    The `answer` field contains indices of correct options.
  - Response: Created quiz details

- **GET /api/quizzes/{id}**: Get a specific quiz by ID
  - Response: Quiz details or 404 Not Found

- **GET /api/quizzes**: Get all quizzes (paginated)
  - Response: Page of quizzes

- **POST /api/quizzes/{id}/solve**: Submit an answer for a quiz
  - Request body: `{ "answer": [0, 2] }` (where the array contains indices of selected options)
  - Response: Feedback on whether the answer is correct

- **DELETE /api/quizzes/{id}**: Delete a quiz (only by the author)
  - Response: 204 No Content if successful, 404 Not Found if quiz doesn't exist, 403 Forbidden if not the author

- **GET /api/quizzes/completed**: Get all quizzes completed by the current user (paginated)
  - Query parameters: `page`, `size`, `sort`
  - Response: Page of completed quizzes with completion timestamps

## Features

- **User Authentication**: Secure registration and authentication
- **Quiz Creation**: Create quizzes with multiple-choice questions and multiple correct answers
- **Quiz Taking**: Solve quizzes and get immediate feedback
- **Quiz Management**: View, delete, and track quizzes
- **Pagination**: Efficient retrieval of quizzes and completions

## Database Schema

The application uses the following entities:
- **AppUser**: Represents a user with email, password, and associated quizzes
- **Quiz**: Represents a quiz with title, text, options, answers, and author
- **QuizCompletion**: Tracks when a user completes a quiz