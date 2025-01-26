package com.ixtlan.aurora.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.model.FolderRequest
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.security.CustomUserDetailsService
import com.ixtlan.aurora.service.FolderService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.ninjasquad.springmockk.MockkBean
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import java.time.Instant

@WebMvcTest(
    controllers = [FolderController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Add this annotation
class FolderControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var folderService: FolderService

    @MockkBean
    lateinit var authenticationUtil: AuthenticationUtil

    @MockkBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `test getFolders should return a list of folders`() {
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
                id = 10, folderName = "Folder A", notes = mutableListOf(
                    Note(
                        id = 100,
                        title = "Note Title",
                        content = "This is the content of the note",
                        modifiedDate = Instant.now().toEpochMilli(),
                        folder = null,
                        user = mockUser
                    )
                ), user = mockUser
            ), Folder(
                id = 11, folderName = "Folder B", notes = mutableListOf(), user = mockUser
            )
        )

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.getAllFolders(mockUser) } returns mockFolders

        mockMvc.perform(get("/api/folders")).andExpect(status().isOk).andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].folderName").value("Folder A"))
            .andExpect(jsonPath("$[0].notes[0].title").value("Note Title")).andExpect(jsonPath("$[1].id").value(11))
            .andExpect(jsonPath("$[1].folderName").value("Folder B"))
    }

    @Test
    fun `test getFolderById - found`() {
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
            id = 99, folderName = "Special Folder", user = mockUser
        )

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.getFolderById(99, mockUser) } returns mockFolder

        mockMvc.perform(get("/api/folders/{id}", 99)).andExpect(status().isOk).andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.folderName").value("Special Folder"))
    }

    @Test
    fun `test getFolderById - not found`() {
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

        mockMvc.perform(get("/api/folders/{id}", 999)).andExpect(status().isNotFound)
    }

    @Test
    fun `test createFolder`() {
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
            id = 123, folderName = "New Folder", user = mockUser
        )

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.createFolder(any(), mockUser) } returns mockFolder

        val jsonBody = objectMapper.writeValueAsString(folderRequest)

        mockMvc.perform(
            post("/api/folders").contentType(MediaType.APPLICATION_JSON).content(jsonBody)
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(123))
            .andExpect(jsonPath("$.folderName").value("New Folder"))
    }

    @Test
    fun `test updateFolder`() {
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
            id = 55, folderName = "Old Folder Name", user = mockUser
        )
        val updatedFolder = existingFolder.copy(folderName = "Updated Folder Name")

        every { authenticationUtil.getCurrentUser() } returns mockUser
        every { folderService.updateFolder(55, any(), mockUser) } returns updatedFolder

        val jsonBody = objectMapper.writeValueAsString(folderRequest)

        mockMvc.perform(
            put("/api/folders/{id}", 55).contentType(MediaType.APPLICATION_JSON).content(jsonBody)
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(55))
            .andExpect(jsonPath("$.folderName").value("Updated Folder Name"))
    }

    @Test
    fun `test deleteFolder - success`() {
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
        every { folderService.deleteFolder(66, false, mockUser) } returns Unit

        mockMvc.perform(delete("/api/folders/{id}", 66)).andExpect(status().isNoContent)
    }

    @Test
    fun `test deleteFolder - folder has notes and cascadeDelete is false`() {
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
                77, false, mockUser
            )
        } throws IllegalStateException("Cannot delete folder with existing notes")

        mockMvc.perform(delete("/api/folders/{id}", 77)).andExpect(status().isBadRequest)
            .andExpect(content().string("Cannot delete folder with existing notes"))
    }
}