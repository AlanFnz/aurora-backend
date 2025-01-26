package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
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

    private val testUser = User(
        id = 1,
        username = "testUser",
        password = "password",
        email = "test@test.com",
        firstName = "Test",
        lastName = "User",
        roles = mutableSetOf("ROLE_USER")
    )

    init {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `getAllFolders should return all folders`() {
        val folders = listOf(
            Folder(id = 1, folderName = "Work", notes = mutableListOf(), user = testUser),
            Folder(id = 2, folderName = "Personal", notes = mutableListOf(), user = testUser)
        )
        `when`(folderRepository.findAllByUser(testUser)).thenReturn(folders)

        val result = folderService.getAllFolders(testUser)

        assertEquals(2, result.size)
        assertEquals("Work", result[0].folderName)
        assertEquals("Personal", result[1].folderName)
        verify(folderRepository).findAllByUser(testUser)
    }

    @Test
    fun `getFolderById should return folder if it exists`() {
        val folder = Folder(id = 1, folderName = "Work", notes = mutableListOf(), user = testUser)
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(folder)

        val result = folderService.getFolderById(1, testUser)

        assertNotNull(result)
        assertEquals("Work", result?.folderName)
        verify(folderRepository).findByIdAndUser(1, testUser)
    }

    @Test
    fun `getFolderById should return null if folder does not exist`() {
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(null)

        val result = folderService.getFolderById(1, testUser)

        assertNull(result)
        verify(folderRepository).findByIdAndUser(1, testUser)
    }

    @Test
    fun `createFolder should save and return the folder`() {
        val folder = Folder(folderName = "New Folder", notes = mutableListOf(), user = testUser)
        val savedFolder = folder.copy(id = 1)
        `when`(folderRepository.save(folder)).thenReturn(savedFolder)

        val result = folderService.createFolder(folder, testUser)

        assertEquals(1, result.id)
        assertEquals("New Folder", result.folderName)
        verify(folderRepository).save(folder)
    }

    @Test
    fun `updateFolder should update and return the folder`() {
        val existingFolder = Folder(id = 1, folderName = "Old Name", notes = mutableListOf(), user = testUser)
        val updatedFolder = existingFolder.copy(folderName = "New Name")
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(existingFolder)
        `when`(folderRepository.save(any(Folder::class.java))).thenReturn(updatedFolder)

        val result = folderService.updateFolder(1, updatedFolder, testUser)

        assertEquals("New Name", result.folderName)
        verify(folderRepository).findByIdAndUser(1, testUser)
        verify(folderRepository).save(any(Folder::class.java))
    }

    @Test
    fun `deleteFolder should delete folder if cascadeDelete is true`() {
        val folder = Folder(
            id = 1,
            folderName = "Work",
            notes = mutableListOf(
                Note(title = "Task 1", modifiedDate = System.currentTimeMillis(), folder = null, user = testUser)
            ),
            user = testUser
        )
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(folder)

        folderService.deleteFolder(1, cascadeDelete = true, user = testUser)

        verify(noteRepository).deleteAll(folder.notes)
        verify(folderRepository).delete(folder)
    }

    @Test
    fun `deleteFolder should throw exception if folder contains notes and cascadeDelete is false`() {
        val folder = Folder(
            id = 1,
            folderName = "Work",
            notes = mutableListOf(
                Note(title = "Task 1", modifiedDate = System.currentTimeMillis(), folder = null, user = testUser)
            ),
            user = testUser
        )
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(folder)

        assertThrows<IllegalStateException> {
            folderService.deleteFolder(1, cascadeDelete = false, user = testUser)
        }
        verify(noteRepository, never()).deleteAll(anyList())
        verify(folderRepository, never()).delete(folder)
    }

    @Test
    fun `deleteFolder should delete folder if it contains no notes`() {
        val folder = Folder(id = 1, folderName = "Work", notes = mutableListOf(), user = testUser)
        `when`(folderRepository.findByIdAndUser(1, testUser)).thenReturn(folder)

        folderService.deleteFolder(1, cascadeDelete = false, user = testUser)

        verify(noteRepository, never()).deleteAll(anyList())
        verify(folderRepository).delete(folder)
    }
}
