package com.ixtlan.aurora.repository

import com.ixtlan.aurora.entity.Folder
import com.ixtlan.aurora.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, Long> {
    fun findAllByUser(user: User): List<Folder>
    fun findByIdAndUser(id: Long, user: User): Folder?
}
