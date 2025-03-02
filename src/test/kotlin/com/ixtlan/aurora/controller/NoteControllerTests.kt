package com.ixtlan.aurora.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.exception.FolderNotFoundException
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.security.CustomUserDetailsService
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
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
    controllers = [NoteController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var noteService: NoteService

    @MockkBean
    lateinit var folderService: FolderService

    @MockkBean
    lateinit var authenticationUtil: AuthenticationUtil

    @MockkBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `test getNotes should return a list of NoteResponse`() {
        val mockFolder = Folder(
            id = 10, folderName = "Folder A", user = User(
                id = 1,
                username = "testUser",
                password = "secret",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                roles = mutableSetOf("ROLE_USER")
            )
        )
        val mockNote1 = Note(
            id = 100,
            title = "Title1",
            content = "Content1",
            modifiedDate = Instant.now().toEpochMilli(),
            folder = mockFolder,
            user = mockFolder.user
        )
        val mockNote2 = Note(
            id = 101,
            title = "Title2",
            content = null,
            modifiedDate = Instant.now().toEpochMilli(),
            folder = null,
            user = mockFolder.user
        )

        every { noteService.getAllNotes() } returns listOf(mockNote1, mockNote2)
        mockMvc.perform(get("/api/notes")).andExpect(status().isOk).andExpect(jsonPath("$[0].id").value(100))
            .andExpect(jsonPath("$[0].title").value("Title1")).andExpect(jsonPath("$[0].folderId").value(10))
            .andExpect(jsonPath("$[1].id").value(101)).andExpect(jsonPath("$[1].title").value("Title2"))
    }

    @Test
    fun `test getNoteById - found`() {
        val mockFolder = Folder(
            id = 10, folderName = "Folder A", user = User(
                id = 1,
                username = "testUser",
                password = "secret",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                roles = mutableSetOf("ROLE_USER")
            )
        )
        val mockNote = Note(
            id = 200,
            title = "Some Title",
            content = "Some Content",
            modifiedDate = 123456789,
            folder = mockFolder,
            user = mockFolder.user
        )

        every { noteService.getNoteById(200) } returns mockNote
        mockMvc.perform(get("/api/notes/{id}", 200)).andExpect(status().isOk).andExpect(jsonPath("$.id").value(200))
            .andExpect(jsonPath("$.title").value("Some Title")).andExpect(jsonPath("$.folderId").value(10))
    }

    @Test
    fun `test getNoteById - not found`() {
        every { noteService.getNoteById(999) } returns null
        mockMvc.perform(get("/api/notes/{id}", 999)).andExpect(status().isNotFound)
    }

    @Test
    fun `test createNote`() {
        val mockUser = User(
            id = 4,
            username = "createUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser

        val mockFolder = Folder(id = 10, folderName = "Existing Folder", user = mockUser)

        every { folderService.getFolderById(10, mockUser) } returns mockFolder

        val noteRequest = NoteRequest(title = "New Title", content = "New Content", folderId = 10)
        val mockNote = Note(
            id = 300,
            title = "New Title",
            content = "New Content",
            modifiedDate = 123456L,
            folder = mockFolder,
            user = mockUser
        )

        every {
            noteService.createNote(
                title = "New Title", content = "New Content", folderId = 10, user = mockUser
            )
        } returns mockNote

        val jsonBody = objectMapper.writeValueAsString(noteRequest)

        mockMvc.perform(
            post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(jsonBody)
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(300))
            .andExpect(jsonPath("$.title").value("New Title")).andExpect(jsonPath("$.folderId").value(10))
    }

    @Test
    fun `test updateNote - success`() {
        val mockUser = User(
            id = 5,
            username = "updateUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser

        val existingNote = Note(id = 400, title = "Old Title", content = "Old Content", user = mockUser, modifiedDate = 0)
        every { noteService.getNoteByIdAndUser(400, mockUser) } returns existingNote

        val updatedNote = existingNote.copy(
            title = "Updated Title",
            content = "Updated Content",
            modifiedDate = 999999L,
            folder = Folder(id = 10, folderName = "Some Folder", user = mockUser)
        )
        every { noteService.updateNote(400, any(), mockUser) } returns updatedNote

        val requestBody = NoteRequest(title = "Updated Title", content = "Updated Content", folderId = 10)
        val jsonBody = objectMapper.writeValueAsString(requestBody)

        mockMvc.perform(
            put("/api/notes/{id}", 400)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(400))
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.folderId").value(10))
    }

    @Test
    fun `test updateNote - folder not found`() {
        val mockUser = User(
            id = 6,
            username = "noFolderUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        every { authenticationUtil.getCurrentUser() } returns mockUser

        val existingNote = Note(id = 777, title = "Something", user = mockUser, modifiedDate = 0)
        every { noteService.getNoteByIdAndUser(777, mockUser) } returns existingNote

        every { noteService.updateNote(777, any(), mockUser) } throws FolderNotFoundException("Folder not found")

        val badRequestBody = NoteRequest(title = "Bad Title", content = "Bad Content", folderId = 999)
        val jsonBody = objectMapper.writeValueAsString(badRequestBody)

        mockMvc.perform(
            put("/api/notes/{id}", 777)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        )
            // Because we catch FolderNotFoundException in the controller => 400
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test deleteNoteById - success`() {
        every { noteService.deleteNote(123) } just runs
        mockMvc.perform(delete("/api/notes/{id}", 123)).andExpect(status().isNoContent)
    }
}