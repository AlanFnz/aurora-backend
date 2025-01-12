package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.repository.NoteRepository
import com.ixtlan.aurora.repository.FolderRepository
import org.springframework.stereotype.Service

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) {

    fun getAllNotes(): List<Note> = noteRepository.findAll()

    fun getNoteById(id: Long): Note? = noteRepository.findById(id).orElse(null)

    fun createNote(note: Note): Note {
        folderRepository.findById(note.folder.id).orElseThrow {
            IllegalArgumentException("Folder with id ${note.folder.id} not found")
        }
        return noteRepository.save(note)
    }

    fun updateNote(id: Long, updatedNote: Note): Note {
        val existingNote = noteRepository.findById(id).orElseThrow {
            IllegalArgumentException("Note with id $id not found")
        }

        folderRepository.findById(updatedNote.folder.id).orElseThrow {
            IllegalArgumentException("Folder with id ${updatedNote.folder.id} not found")
        }

        val noteToSave = existingNote.copy(
            title = updatedNote.title,
            content = updatedNote.content,
            modifiedDate = updatedNote.modifiedDate,
            folder = updatedNote.folder
        )
        return noteRepository.save(noteToSave)
    }

    fun deleteNote(id: Long) = noteRepository.deleteById(id)
}
