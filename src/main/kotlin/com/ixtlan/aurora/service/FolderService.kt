package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository, private val noteRepository: NoteRepository,
) {

    fun getAllFolders(): List<Folder> = folderRepository.findAll()

    fun getFolderById(id: Long): Folder? = folderRepository.findById(id).orElse(null)

    fun createFolder(folder: Folder): Folder = folderRepository.save(folder)

    fun updateFolder(id: Long, updatedFolder: Folder): Folder {
        val existingFolder = folderRepository.findById(id).orElseThrow {
            IllegalArgumentException("Folder with id $id not found")
        }
        val folderToSave = existingFolder.copy(folderName = updatedFolder.folderName)
        return folderRepository.save(folderToSave)
    }

    fun deleteFolder(id: Long, cascadeDelete: Boolean) {
        val folder = folderRepository.findById(id).orElseThrow {
            IllegalArgumentException("Folder with id $id not found")
        }

        if (cascadeDelete) {
            noteRepository.deleteAll(folder.notes)
        } else {
            if (folder.notes.isNotEmpty()) {
                throw IllegalStateException("Cannot delete folder with existing notes. Use cascadeDelete=true.")
            }
        }
        folderRepository.delete(folder)
    }
}
