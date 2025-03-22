package com.ixtlan.aurora.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(FolderNotFoundException::class)
    fun handleFolderNotFoundException(ex: FolderNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                error = "Folder Not Found",
                message = ex.message ?: "The specified folder does not exist or does not belong to the user"
            )
        )
    }

    @ExceptionHandler(FolderNotEmptyException::class)
    fun handleFolderNotEmptyException(ex: FolderNotEmptyException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                error = "Folder Not Empty",
                message = ex.message
                    ?: "The folder cannot be deleted because it contains notes. Use cascadeDelete=true."
            )
        )
    }

    @ExceptionHandler(InvalidFolderException::class)
    fun handleInvalidFolderException(ex: InvalidFolderException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                error = "Invalid Folder",
                message = ex.message ?: "Folder ID is required"
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                error = "Bad Request",
                message = ex.message ?: "Invalid input provided"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                error = "Internal Server Error",
                message = "An unexpected error occurred"
            )
        )
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: org.springframework.http.converter.HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                error = "Bad Request",
                message = "Invalid or missing request body."
            )
        )
    }
}

data class ErrorResponse(
    val error: String,
    val message: String
)