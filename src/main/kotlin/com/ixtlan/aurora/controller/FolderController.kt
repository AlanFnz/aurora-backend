package com.ixtlan.aurora.controller

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.model.FolderRequest
import com.ixtlan.aurora.model.FolderResponse
import com.ixtlan.aurora.security.AuthenticationUtil
import com.ixtlan.aurora.service.FolderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/folders")
class FolderController(private val folderService: FolderService, private val authenticationUtil: AuthenticationUtil) {

    @GetMapping
    fun getFolders(): ResponseEntity<List<FolderResponse>> {
        val currentUser = authenticationUtil.getCurrentUser()
        val folders = folderService.getAllFolders(currentUser).map {
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
        val currentUser = authenticationUtil.getCurrentUser()
        val folder = folderService.getFolderById(id, currentUser) ?: return ResponseEntity.notFound().build()
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
        val currentUser = authenticationUtil.getCurrentUser()
        val folder =
            folderService.createFolder(Folder(folderName = folderRequest.folderName, user = currentUser), currentUser)
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
        val currentUser = authenticationUtil.getCurrentUser()
        val updatedFolder = folderService.updateFolder(
            id,
            Folder(folderName = updatedFolderRequest.folderName, user = currentUser),
            currentUser
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
        val currentUser = authenticationUtil.getCurrentUser()
        folderService.deleteFolder(id, cascadeDelete, currentUser)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }
}
