package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.exception.FolderNotEmptyException
import com.ixtlan.aurora.exception.FolderNotFoundException
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.repository.NoteRepository
import org.springframework.stereotype.Service

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
) {

    fun getAllFolders(user: User): List<Folder> = folderRepository.findAllByUser(user)

    fun getFolderById(id: Long, user: User): Folder? = folderRepository.findByIdAndUser(id, user)

    fun createFolder(folder: Folder, user: User): Folder {
        val folderToSave = folder.copy(user = user)
        return folderRepository.save(folderToSave)
    }

    fun updateFolder(id: Long, updatedFolder: Folder, user: User): Folder {
        val existingFolder = folderRepository.findByIdAndUser(id, user)
            ?: throw FolderNotFoundException("Folder with id $id not found")

        val folderToSave = existingFolder.copy(folderName = updatedFolder.folderName)
        return folderRepository.save(folderToSave)
    }

    fun deleteFolder(id: Long, cascadeDelete: Boolean, user: User) {
        val folder = folderRepository.findByIdAndUser(id, user)
            ?: throw FolderNotFoundException("Folder with id $id not found")

        if (cascadeDelete) {
            noteRepository.deleteAll(folder.notes)
        } else if (folder.notes.isNotEmpty()) {
            throw FolderNotEmptyException("Cannot delete folder with existing notes. Use cascadeDelete=true.")
        }

        folderRepository.delete(folder)
    }
}
