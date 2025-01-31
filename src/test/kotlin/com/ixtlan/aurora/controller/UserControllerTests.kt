package com.ixtlan.aurora.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ixtlan.aurora.model.LoginRequest
import com.ixtlan.aurora.model.UserRegistrationRequest
import com.ixtlan.aurora.model.UserResponse
import com.ixtlan.aurora.security.CustomUserDetailsService
import com.ixtlan.aurora.service.UserService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.ninjasquad.springmockk.MockkBean
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.authentication.AuthenticationManager

@WebMvcTest(
    controllers = [UserController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var userService: UserService

    @MockkBean
    lateinit var authenticationManager: AuthenticationManager

    @MockkBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `test registerUser - success`() {
        val request = UserRegistrationRequest(
            username = "testUser", password = "password", email = "test@test.com", firstName = "Test", lastName = "User"
        )
        val response = UserResponse(
            id = 1,
            username = "testUser",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = setOf("ROLE_USER")
        )

        every { userService.registerUser(request) } returns response

        val jsonBody = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonBody)
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testUser")).andExpect(jsonPath("$.email").value("test@test.com"))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
    }

    @Test
    fun `test registerUser - bad request`() {
        val request = UserRegistrationRequest(
            username = "", password = "", email = "", firstName = "", lastName = ""
        )

        every { userService.registerUser(request) } throws IllegalArgumentException("Invalid request")

        val jsonBody = objectMapper.writeValueAsString(request)

        mockMvc.perform(
            post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonBody)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `test getUser - found`() {
        val userResponse = UserResponse(
            id = 1,
            username = "testUser",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = setOf("ROLE_USER")
        )

        every { userService.getUserById(1) } returns userResponse

        mockMvc.perform(get("/api/users/{id}", 1)).andExpect(status().isOk).andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testUser")).andExpect(jsonPath("$.email").value("test@test.com"))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
    }

    @Test
    fun `test getUser - not found`() {
        every { userService.getUserById(999) } returns null

        mockMvc.perform(get("/api/users/{id}", 999)).andExpect(status().isNotFound)
    }

    @Test
    fun `test login - success`() {
        val request = LoginRequest(username = "testUser", password = "password")
        val authentication: Authentication = mockk()
        every { authentication.isAuthenticated } returns true

        every {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
        } returns authentication

        mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk).andExpect(jsonPath("$.token").isString)
    }
}