package com.ixtlan.aurora.controller

import io.mockk.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.model.FolderRequest
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.service.FolderService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import java.time.Instant

@WebMvcTest(FolderController::class)
@AutoConfigureMockMvc(addFilters = false)
class FolderControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var folderService: FolderService

    @MockkBean
    lateinit var authenticationUtil: AuthenticationUtil

    @Test
    fun `test getFolders should return a list of folders`() {
        // Given
        val mockUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        val mockFolders = listOf(
            Folder(
                id = 10,
                folderName = "Folder A",
                notes = mutableListOf(
                    Note(
                        id = 100,
                        title = "Note Title",
                        content = "This is the content of the note",
                        modifiedDate = Instant.now().toEpochMilli(),
                        folder = null,
                        user = mockUser
                    )
                ),
                user = mockUser
            ),
            Folder(
                id = 11,
                folderName = "Folder B",
                notes = mutableListOf(),
                user = mockUser
            )
        )

        // When
        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.getAllFolders(mockUser) } returns mockFolders

        // Then
        mockMvc.perform(get("/api/folders"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].folderName").value("Folder A"))
            .andExpect(jsonPath("$[0].notes[0].title").value("Note Title"))
            .andExpect(jsonPath("$[1].id").value(11))
            .andExpect(jsonPath("$[1].folderName").value("Folder B"))
    }

    @Test
    fun `test getFolderById - found`() {
        // Given
        val mockUser = User(
            id = 2,
            username = "anotherUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        val mockFolder = Folder(
            id = 99,
            folderName = "Special Folder",
            user = mockUser
        )

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.getFolderById(99, mockUser) } returns mockFolder

        // When & Then
        mockMvc.perform(get("/api/folders/{id}", 99))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.folderName").value("Special Folder"))
    }

    @Test
    fun `test getFolderById - not found`() {
        // Given
        val mockUser = User(
            id = 3,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.getFolderById(any(), mockUser) } returns null

        // When & Then
        mockMvc.perform(get("/api/folders/{id}", 999))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test createFolder`() {
        // Given
        val mockUser = User(
            id = 4,
            username = "createUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        val folderRequest = FolderRequest(folderName = "New Folder")
        val mockFolder = Folder(
            id = 123,
            folderName = "New Folder",
            user = mockUser
        )

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.createFolder(any(), mockUser) } returns mockFolder

        // When
        val jsonBody = objectMapper.writeValueAsString(folderRequest)

        // Then
        mockMvc.perform(
            post("/api/folders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(123))
            .andExpect(jsonPath("$.folderName").value("New Folder"))
    }

    @Test
    fun `test updateFolder`() {
        // Given
        val mockUser = User(
            id = 5,
            username = "updateUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        val folderRequest = FolderRequest(folderName = "Updated Folder Name")

        val existingFolder = Folder(
            id = 55,
            folderName = "Old Folder Name",
            user = mockUser
        )
        val updatedFolder = existingFolder.copy(folderName = "Updated Folder Name")

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.updateFolder(55, any(), mockUser) } returns updatedFolder

        // When
        val jsonBody = objectMapper.writeValueAsString(folderRequest)

        // Then
        mockMvc.perform(
            put("/api/folders/{id}", 55)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(55))
            .andExpect(jsonPath("$.folderName").value("Updated Folder Name"))
    }

    @Test
    fun `test deleteFolder - success`() {
        // Given
        val mockUser = User(
            id = 6,
            username = "deleteUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser
        // We don't need to return anything from delete, just ensure no exception is thrown
        every { folderService.deleteFolder(66, false, mockUser) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/folders/{id}", 66))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `test deleteFolder - folder has notes and cascadeDelete is false`() {
        // Given
        val mockUser = User(
            id = 7,
            username = "deleteUserWithNotes",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser
        every {
            folderService.deleteFolder(
                77,
                false,
                mockUser
            )
        } throws IllegalStateException("Cannot delete folder with existing notes")

        // When & Then
        mockMvc.perform(delete("/api/folders/{id}", 77))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Cannot delete folder with existing notes"))
    }
}
