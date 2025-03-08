package com.ixtlan.aurora.service

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.User
import com.ixtlan.aurora.event.UserRegisteredEvent
import com.ixtlan.aurora.model.UserRegistrationRequest
import com.ixtlan.aurora.model.UserResponse
import com.ixtlan.aurora.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    // private val eventPublisher: ApplicationEventPublisher
    private val folderService: FolderService,
    private val noteService: NoteService
) {
    @Transactional
    fun registerUser(request: UserRegistrationRequest): UserResponse {
        val finalEmail = request.email
        val finalFirstName = request.firstName ?: ""
        val finalLastName = request.lastName ?: ""

        if (finalEmail != null && userRepository.existsByEmail(finalEmail)) {
            throw IllegalArgumentException("Email already taken")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already taken")
        }

        val newUser = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = finalEmail,
            firstName = finalFirstName,
            lastName = finalLastName,
            roles = mutableSetOf("USER")
        )

        val savedUser = userRepository.save(newUser)

        val savedFolder = folderService.createFolder(
            Folder(folderName = "Your first folder", user = savedUser),
            savedUser
        )

        noteService.createNote(
            title = "Your first note",
            content = "Hello world!",
            folderId = savedFolder.id,
            user = savedUser
        )

        // TODO: Would be great to make it work with events
        // eventPublisher.publishEvent(UserRegisteredEvent(this, savedUser))

        return UserResponse(
            id = savedUser.id,
            username = savedUser.username,
            email = savedUser.email,
            firstName = savedUser.firstName,
            lastName = savedUser.lastName,
            roles = savedUser.roles
        )
    }

    fun getUserById(userId: Long): UserResponse? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            roles = user.roles
        )
    }
}
