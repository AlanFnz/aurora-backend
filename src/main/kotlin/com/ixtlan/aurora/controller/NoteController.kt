package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.exception.FolderNotFoundException
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.model.NoteResponse
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService,
    private val folderService: FolderService,
    private val authenticationUtil: AuthenticationUtil
) {

    @GetMapping
    fun getNotes(): ResponseEntity<List<NoteResponse>> {
        val notes = noteService.getAllNotes().map { note ->
            NoteResponse(
                id = note.id,
                title = note.title,
                content = note.content,
                folderId = note.folder?.id ?: 0L,
                modifiedDate = note.modifiedDate
            )
        }
        return ResponseEntity.ok(notes)
    }

    @GetMapping("/{id}")
    fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            NoteResponse(
                id = note.id,
                title = note.title,
                content = note.content,
                folderId = note.folder?.id ?: 0L,
                modifiedDate = note.modifiedDate
            )
        )
    }

    @PostMapping
    fun createNote(@RequestBody noteRequest: NoteRequest): ResponseEntity<NoteResponse> {
        val currentUser = authenticationUtil.getCurrentUser()
        val note = noteService.createNote(
            title = noteRequest.title,
            content = noteRequest.content,
            folderId = noteRequest.folderId,
            user = currentUser

        )

        return ResponseEntity.ok(
            NoteResponse(
                id = note.id,
                title = note.title,
                content = note.content,
                folderId = note.folder?.id ?: 0L,
                modifiedDate = note.modifiedDate
            )
        )
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody updatedNoteRequest: NoteRequest,
    ): ResponseEntity<NoteResponse> {
        val currentUser = authenticationUtil.getCurrentUser()

        noteService.getNoteByIdAndUser(id, currentUser)
            ?: return ResponseEntity.notFound().build()

        return try {
            val updatedNote = noteService.updateNote(
                id,
                Note(
                    id = id,
                    title = updatedNoteRequest.title,
                    content = updatedNoteRequest.content,
                    modifiedDate = System.currentTimeMillis(),
                    folder = updatedNoteRequest.folderId?.let { fid ->
                        Folder(id = fid, folderName = "", user = currentUser)
                    },
                    user = currentUser
                ),
                currentUser
            )

            ResponseEntity.ok(
                NoteResponse(
                    id = updatedNote.id,
                    title = updatedNote.title,
                    content = updatedNote.content,
                    folderId = updatedNote.folder?.id ?: 0L,
                    modifiedDate = updatedNote.modifiedDate
                )
            )
        } catch (ex: FolderNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteNoteById(@PathVariable id: Long): ResponseEntity<Void> {
        noteService.deleteNote(id)
        return ResponseEntity.noContent().build()
    }
}
