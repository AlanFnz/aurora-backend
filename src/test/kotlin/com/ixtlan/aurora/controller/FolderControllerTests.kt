package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.model.FolderRequest
import com.ixtlan.aurora.service.FolderService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FolderControllerTest {

    @Mock
    private lateinit var folderService: FolderService

    @InjectMocks
    private lateinit var folderController: FolderController

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(folderController).setControllerAdvice().build()
        objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }

    @Test
    fun `getFolders should return list of folders`() {
        val folder = Folder(
            id = 1L, folderName = "Test Folder", notes = mutableListOf(
                Note(
                    id = 1L, title = "Test Note", content = "Test Content", modifiedDate = System.currentTimeMillis()
                )
            )
        )

        `when`(folderService.getAllFolders()).thenReturn(listOf(folder))

        mockMvc.perform(get("/api/folders")).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].folderName").value("Test Folder"))
            .andExpect(jsonPath("$[0].notes[0].id").value(1L))
            .andExpect(jsonPath("$[0].notes[0].title").value("Test Note"))
    }

    @Test
    fun `getFolderById should return folder when exists`() {
        val folder = Folder(
            id = 1L, folderName = "Test Folder", notes = mutableListOf()
        )

        `when`(folderService.getFolderById(1L)).thenReturn(folder)

        mockMvc.perform(get("/api/folders/1")).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.folderName").value("Test Folder"))
    }

    @Test
    fun `getFolderById should return 404 when folder doesn't exist`() {
        `when`(folderService.getFolderById(1L)).thenReturn(null)

        mockMvc.perform(get("/api/folders/1")).andExpect(status().isNotFound)
    }

    @Test
    fun `createFolder should create and return new folder`() {
        val folderRequest = FolderRequest("New Folder")
        val createdFolder = Folder(id = 1L, folderName = "New Folder", notes = mutableListOf())

        val expectedFolder = Folder(folderName = "New Folder", notes = mutableListOf())
        `when`(folderService.createFolder(expectedFolder)).thenReturn(createdFolder)

        mockMvc.perform(
            post("/api/folders").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(folderRequest))
        ).andExpect(status().isOk).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.folderName").value("New Folder"))
    }

    @Test
    fun `updateFolder should update and return folder`() {
        val folderRequest = FolderRequest("Updated Folder")
        val updatedFolder = Folder(id = 1L, folderName = "Updated Folder", notes = mutableListOf())

        val expectedFolder = Folder(folderName = "Updated Folder", notes = mutableListOf())
        `when`(folderService.updateFolder(1L, expectedFolder)).thenReturn(updatedFolder)

        mockMvc.perform(
            put("/api/folders/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(folderRequest))
        ).andExpect(status().isOk).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.folderName").value("Updated Folder"))
    }

    @Test
    fun `deleteFolder should return no content`() {
        doNothing().`when`(folderService).deleteFolder(1L, false)

        mockMvc.perform(delete("/api/folders/1")).andExpect(status().isNoContent)
    }

    @Test
    fun `deleteFolder should handle IllegalArgumentException`() {
        doThrow(IllegalArgumentException("Folder not found")).`when`(folderService).deleteFolder(1L, false)

        mockMvc.perform(delete("/api/folders/1")).andExpect(status().isBadRequest)
            .andExpect(content().string("Folder not found"))
    }

    @Test
    fun `deleteFolder should handle IllegalStateException`() {
        doThrow(IllegalStateException("Cannot delete folder with existing notes")).`when`(folderService)
            .deleteFolder(1L, false)

        mockMvc.perform(delete("/api/folders/1")).andExpect(status().isBadRequest)
            .andExpect(content().string("Cannot delete folder with existing notes"))
    }
}