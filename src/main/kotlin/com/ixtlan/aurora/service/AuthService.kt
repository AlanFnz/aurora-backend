package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.RefreshToken
import com.ixtlan.aurora.model.AuthRequest
import com.ixtlan.aurora.model.AuthResponse
import com.ixtlan.aurora.model.RefreshRequest
import com.ixtlan.aurora.repository.RefreshTokenRepository
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
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val refreshTokenRepository: RefreshTokenRepository

    // TODO: inject these from application.properties/environment variables
    //@Value("\${app.jwt.secret}")
    //private val jwtSecret: String,
    //@Value("\${app.jwt.refresh-secret}")
    //private val refreshSecret: String,
    //@Value("\${app.jwt.expiration-ms}")
    //private val jwtExpirationMs: Long,
    //@Value("\${app.jwt.refresh-expiration-ms}")
    //private val refreshExpirationMs: Long,
) {

    // TODO: temporary hard coded values
    private val jwtSecret = "mysecretkeymysecretkeymysecretkeyABCD"
    private val refreshSecret = "myrefreshkeymyrefreshkeymyrefreshkeyXYZ"
    private val jwtExpirationMs = 900_000L         // 15 minutes
    private val refreshExpirationMs = 604_800_000L // 7 days

    fun login(authRequest: AuthRequest): AuthResponse {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password)
            )

            val userDetails = authentication.principal as UserDetails
            val accessToken = generateAccessToken(userDetails.username)
            val refreshToken = generateRefreshToken(userDetails.username)

            val expiresAt = Instant.now().plusMillis(refreshExpirationMs)
            val refreshTokenEntity = RefreshToken(
                token = refreshToken,
                username = userDetails.username,
                expiresAt = expiresAt
            )
            refreshTokenRepository.save(refreshTokenEntity)

            return AuthResponse(accessToken, refreshToken)
        } catch (ex: BadCredentialsException) {
            throw ex // TODO: throw a custom exception?
        }
    }

    fun refreshToken(refreshRequest: RefreshRequest): AuthResponse {
        val username = validateRefreshToken(refreshRequest.refreshToken)

        val storedToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw BadCredentialsException("Invalid refresh token")

        if (storedToken.revoked) {
            throw BadCredentialsException("Refresh token revoked")
        }

        if (storedToken.expiresAt.isBefore(Instant.now())) {
            // token expired in the DB
            refreshTokenRepository.delete(storedToken) // cleanup
            throw BadCredentialsException("Refresh token expired")
        }

        refreshTokenRepository.delete(storedToken)

        val newAccessToken = generateAccessToken(username)
        val newRefreshTokenStr = generateRefreshToken(username)

        val expiresAt = Instant.now().plusMillis(refreshExpirationMs)
        val newRefreshTokenEntity = RefreshToken(
            token = newRefreshTokenStr,
            username = username,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(newRefreshTokenEntity)

        return AuthResponse(newAccessToken, newRefreshTokenStr)
    }

    fun logout(refreshToken: String) {
        val storedToken = refreshTokenRepository.findByToken(refreshToken)
            ?: throw BadCredentialsException("Invalid token")

        // removing it for now, but a revoked flag can be added later
        refreshTokenRepository.delete(storedToken)
        // storedToken.revoked = true
        // refreshTokenRepository.save(storedToken)
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
}
