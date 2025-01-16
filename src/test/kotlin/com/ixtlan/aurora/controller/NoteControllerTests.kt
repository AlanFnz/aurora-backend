package com.ixtlan.aurora.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class NoteControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var noteService: NoteService
    private lateinit var folderService: FolderService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        noteService = mockk(relaxed = true)
        folderService = mockk(relaxed = true)
        objectMapper = ObjectMapper()
        val noteController = NoteController(noteService, folderService)
        mockMvc = MockMvcBuilders.standaloneSetup(noteController).build()
    }

    @Test
    fun `get all notes returns list of notes`() {
        val folder = Folder(id = 1, folderName = "Test Folder")
        val note = Note(
            id = 1,
            title = "Test Note",
            content = "Test Content",
            modifiedDate = System.currentTimeMillis(),
            folder = folder
        )

        every { noteService.getAllNotes() } returns listOf(note)

        mockMvc.perform(get("/api/notes"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("Test Note"))
            .andExpect(jsonPath("$[0].content").value("Test Content"))
            .andExpect(jsonPath("$[0].folderId").value(1))
    }

    @Test
    fun `get note by id returns note when exists`() {
        val folder = Folder(id = 1, folderName = "Test Folder")
        val note = Note(
            id = 1,
            title = "Test Note",
            content = "Test Content",
            modifiedDate = System.currentTimeMillis(),
            folder = folder
        )

        every { noteService.getNoteById(1) } returns note

        mockMvc.perform(get("/api/notes/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Note"))
            .andExpect(jsonPath("$.folderId").value(1))
    }

    @Test
    fun `create note returns created note`() {
        val folder = Folder(id = 1, folderName = "Test Folder")
        val noteRequest = NoteRequest(
            title = "New Note",
            content = "New Content",
            folderId = 1
        )

        val createdNote = Note(
            id = 1,
            title = "New Note",
            content = "New Content",
            modifiedDate = System.currentTimeMillis(),
            folder = folder
        )

        every { noteService.createNote(any()) } returns createdNote

        mockMvc.perform(
            post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("New Note"))
            .andExpect(jsonPath("$.content").value("New Content"))
            .andExpect(jsonPath("$.folderId").value(1))
    }

    @Test
    fun `update note returns updated note when exists`() {
        val folder = Folder(id = 1, folderName = "Test Folder")
        val noteRequest = NoteRequest(
            title = "Updated Note",
            content = "Updated Content",
            folderId = 1
        )

        val updatedNote = Note(
            id = 1,
            title = "Updated Note",
            content = "Updated Content",
            modifiedDate = System.currentTimeMillis(),
            folder = folder
        )

        every { folderService.getFolderById(1) } returns folder
        every { noteService.updateNote(1, any()) } returns updatedNote

        mockMvc.perform(
            put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("Updated Note"))
            .andExpect(jsonPath("$.content").value("Updated Content"))
            .andExpect(jsonPath("$.folderId").value(1))
    }

    @Test
    fun `delete note returns no content`() {
        mockMvc.perform(delete("/api/notes/1"))
            .andExpect(status().isNoContent)

        verify { noteService.deleteNote(1) }
    }

    @Test
    fun `get note by id returns not found when note doesn't exist`() {
        every { noteService.getNoteById(999) } returns null

        mockMvc.perform(get("/api/notes/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `update note returns bad request when folder doesn't exist`() {
        val noteRequest = NoteRequest(
            title = "Updated Note",
            content = "Updated Content",
            folderId = 999
        )

        every { folderService.getFolderById(999) } returns null

        mockMvc.perform(
            put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noteRequest))
        )
            .andExpect(status().isBadRequest)
    }
}