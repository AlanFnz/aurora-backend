package com.ixtlan.aurora.service

import com.ixtlan.aurora.model.AuthRequest
import com.ixtlan.aurora.model.AuthResponse
import com.ixtlan.aurora.model.RefreshRequest
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,

    // TODO: inject these from application.properties/environment variables
    //@Value("\${app.jwt.secret}")
    private val jwtSecret: String,
    //@Value("\${app.jwt.refresh-secret}")
    private val refreshSecret: String,

    //@Value("\${app.jwt.expiration-ms}")
    private val jwtExpirationMs: Long,
    //@Value("\${app.jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,

    // Optional: a RefreshTokenRepository if you want to store tokens in DB
    // private val refreshTokenRepository: RefreshTokenRepository
) {

    fun login(authRequest: AuthRequest): AuthResponse {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password)
            )

            val userDetails = authentication.principal as UserDetails
            val accessToken = generateAccessToken(userDetails.username)
            val refreshToken = generateRefreshToken(userDetails.username)

            // TODO: store the refresh token in db to track invalidation/logout
            // refreshTokenRepository.save(RefreshToken(...))

            return AuthResponse(accessToken, refreshToken)
        } catch (ex: BadCredentialsException) {
            throw ex // or throw a custom exception
        }
    }

    fun refreshToken(refreshRequest: RefreshRequest): AuthResponse {
        val username = validateRefreshToken(refreshRequest.refreshToken)
        val newAccessToken = generateAccessToken(username)
        val newRefreshToken = generateRefreshToken(username)

        // TODO: look up and verify the old token
        // refreshTokenRepository.findByToken(refreshRequest.refreshToken) ?: throw UnauthorizedException("Invalid refresh token")
        // refreshTokenRepository.delete(...) // if you want a rolling refresh strategy
        // refreshTokenRepository.save(RefreshToken(...))

        return AuthResponse(newAccessToken, newRefreshToken)
    }

    private fun generateAccessToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpirationMs)
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun generateRefreshToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + refreshExpirationMs)
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(Keys.hmacShaKeyFor(refreshSecret.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun validateRefreshToken(token: String): String {
        val claims: Claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(refreshSecret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
        return claims.subject ?: throw BadCredentialsException("Invalid refresh token")
    }

    // TODO: logout method for when refresh tokens are stored in db
    // fun logout(refreshToken: String) {
    //     val storedToken = refreshTokenRepository.findByToken(refreshToken)
    //         ?: throw UnauthorizedException("Invalid token")
    //     refreshTokenRepository.delete(storedToken)
    // }
}
