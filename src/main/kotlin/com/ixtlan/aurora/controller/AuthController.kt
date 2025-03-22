package com.ixtlan.aurora.controller

import com.ixtlan.aurora.model.AuthRequest
import com.ixtlan.aurora.model.RefreshRequest
import com.ixtlan.aurora.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthRequest): ResponseEntity<Any> {
        return try {
            val authResponse = authService.login(authRequest)
            ResponseEntity.ok(authResponse)
        } catch (ex: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized", "message" to "Invalid credentials"))
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshRequest: RefreshRequest): ResponseEntity<Any> {
        return try {
            val authResponse = authService.refreshToken(refreshRequest)
            ResponseEntity.ok(authResponse)
        } catch (ex: BadCredentialsException) {
            // TODO: custom exception?
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized", "message" to ex.message))
        }
    }
}
