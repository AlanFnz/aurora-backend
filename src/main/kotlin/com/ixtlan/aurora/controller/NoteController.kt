package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.model.NoteResponse
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
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
            note.folder?.id?.let {
                NoteResponse(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = it,
                    modifiedDate = note.modifiedDate
                )
            }
        )
    }

    @PostMapping
    fun createNote(@RequestBody noteRequest: NoteRequest): ResponseEntity<NoteResponse> {
        val currentUser = authenticationUtil.getCurrentUser()
        val folder = noteRequest.folderId?.let {
            folderService.getFolderById(it, currentUser) ?: Folder(
                id = it,
                folderName = "New Folder",
                user = currentUser
            )
        } ?: Folder(folderName = "Default Folder", user = currentUser)

        val note = noteService.createNote(
            Note(
                title = noteRequest.title,
                content = noteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = folder,
                user = currentUser
            )
        )

        return ResponseEntity.ok(
            NoteResponse(
                id = note.id,
                title = note.title,
                content = note.content,
                folderId = note.folder!!.id,
                modifiedDate = note.modifiedDate
            )
        )
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @RequestBody updatedNoteRequest: NoteRequest
    ): ResponseEntity<NoteResponse> {
        val currentUser = authenticationUtil.getCurrentUser()
        val folder = updatedNoteRequest.folderId?.let { folderService.getFolderById(it, currentUser) }
            ?: return ResponseEntity.badRequest().body(null)

        val updatedNote = noteService.updateNote(
            id,
            Note(
                id = id,
                title = updatedNoteRequest.title,
                content = updatedNoteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = folder,
                user = currentUser
            )
        )
        return ResponseEntity.ok(
            updatedNote.folder?.id?.let {
                NoteResponse(
                    id = updatedNote.id,
                    title = updatedNote.title,
                    content = updatedNote.content,
                    folderId = it,
                    modifiedDate = updatedNote.modifiedDate
                )
            }
        )
    }

    @DeleteMapping("/{id}")
    fun deleteNoteById(@PathVariable id: Long): ResponseEntity<Void> {
        noteService.deleteNote(id)
        return ResponseEntity.noContent().build()
    }
}
