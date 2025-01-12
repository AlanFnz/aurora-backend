package com.ixtlan.aurora.repository

import com.ixtlan.aurora.entity.Folder
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, Long>
