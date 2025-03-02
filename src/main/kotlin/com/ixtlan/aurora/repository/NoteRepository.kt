package com.ixtlan.aurora.repository

import com.ixtlan.aurora.entity.Note
import com.ixtlan.aurora.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface NoteRepository : JpaRepository<Note, Long> {
    fun findByIdAndUser(id: Long, user: User): Note?
}
