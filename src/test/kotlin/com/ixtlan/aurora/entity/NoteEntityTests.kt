package com.ixtlan.aurora.entity

import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class NoteEntityTest {

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var folderRepository: FolderRepository

    @Test
    fun `should save and retrieve note`() {
        val note = Note(title = "Meeting Notes", modifiedDate = System.currentTimeMillis())
        val savedNote = noteRepository.save(note)

        val retrievedNote = noteRepository.findById(savedNote.id).orElse(null)
        assertNotNull(retrievedNote)
        assertEquals("Meeting Notes", retrievedNote?.title)
    }

    @Test
    fun `should associate note with folder`() {
        val folder = Folder(folderName = "Important")
        folderRepository.save(folder)

        val note = Note(title = "Test Note", modifiedDate = System.currentTimeMillis(), folder = folder)
        val savedNote = noteRepository.save(note)

        val retrievedNote = noteRepository.findById(savedNote.id).orElse(null)
        assertNotNull(retrievedNote)
        assertEquals("Important", retrievedNote?.folder?.folderName)
    }
}
