package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.repository.NoteRepository
import com.ixtlan.aurora.repository.FolderRepository
import com.ixtlan.aurora.security.AuthenticationUtil
import org.springframework.stereotype.Service

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val authenticationUtil: AuthenticationUtil,
) {

    fun getAllNotes(): List<Note> = noteRepository.findAll()

    fun getNoteById(id: Long): Note? = noteRepository.findById(id).orElse(null)

    fun createNote(note: Note): Note {
        val currentUser = authenticationUtil.getCurrentUser()
        val folder = note.folder?.id?.let {
            folderRepository.findById(it).orElseGet {
                folderRepository.save(Folder(folderName = "Default Folder", user = currentUser))
            }
        } ?: folderRepository.save(Folder(folderName = "Default Folder", user = currentUser))

        val noteToSave = note.copy(folder = folder)
        return noteRepository.save(noteToSave)
    }

    fun updateNote(id: Long, updatedNote: Note): Note {
        val existingNote = noteRepository.findById(id).orElseThrow {
            IllegalArgumentException("Note with id $id not found")
        }

        val updatedFolder = updatedNote.folder?.id?.let { folderId ->
            folderRepository.findById(folderId).orElseThrow {
                IllegalArgumentException("Folder with id $folderId not found")
            }
        }

        val noteToSave = existingNote.copy(
            title = updatedNote.title,
            content = updatedNote.content,
            modifiedDate = updatedNote.modifiedDate,
            folder = updatedFolder ?: existingNote.folder
        )
        return noteRepository.save(noteToSave)
    }

    fun deleteNote(id: Long) = noteRepository.deleteById(id)
}
