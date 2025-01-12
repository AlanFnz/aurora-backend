package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.model.FolderRequest
import com.ixtlan.aurora.model.FolderResponse
import com.ixtlan.aurora.service.FolderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/folders")
class FolderController(private val folderService: FolderService) {

    @GetMapping
    fun getFolders(): ResponseEntity<List<FolderResponse>> {
        val folders = folderService.getAllFolders().map {
            FolderResponse(
                id = it.id,
                folderName = it.folderName,
                notes = it.notes.map { note ->
                    FolderResponse.NoteResponse(
                        id = note.id,
                        title = note.title,
                        snippet = note.content?.take(50) ?: "",
                        modifiedDate = note.modifiedDate
                    )
                }
            )
        }
        return ResponseEntity.ok(folders)
    }

    @GetMapping("/{id}")
    fun getFolderById(@PathVariable id: Long): ResponseEntity<FolderResponse> {
        val folder = folderService.getFolderById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            FolderResponse(
                id = folder.id,
                folderName = folder.folderName,
                notes = folder.notes.map {
                    FolderResponse.NoteResponse(
                        id = it.id,
                        title = it.title,
                        snippet = it.content?.take(50) ?: "",
                        modifiedDate = it.modifiedDate
                    )
                }
            )
        )
    }

    @PostMapping
    fun createFolder(@RequestBody folderRequest: FolderRequest): ResponseEntity<FolderResponse> {
        val folder = folderService.createFolder(Folder(folderName = folderRequest.folderName))
        return ResponseEntity.ok(
            FolderResponse(
                id = folder.id,
                folderName = folder.folderName,
                notes = emptyList()
            )
        )
    }

    @PutMapping("/{id}")
    fun updateFolder(
        @PathVariable id: Long,
        @RequestBody updatedFolderRequest: FolderRequest
    ): ResponseEntity<FolderResponse> {
        val updatedFolder = folderService.updateFolder(
            id,
            Folder(folderName = updatedFolderRequest.folderName)
        )
        return ResponseEntity.ok(
            FolderResponse(
                id = updatedFolder.id,
                folderName = updatedFolder.folderName,
                notes = emptyList()
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteFolder(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") cascadeDelete: Boolean
    ): ResponseEntity<Void> {
        folderService.deleteFolder(id, cascadeDelete)
        return ResponseEntity.noContent().build()
    }
}
