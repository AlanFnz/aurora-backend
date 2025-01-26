package com.ixtlan.aurora.controller

import com.ixtlan.aurora.model.LoginRequest
import com.ixtlan.aurora.model.LoginResponse
import com.ixtlan.aurora.model.UserRegistrationRequest
import com.ixtlan.aurora.model.UserResponse
import com.ixtlan.aurora.service.UserService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager
) {

    private val secretKey = "mysecretkeymysecretkeymysecretkey12"
    private val expirationMs = 3600000

    @PostMapping("/register")
    fun registerUser(@RequestBody request: UserRegistrationRequest): ResponseEntity<UserResponse> {
        return try {
            val userResponse = userService.registerUser(request)
            ResponseEntity.ok(userResponse)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // Generate JWT
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        val token = Jwts.builder()
            .setSubject(request.username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()

        return LoginResponse(token)
    }
}
