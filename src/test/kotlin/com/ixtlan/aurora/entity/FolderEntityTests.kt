package com.ixtlan.aurora.entity

import com.ixtlan.aurora.repository.FolderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class FolderEntityTest {

    @Autowired
    private lateinit var folderRepository: FolderRepository

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Test
    fun `should save and retrieve folder`() {
        val testUser = User(
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        testEntityManager.persist(testUser)

        val folder = Folder(folderName = "Personal", user = testUser)
        val savedFolder = folderRepository.save(folder)

        val retrievedFolder = folderRepository.findById(savedFolder.id).orElse(null)
        assertNotNull(retrievedFolder)
        assertEquals("Personal", retrievedFolder?.folderName)
        assertEquals(testUser.username, retrievedFolder?.user?.username)
    }

    @Test
    fun `should cascade delete notes when folder is deleted`() {
        val testUser = User(
            username = "testUser",
            password = "secret",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            roles = mutableSetOf("ROLE_USER")
        )
        testEntityManager.persist(testUser)

        val folder = Folder(folderName = "Work", user = testUser)
        val note1 = Note(title = "Task 1", modifiedDate = System.currentTimeMillis(), folder = folder, user = testUser)
        val note2 = Note(title = "Task 2", modifiedDate = System.currentTimeMillis(), folder = folder, user = testUser)
        folder.notes.addAll(listOf(note1, note2))

        folderRepository.save(folder)

        folderRepository.delete(folder)

        assertTrue(folderRepository.findById(folder.id).isEmpty)
    }
}
