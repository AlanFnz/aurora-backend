package com.ixtlan.aurora.repository

import com.ixtlan.aurora.entity.Note
import org.springframework.data.jpa.repository.JpaRepository

interface NoteRepository : JpaRepository<Note, Long>
