package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import com.ixtlan.aurora.security.AuthenticationUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import java.util.*

class NoteServiceTest {

    private val noteRepository = mock(NoteRepository::class.java)
    private val folderRepository = mock(FolderRepository::class.java)
    private val authenticationUtil = mock(AuthenticationUtil::class.java)

    private val noteService = NoteService(noteRepository, folderRepository)

    @Test
    fun `getAllNotes should return all notes`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        val notes = listOf(
            Note(1, "Note 1", "Content 1", System.currentTimeMillis(), null, currentUser),
            Note(2, "Note 2", "Content 2", System.currentTimeMillis(), null, currentUser)
        )
        `when`(noteRepository.findAll()).thenReturn(notes)

        val result = noteService.getAllNotes()

        assertEquals(2, result.size)
        assertEquals("Note 1", result[0].title)
        assertEquals("Note 2", result[1].title)

        verify(noteRepository).findAll()
    }

    @Test
    fun `getNoteById should return note if found`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        val note = Note(1, "Test Note", "Test Content", System.currentTimeMillis(), null, currentUser)
        `when`(noteRepository.findById(1)).thenReturn(Optional.of(note))

        val result = noteService.getNoteById(1)

        assertNotNull(result)
        assertEquals("Test Note", result?.title)
        verify(noteRepository).findById(1)
    }

    @Test
    fun `getNoteById should return null if not found`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        `when`(noteRepository.findById(1)).thenReturn(Optional.empty())

        val result = noteService.getNoteById(1)

        assertNull(result)
        verify(noteRepository).findById(1)
    }

    @Test
    fun `createNote should use existing folder if folderId is provided`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        val existingFolder = Folder(id = 2, folderName = "Personal", user = currentUser)
        val savedNote = Note(
            id = 20,
            title = "Test Note",
            content = "Test Content",
            modifiedDate = System.currentTimeMillis(),
            folder = existingFolder,
            user = currentUser
        )

        `when`(folderRepository.findByIdAndUser(2, currentUser)).thenReturn(existingFolder)
        `when`(noteRepository.save(any(Note::class.java))).thenReturn(savedNote)

        val result = noteService.createNote(
            title = "Test Note", content = "Test Content", folderId = 2, user = currentUser
        )

        assertNotNull(result)
        assertEquals(20, result.id)
        assertEquals("Personal", result.folder?.folderName)

        verify(folderRepository).findByIdAndUser(2, currentUser)
        verify(folderRepository, never()).save(any(Folder::class.java))
        verify(noteRepository).save(any(Note::class.java))
    }

    @Test
    fun `updateNote should update an existing note`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        val existingNote = Note(
            id = 1,
            title = "Old Title",
            content = "Old Content",
            modifiedDate = System.currentTimeMillis(),
            folder = null,
            user = currentUser
        )
        val updatedNote = existingNote.copy(title = "New Title", content = "New Content")

        `when`(noteRepository.findByIdAndUser(1, currentUser)).thenReturn(existingNote)

        `when`(noteRepository.save(any(Note::class.java))).thenReturn(updatedNote)

        val result = noteService.updateNote(1, updatedNote, currentUser)

        assertEquals("New Title", result.title)
        assertEquals("New Content", result.content)
        verify(noteRepository).findByIdAndUser(1, currentUser)
        verify(noteRepository).save(any(Note::class.java))
    }

    @Test
    fun `deleteNote should delete note by id`() {
        val currentUser = User(
            id = 1,
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        `when`(authenticationUtil.getCurrentUser()).thenReturn(currentUser)

        doNothing().`when`(noteRepository).deleteById(1)

        noteService.deleteNote(1)

        verify(noteRepository).deleteById(1)
    }
}
