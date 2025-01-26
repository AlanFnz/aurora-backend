package com.ixtlan.aurora.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val secretKey = "mysecretkeymysecretkeymysecretkey12" // 32+ chars for HMAC-SHA256

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val claims: Claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.toByteArray()))
                    .build()
                    .parseClaimsJws(token)
                    .body

                val username = claims.subject
                if (username != null) {
                    val userDetails: UserDetails = customUserDetailsService.loadUserByUsername(username)
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    SecurityContextHolder.getContext().authentication = authToken
                }
            } catch (ex: Exception) {
                // token invalid or expired
                // TODO: do something
            }
        }
        chain.doFilter(request, response)
    }
}
