package com.ixtlan.aurora.entity

import com.ixtlan.aurora.repository.FolderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class FolderEntityTest {

    @Autowired
    private lateinit var folderRepository: FolderRepository

    @Test
    fun `should save and retrieve folder`() {
        val folder = Folder(folderName = "Personal")
        val savedFolder = folderRepository.save(folder)

        val retrievedFolder = folderRepository.findById(savedFolder.id).orElse(null)
        assertNotNull(retrievedFolder)
        assertEquals("Personal", retrievedFolder?.folderName)
    }

    @Test
    fun `should cascade delete notes when folder is deleted`() {
        val folder = Folder(folderName = "Work")
        val note1 = Note(title = "Task 1", modifiedDate = System.currentTimeMillis(), folder = folder)
        val note2 = Note(title = "Task 2", modifiedDate = System.currentTimeMillis(), folder = folder)
        folder.notes.addAll(listOf(note1, note2))

        folderRepository.save(folder)
        folderRepository.delete(folder)

        assertTrue(folderRepository.findById(folder.id).isEmpty)
    }
}
