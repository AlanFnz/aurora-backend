package com.ixtlan.aurora.entity

import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class NoteEntityTest {

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var folderRepository: FolderRepository

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Test
    fun `should save and retrieve note`() {
        val testUser = User(
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        testEntityManager.persist(testUser)

        val note = Note(
            title = "Meeting Notes",
            content = "Some content",
            modifiedDate = System.currentTimeMillis(),
            folder = null,
            user = testUser
        )
        val savedNote = noteRepository.save(note)

        val retrievedNote = noteRepository.findById(savedNote.id).orElse(null)
        assertNotNull(retrievedNote)
        assertEquals("Meeting Notes", retrievedNote?.title)
        assertEquals("testUser", retrievedNote?.user?.username)
    }

    @Test
    fun `should associate note with folder`() {
        val testUser = User(
            username = "testUser2",
            password = "secret2",
            email = "test2@test.com",
            firstName = "Test2",
            lastName = "User2",
            roles = mutableSetOf("ROLE_USER")
        )
        testEntityManager.persist(testUser)

        val folder = Folder(folderName = "Important", user = testUser)
        folderRepository.save(folder)

        val note = Note(
            title = "Test Note",
            content = "Note content",
            modifiedDate = System.currentTimeMillis(),
            folder = folder,
            user = testUser
        )
        val savedNote = noteRepository.save(note)

        val retrievedNote = noteRepository.findById(savedNote.id).orElse(null)
        assertNotNull(retrievedNote)
        assertEquals("Important", retrievedNote?.folder?.folderName)
        assertEquals("testUser2", retrievedNote?.user?.username)
    }
}
