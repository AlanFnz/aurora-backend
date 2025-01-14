package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.model.NoteResponse
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(private val noteService: NoteService, private val folderService: FolderService) {

    @GetMapping
    fun getNotes(): ResponseEntity<List<NoteResponse>> {
        val notes = noteService.getAllNotes().map {
            NoteResponse(
                id = it.id,
                title = it.title,
                content = it.content,
                folderId = it.folder.id ?: 0,
                modifiedDate = it.modifiedDate
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
                folderId = note.folder.id ?: 0,
                modifiedDate = note.modifiedDate
            )
        )
    }

    @PostMapping
    fun createNote(@RequestBody noteRequest: NoteRequest): ResponseEntity<NoteResponse> {
        val folder = folderService.getFolderById(noteRequest.folderId)
            ?: return ResponseEntity.badRequest().body(null)

        val note = noteService.createNote(
            Note(
                title = noteRequest.title,
                content = noteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = folder
            )
        )
        return ResponseEntity.ok(
                NoteResponse(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folder.id ?: 0,
                    modifiedDate = note.modifiedDate
                )

        )
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @RequestBody updatedNoteRequest: NoteRequest
    ): ResponseEntity<NoteResponse> {
        val folder = folderService.getFolderById(updatedNoteRequest.folderId)
            ?: return ResponseEntity.badRequest().body(null)

        val updatedNote = noteService.updateNote(
            id,
            Note(
                id = id,
                title = updatedNoteRequest.title,
                content = updatedNoteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = folder
            )
        )
        return ResponseEntity.ok(
            updatedNote.folder?.let {
                NoteResponse(
                    id = updatedNote.id,
                    title = updatedNote.title,
                    content = updatedNote.content,
                    folderId = it.id,
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
