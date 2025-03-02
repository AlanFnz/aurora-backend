package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.exception.FolderNotFoundException
import com.ixtlan.aurora.repository.NoteRepository
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.security.AuthenticationUtil
import org.springframework.stereotype.Service

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
) {

    fun getAllNotes(): List<Note> = noteRepository.findAll()

    fun getNoteById(id: Long): Note? = noteRepository.findById(id).orElse(null)

    fun getNoteByIdAndUser(id: Long, user: User): Note? {
        return noteRepository.findByIdAndUser(id, user)
    }

    fun createNote(
        title: String, content: String?, folderId: Long?, user: User
    ): Note {
        val folder = folderId?.let { id ->
            folderRepository.findByIdAndUser(id, user)
                ?: throw FolderNotFoundException("Folder with id $id not found or does not belong to the user")
        } ?: throw IllegalArgumentException("Folder ID is required")

        val note = Note(
            title = title, content = content, modifiedDate = System.currentTimeMillis(), folder = folder, user = user
        )

        return noteRepository.save(note)
    }

    fun updateNote(id: Long, updatedNote: Note, user: User): Note {
        val existingNote = noteRepository.findByIdAndUser(id, user)
            ?: throw IllegalArgumentException("Note with id $id not found or does not belong to the user")

        val updatedFolder = updatedNote.folder?.let { folder ->
            folderRepository.findByIdAndUser(folder.id, user)
                ?: throw FolderNotFoundException("Folder with id ${folder.id} not found or does not belong to the user")
        } ?: existingNote.folder

        val noteToSave = existingNote.copy(
            title = updatedNote.title,
            content = updatedNote.content,
            modifiedDate = updatedNote.modifiedDate,
            folder = updatedFolder
        )

        return noteRepository.save(noteToSave)
    }

    fun deleteNote(id: Long) = noteRepository.deleteById(id)
}
