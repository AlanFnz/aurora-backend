package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class FolderServiceTest {

    @Mock
    private lateinit var folderRepository: FolderRepository

    @Mock
    private lateinit var noteRepository: NoteRepository

    @InjectMocks
    private lateinit var folderService: FolderService

    init {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `getAllFolders should return all folders`() {
        val folders = listOf(
            Folder(id = 1, folderName = "Work", notes = mutableListOf()),
            Folder(id = 2, folderName = "Personal", notes = mutableListOf())
        )
        `when`(folderRepository.findAll()).thenReturn(folders)

        val result = folderService.getAllFolders()

        assertEquals(2, result.size)
        assertEquals("Work", result[0].folderName)
        assertEquals("Personal", result[1].folderName)
        verify(folderRepository).findAll()
    }

    @Test
    fun `getFolderById should return folder if it exists`() {
        val folder = Folder(id = 1, folderName = "Work", notes = mutableListOf())
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.of(folder))

        val result = folderService.getFolderById(1)

        assertNotNull(result)
        assertEquals("Work", result?.folderName)
        verify(folderRepository).findById(1)
    }

    @Test
    fun `getFolderById should return null if folder does not exist`() {
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.empty())

        val result = folderService.getFolderById(1)

        assertNull(result)
        verify(folderRepository).findById(1)
    }

    @Test
    fun `createFolder should save and return the folder`() {
        val folder = Folder(folderName = "New Folder", notes = mutableListOf())
        val savedFolder = folder.copy(id = 1)
        `when`(folderRepository.save(folder)).thenReturn(savedFolder)

        val result = folderService.createFolder(folder)

        assertEquals(1, result.id)
        assertEquals("New Folder", result.folderName)
        verify(folderRepository).save(folder)
    }

    @Test
    fun `updateFolder should update and return the folder`() {
        val existingFolder = Folder(id = 1, folderName = "Old Name", notes = mutableListOf())
        val updatedFolder = existingFolder.copy(folderName = "New Name")
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.of(existingFolder))
        `when`(folderRepository.save(any(Folder::class.java))).thenReturn(updatedFolder)

        val result = folderService.updateFolder(1, updatedFolder)

        assertEquals("New Name", result.folderName)
        verify(folderRepository).findById(1)
        verify(folderRepository).save(any(Folder::class.java))
    }

    @Test
    fun `deleteFolder should delete folder if cascadeDelete is true`() {
        val folder = Folder(
            id = 1,
            folderName = "Work",
            notes = mutableListOf(
                Note(title = "Task 1", modifiedDate = System.currentTimeMillis())
            )
        )
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.of(folder))

        folderService.deleteFolder(1, cascadeDelete = true)

        verify(noteRepository).deleteAll(folder.notes)
        verify(folderRepository).delete(folder)
    }

    @Test
    fun `deleteFolder should throw exception if folder contains notes and cascadeDelete is false`() {
        val folder = Folder(
            id = 1,
            folderName = "Work",
            notes = mutableListOf(
                Note(title = "Task 1", modifiedDate = System.currentTimeMillis())
            )
        )
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.of(folder))

        assertThrows<IllegalStateException> {
            folderService.deleteFolder(1, cascadeDelete = false)
        }
        verify(noteRepository, never()).deleteAll(anyList())
        verify(folderRepository, never()).delete(folder)
    }

    @Test
    fun `deleteFolder should delete folder if it contains no notes`() {
        val folder = Folder(id = 1, folderName = "Work", notes = mutableListOf())
        `when`(folderRepository.findById(1)).thenReturn(java.util.Optional.of(folder))

        folderService.deleteFolder(1, cascadeDelete = false)

        verify(noteRepository, never()).deleteAll(anyList())
        verify(folderRepository).delete(folder)
    }
}
