package com.ixtlan.aurora.listener

import com.ixtlan.aurora.event.UserRegisteredEvent
import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.service.FolderService
import com.ixtlan.aurora.service.NoteService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserRegistrationListener(
    private val folderService: FolderService,
    private val noteService: NoteService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegistered(event: UserRegisteredEvent) {
        val user = event.user

        val folder = Folder(folderName = "Your first folder", user = user)
        val savedFolder = folderService.createFolder(folder, user)

        noteService.createNote(
            title = "Your first note",
            content = "Hello world!",
            folderId = savedFolder.id,
            user = user
        )
    }
}
