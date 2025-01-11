package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.model.NoteRequest
import com.ixtlan.aurora.model.NoteResponse
import com.ixtlan.aurora.service.NoteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(private val noteService: NoteService) {

    @GetMapping
    // TODO: update return type once the folder resource is created
    fun getNotes(): ResponseEntity<List<NoteResponse?>> {
        val notes = noteService.getAllNotes().map {
            // TODO: remove this wrapper once the folder resource is created
            it.folder?.let { it1 ->
                NoteResponse(
                    id = it.id,
                    title = it.title,
                    content = it.content,
                    folderId = it1.id,
                    modifiedDate = it.modifiedDate
                )
            }
        }
        return ResponseEntity.ok(notes)
    }

    @GetMapping("/{id}")
    fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            // TODO: remove this wrapper once the folder resource is created
            note.folder?.let {
                NoteResponse(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = it.id,
                    modifiedDate = note.modifiedDate
                )
            }
        )
    }

    @PostMapping
    fun createNote(@RequestBody noteRequest: NoteRequest): ResponseEntity<NoteResponse> {
        // temporary mock folder
        val mockFolder = Folder(id = noteRequest.folderId, folderName = "Mock Folder", notes = emptyList())

        val note = noteService.createNote(
            Note(
                title = noteRequest.title,
                content = noteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = mockFolder
            )
        )
        return ResponseEntity.ok(
            // TODO: remove this wrapper once the folder resource is created
            note.folder?.let {
                NoteResponse(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = it.id,
                    modifiedDate = note.modifiedDate
                )
            }
        )
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @RequestBody updatedNoteRequest: NoteRequest
    ): ResponseEntity<NoteResponse> {
        // temporary mock folder
        val mockFolder = Folder(id = updatedNoteRequest.folderId, folderName = "Mock Folder", notes = emptyList())

        val updatedNote = noteService.updateNote(
            id,
            Note(
                id = id,
                title = updatedNoteRequest.title,
                content = updatedNoteRequest.content,
                modifiedDate = System.currentTimeMillis(),
                folder = mockFolder
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
