package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

class NoteServiceTest {

    private val noteRepository: NoteRepository = mock(NoteRepository::class.java)
    private val folderRepository: FolderRepository = mock(FolderRepository::class.java)
    private val noteService = NoteService(noteRepository, folderRepository)

    @Test
    fun `getAllNotes should return all notes`() {
        val notes = listOf(
            Note(1, "Note 1", "Content 1", System.currentTimeMillis(), null),
            Note(2, "Note 2", "Content 2", System.currentTimeMillis(), null)
        )
        `when`(noteRepository.findAll()).thenReturn(notes)

        val result = noteService.getAllNotes()

        assertEquals(2, result.size)
        assertEquals("Note 1", result[0].title)
        verify(noteRepository).findAll()
    }

    @Test
    fun `getNoteById should return note if found`() {
        val note = Note(1, "Test Note", "Test Content", System.currentTimeMillis(), null)
        `when`(noteRepository.findById(1)).thenReturn(Optional.of(note))

        val result = noteService.getNoteById(1)

        assertNotNull(result)
        assertEquals("Test Note", result?.title)
        verify(noteRepository).findById(1)
    }

    @Test
    fun `getNoteById should return null if not found`() {
        `when`(noteRepository.findById(1)).thenReturn(Optional.empty())

        val result = noteService.getNoteById(1)

        assertNull(result)
        verify(noteRepository).findById(1)
    }

    @Test
    fun `createNote should create note with default folder if folder is null`() {
        val defaultFolder = Folder(1, "Default Folder")
        val noteWithoutFolder = Note(0, "Test Note", "Test Content", System.currentTimeMillis(), null)
        val savedNote = noteWithoutFolder.copy(id = 1, folder = defaultFolder)

        `when`(folderRepository.save(any(Folder::class.java))).thenReturn(defaultFolder)
        `when`(noteRepository.save(any(Note::class.java))).thenReturn(savedNote)

        val result = noteService.createNote(noteWithoutFolder)

        assertNotNull(result)
        assertEquals("Default Folder", result.folder?.folderName)
        verify(folderRepository).save(any(Folder::class.java))
        verify(noteRepository).save(any(Note::class.java))
    }

    @Test
    fun `createNote should use existing folder if folderId is provided`() {
        val existingFolder = Folder(2, "Personal")
        val noteWithFolder = Note(0, "Test Note", "Test Content", System.currentTimeMillis(), existingFolder)
        val savedNote = noteWithFolder.copy(id = 1)

        `when`(folderRepository.findById(2)).thenReturn(Optional.of(existingFolder))
        `when`(noteRepository.save(any(Note::class.java))).thenReturn(savedNote)

        val result = noteService.createNote(noteWithFolder)

        assertNotNull(result)
        assertEquals("Personal", result.folder?.folderName)
        verify(folderRepository).findById(2)
        verify(folderRepository, never()).save(any(Folder::class.java))
        verify(noteRepository).save(any(Note::class.java))
    }

    @Test
    fun `updateNote should update an existing note`() {
        val existingNote = Note(1, "Old Title", "Old Content", System.currentTimeMillis(), null)
        val updatedNote = existingNote.copy(title = "New Title", content = "New Content")
        `when`(noteRepository.findById(1)).thenReturn(Optional.of(existingNote))
        `when`(noteRepository.save(any(Note::class.java))).thenReturn(updatedNote)

        val result = noteService.updateNote(1, updatedNote)

        assertEquals("New Title", result.title)
        assertEquals("New Content", result.content)
        verify(noteRepository).findById(1)
        verify(noteRepository).save(any(Note::class.java))
    }

    @Test
    fun `deleteNote should delete note by id`() {
        doNothing().`when`(noteRepository).deleteById(1)

        noteService.deleteNote(1)

        verify(noteRepository).deleteById(1)
    }
}
