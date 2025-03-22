package com.ixtlan.aurora.security.config

import com.ixtlan.aurora.security.CustomAuthenticationEntryPoint
import com.ixtlan.aurora.security.CustomUserDetailsService
import com.ixtlan.aurora.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // existing endpoints
                auth.requestMatchers("/api/users/register").permitAll()
                auth.requestMatchers("/api/users/login").permitAll()

                // new auth endpoints
                auth.requestMatchers("/api/auth/login").permitAll()
                auth.requestMatchers("/api/auth/refresh").permitAll()

                // everything else requires authentication
                auth.anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(customAuthenticationEntryPoint) }
            .addFilterBefore(
                JwtAuthenticationFilter(customUserDetailsService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}
